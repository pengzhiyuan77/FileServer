package com.yealink.ims.fileshare.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.junit.Test;

/**
 * 客户端测试
 * @author pengzhiyuan
 *
 */
public class TestClient {
	public void connect(int port, String host) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast("LengthFieldBasedFrameDecoder", new LengthFieldBasedFrameDecoder(64*1024, 2, 2, 0, 0));
							ch.pipeline().addLast(new ClineDecoder());
							ch.pipeline().addLast(new SimpleChanneHandler());
						}
					});
			// 发起异步链接操作
			ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 关闭，释放线程资源
			group.shutdownGracefully();
		}
	}
	
	public static void main(String[]args) {
		int count = 10000;
		for (int i=0; i<count; i++) {
			new TestClient().connect(3333, "10.3.19.168");
		}
	}

	@Test
	public void nettyClient() {
		new TestClient().connect(3333, "10.3.19.168");
	}
}
