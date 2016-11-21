package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.conn.Connection;
import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.exception.FileShareException;
import com.yealink.ims.fileshare.of.FileSystemMsgService;
import com.yealink.ims.fileshare.store.IFileStore;
import com.yealink.ims.fileshare.util.AESUtil;
import com.yealink.ims.fileshare.util.ByteUtil;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.util.concurrent.CountDownLatch;

/**
 * 服务端请求文件，客户端的响应处理
 * author:pengzhiyuan
 * Created on:2016/6/3.
 */
public class ServerRequestFileDealer implements IDealer {
    private static final Logger LOG = LoggerFactory.getLogger(FileDealerProcessManager.class);
    private AESUtil util = new AESUtil();

    @Override
    public void deal(FsMsg fsMsg) throws FileShareException {
        EventType eventType = fsMsg.getCmd();

        //请求成功
        if (eventType.getCmd() == FileShareConstant.FILE_REQUEST_SUCCESS) {
            IFileStore fileStore = FileDealerProcessManager.getInstance().getFileStore();
            ChannelHandlerContext ctx = fsMsg.getCtx();
            // 获取数据保存
            Attribute<Connection> attr = ctx.channel().attr(FileShareConstant.CONN_KEY);
            Connection conn = attr.get();
            String digest = conn.getDigest();
            Attribute<Cipher> deCipherKey = ctx.channel().attr(FileShareConstant.DE_CIPHER_KEY);
            // 采用nopadding先解密 然后取出原内容长度进行截取
            Cipher deCipher = deCipherKey.get();

            byte[] data = null;
            if (deCipher != null) {
                data = util.decrypt(deCipher, fsMsg.getData());
            } else {
                data = fsMsg.getData();
            }
            if (data.length <= 8) {
                LOG.info("数据格式错误,{};" + digest);
                ctx.pipeline().close();
                return;
            }

            long offset = ByteUtil.bytesToLong2(ByteUtil.subBytes(data, 0, 6), 0);
            int length = ByteUtil.unSignBytesToInt(ByteUtil.subBytes(data, 6, 2), 0);

            Attribute<String> fileNameAttr = ctx.channel().attr(FileShareConstant.FILENAME_KEY);
            // 文件名称
            String fileName = fileNameAttr.get();

            int requestSize = FileServerProtocalHandler.getRequestSize();
            // 临时文件名为 fileName+序列号(总共16位，左边不足补0)
            String tmpFileName = fileName + CommonUtil.makeFileSeq(offset/requestSize);

            // 保存文件
            byte[] tmpFileData = ByteUtil.subBytes(data,8,length);
            Attribute<String> fileSavePathAttr = ctx.channel().attr(FileShareConstant.FILE_SAVEPATH_KEY);
            // 保存临时文件
            boolean isSaveTmpSuccess = fileStore.saveFile(fileSavePathAttr.get(), tmpFileName, tmpFileData);

            LOG.debug("isSaveTmpSuccess="+isSaveTmpSuccess+","+tmpFileData.length);

            // 临时文件生成成功
            if (isSaveTmpSuccess) {
//                // 判断如果是已经是收到的最后一个文件块，则把所有临时文件合并生成最终目标文件
//                Attribute<Long> chunkNumberAttr = ctx.channel().attr(FileShareConstant.CHUNK_KEY);
//                long chunkNumber = chunkNumberAttr.get();
//                //如果已经接收到全部文件
//                if (fileStore.isGetAllFile(fileSavePathAttr.get(), chunkNumber)) {
//                    // 进行合并验证处理
//                    FileServerProtocalHandler.fileRequestFinish(fsMsg, digest, fileName);
//                }
                // ======== *****  关于控制文件接收完毕先用countdownlatch控制         *****=============
                // ======== *****  后续看性能如何，看是否优化采用上面注释的代码进行处理   *****=============
                // 单个文件块接收完毕 countdown
                Attribute<CountDownLatch> countDownLatchAttr = fsMsg.getCtx().channel().attr(FileShareConstant.COUNTDOWN_KEY);
                CountDownLatch countDownLatch = countDownLatchAttr.get();
                countDownLatch.countDown();
            } else {
                // 通知of文件上传失败
                FileSystemMsgService.sendFileTransferNotify(digest, "", false, "Interval Server error!");
                // 释放缓存资源 关闭连接
                FileServerProtocalHandler.releaseFileRequestInfo(digest);
            }
        }
        // 请求响应失败
        else if (eventType.getCmd() == FileShareConstant.FILE_REQUEST_FAIL) {
            // 请求文件响应失败，关闭连接
            fsMsg.getCtx().pipeline().close();
        }
    }
}
