package com.yealink.ims.fileshare.busi;

import com.yealink.dataservice.client.IRemoteDataService;
import com.yealink.dataservice.client.RemoteServiceFactory;
import com.yealink.dataservice.client.util.Filter;
import com.yealink.ims.fileshare.event.CommonPicFile;
import com.yealink.ims.fileshare.exception.FileShareException;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件服务数据库操作
 * author:pengzhiyuan
 * Created on:2016/7/21.
 */
@Repository
public class FileServerProvider {
    private static final Logger logger = LoggerFactory.getLogger(FileServerProvider.class);

    // 头像
    private static final String YL_AVATARPICFILE_ENTITYNAME = "ylAvatarPicFile";
    // 服务器状态收集
    private static final String YL_FILESERVERSTAT_ENTITYNAME = "ylFileServerStat";
    private static final String YL_PROPERTY_ENTITYNAME="Property";

    // 通用图片
    private static final String YL_COMMON_PICFILE_ENTITYNAME="ylCommonPicFile";

    private static IRemoteDataService remoteDataService = RemoteServiceFactory.getInstance().getRemoteDataService();

    /**
     * 查询头像文件记录
     * @param id
     * @return
     */
    public Map<String, Object> queryAvatarPicFile(String id) {
        String queryFields = "sender,userId,sendDate,fileName,savePath,receiveFileSize,mimeType,md5";
        Map<String, Object> avatarPicFileMap = remoteDataService.queryOne(YL_AVATARPICFILE_ENTITYNAME, queryFields,
                Filter.eq("_id", id).toMap());
        return avatarPicFileMap;
    }

    /**
     * 保存文件服务器状态收集信息
     *
     * @param statMap
     * @return
     */
    public void saveFileServerStat(Map<String, Object> statMap) {
        String _id= UUID.randomUUID().toString();
        statMap.put("_id", _id);
        statMap.put("createDate", XmppDateUtil.formatDate(new Date()));
        try {
            remoteDataService.insertOne(YL_FILESERVERSTAT_ENTITYNAME, statMap);
        } catch (Exception e) {
            logger.error("remote save fileserverstat error!" + e.getMessage());
            throw new FileShareException(e);
        }
    }

    /**
     * 删除服务器状态收集记录
     * @param deadLine -时间，该时间后的数据删除
     */
    public void deleteFileServerStatByDeadLine(String deadLine) {
        remoteDataService.deleteMany(YL_FILESERVERSTAT_ENTITYNAME,
                Filter.lte("createDate", deadLine).toMap());
    }

    /**
     * 查询属性信息
     * @param id
     * @return
     */
    public Map<String, Object> queryPropertyById(String id) {
        String queryFields = "propValue";
        Map<String, Object> fileMap = remoteDataService.queryOne(YL_PROPERTY_ENTITYNAME, queryFields,
                Filter.eq("_id", id).toMap());
        return fileMap;
    }

    /**
     * 保存通用图片信息
     * @param commonPicFile
     */
    public String saveCommonPicFile(CommonPicFile commonPicFile) {
        String _id= UUID.randomUUID().toString();
        commonPicFile.set_id(_id);
        commonPicFile.setCreateDate(XmppDateUtil.formatDate(new Date()));
        try {
            remoteDataService.insertOne(YL_COMMON_PICFILE_ENTITYNAME, commonPicFile);
        } catch (Exception e) {
            logger.error("remote save commonpicfile error!" + e.getMessage());
            throw new RuntimeException(e);
        }
        return _id;
    }

    /**
     * 查询通用图片数据
     * @param id
     * @return
     */
    public Map<String, Object> queryCommonPicFile(String id) {
        String queryFields = "userName,createDate,fileName,savePath,size,mimeType,md5,busiType,busiSubType,flag";
        Map<String, Object> commonPicFileMap = remoteDataService.queryOne(YL_COMMON_PICFILE_ENTITYNAME, queryFields,
                Filter.eq("_id", id).toMap());
        return commonPicFileMap;
    }

    /**
     * 更新数据有效标志
     * @param _id
     * @param flag
     */
    public void updateCommonPicFileFlag(String _id, boolean flag) {
        Map<String,Object> updateFields = new HashMap<String, Object>();
        updateFields.put("flag", flag);
        remoteDataService.updateOne(YL_COMMON_PICFILE_ENTITYNAME, _id, updateFields);
    }

    /**
     * 根据有效标志查询对应的图片列表
     * @param flag 有效标志
     * @return
     */
    public List<Map<String,Object>> queryCommonPicFileByFlag(boolean flag) {
        String queryFields = "_id,userName,createDate,fileName,savePath,size,mimeType,md5,busiType,busiSubType,flag";
        List<Map<String,Object>> commonPicFileMapList = remoteDataService.query(YL_COMMON_PICFILE_ENTITYNAME, queryFields,
                Filter.eq("flag", flag).toMap());
        return commonPicFileMapList;
    }

    /**
     * 根据时间和有效标志查询对应的图片列表
     * @param endDate -- 多长时间前的文件
     * @param flag
     * @return
     */
    public List<Map<String,Object>> queryCommonPicFileByFlag(String endDate, boolean flag) {
        String queryFields = "_id,userName,createDate,fileName,savePath,size,mimeType,md5,busiType,busiSubType,flag";
        List<Map<String,Object>> commonPicFileMapList = remoteDataService.query(YL_COMMON_PICFILE_ENTITYNAME, queryFields,
                Filter.and(Filter.lt("createDate", endDate), Filter.eq("flag", flag)).toMap());
        return commonPicFileMapList;
    }

    /**
     * 删除通用图片文件
     */
    public void deleteCommonPicFileById(String id) {
        remoteDataService.deleteOne(YL_COMMON_PICFILE_ENTITYNAME, id);
    }

}
