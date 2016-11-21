package com.yealink.ims.fileshare.busi.http;

import com.yealink.ims.fileshare.event.FsHttpMsg;
import com.yealink.ims.fileshare.event.PathType;
import com.yealink.ims.fileshare.util.FileShareThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件http服务管理类
 * author:pengzhiyuan
 * Created on:2016/7/20.
 */
@Component
public class FileHttpProcessManager {
    private static final Logger LOG = LoggerFactory.getLogger(FileHttpProcessManager.class);
    private static final Map<PathType, IHttpProcess> PATH_ROUTER = new HashMap<PathType, IHttpProcess>();

    public static IHttpProcess getHttpProcess(PathType path) {
        return PATH_ROUTER.get(path);
    }

    /**
     * 提交http请求服务处理
     * @param process
     * @param fsHttpMsg
     */
    public static void submitHttpProcess(final IHttpProcess process, final FsHttpMsg fsHttpMsg) {
        FileShareThreadExecutor.getInstance().getHttpThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                process.execute(fsHttpMsg);
            }
        });
    }

    @PostConstruct
    public void init() {
        LOG.info("File http processmanager init..");

        // 头像服务
        PATH_ROUTER.put(PathType.AVATAR, new AvatarHttpProcess());

        // 文件服务器监控
        PATH_ROUTER.put(PathType.MONITOR, new FileServerMonitorHttpProcess());

        // 通用图片处理
        PATH_ROUTER.put(PathType.IMAGE, new CommonImageHttpProcess());
    }

}
