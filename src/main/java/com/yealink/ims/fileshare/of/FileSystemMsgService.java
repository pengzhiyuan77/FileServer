package com.yealink.ims.fileshare.of;

import com.yealink.dataservice.client.IMQSender;
import com.yealink.dataservice.client.RemoteServiceFactory;
import com.yealink.dataservice.client.util.Event;
import com.yealink.ims.fileshare.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件服务器 发布消息给of
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class FileSystemMsgService {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemMsgService.class);
    private static IMQSender mqSender = RemoteServiceFactory.getInstance().getMQSender();

    public static void sendMsgToOF(Event event) {
        mqSender.publishEvent(event);
    }

    /**
     * 发送文件传输消息给OF
     * @param digest
     * @param savePath
     */
    public static void sendFileTransferNotify(String digest, String savePath, boolean isSuccess, String message) {
        Event fileEvent = new Event();
        fileEvent.setTopic(Event.TOPIC_FILE_SERVICE);
        fileEvent.setOperation(FileShareEventOperation.OPT_TRANSFER_SUCCEED);
        fileEvent.setEventTime(new Date().getTime());

        fileEvent.setResourceId(digest);
        Map<String,Object> valueMap=new HashMap<String,Object>();
        valueMap.put("savePath", CommonUtil.getUtf8String(savePath));
        valueMap.put("flag", isSuccess);
        valueMap.put("message", message);
        fileEvent.setExValue(valueMap);
        sendMsgToOF(fileEvent);
    }

    /**
     * http文件上传成功通知of 保存记录及消息通知
     * @param digest
     * @param infoMap
     */
    public static void sendSuccessTrasnferInfoByHttp(String digest, Map<String, Object> infoMap) {
        Event fileEvent = new Event();
        fileEvent.setTopic(Event.TOPIC_FILE_SERVICE);
        fileEvent.setOperation(FileShareEventOperation.OPT_TRANSFER_SUCCEED_HTTP);
        fileEvent.setEventTime(new Date().getTime());

        fileEvent.setResourceId(digest);
        fileEvent.setExValue(infoMap);
        sendMsgToOF(fileEvent);
    }
}

