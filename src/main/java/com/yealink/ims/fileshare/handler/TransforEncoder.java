package com.yealink.ims.fileshare.handler;

import com.yealink.ims.fileshare.event.FsMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class TransforEncoder extends MessageToByteEncoder<FsMsg> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FsMsg msg, ByteBuf out) throws Exception {

    }

}
