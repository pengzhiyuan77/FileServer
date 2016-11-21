package com.yealink.ims.fileshare.run;

import com.yealink.ims.fileshare.config.ApplicationConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * 文件共享服务器启动类
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class FileShareRun {
    private static final Logger LOG = LoggerFactory.getLogger(FileShareRun.class);
    public static final Object waitObj = new Object();

    public static void main(String[] args) throws InterruptedException {
        String pidFileName = "fs_pid";
        // 获取当前进程PID
        String vm = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isBlank(vm)) {
            LOG.error("can not get pid!");
            return;
        }
        FileOutputStream out = null;
        try {
            // 将jvm pid保存到文件
            File pid = new File(pidFileName);
            if (pid.exists()) {
                LOG.error("the pid file is exist at {}", pid.getAbsolutePath());
                return;
            }
            out = new FileOutputStream(pid);
            out.write(vm.split("@")[0].getBytes());
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOG.info("pid file not found...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        LOG.info("file share main starting....");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        // 注册虚拟机关闭事件
        context.registerShutdownHook();
        context.start();

        // 打印定义的bean
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.print("File Server beans:");
        for (String string : beanNames) {
            System.out.print(string + ",");
        }
        System.out.println();

//        synchronized (waitObj) {
//            waitObj.wait();
//        }
//
//        context.close();

    }
}
