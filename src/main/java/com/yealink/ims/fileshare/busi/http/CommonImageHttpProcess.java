package com.yealink.ims.fileshare.busi.http;

import com.yealink.ims.fileshare.busi.FileDealerProcessManager;
import com.yealink.ims.fileshare.busi.FileServerProvider;
import com.yealink.ims.fileshare.event.CommonPicFile;
import com.yealink.ims.fileshare.event.FsHttpMsg;
import com.yealink.ims.fileshare.store.DefaultFileStore;
import com.yealink.ims.fileshare.store.IFileStore;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import com.yealink.ims.fileshare.util.MD5Util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通用图片上传下载接口
 *
 * 后续和头像那部分代码重构一下
 *
 * @author pzy
 *
 */
public class CommonImageHttpProcess extends DefaultHttpProcess {
	private static final Logger LOG = LoggerFactory.getLogger(AvatarHttpProcess.class);
	private HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	@Override
	protected void doExeGetRequest(FsHttpMsg fsHttpMsg, FullHttpResponse response) throws Exception {

		FileServerProvider fileServerProvider = new FileServerProvider();
		ChannelHandlerContext ctx = fsHttpMsg.getCtx();
		FullHttpRequest request = fsHttpMsg.getRequest();
		//获取通用图片返回
		// 入参
		Map<String, List<String>> paramMap = fsHttpMsg.getParamMap();
		if (paramMap != null && paramMap.containsKey("id")) {
			List<String> idList = paramMap.get("id");
			if (idList==null || idList.size()==0 ||
					CommonUtil.getString(idList.get(0)).equals("")) {
				HttpUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
				return;
			}
			Map<String, Object> commonPicFileMap = fileServerProvider.queryCommonPicFile(CommonUtil.getString(idList.get(0)));
			if (commonPicFileMap == null) {
				HttpUtils.sendError(ctx, HttpResponseStatus.NOT_FOUND);
				return;
			}

			// 获取通用图片存储路径
			String filePath = CommonUtil.getString(commonPicFileMap.get("savePath"));
			filePath = CommonUtil.getFormatPath(filePath);

			// 获取头像数据
			DefaultFileStore fileStore = (DefaultFileStore) FileDealerProcessManager.getInstance().getFileStore();
//                byte[] fileData = fileStore.getFileData(filePath);
			File imgFile = new File(fileStore.getStoreDirPath()+filePath);

			// 缓存处理
			String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
			if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
				SimpleDateFormat dateFormatter = new SimpleDateFormat(HttpUtils.DATE_FORMAT);
				Date ifModifiedSinceDate = null;
				try {
					ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (ifModifiedSinceDate != null) {
					// 比较到秒 看发送给客户端的last-modified时间格式
					long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime()/1000;
					long fileLastModifiedSeconds = imgFile.lastModified()/1000;
					if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
						HttpUtils.sendNotModified(ctx);
						return;
					}
				}
			}

			// 获取申请的大小
			List<String> xList = paramMap.get("x"); //获取宽度参数
			List<String> yList = paramMap.get("y"); //获取高度参数
			int width=0;
			int height=0;
			if (xList != null && xList.size()>0) {
				try {
					width = Integer.parseInt(xList.get(0));
				} catch (Exception e) {
					LOG.error("avatar,x format error:", e);
				}
			}
			if (yList != null && yList.size()>0) {
				try {
					height = Integer.parseInt(yList.get(0));
				} catch (Exception e) {
					LOG.error("avatar,y format error:", e);
				}
			}
			byte[] fileData = CommonUtil.toThumbnail(imgFile, width, height);

			// 设置响应头
			HttpUtils.setContentTypeHeader(response, imgFile);
			LOG.debug("content-type:"+response.headers().get(HttpHeaderNames.CONTENT_TYPE));
			// 设置缓存
			HttpUtils.setDateAndCacheHeaders(response, imgFile);
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileData.length);
//                if (HttpUtil.isKeepAlive(request)) {
//                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//                }
			// 设置响应内容主体
			response.content().writeBytes(fileData);
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
		//==== 上传通用 图片
		FileServerProvider fileServerProvider = new FileServerProvider();
		FullHttpRequest request = fsHttpMsg.getRequest();
		ChannelHandlerContext ctx = fsHttpMsg.getCtx();
		Attribute<HttpPostRequestDecoder> attr = ctx.channel().attr(FileShareConstant.POST_DECODER_KEY);
		HttpPostRequestDecoder decoder = attr.get();
		// 文件上传其它参数主要还是通过url param传递吧
		Map<String, List<String>> paramMap = fsHttpMsg.getParamMap();
		List<String> userNameList = paramMap.get("userName"); //获取用户名参数
		List<String> busiTypeList = paramMap.get("busiType"); //获取业务类型
		List<String> busiSubTypeList = paramMap.get("busiSubType"); //获取业务子类参数
		List<String> md5List = paramMap.get("md5"); //获取md5参数
		List<String> sizeList = paramMap.get("size"); //获取文件大小参数
		List<String> validList = paramMap.get("valid"); //获取生效标志参数
		long fileSize = 0; // 文件大小
		String userName = "";
		String busiType = "";
		String busiSubType = "";
		String md5="";
		String saveFileNamePath=""; // 存储路径
		String valid = "";
		if (userNameList != null && userNameList.size() > 0) {
			userName = CommonUtil.getString(userNameList.get(0));
		}
		if (busiTypeList != null && busiTypeList.size() > 0) {
			busiType = CommonUtil.getString(busiTypeList.get(0));
		}
		if (busiSubTypeList != null && busiSubTypeList.size() > 0) {
			busiSubType = CommonUtil.getString(busiSubTypeList.get(0));
		}
		if (md5List != null && md5List.size() > 0) {
			md5 = CommonUtil.getString(md5List.get(0));
		}
		if (sizeList != null && sizeList.size() > 0) {
			fileSize = Long.valueOf(sizeList.get(0));
		}
		if (validList != null && validList.size() > 0) {
			valid = CommonUtil.getString(validList.get(0));
		}

