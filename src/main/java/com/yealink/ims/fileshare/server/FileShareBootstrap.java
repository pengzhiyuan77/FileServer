package com.yealink.ims.fileshare.server;

import com.yealink.ims.fileshare.busi.FileDealerProcess;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
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
 * 文件服务创建tcp服务
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@Component
public class FileShareBootstrap implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FileShareBootstrap.class);
    private volatile boolean run = false;

    /**
     * 文件处理线程
     */
    public static FileDealerProcess fileDealerProcess = new FileDealerProcess();

    @Autowired
    private ServerBootstrap serverBootstrap;
    @Resource(name = "FileShareChannelInit")
    private ChannelInitializer<SocketChannel> fileShareChannelInit;
    @Resource(name = "boss")
    private EventLoopGroup boss;
    @Resource(name = "tcpWorker")
    private EventLoopGroup worker;

    /**
     * 主机地址
     */
    @Value("${fs.host}")
    private String host;
    /**
     * 本地tcp服务端口
     */
    @Value("${fs.port}")
    private String servicePort;

    @Override
    public void run() {

        LOG.info("设置serverbootstrap");
        // 设置工作线程池
        serverBootstrap.group(boss, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.childHandler(fileShareChannelInit);
        // 设置为pooled的allocator
        serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 直接发包
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        try {
            // 启动工作线程
            Thread processThread = new Thread(fileDealerProcess);
            processThread.setDaemon(true);
            LOG.info("启动文件处理线程");
            processThread.start();

            ChannelFuture bindf = serverBootstrap.bind(host, Integer.valueOf(servicePort));
            // 阻塞直到服务器完成绑定
            ChannelFuture bsync = bindf.sync();

            Channel ch = bsync.channel();
            LOG.info("listen port:{}", servicePort);
            run = true;

            LOG.info("等待TCP over...");
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.info("fs interrupted", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            run = false;
        }
    }

    public boolean isRun() {
        return run;
    }
}
