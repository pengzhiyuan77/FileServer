package com.yealink.ims.fileshare.of;

/**
 * 发给文件服务器消息 操作类型
 * author:pengzhiyuan
 * Created on:2016/5/26.
 */
public class FileShareEventOperation {

    /**
     * 初次发送文件信息
     */
    public static final String OPT_INIT = "init";
    /**
     * 客户端链路建立成功，通知连接的服务器
     */
    public static final String OPT_STREAM_USED = "streamUsed";
    /**
     * 客户端链路建立失败 通知文件服务器
     */
    public static final String OPT_TERMINATE = "terminate";
    /**
     * 文件传输完成通知
     */
    public static final String OPT_TRANSFER_SUCCEED = "success";
    /**
     * 通知删除群文件
     */
    public static final String OPT_DELETE_GROUPFILE = "delGroup";
    /**
     * 通知删除离线文件
     */
    public static final String OPT_DELETE_OFFLINEFILE = "delOffline";
    /**
     * 广播获取可用文件服务器
     */
    public static final String OPT_GET_FS = "getFS";
    /**
     * 文件服务状态主动通知
     */
    public static final String OPT_SERVER_NOTIFY = "fsState";
    /**
     * http文件传输完成通知
     */
    public static final String OPT_TRANSFER_SUCCEED_HTTP = "successByHttp";
    /**
     * 通用图片有效标志更新
     */
    public static final String OPT_UPDATE_VALID_PIC = "updatePic";

}
