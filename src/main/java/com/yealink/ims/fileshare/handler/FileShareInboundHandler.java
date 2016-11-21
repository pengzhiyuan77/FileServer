package com.yealink.ims.fileshare.handler;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yealink.ims.fileshare.cache.FileShareCacheManager;
import com.yealink.ims.fileshare.conn.Connection;
import com.yealink.ims.fileshare.conn.ConnectionManager;
import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.server.FileShareBootstrap;
import com.yealink.ims.fileshare.util.FileShareConstant;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;

/**
 * 文件共享 收到消息处理
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@ChannelHandler.Sharable
public class FileShareInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(FileShareInboundHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FsMsg) {
                EventType eventType = ((FsMsg) msg).getCmd();

                Attribute<Connection> attr = ctx.channel().attr(FileShareConstant.CONN_KEY);
                Connection conn = attr.get();
                // 非请求鉴权命令，且连接中digest为空，还未验证过
                if (eventType.getCmd() != FileShareConstant.AUTH &&
                        StringUtils.isBlank(conn.getDigest())) {
                    LOG.info("连接未验证...");
                    ctx.pipeline().close();
                    return;
                }
                LOG.debug("收到命令...{}", eventType.getCmd());
                // 加入到任务队列
                FileShareBootstrap.fileDealerProcess.putFsMsgQueue((FsMsg) msg);

            } else {
                LOG.error("error Object in channelRead:{}", msg.toString());
            }

        } finally {
            // FsMsg不会被release
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("file share inbound handler error", cause);
        ctx.pipeline().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 连接激活后创建一个新connection
        Connection conn = ConnectionManager.getInstance().newConnection(ctx);
        // 把connection加入到channel属性中后续鉴权 文件请求用
        Attribute<Connection> connAttr = ctx.channel().attr(FileShareConstant.CONN_KEY);
        connAttr.set(conn);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // channel失效后 删除属性和连接
        Attribute<Connection> attr = ctx.channel().attr(FileShareConstant.CONN_KEY);
        Connection conn = attr.get();
        ConnectionManager.getInstance().removeConn(conn.getChannelId());
        attr.remove();
        ctx.channel().attr(FileShareConstant.FILENAME_KEY).remove();
        ctx.channel().attr(FileShareConstant.MD5_KEY).remove();
        ctx.channel().attr(FileShareConstant.CHUNK_KEY).remove();
        ctx.channel().attr(FileShareConstant.EN_CIPHER_KEY).remove();
        ctx.channel().attr(FileShareConstant.DE_CIPHER_KEY).remove();
        ctx.channel().attr(FileShareConstant.FILE_SAVEPATH_KEY).remove();
        ctx.channel().attr(FileShareConstant.COUNTDOWN_KEY).remove();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                // 读取空闲超时 目前设置20秒
                Attribute<Connection> attr = ctx.channel().attr(FileShareConstant.CONN_KEY);
                Connection conn = attr.get();
                if (conn != null) {
                    // 空闲时间比较长 关闭连接
                    LOG.info("超时，断开连接:", conn.getDigest());

                    FileShareCacheManager fileShareCacheManager = FileShareCacheManager.getInstance();
                    fileShareCacheManager.removeFromCache(conn.getDigest());
                    ConnectionManager.getInstance().closeAllConnByDigest(conn.getDigest());
                } else {
                    ctx.pipeline().close();
                }
            }
        }
    }
}
