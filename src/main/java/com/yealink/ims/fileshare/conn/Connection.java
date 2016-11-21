package com.yealink.ims.fileshare.conn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

/**
 * 文件服务连接
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class Connection {

    /**
     * 连接
     */
    private final long id;

    private ChannelId channelId;

    private ChannelHandlerContext ctx;
    /**
     * 文化传输的唯一标识
     */
    private volatile String digest;

    public Connection(long id, ChannelHandlerContext ctx) {
        this.id = id;
        this.ctx = ctx;
    }

    public long getId() {
        return id;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Connection other = (Connection) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(super.hashCode()) + "->Connection [id=" + id
                + ", ctx=" + ctx + "]";
    }

}
