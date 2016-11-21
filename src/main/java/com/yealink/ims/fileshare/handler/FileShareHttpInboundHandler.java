package com.yealink.ims.fileshare.handler;

import com.yealink.ims.fileshare.busi.http.FileHttpProcessManager;
import com.yealink.ims.fileshare.busi.http.HttpUtils;
import com.yealink.ims.fileshare.busi.http.IHttpProcess;
import com.yealink.ims.fileshare.event.FsHttpMsg;
import com.yealink.ims.fileshare.event.PathType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http处理器
 * @author pzy
 *
 */
public class FileShareHttpInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static final Logger LOG = LoggerFactory.getLogger(FileShareHttpInboundHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		// 解码失败
		if (!request.decoderResult().isSuccess()) {
			HttpUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}

		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
		String path = queryStringDecoder.path();

		if ("/".equals(path)) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE);
			HttpUtils.sendSuccessResponseAndClose(ctx, response);
			return;
		}
		if ("/favicon.ico".equals(path)) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
			HttpUtils.sendSuccessResponseAndClose(ctx, response);
			return;
		}

		LOG.debug("queryStringDecoder.path():" + path);
		LOG.debug("queryStringDecoder.parameters();:" + queryStringDecoder.parameters());

		PathType pathType = PathType.valuesOf(queryStringDecoder.path());
		IHttpProcess process = FileHttpProcessManager.getHttpProcess(pathType);
		if (process == null) {
			LOG.debug("cannot found process class..");
			HttpUtils.sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		FsHttpMsg fsHttpMsg = new FsHttpMsg();
		fsHttpMsg.setCtx(ctx);
		fsHttpMsg.setPathType(pathType);
		fsHttpMsg.setRequest(request.copy());
		fsHttpMsg.setParamMap(queryStringDecoder.parameters());
		// 派发任务
		FileHttpProcessManager.submitHttpProcess(process, fsHttpMsg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOG.info("http server exception:", cause);
		if (ctx.channel().isActive()) {
			HttpUtils.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
