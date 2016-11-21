package com.yealink.ims.fileshare.busi.http;

import com.yealink.ims.fileshare.busi.FileDealerProcessManager;
import com.yealink.ims.fileshare.event.FsHttpMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * http服务处理抽象类
 * author:pengzhiyuan
 * Created on:2016/7/21.
 */
public abstract class DefaultHttpProcess implements IHttpProcess  {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpProcess.class);

    @Override
    public void execute(FsHttpMsg fsHttpMsg) {
        FullHttpRequest request = fsHttpMsg.getRequest();
        ChannelHandlerContext ctx = fsHttpMsg.getCtx();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        try {
            if (request.method() == HttpMethod.GET) {
                doExeGetRequest(fsHttpMsg, response);
            } else if (request.method() == HttpMethod.POST) {
                doExePostRequest(fsHttpMsg, response);
            } else {
                // 暂时只支持post和get，其它的以后再说
                throw new Exception("HTTP method not support:" + request.method().name());
            }
        } catch (Exception e) {
            LOG.error("http process error:", e);
            if(response.refCnt() > 0) {
                response.release();
            }
            HttpUtils.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (request.refCnt() > 0) {
                ReferenceCountUtil.release(request);
            }
        }
    }

    /**
     * 写文件数据
     * @param fileType 文件业务类型-用于存储路径处理
     * @param data
     * @param digest
     * @throws IOException
     */
    protected String writeHttpData(String fileType, InterfaceHttpData data, String digest) throws IOException {
        FileUpload fileUpload = (FileUpload) data;
        if (fileUpload.isCompleted()) {
            // 文件名
            String fileName = fileUpload.getFilename();
            // 头像存储路径
            String baseDir = FileDealerProcessManager.getInstance().getStoreDirPath();
            String savePath = FileDealerProcessManager.getInstance().getFileSavePath(fileType, digest);
            String saveFileNamePath = savePath + File.separator + fileName;
            File toFileDir = new File(baseDir + savePath);
            if (!toFileDir.exists()) {
                toFileDir.mkdirs();
            }
            // 比较耗内存
            fileUpload.renameTo(new File(baseDir + saveFileNamePath));
            return saveFileNamePath;
        }
        return null;
    }

    /**
     * 处理get请求
     * @param fsHttpMsg
     * @param response
     */
    protected abstract void doExeGetRequest(FsHttpMsg fsHttpMsg, FullHttpResponse response) throws Exception;

    /**
     * 处理post请求
     * @param fsHttpMsg
     * @param response
     */
    protected abstract void doExePostRequest(FsHttpMsg fsHttpMsg, FullHttpResponse response) throws Exception;
}
