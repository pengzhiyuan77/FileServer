package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.util.AESUtil;
import com.yealink.ims.fileshare.util.ByteUtil;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.FileShareConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;

/**
 * 文件传输 字节缓存生成
 * author:pengzhiyuan
 * Created on:2016/6/3.
 */
public class ByteBufBuilder {

    /**
     * 返回请求失败信息
     * 包括鉴权 请求文件
     *
     * @param cmd --命令类型
     * @return
     */
    public static ByteBuf generateRequestFail(short cmd, int httpCode, Cipher enCipher) {
        byte[] data = null;
        ByteBuf frame = null;
        if (FileShareConstant.AUTH == cmd) {
            frame = Unpooled.buffer(8);
            // 鉴权失败 返回响应错误码 httpCode不用加密
            data = FileShareConstant.auth_fail_bytearr;
            frame.writeBytes(data);
            frame.writeShort(4);
            frame.writeInt(httpCode);
        } else if (FileShareConstant.FILE_REQUEST == cmd) {
            AESUtil util = new AESUtil();
            // 加密数据
            byte[] httpCodeArr = ByteUtil.intToBytes2(httpCode);
            byte[] enData = null;
            if (enCipher != null) {
                enData = util.encrypt(enCipher, httpCodeArr);
            } else {
                enData = httpCodeArr;
            }
            frame = Unpooled.buffer(4+enData.length);
            data = FileShareConstant.file_request_fail_bytearr;
            frame.writeBytes(data);
            frame.writeBytes(ByteUtil.intToUnsignShortBytes(enData.length));
            frame.writeBytes(enData);
        }
        return frame;
    }

    /**
     * 鉴权成功
     * @return
     */
    public static ByteBuf generateAuthSuccess(String digest) {
        byte[] digestArr = digest.getBytes();
        ByteBuf frame = Unpooled.buffer(4 + digestArr.length);
        frame.writeShort(FileShareConstant.AUTH_SUCCESS);
        frame.writeBytes(ByteUtil.intToUnsignShortBytes(digestArr.length));
        frame.writeBytes(digestArr);
        return frame;
    }

    /**
     * 生成服务端请求文件帧
     * @param offset
     * @param length
     * @param fileName
     * @param enCipher
     * @return
     */
    public static ByteBuf generateFileRequest(long offset, int length, String fileName, Cipher enCipher) {
        AESUtil util = new AESUtil();

        // 对文件名UTF-8编码
        fileName = CommonUtil.getUtf8String(fileName);

        byte[] fileNameArr = fileName.getBytes();
        ByteBuf tempBuf = Unpooled.buffer(8+fileNameArr.length);
        tempBuf.writeBytes(ByteUtil.longToBytes2(offset));
        tempBuf.writeBytes(ByteUtil.intToUnsignShortBytes(length));
        tempBuf.writeBytes(fileNameArr);
        byte[] enFileData = null;
        // 加密
        if (enCipher != null) {
            enFileData = util.encrypt(enCipher, tempBuf.array());
        } else {
            enFileData =  tempBuf.array();
        }
        tempBuf.release();

        ByteBuf frame = Unpooled.buffer(4 + enFileData.length);
        frame.writeShort(FileShareConstant.FILE_REQUEST);
        frame.writeBytes(ByteUtil.intToUnsignShortBytes(enFileData.length));
        frame.writeBytes(enFileData);
        return frame;
    }

    /**
     * 返回客户端请求文件的成功响应
     * @param data
     * @return
     */
    public static ByteBuf generateFileRequestSuccess(byte[]data) {
        ByteBuf frame = Unpooled.buffer(4 + data.length);
        frame.writeShort(FileShareConstant.FILE_REQUEST_SUCCESS);
        frame.writeBytes(ByteUtil.intToUnsignShortBytes(data.length));
        frame.writeBytes(data);
        return frame;
    }
}
