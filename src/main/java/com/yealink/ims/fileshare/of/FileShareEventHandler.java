package com.yealink.ims.fileshare.of;

import com.yealink.dataservice.client.IEventHandler;
import com.yealink.dataservice.client.util.Event;
import com.yealink.ims.fileshare.busi.FileDealerProcessManager;
import com.yealink.ims.fileshare.busi.FileServerProvider;
import com.yealink.ims.fileshare.cache.FileShareCacheManager;
import com.yealink.ims.fileshare.conn.ConnectionManager;
import com.yealink.ims.fileshare.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文件服务器 和 OF之间的消息交互
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
@Component
public class FileShareEventHandler implements IEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FileShareEventHandler.class);
    public String getTopic() {
        return Event.TOPIC_FILE_SERVICE;
    }

    public void onEvent(Event event) {
        String operation = event.getOperation();
        Map<String,Object> valueMap = event.getExValue();
        String digest = String.valueOf(event.getResourceId());

        // 对文件名进行转码
        Object filenameFromOf = valueMap.get("filename");
        if (filenameFromOf != null) {
            valueMap.put("filename", CommonUtil.getStringFromUtf8(String.valueOf(filenameFromOf)));
        }

        LOG.debug("Receiver of message:"+digest+","+valueMap);

        // 接收of发的 文件信息
        if (FileShareEventOperation.OPT_INIT.equals(operation)) {
            // 添加文件信息到缓存
            FileShareCacheManager.getInstance().putCache(digest, valueMap);
        } else if (FileShareEventOperation.OPT_GET_FS.equals(operation)) {
            // 获取文件服务器状态信息
            FileServerStateManager.getInstance().fireFsNotify();
        } else if (FileShareEventOperation.OPT_STREAM_USED.equals(operation)) {
            String streamHost = String.valueOf(valueMap.get("streamHost"));
            if (!streamHost.equals(FileServerStateManager.getInstance().getFsJid())) {
                FileShareCacheManager.getInstance().removeFromCache(digest);
            }
        } else if (FileShareEventOperation.OPT_TERMINATE.equals(operation)) {
            FileShareCacheManager.getInstance().removeFromCache(digest);
        } else if (FileShareEventOperation.OPT_TRANSFER_SUCCEED.equals(operation)) {
            // 针对客户端为接收端，接收文件完成后通知文件服务器
            // 文件服务器删掉缓存 关闭该连接
            FileShareCacheManager.getInstance().removeFromCache(digest);
            ConnectionManager.getInstance().closeAllConnByDigest(digest);
        } else if (FileShareEventOperation.OPT_DELETE_GROUPFILE.equals(operation)) {
            // 删除群文件
            String savePath = String.valueOf(valueMap.get("savePath")); // 路径 包含了文件名
            String fileName = CommonUtil.getStringFromUtf8(String.valueOf(valueMap.get("fileName")));
            FileDealerProcessManager.getInstance().getFileStore().deleteFile(savePath);
        } else if (FileShareEventOperation.OPT_DELETE_OFFLINEFILE.equals(operation)) {
            // 文件路径
            String savePath = event.getSource();
            FileDealerProcessManager.getInstance().getFileStore().deleteFile(savePath); // 路径 包含了文件名
        } else if (FileShareEventOperation.OPT_UPDATE_VALID_PIC.equals(operation)) {
            FileServerProvider fileServerProvider = new FileServerProvider();
            // 图片ID 用英文逗号隔开
            String ids = CommonUtil.getString(event.getSource());
            if (!ids.equals("")) {
                String[] idArr = ids.split(",");
                for (String id : idArr) {
                    fileServerProvider.updateCommonPicFileFlag(id, true);
                }
            }
        }
    }
}
