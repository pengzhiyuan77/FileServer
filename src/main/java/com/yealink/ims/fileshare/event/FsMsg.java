package com.yealink.ims.fileshare.event;

import io.netty.channel.ChannelHandlerContext;

/**
 * 文件共享 消息
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class FsMsg {
    /**
     * 消息类型
     */
    private EventType cmd;
    /**
     * 业务数据
     */
    private byte[] data;

    private ChannelHandlerContext ctx;

    public EventType getCmd() {
        return cmd;
    }

    public void setCmd(EventType cmd) {
        this.cmd = cmd;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
}