		//如果参数传的不对，直接返回客户端错误
		if (busiType.equals("")) {
			HttpUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}

		// 生成唯一码
		String sessionId = UUID.randomUUID().toString();
		String digest = CommonUtil.createSha1Digest(sessionId,userName,busiType);

		if (request instanceof HttpRequest) {
			if (decoder != null) {
				// 清除临时文件
				decoder.cleanFiles();
				decoder = null;
			}
			try {
				decoder = new HttpPostRequestDecoder(factory, request);
				attr.set(decoder);
			} catch (Exception e) {
				e.printStackTrace();
				HttpUtils.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				return;
			}
		}
		if (decoder != null && request instanceof HttpContent) {
			try {
				decoder.offer(request);
			} catch (Exception e) {
				e.printStackTrace();
				HttpUtils.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				return;
			}
			// 读取post过来的数据
			try {
				while (decoder.hasNext()) {
					InterfaceHttpData data = decoder.next();
					if (data != null) {
						try {
							// 文件上传
							if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
								saveFileNamePath = writeHttpData(busiType, data, digest);
							}
						} finally {
							data.release();
						}
					}
				}
			} catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
				e1.printStackTrace();
				LOG.debug("end chunk...");
			}

			// 最后一个 httpcontent
			if (request instanceof LastHttpContent) {
				//销毁decoder释放所有的资源
				decoder.destroy();
				decoder = null;
				request.release();

				// 上传成功, 校验md5
				if (saveFileNamePath != null) {
					IFileStore fileStore = FileDealerProcessManager.getInstance().getFileStore();
					// 进行md5校验
					boolean md5Verify = fileStore.verifyFileMd5(saveFileNamePath, md5);
					LOG.debug("commonfile upload, md5="+md5+",md5Verify="+md5Verify+"，savePath="+saveFileNamePath);
					// MD5校验失败
					if (!md5Verify) {
						fileStore.deleteFile(saveFileNamePath);
						// 返回错误信息
						HttpUtils.sendError(ctx, HttpResponseStatus.CONFLICT);
						return;
					}

					// 文件名
					String fileName = saveFileNamePath.substring(saveFileNamePath.lastIndexOf(File.separator)+1);
					String filePath = FileDealerProcessManager.getInstance().getStoreDirPath()+saveFileNamePath;
					// 保存到数据库
					String fileId = "";
					boolean flag = false;
					if (valid.equals("1")) {
						flag = true;
					}
					if ("".equals(md5)) {
						md5 = MD5Util.md5File(filePath);
					}
					if (fileSize == 0) {
						File file = new File(filePath);
						fileSize = file.length();
					}
					CommonPicFile commonPicFile = new CommonPicFile();
					commonPicFile.setBusiSubType(busiSubType);
					commonPicFile.setBusiType(busiType);
					commonPicFile.setFileName(fileName);
					commonPicFile.setFlag(flag);
					commonPicFile.setMd5(md5);
					commonPicFile.setSize(fileSize);
					commonPicFile.setSavePath(saveFileNamePath);
					commonPicFile.setUserName(userName);
					commonPicFile.setMimeType(HttpUtils.getMimeType(filePath));
					fileId = fileServerProvider.saveCommonPicFile(commonPicFile);

					// 返回保存的ID
					String result = "{\"id\":\"" + fileId + "\"}";
					response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON);
					response.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.getBytes().length);
					response.content().writeBytes(result.getBytes());
					ctx.channel().attr(FileShareConstant.POST_DECODER_KEY).remove();
					ctx.write(response);
					// 设置响应结束
					ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
					lastContentFuture.addListener(ChannelFutureListener.CLOSE);
				} else {
					ctx.channel().attr(FileShareConstant.POST_DECODER_KEY).remove();
					HttpUtils.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
	}

}
