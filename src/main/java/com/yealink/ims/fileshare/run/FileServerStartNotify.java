package com.yealink.ims.fileshare.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 文件服务启动完处理
 * author:pengzhiyuan
 * Created on:2016/6/2.
 */
@Component
public class FileServerStartNotify implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(FileServerStartNotify.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
//        ApplicationContext  context = event.getApplicationContext();
//        FileSystemService fs = (FileSystemService)context.getBean("fileSystemService");
//
//        LOG.debug("File server spring start finished..");


    }
}
