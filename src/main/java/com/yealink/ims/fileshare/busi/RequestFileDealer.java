package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.cache.FileShareCacheManager;
import com.yealink.ims.fileshare.conn.Connection;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.exception.FileShareException;
import com.yealink.ims.fileshare.util.AESUtil;
import com.yealink.ims.fileshare.util.ByteUtil;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.util.Map;

/**
 * 客户端请求文件处理 author:pengzhiyuan Created on:2016/6/1.
 */
public class RequestFileDealer implements IDealer {
    private static final Logger LOG = LoggerFactory.getLogger(RequestFileDealer.class);
    private AESUtil util = new AESUtil();

    @Override
    public void deal(FsMsg fsMsg) throws FileShareException {
        ChannelHandlerContext ctx = fsMsg.getCtx();
        // 获取连接的信息
        Attribute<Connection> attr = ctx.channel().attr(FileShareConstant.CONN_KEY);
        Connection conn = attr.get();
        String digest = conn.getDigest();

        // 获取本次加解密的密码器
        Attribute<Cipher> enCipherKey = ctx.channel().attr(FileShareConstant.EN_CIPHER_KEY);
        Cipher enCipher = enCipherKey.get();
        // nopadding 解密数据
        Attribute<Cipher> deCipherKey = ctx.channel().attr(FileShareConstant.DE_CIPHER_KEY);
        Cipher deCipher = deCipherKey.get();

        // 进行解密
        byte[] data = null;
        if (deCipher != null) {
            LOG.debug("加密前数据长度:"+fsMsg.getData().length);
            data = util.decrypt(deCipher, fsMsg.getData());
            LOG.debug("解密后数据长度:"+data.length);
        } else {
            data = fsMsg.getData();
        }
        if (data.length <= 8) {
            LOG.info("请求文件数据格式错误,{};", digest);
            ctx.pipeline().writeAndFlush(ByteBufBuilder.generateRequestFail(FileShareConstant.FILE_REQUEST,
                    FileShareConstant.HTTP_CODE_NOTFOUND, enCipher));
            return;
        }

        long offset = ByteUtil.bytesToLong2(ByteUtil.subBytes(data, 0, 6), 0);
        int length = ByteUtil.unSignBytesToInt(ByteUtil.subBytes(data, 6, 2), 0);

        if (data.length > 8) {
            LOG.debug("offset="+offset);
            LOG.debug("length="+length);
            String fileName = CommonUtil.getStringFromUtf8(new String(ByteUtil.subBytes(data, 8, data.length-8)));
            LOG.debug("客户端请求文件发送的文件名称：{}", fileName);
        }

        // 从缓存获取文件信息
        Object obj = FileShareCacheManager.getInstance().getDataFromCache(digest);
        if (obj != null) {
            Map<String, Object> valueMap = (Map<String, Object>) obj;
            // 获取文件的存储路径 包含文件名
            String fileNamePath = String.valueOf(valueMap.get("savePath"));

            byte[] fileData = FileDealerProcessManager.getInstance().getFileStore().getFileData(fileNamePath, offset,
                    length);
            if (fileData != null && fileData.length > 0) {
                // 加密数据
                // 加密offset+length+fileData
                ByteBuf tempBuf = Unpooled.buffer(8+fileData.length);
                tempBuf.writeBytes(ByteUtil.longToBytes2(offset));
                tempBuf.writeBytes(ByteUtil.intToUnsignShortBytes(length));
                tempBuf.writeBytes(fileData);
                // 加密
                byte[] enFileData = null;
                if (enCipher != null) {
                    enFileData = util.encrypt(enCipher, tempBuf.array());
                } else {
                    enFileData = tempBuf.array();
                }
                tempBuf.release();

                ChannelFuture channelFuture = ctx.pipeline().writeAndFlush(ByteBufBuilder.generateFileRequestSuccess(enFileData));
                channelFuture.addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        // 传输完成

                    }

                });
            }
            else {
                ctx.pipeline().writeAndFlush(ByteBufBuilder.generateRequestFail(FileShareConstant.FILE_REQUEST,
                        FileShareConstant.HTTP_CODE_NOTFOUND, enCipher));
            }
        }
    }
}
