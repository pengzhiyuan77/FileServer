package com.yealink.ims.fileshare.run;

import com.yealink.ims.fileshare.of.FileServerStateManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 文件服务停止通知
 * author:pengzhiyuan
 * Created on:2016/6/2.
 */
@Component
public class FileServerStopNotify implements ApplicationListener<ContextClosedEvent>{
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // 通知of文件服务已关闭
        System.out.println("file server closing..........");

        ApplicationContext context = event.getApplicationContext();
        FileSystemService fs = (FileSystemService)context.getBean("fileSystemService");

        FileServerStateManager fileServerStateManager = FileServerStateManager.getInstance();
        fileServerStateManager.setRunning(false);
        fileServerStateManager.setFsJid(fs.getFsJid());
        fileServerStateManager.setHost(fs.getHost());
        fileServerStateManager.setPort(fs.getPort());
        fileServerStateManager.fireFsNotify();
    }
}
