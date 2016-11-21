package com.yealink.ims.fileshare.util;

import com.yealink.ims.fileshare.conn.Connection;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.AttributeKey;

import javax.crypto.Cipher;
import java.util.concurrent.CountDownLatch;

/**
 * 文件服务常量
 * author:pengzhiyuan
 * Created on:2016/6/1.
 */
public class FileShareConstant {

    /**
     * channel属性
     * 连接
     */
    public static final AttributeKey<Connection> CONN_KEY = AttributeKey.valueOf("CONN_KEY");
    /**
     * 文件名称属性
     */
    public static final AttributeKey<String> FILENAME_KEY = AttributeKey.valueOf("FILENAME_KEY");
    /**
     * 文件的MD5码
     */
    public static final AttributeKey<String> MD5_KEY = AttributeKey.valueOf("MD5_KEY");
    /**
     * 请求的文件块数目
     */
    public static final AttributeKey<Long> CHUNK_KEY = AttributeKey.valueOf("CHUNK_KEY");
    /**
     * 加密密码器
     */
    public static final AttributeKey<Cipher> EN_CIPHER_KEY = AttributeKey.valueOf("EN_CIPHER_KEY");
    /**
     * 解密密码器
     */
    public static final AttributeKey<Cipher> DE_CIPHER_KEY = AttributeKey.valueOf("DE_CIPHER_KEY");
    /**
     * 文件的保存路径
     */
    public static final AttributeKey<String> FILE_SAVEPATH_KEY = AttributeKey.valueOf("FILE_SAVEPATH_KEY");
    /**
     * 文件请求countdownlatch
     */
    public static final AttributeKey<CountDownLatch> COUNTDOWN_KEY = AttributeKey.valueOf("COUNTDOWN_KEY");
    /**
     * HTTP POST 解码器
     */
    public static final AttributeKey<HttpPostRequestDecoder> POST_DECODER_KEY = AttributeKey.valueOf("POST_DECODER_KEY");

    /**
     * 请求鉴权
     */
    public static final short AUTH = 0x1002;
    /**
     * 请求鉴权成功
     */
    public static final short AUTH_SUCCESS = 0x1102;
    /**
     * 请求鉴权失败
     */
    public static final short AUTH_FAIL = 0x1202;
    /**
     * 请求文件
     */
    public static final short FILE_REQUEST = 0x1001;
    /**
     * 请求文件成功
     */
    public static final short FILE_REQUEST_SUCCESS = 0x1101;
    /**
     * 请求文件失败
     */
    public static final short FILE_REQUEST_FAIL = 0x1201;

    /**
     * 请求鉴权成功
     */
    public static final byte[] auth_success_bytearr = new byte[]{0x11, 0x02};
    /**
     * 请求鉴权失败
     */
    public static final byte[] auth_fail_bytearr = new byte[]{0x12, 0x02};
    /**
     * 请求文件
     */
    public static final byte[] file_request_bytearr = new byte[]{0x10, 0x01};
    /**
     * 请求文件成功
     */
    public static final byte[] file_request_succ_bytearr = new byte[]{0x11, 0x01};
    /**
     * 请求文件失败
     */
    public static final byte[] file_request_fail_bytearr = new byte[]{0x12, 0x01};

    /**
     * http响应码
     */
    public static final int HTTP_CODE_FORBIDDEN = 403;
    public static final int HTTP_CODE_NOTFOUND = 404;
    public static final int HTTP_CODE_NOTATUH = 401;

    /**
     * 上传 或者 下载
     */
    public static final byte DIRECTION_UP = 0; //上传
    public static final byte DIRECTION_DOWN = 1; //下载

    /**
     * 制定默认缩略图 宽度 高度
     */
    public static final int DEFAULT_IMAGE_THUMB_WIDTH=48;
    public static final int DEFAULT_IMAGE_THUMB_HEIGHT=48;

    /** === 属性设置 _id常量值 ===*/
    /**
     * 服务器存储位置
     */
    public static final String PROPERTY_STORAGE_SERVER_SAVEPATH = "uc.storage.save.path";
}
