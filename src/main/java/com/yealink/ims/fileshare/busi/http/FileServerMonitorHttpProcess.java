package com.yealink.ims.fileshare.busi.http;

import com.yealink.ims.fileshare.event.FsHttpMsg;
import com.yealink.ims.fileshare.server.FileServerMonitor;
import com.yealink.ims.fileshare.util.CommonUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * 文件服务器监控
 * author:pengzhiyuan
 * Created on:2016/7/22.
 */
public class FileServerMonitorHttpProcess extends DefaultHttpProcess {
    @Override
    protected void doExeGetRequest(FsHttpMsg fsHttpMsg, FullHttpResponse response) throws Exception {
        ChannelHandlerContext ctx = fsHttpMsg.getCtx();
        FullHttpRequest request = fsHttpMsg.getRequest();

        // 入参
        Map<String, List<String>> paramMap = fsHttpMsg.getParamMap();
        if (paramMap != null && paramMap.containsKey("method")) {

            List<String> methodList = paramMap.get("method");
            if (methodList==null || methodList.size()==0 ||
                    CommonUtil.getString(methodList.get(0)).equals("")) {
                HttpUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            // 目前只要string类型，其它以后扩展
            String value = null;
            List<String> valueList = paramMap.get("value");
            if (valueList != null && valueList.size() > 0) {
                value = CommonUtil.getString(valueList.get(0));
            }

            // 获取查询方法
            String methodName = CommonUtil.getString(methodList.get(0));
            FileServerMonitor fileServerMonitor = new FileServerMonitor();
            Class clazz = FileServerMonitor.class;

            // 执行获取结果
            String result = "";
            Class parameterTypes = null;
            if (value != null) {
                parameterTypes = String.class;
            }
            Method method = null;
            if (parameterTypes != null) {
                method = clazz.getMethod(methodName, parameterTypes);
                result = CommonUtil.getString(method.invoke(fileServerMonitor, value));
            } else {
                method = clazz.getMethod(methodName);
                result = CommonUtil.getString(method.invoke(fileServerMonitor));
            }

            // 设置响应头
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN);
            // 设置响应主体内容
            response.content().writeBytes(result.getBytes(Charset.forName("UTF-8")));
            ctx.write(response);
            // 设置响应结束
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);

        } else {
            HttpUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
        }
    }

    @Override
    protected void doExePostRequest(FsHttpMsg fsHttpMsg, FullHttpResponse response) throws Exception {

    }
}
