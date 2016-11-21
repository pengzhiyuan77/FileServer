package com.yealink.ims.fileshare.event;

/**
 * 通用图片
 * author:pengzhiyuan
 * Created on:2016/9/9.
 */
public class CommonPicFile {
    private String _id;    //主键
    private String userName;   //上传者
    private String createDate; //生成时间
    private String fileName;  //文件名称
    private String md5;     //文件MD5码
    private String savePath;  //存储路径
    private long size;     //文件大小
    private String mimeType; //mimetype
    private String busiType; //业务类型
    private String busiSubType; // 业务子类型
    private boolean flag; //有效标志

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getBusiType() {
        return busiType;
    }

    public void setBusiType(String busiType) {
        this.busiType = busiType;
    }

    public String getBusiSubType() {
        return busiSubType;
    }

    public void setBusiSubType(String busiSubType) {
        this.busiSubType = busiSubType;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
