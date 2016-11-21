package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.exception.FileShareException;

/**
 * 文件 处理接口
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public interface IDealer {
    public void deal(FsMsg fsMsg) throws FileShareException;
}

