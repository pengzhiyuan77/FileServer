package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.cache.FileShareCacheManager;
import com.yealink.ims.fileshare.conn.Connection;
import com.yealink.ims.fileshare.conn.ConnectionManager;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.exception.FileShareException;
import com.yealink.ims.fileshare.util.AESUtil;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import com.yealink.ims.fileshare.util.FileShareThreadExecutor;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.util.Date;
import java.util.Map;

/**
 * 鉴权处理
 * author:pengzhiyuan
 * Created on:2016/6/1.
 */
public class AuthDealer implements IDealer {
    private static final Logger LOG = LoggerFactory.getLogger(AuthDealer.class);

    @Override
    public void deal(final FsMsg fsMsg) throws FileShareException {
        byte[] data = fsMsg.getData();
        final String digest = CommonUtil.encodeHex(data);
        LOG.info("auth receiver sh1 digest:{}", digest);

        Object obj = FileShareCacheManager.getInstance().getDataFromCache(digest);
        if (obj != null) {
            final Map<String,Object> valueMap = (Map<String,Object>)obj;
            // 缓存存在对应的文件信息 当做验证通过
            LOG.info("验证通过{}", digest);
            // 设置digest到connection
            Attribute<Connection> attr = fsMsg.getCtx().channel().attr(FileShareConstant.CONN_KEY);
            Connection conn = attr.get();
            conn.setDigest(digest);
            attr.set(conn);
            // 加入到连接管理
            ConnectionManager.getInstance().addConn(fsMsg.getCtx().channel().id(), conn);
            // 回复客户端鉴权成功
            ChannelFuture channnelFuture = FileServerProtocalHandler.handleAuthSuccess(fsMsg, digest);

            // 设置文件名称属性
            Attribute<String> fileNameAttr = fsMsg.getCtx().channel().attr(FileShareConstant.FILENAME_KEY);
            fileNameAttr.set(CommonUtil.getString(valueMap.get("filename")));

            // 设置文件的md5码
            Attribute<String> md5Key = fsMsg.getCtx().channel().attr(FileShareConstant.MD5_KEY);
            md5Key.set(CommonUtil.getString(valueMap.get("md5")));

            // 如果需要加密 设置密码器到channel属性
            if (FileDealerProcessManager.getInstance().isFileEncrypt()) {
                AESUtil aesUtil = new AESUtil();
                Cipher enCipher = aesUtil.initAESEnCipherZeroPadding(String.valueOf(valueMap.get("secretKey")));
                Cipher deCipher = aesUtil.initAESDeCipherNopadding(String.valueOf(valueMap.get("secretKey")));
                Attribute<Cipher> enCipherKey = fsMsg.getCtx().channel().attr(FileShareConstant.EN_CIPHER_KEY);
                Attribute<Cipher> deCipherKey = fsMsg.getCtx().channel().attr(FileShareConstant.DE_CIPHER_KEY);
                enCipherKey.set(enCipher);
                deCipherKey.set(deCipher);
            }

            // 设置文件的保存路径
            Attribute<String> fileSavePathAttr = fsMsg.getCtx().channel().attr(FileShareConstant.FILE_SAVEPATH_KEY);
            String fileType = String.valueOf(valueMap.get("fileType"));
            String savePath = FileDealerProcessManager.getInstance().getFileSavePath(fileType, digest);
            fileSavePathAttr.set(savePath);

            // 如果是文件上传，则服务端主动发出文件请求命令
            channnelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    byte direction = Byte.valueOf(String.valueOf(valueMap.get("direction")));
                    if (FileShareConstant.DIRECTION_UP == direction) {
                        FileShareThreadExecutor.getInstance().getFileRequestthreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                LOG.debug("文件请求开始===="+ XmppDateUtil.utcTimeToLocal(new Date()));
                                FileServerProtocalHandler.handleRequestFileData(fsMsg, valueMap, digest);
                            }
                        });
                    }
                }
            });
        }
        else {
            LOG.info("验证未通过，找不到对应的digest:{}", digest);
            //返回客户端 鉴权失败信息
            FileServerProtocalHandler.handleAuthFail(fsMsg);
        }
    }
}
