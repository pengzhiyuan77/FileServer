package com.yealink.ims.fileshare.event;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Map;

/**
 * 文件 http消息
 * author:pengzhiyuan
 * Created on:2016/7/20.
 */
public class FsHttpMsg {
    // 路径类型
    private PathType pathType;

    private ChannelHandlerContext ctx;

    private FullHttpRequest request;

    private Map<String, List<String>> paramMap;

    public Map<String, List<String>> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, List<String>> paramMap) {
        this.paramMap = paramMap;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public void setRequest(FullHttpRequest request) {
        this.request = request;
    }

    public PathType getPathType() {

        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }
}
