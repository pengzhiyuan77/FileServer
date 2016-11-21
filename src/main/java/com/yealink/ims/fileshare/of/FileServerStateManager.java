package com.yealink.ims.fileshare.of;

import com.yealink.dataservice.client.util.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件服务状态管理 用于跟of之间交互
 * author:pengzhiyuan
 * Created on:2016/6/2.
 */
public class FileServerStateManager {
    private static final Logger LOG = LoggerFactory.getLogger(FileServerStateManager.class);
    private static FileServerStateManager fileServerStateManager = new FileServerStateManager();

    private boolean isRunning = false;
    private String fsJid;
    private String host;
    private String port;

    private FileServerStateManager(){}

    public static FileServerStateManager getInstance() {
        return fileServerStateManager;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getFsJid() {
        return fsJid;
    }

    public void setFsJid(String fsJid) {
        this.fsJid = fsJid;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /**
     * 服务消息通知
     */
    public void fireFsNotify() {
        Event event = new Event();
        event.setTopic(Event.TOPIC_FILE_SERVICE);
        event.setEventTime(new Date().getTime());
        event.setOperation(FileShareEventOperation.OPT_SERVER_NOTIFY);
        event.setResourceId(fsJid);
        Map<String,Object> valueMap=new HashMap<String,Object>();
        valueMap.put("isRunning", isRunning);
        valueMap.put("host", host);
        valueMap.put("port", port);
        event.setExValue(valueMap);
        LOG.debug("send fileserver message notify:"+host+","+isRunning);
        new FileSystemMsgService().sendMsgToOF(event);
    }
}
