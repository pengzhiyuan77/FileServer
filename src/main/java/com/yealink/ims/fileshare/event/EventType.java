package com.yealink.ims.fileshare.event;

import com.yealink.ims.fileshare.util.FileShareConstant;

/**
 * 事件命令类型
 * 鉴权或者请求数据
 * author:pengzhiyuan
 * Created on:2016/6/1.
 */
public enum EventType {
    /**
     * 请求文件
     */
    REQUEST(FileShareConstant.FILE_REQUEST),
    REQUEST_SUCC(FileShareConstant.FILE_REQUEST_SUCCESS),
    REQUEST_FAIL(FileShareConstant.FILE_REQUEST_FAIL),
    /**
     * 鉴权
     */
    AUTH(FileShareConstant.AUTH),
    AUTH_SUCC(FileShareConstant.AUTH_SUCCESS),
    AUTH_FAIL(FileShareConstant.AUTH_FAIL);

    private short cmd;

    private  EventType(short cmd) {
        this.cmd = cmd;
    }

    public short getCmd() {
        return cmd;
    }

    public static EventType valuesOf(short e) {
        EventType[] vs = EventType.values();
        if (vs == null || vs.length == 0) {
            return null;
        }
        for (EventType event : vs) {
            if (event.getCmd() == e) {
                return event;
            }
        }
        return null;
    }

}
