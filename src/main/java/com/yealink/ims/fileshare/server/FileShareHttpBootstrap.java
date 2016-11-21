package com.yealink.ims.fileshare.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * http服务
 * @author pzy
 *
 */
@Component
public class FileShareHttpBootstrap implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(FileShareHttpBootstrap.class);
	
	@Autowired
    private ServerBootstrap serverBootstrap;
    @Resource(name = "FileShareHttpChannelInit")
    private ChannelInitializer<SocketChannel> fileShareHttpChannelInit;
    @Resource(name = "boss")
    private EventLoopGroup boss;
    @Resource(name = "httpWorker")
    private EventLoopGroup worker;
    
    /**
     * 主机地址
     */
    @Value("${fs.host}")
    private String host;
    /**
     * http端口
     */
    @Value("${http.port}")
    private String httpPort;

	@Override
	public void run() {
		LOG.info("设置FileShareHttpBootstrap");
        // 设置工作线程池
        serverBootstrap.group(boss, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.childHandler(fileShareHttpChannelInit);
        serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 直接发包
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        Channel ch;
        try {
            ChannelFuture bindf = serverBootstrap.bind(host, Integer.valueOf(httpPort));
            ChannelFuture bsync = bindf.sync();
            ch = bsync.channel();
            LOG.info("等待HTTP over...");
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.info("http interrupted", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
	}

}
