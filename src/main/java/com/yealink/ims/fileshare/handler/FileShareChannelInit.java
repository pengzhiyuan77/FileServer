package com.yealink.ims.fileshare.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.stereotype.Component;

/**
 *  channel初始化
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@Component("FileShareChannelInit")
public class FileShareChannelInit extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
//        pipeline.addLast(new IdleStateHandler(20, 0, 0));
        // 第3,4个字节定义长度，长度不包含报文头4个字节
        // 协议版本(1)+命令码(1)+消息长度(2)+消息
        pipeline.addLast("LengthFieldBasedFrameDecoder", new LengthFieldBasedFrameDecoder(64*1024, 2, 2, 0, 0));
        pipeline.addLast("TransforDecoder", new TransforDecoder());
        pipeline.addLast("fileInboundhandler", new FileShareInboundHandler());
        pipeline.addLast("TransforEncoder", new TransforEncoder());
    }

}
