package com.yealink.ims.fileshare.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件共享线程池
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class FileShareThreadExecutor {

    private static FileShareThreadExecutor executor = new FileShareThreadExecutor();
    private ExecutorService serverthreadPool = null;
    private ExecutorService busiDealthreadPool = null;
    private ExecutorService fileRequestthreadPool = null;
    private ExecutorService httpThreadPool = null;

    private FileShareThreadExecutor() {}

    public static FileShareThreadExecutor getInstance() {
        return executor;
    }

    public void init() {
        serverthreadPool = Executors.newFixedThreadPool(3);
        busiDealthreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*4);
        fileRequestthreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*8);
        httpThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
    }

    /**
     * 返回服务线程池
     */
    public ExecutorService getServerthreadPool() {
        return serverthreadPool;
    }

    /**
     * 发文件请求线程池
     * @return
     */
    public ExecutorService getFileRequestthreadPool() {
        return fileRequestthreadPool;
    }

    /**
     * 业务处理线程池
     * @return
     */
    public ExecutorService getBusiDealthreadPool() {
        return busiDealthreadPool;
    }

    /**
     * http服务处理线程池
     * @return
     */
    public ExecutorService getHttpThreadPool() {
        return httpThreadPool;
    }

    /**
     * 关闭线程池
     */
    public void stop() {
        if (serverthreadPool != null) {
            serverthreadPool.shutdown();
        }
        if (fileRequestthreadPool != null) {
            fileRequestthreadPool.shutdown();
        }
        if (busiDealthreadPool != null) {
            busiDealthreadPool.shutdown();
        }
        if (httpThreadPool != null) {
            httpThreadPool.shutdown();
        }
    }

}
