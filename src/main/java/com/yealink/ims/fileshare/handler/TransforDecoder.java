package com.yealink.ims.fileshare.handler;

import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.event.FsMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 消息解码
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class TransforDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() <= 4) {
            ctx.pipeline().close();
            return;
        }
        FsMsg event = new FsMsg();
        event.setCtx(ctx);
        event.setCmd(EventType.valuesOf(in.readShort()));
        // 读取长度
        int length = in.readUnsignedShort();
        byte[] data = new byte[length];
        in.readBytes(data);
        event.setData(data);
        out.add(event);
    }

}
