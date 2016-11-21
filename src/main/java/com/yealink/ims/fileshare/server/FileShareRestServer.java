package com.yealink.ims.fileshare.server;

//import org.glassfish.grizzly.http.server.HttpServer;
//import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
//import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;

/**
 * 文件服务器restful ws服务
 *
 * 弃用 采用netty http服务处理 参见FileServerMonitorHttpProcess.java
 *
 * author:pengzhiyuan
 * Created on:2016/7/7.
 */
@Deprecated
public class FileShareRestServer {
//    private static final Logger LOG = LoggerFactory.getLogger(FileShareRestServer.class);
//
//    @Value("${fs.rest.service.uri}")
//    private String servicePort;
//
//    private HttpServer server;
//    public static final String ROOT_PATH = "monitor";
//
//    @PostConstruct
//    private void init() {
//        final ResourceConfig resourceConfig = new ResourceConfig(FileServerMonitor.class);
//
//        URI BASE_URI = URI.create(servicePort);
//        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
//        try {
//            server.start();
//            LOG.info(String.format("FileServer Restful Service started." +
//                    "\nTry out %s%s\n",
//                    BASE_URI, ROOT_PATH));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @PreDestroy
//    private void destroy() {
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            @Override
//            public void run() {
//                server.shutdownNow();
//            }
//        }));
//    }

}
