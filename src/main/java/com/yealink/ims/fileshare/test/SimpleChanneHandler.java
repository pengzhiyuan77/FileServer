package com.yealink.ims.fileshare.test;

import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.util.AESUtil;
import com.yealink.ims.fileshare.util.ByteUtil;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class SimpleChanneHandler extends ChannelInboundHandlerAdapter {
	private ByteBuf clientMessage;

	public SimpleChanneHandler() {

		// 发送鉴权信息
		String digest = "ff46c072eb997dd754f0720caa076495ceb9ef0a";
		byte[] data = new byte[0];
		try {
			data = digest.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		clientMessage = Unpooled.buffer(4 + data.length);
		clientMessage.writeShort(0x1002);
		clientMessage.writeBytes(ByteUtil.intToUnsignShortBytes(data.length));
		clientMessage.writeBytes(data);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(clientMessage);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (msg instanceof FsMsg) {
				EventType eventType = ((FsMsg) msg).getCmd();

				System.out.println("服务端发来命令：" + eventType.getCmd());

				//文件请求
				if (eventType.getCmd() == 0x1001) {
					AESUtil util = new AESUtil();

					byte[] data = ((FsMsg) msg).getData();

					String key = "gbXV0361AmRSJH1jMdBa1A==";

					Cipher enCipher = null;
					Cipher deCipher = null;

					// 是否需要加密
					boolean isEncrypt = true;

					if (isEncrypt) {
						enCipher = util.initAESEnCipherZeroPadding(key);
						deCipher = util.initAESDeCipherZeroPadding(key);
					}

					if (deCipher != null) {
						data =  util.decrypt(deCipher, data);
					}


					System.out.println("服务端发来命令：" + data.length);

					if (data.length <= 8) {
						System.out.println("数据格式错误。。。");
					}

					long offset = ByteUtil.bytesToLong2(ByteUtil.subBytes(data, 0, 6), 0);
					int length = ByteUtil.unSignBytesToInt(ByteUtil.subBytes(data, 6, 2), 0);

					System.out.println("offset。。。"+offset);
					System.out.println("length。。。"+length);

					byte[] fileData = readBigFile(offset, length);

					// 加密offset+length_fileData
					ByteBuf tempBuf = Unpooled.buffer(8+fileData.length);
					tempBuf.writeBytes(ByteUtil.longToBytes2(offset));
					tempBuf.writeBytes(ByteUtil.intToUnsignShortBytes(length));
					tempBuf.writeBytes(fileData);
					// 加密
					byte[] unionData = tempBuf.array();

					byte[] enFileData = null;
					if (enCipher != null) {
						enFileData = util.encrypt(enCipher, unionData);
					} else {
						enFileData = unionData;
					}
					tempBuf.release();

					System.out.println("发送消息体大小:"+unionData.length);
					System.out.println("加密发送消息体大小:"+enFileData.length);

					ByteBuf frame = Unpooled.buffer(4 + enFileData.length);
					frame.writeShort(0x1101);
					frame.writeBytes(ByteUtil.intToUnsignShortBytes(enFileData.length));
					frame.writeBytes(enFileData);
					ChannelFuture channelFutture = ctx.channel().writeAndFlush(frame);
					channelFutture.addListener(new ChannelFutureListener(){

						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							System.out.println(XmppDateUtil.utcTimeToLocal(new Date())+",发送文件数据。。结束");
						}

					});
				}

			} else {
				System.out.println("格式不对。。。");
			}
		} finally {
			// FsMsg不会被release
			System.out.println("收到：" + msg);
			ReferenceCountUtil.release(msg);
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

	public static byte[] readBigFile(long offset, int length) throws IOException {
		String fileName = "D:\\开发工具\\Gerrit+Git.rar";
		RandomAccessFile randomFile = null;
		randomFile = new RandomAccessFile(fileName, "r");
		long fileLength = randomFile.length();
		System.out.println("文件大小:" + fileLength);
		randomFile.seek(offset);
		byte[] bytes = new byte[length];
		int byteread = 0;
		byteread = randomFile.read(bytes);
		System.out.println(bytes.length);
		System.out.println(byteread);
		if (randomFile != null) {
			randomFile.close();
		}

		return bytes;
	}

}
