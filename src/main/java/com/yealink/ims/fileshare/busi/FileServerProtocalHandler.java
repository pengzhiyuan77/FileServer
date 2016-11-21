package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.cache.FileShareCacheManager;
import com.yealink.ims.fileshare.conn.ConnectionManager;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.of.FileSystemMsgService;
import com.yealink.ims.fileshare.store.IFileStore;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务 协议处理
 * 主要负责跟客户端 传输数据处理回复
 * author:pengzhiyuan
 * Created on:2016/6/3.
 */
public class FileServerProtocalHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FileServerProtocalHandler.class);

    /**
     * 获取服务端每次请求文件的大小
     * @return
     */
    public static int getRequestSize() {
        int requestSize;
        try {
            requestSize = Integer.parseInt(FileDealerProcessManager.getInstance().getRequestSize());
        } catch(Exception e) {
            //默认每次请求4096
            requestSize = 4096;
        }
        return requestSize;
    }

    /**
     * 处理鉴权失败
     * @param fsMsg
     */
    public static void handleAuthFail(FsMsg fsMsg) {
        ChannelHandlerContext ctx = fsMsg.getCtx();
        if (ctx.channel().isWritable()) {
            ByteBuf failBuf = ByteBufBuilder.generateRequestFail(FileShareConstant.AUTH, FileShareConstant.HTTP_CODE_NOTATUH, null);
            ctx.pipeline().writeAndFlush(failBuf).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.pipeline().close();
        }
    }

    /**
     * 鉴权成功
     * @param fsMsg
     */
    public static ChannelFuture handleAuthSuccess(FsMsg fsMsg, String digest) {
        ChannelHandlerContext ctx = fsMsg.getCtx();
        if (ctx.channel().isWritable()) {
            ChannelFuture channleFuture = ctx.pipeline().writeAndFlush(ByteBufBuilder.generateAuthSuccess(digest));
            return channleFuture;
        } else {
            ctx.pipeline().close();
            return null;
        }
    }

    /**
     * 服务端 向客户端请求文件
     * @param fsMsg
     * @param valueMap
     */
    public static void handleRequestFileData(FsMsg fsMsg, Map<String,Object> valueMap, final String digest) {
        ChannelHandlerContext ctx = fsMsg.getCtx();
        if (ctx.channel().isWritable()) {
            int requestSize = getRequestSize();
            long size = Long.parseLong(String.valueOf(valueMap.get("size")));
            long chunkNumber = CommonUtil.calChunkNumber(requestSize, size);

            //文件信息
            String fileName = String.valueOf(valueMap.get("filename"));
            // 循环发送文件块大小
            long offset=0;
            int length=0;
            // 设置chunkNumber
            Attribute<Long> chunkNumberAttr = fsMsg.getCtx().channel().attr(FileShareConstant.CHUNK_KEY);
            chunkNumberAttr.set(chunkNumber);
            // 加密密码器
            Attribute<Cipher> enCipherKey = fsMsg.getCtx().channel().attr(FileShareConstant.EN_CIPHER_KEY);
            Cipher enCipher = enCipherKey.get();
            // 文件保存路径
            Attribute<String> fileSavePathAttr = fsMsg.getCtx().channel().attr(FileShareConstant.FILE_SAVEPATH_KEY);
            Attribute<String> md5Key = fsMsg.getCtx().channel().attr(FileShareConstant.MD5_KEY);
            String md5 = md5Key.get();
            // 已经完成的文件块
            int hasFinishCount = 0;
            // 控制文件全部接收完毕
            Attribute<CountDownLatch> countDownLatchAttr = fsMsg.getCtx().channel().attr(FileShareConstant.COUNTDOWN_KEY);
            CountDownLatch countDownLatch = new CountDownLatch(Integer.parseInt(String.valueOf(chunkNumber)));
            countDownLatchAttr.set(countDownLatch);
            for (int i=0; i<chunkNumber; i++) {
                offset = i*requestSize;
                //最后一次
                if (i+1 == chunkNumber) {
                    length = (int)(size-i*requestSize);
                } else {
                    length=requestSize;
                }

                // ===续传的处理====
                //获取生成临时文件名称
                String tmpFileName = fileName + CommonUtil.makeFileSeq(offset/requestSize);
                byte[] tmpFileData = FileDealerProcessManager.getInstance().getFileStore().getFileData(fileSavePathAttr.get()+File.separator+tmpFileName);
                // 当前该文件块在服务器上已存在
                if (tmpFileData != null && tmpFileData.length > 0) {
                    // 则只需要请求该文件块的后面部分数据
                    offset = offset+tmpFileData.length;
                    length = length-tmpFileData.length;
                    if (length == 0) {
                        // 文件块已请求结束
                        hasFinishCount++;
                        countDownLatch.countDown();
                        continue;
                    }
                }
                ChannelFuture channelFuture = ctx.pipeline().writeAndFlush(ByteBufBuilder.generateFileRequest(offset, length, fileName, enCipher));
                channelFuture.addListener(new ChannelFutureListener(){
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            // 发送成功
                        } else {
                            // 文件请求失败后 释放资源 关闭连接
                            releaseFileRequestInfo(digest);
                        }
                    }
                });
            }
            // 如果临时文件已经全部完成 则合并
            if (hasFinishCount == chunkNumber) {
                fileRequestFinish(md5, fileSavePathAttr.get(), digest, fileName);
            } else {
                try {
                    countDownLatch.await(30, TimeUnit.MINUTES);//30分钟？不科学啊
                    // 全部接收完毕后进行合并验证处理
                    LOG.debug("count down await finish....");
                    fileRequestFinish(md5, fileSavePathAttr.get(), digest, fileName);
                } catch (InterruptedException e) {
                    LOG.error("file request await interrupted exception.."+e);
                    ctx.pipeline().close();
                }
            }
        } else {
            ctx.pipeline().close();
        }
    }

    /**
     * 释放文件请求的信息
     * 针对服务端请求文件完成或者失败后 释放资源
     *
     * @param digest
     */
    public static void releaseFileRequestInfo(String digest) {
        FileShareCacheManager fileShareCacheManager = FileShareCacheManager.getInstance();
        // 删掉文件信息缓存 并关闭连接
        fileShareCacheManager.removeFromCache(digest);
        ConnectionManager.getInstance().closeAllConnByDigest(digest);
    }

    /**
     * 文件传输完成 合并处理
     * @param savePath
     * @param digest
     * @param fileName
     */
    public static void fileRequestFinish(String md5, String savePath, String digest, String fileName) {
        IFileStore fileStore = FileDealerProcessManager.getInstance().getFileStore();
        // 合并各个文件 返回保存路径
        String fileNamePath = fileStore.generateFinalFile(savePath, fileName);
        // 文件传输结果信息 给OF用
        boolean isSendSuccess=true; // 文件是否发送成功
        String message=""; // 发送失败信息

        // 目标文件生成成功
        if (fileNamePath != null) {
            // 进行md5校验
            boolean md5Verify = fileStore.verifyFileMd5(fileNamePath, md5);
            LOG.debug("md5="+md5+",md5Verify="+md5Verify+"，savePath="+fileNamePath);
            if (!md5Verify) {
                // 校验失败
                message = "File MD5 check fail!";
                isSendSuccess = false;
                //校验失败 删掉文件
                fileStore.deleteFile(fileNamePath);
            }
        } else {
            // 最终目标文件没保存成功
            // 通知of文件上传失败
            isSendSuccess = false;
            message = "Generate the final file fail!";
        }
        // 通知of文件上传情况
        FileSystemMsgService.sendFileTransferNotify(digest, fileNamePath, isSendSuccess, message);
        // 释放缓存资源 并关闭连接
        FileServerProtocalHandler.releaseFileRequestInfo(digest);
        LOG.debug("文件请求结束===="+XmppDateUtil.utcTimeToLocal(new Date()));
    }
}
