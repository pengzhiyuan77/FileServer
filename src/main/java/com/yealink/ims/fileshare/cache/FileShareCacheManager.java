package com.yealink.ims.fileshare.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 文件服务 缓存管理
 * author:pengzhiyuan
 * Created on:2016/6/2.
 */
@Component
public class FileShareCacheManager {
    private static final Logger LOG = LoggerFactory.getLogger(FileShareCacheManager.class);

    private static FileShareCacheManager fileShareCacheManager = new FileShareCacheManager();
    private final String FILE_CACHE_NAME="fileInfoCache";
    private final String CACHE_CONFIG_PATH = System.getProperty("FileServer_Home")+"/config/ehcache.xml";
    private CacheManager manager = null;
    private static Cache fileCache = null;

    private FileShareCacheManager(){}

    public static FileShareCacheManager getInstance() {
        return fileShareCacheManager;
    }

    @PostConstruct
    private void init() {
        LOG.info("FILE CACHE MANAGER INIT...");
        manager = CacheManager.create(CACHE_CONFIG_PATH);
        fileCache = manager.getCache(FILE_CACHE_NAME);

//        // 测试用 后续删掉
//        String digest="ff46c072eb997dd754f0720caa076495ceb9ef0a";
//        Map<String,Object> valueMap = new HashMap<String,Object>();
//        valueMap.put("filename","test.rar");
//        valueMap.put("direction","0");
//        valueMap.put("size","111457239");
//        valueMap.put("secretKey","gbXV0361AmRSJH1jMdBa1A==");
//        valueMap.put("fileType","offline");
//        boolean add = putCache(digest, valueMap);
//        System.out.println("添加缓存："+add);
        //================================
    }

    @PreDestroy
    private void destroy() {
        LOG.info("FILE CACHE MANAGER DESTROY...");
        manager.shutdown();
    }

    /**
     * 默认
     * @param key
     * @param obj --必须可序列化对象
     * @return true添加成功 false添加失败
     */
    public boolean putCache(String key, Object obj) {
        if (fileCache == null) {
            return false;
        }
        fileCache.put(new Element(key, obj));
        return true;
    }

    /**
     * 直接存储到硬盘
     * @param key
     * @param obj
     * @return
     */
    public boolean putCacheFlush(String key, Object obj) {
        if (fileCache == null) {
            return false;
        }
        fileCache.put(new Element(key, obj));
        fileCache.flush();
        return true;
    }

    /**
     * 从缓存获取数据
     * @param key
     * @return
     */
    public Object getDataFromCache(String key) {
        if (fileCache == null) {
            return null;
        }
        Element element = fileCache.get(key);
        if (element == null) {
            return null;
        }
        return element.getObjectValue();
    }

    /**
     * 删除缓存
     * @param key
     */
    public void removeFromCache(String key) {
        if (fileCache != null) {
            fileCache.remove(key);
        }
    }

    /**
     * 判断当前key是否在缓存中
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        if (fileCache == null) {
            return false;
        }
        return fileCache.isKeyInCache(key);
    }
}
