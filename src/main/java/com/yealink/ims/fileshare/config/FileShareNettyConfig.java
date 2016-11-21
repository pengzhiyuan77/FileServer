package com.yealink.ims.fileshare.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 文件共享 netty的服务引导类配置
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@Configuration
public class FileShareNettyConfig {

    @Bean(name = {"serverBootstrap"})
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ServerBootstrap serverBootstrap() {
        return new ServerBootstrap();
    }

    @Bean(name = {"boss"})
    public EventLoopGroup boss() {
        return new NioEventLoopGroup();
    }

    @Bean(name = {"tcpWorker"})
    public EventLoopGroup tcpWorker() {
        return new NioEventLoopGroup();
    }

    @Bean(name = {"httpWorker"})
    public EventLoopGroup httpWorker() {
        return new NioEventLoopGroup();
    }

}
