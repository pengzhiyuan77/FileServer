package com.yealink.ims.fileshare.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 文件服务 配置
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@Configuration
@PropertySource("file:${FileServer_Home}/config/application.properties")
@Import(value={FileShareNettyConfig.class})
@ComponentScan(basePackages={"com.yealink.ims.fileshare"})
public class ApplicationConfig {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
