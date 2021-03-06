package com.yealink.ims.fileshare.test;

import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.event.FsMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ClineDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 4) {
            ctx.pipeline().close();
            return;
        }
        FsMsg event = new FsMsg();
        event.setCtx(ctx);
        event.setCmd(EventType.valuesOf(in.readShort()));
        // 读取长度
        int length = in.readUnsignedShort();
        System.out.println("读取到消息体 长度:"+length);
        if (length > 0) {
        	byte[] data = new byte[length];
            in.readBytes(data);
            event.setData(data);
        }
        out.add(event);
	}

}
