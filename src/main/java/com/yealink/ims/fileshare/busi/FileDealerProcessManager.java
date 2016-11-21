package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.store.DefaultFileStore;
import com.yealink.ims.fileshare.store.FtpClientManager;
import com.yealink.ims.fileshare.store.IFileStore;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import org.hyperic.sigar.SigarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件业务处理管理
 * author:pengzhiyuan
 * Created on:2016/6/1.
 */
@Component
public class FileDealerProcessManager {
    private static final Logger LOG = LoggerFactory.getLogger(FileDealerProcessManager.class);

    private static FileDealerProcessManager instance;
    public Map<EventType, IDealer> processMap = new HashMap<EventType, IDealer>();
    public static FileDealerProcessManager getInstance() {
        return instance;
    }

    @Value("${fs.request.filesize}")
    private String requestSize;

    @Value("${fs.fileStore.class}")
    private String fileStoreClass;

    @Value("${fs.default.tmp.dir}")
    private String storeDirPath;

    @Value("${fs.file.isEncrypt}")
    private String isFileEncrypt;

    @Autowired
    private FtpClientManager ftpClientManager;
    @Autowired
    private FileServerProvider fileServerProvider;

    // 文件存储接口
    private static IFileStore fileStore;

    public IFileStore getFileStore() {
        return fileStore;
    }

    public String getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(String requestSize) {
        this.requestSize = requestSize;
    }

    public IDealer getDealer(EventType eventType) {
        return processMap.get(eventType);
    }

    public FtpClientManager getFtpClientManager() {
        return ftpClientManager;
    }

    public void setFtpClientManager(FtpClientManager ftpClientManager) {
        this.ftpClientManager = ftpClientManager;
    }

    public boolean isFileEncrypt() {
        return Boolean.valueOf(isFileEncrypt);
    }

    public void setIsFileEncrypt(String isFileEncrypt) {
        this.isFileEncrypt = isFileEncrypt;
    }

    public String getStoreDirPath() {
        return storeDirPath;
    }

    public void setStoreDirPath(String storeDirPath) {
        this.storeDirPath = storeDirPath;
    }

    @PostConstruct
    private void init() {
        LOG.info("init filedealer processmanager......");
        // 获取服务器设置的存储路径
        Map<String, Object> propertyMap = fileServerProvider.queryPropertyById(FileShareConstant.PROPERTY_STORAGE_SERVER_SAVEPATH);
        if (propertyMap != null &&
                !CommonUtil.getString(propertyMap.get("propValue")).equals("")
                && SigarLoader.IS_LINUX) {
            // linux系统才从数据库配置的路径取
            storeDirPath = CommonUtil.getString(propertyMap.get("propValue"))+"/";
        }

        instance = new FileDealerProcessManager();
        instance.setRequestSize(requestSize);
        instance.setFtpClientManager(ftpClientManager);
        instance.setIsFileEncrypt(isFileEncrypt);
        instance.setStoreDirPath(storeDirPath);

        // 鉴权
        instance.processMap.put(EventType.AUTH, new AuthDealer());
        // 请求文件
        instance.processMap.put(EventType.REQUEST, new RequestFileDealer());
        // 请求文件成功
        instance.processMap.put(EventType.REQUEST_SUCC, new ServerRequestFileDealer());
        // 请求文件失败
        instance.processMap.put(EventType.REQUEST_FAIL, new ServerRequestFileDealer());

        //实例化文件存储接口
        try {
            fileStore = (IFileStore) Class.forName(fileStoreClass).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        LOG.info("default save dir:"+storeDirPath);
        if (fileStore instanceof DefaultFileStore) {
            ((DefaultFileStore)fileStore).setStoreDirPath(storeDirPath);
        }

    }

    /**
     *  获取文件存储路径
     * @param fileType - 文件类型
     * @param digest - 唯一标识
     * @return
     */
    public String getFileSavePath(String fileType, String digest) {
        String curYear = XmppDateUtil.getNowYear();
        String curMonth = XmppDateUtil.getNowMonth();
        String curDay = XmppDateUtil.getNowDay();
        String monthDay = curMonth+"-"+curDay;
        return fileType + File.separator + curYear + File.separator + monthDay + File.separator +  digest;
    }
}
