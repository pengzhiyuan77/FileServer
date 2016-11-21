package com.yealink.ims.fileshare.busi.http;

import com.yealink.ims.fileshare.event.FsHttpMsg;

/**
 * http服务处理
 * author:pengzhiyuan
 * Created on:2016/7/20.
 */
public interface IHttpProcess {
    public void execute(FsHttpMsg fsHttpMsg);
}
