package com.yealink.ims.fileshare.handler;

import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * http channel
 * @author pzy
 *
 */
@Component("FileShareHttpChannelInit")
public class FileShareHttpChannelInit extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        // 目前只支持最大传输50M，后续根据实际业务再扩展优化，当前只有头像上传
        pipeline.addLast(new HttpObjectAggregator(50*1024*1024));
        //支持异步发送大的码流，但不会占用过多的内存，防止发生java内存溢出
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new FileShareHttpInboundHandler()); 
	}

}
