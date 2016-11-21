package com.yealink.ims.fileshare.run;

import com.yealink.dataservice.client.RemoteServiceFactory;
import com.yealink.ims.fileshare.busi.http.CommonPicFileTask;
import com.yealink.ims.fileshare.of.FileServerStateManager;
import com.yealink.ims.fileshare.of.FileShareEventHandler;
import com.yealink.ims.fileshare.server.FileShareBootstrap;
import com.yealink.ims.fileshare.server.FileShareHttpBootstrap;
import com.yealink.ims.fileshare.util.FileShareThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * 文件服务
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@Component("fileSystemService")
public class FileSystemService implements Lifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemService.class);
    private boolean started = false;

    @Autowired
    private FileShareEventHandler fileShareEventHandler;
    @Autowired
    private FileShareBootstrap fileShareBootstrap;
    @Autowired
    private FileShareHttpBootstrap fileShareHttpBootstrap;
    @Value("${fs.jid}")
    private String fsJid;
    @Value("${fs.host}")
    private String host;
    @Value("${fs.port}")
    private String port;
    @Autowired
    private CommonPicFileTask commonPicFileTask;

    @Override
    public void start() {
        LOG.info("FileSystemService start");
        //初始化文件共享线程池
        FileShareThreadExecutor.getInstance().init();

        // 发起文件TCP服务
        Future future = FileShareThreadExecutor.getInstance().getServerthreadPool().submit(fileShareBootstrap);

        // tcp服务还没起来 每隔一秒判断是否启动，最多5次
        int sleepCount = 5;
        while (!fileShareBootstrap.isRun()) {
            if (sleepCount < 0) {
                future.cancel(true);
                return;
            }
            try {
                sleepCount--;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.info("File share server InterruptedException:", e);
                e.printStackTrace();
            }
        }

        //发起http服务
        FileShareThreadExecutor.getInstance().getServerthreadPool().submit(fileShareHttpBootstrap);

        //注册MQ事件
        RemoteServiceFactory.getInstance().registEventHandler(fileShareEventHandler);

        //发起通用图片处理任务
        FileShareThreadExecutor.getInstance().getServerthreadPool().execute(commonPicFileTask);

        started = true;

        // 通知of 当前文件服务是否启动
        boolean isRunning = isTcpServerRunning();
        FileServerStateManager fileServerStateManager = FileServerStateManager.getInstance();
        fileServerStateManager.setRunning(isRunning);
        fileServerStateManager.setFsJid(getFsJid());
        fileServerStateManager.setHost(getHost());
        fileServerStateManager.setPort(getPort());
        fileServerStateManager.fireFsNotify();
    }

    @Override
    public void stop() {
        started = false;
        // 关闭线程池
        FileShareThreadExecutor.getInstance().stop();
        // 注销MQ事件
        RemoteServiceFactory.getInstance().unRegistEventHandler(fileShareEventHandler);

        // 通知of 当前文件服务关闭
        FileServerStateManager fileServerStateManager = FileServerStateManager.getInstance();
        fileServerStateManager.setRunning(false);
        fileServerStateManager.setFsJid(getFsJid());
        fileServerStateManager.setHost(getHost());
        fileServerStateManager.setPort(getPort());
        fileServerStateManager.fireFsNotify();

        LOG.info("File Server stop......");
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    /**
     * tcp服务是否启动
     * @return
     */
    public boolean isTcpServerRunning() {
        return fileShareBootstrap.isRun();
    }

    /**
     * 文件服务JID
     * @return
     */
    public String getFsJid() {
        return fsJid;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

}
