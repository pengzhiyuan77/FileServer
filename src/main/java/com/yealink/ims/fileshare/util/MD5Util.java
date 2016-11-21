package com.yealink.ims.fileshare.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MD5码工具
 * 校验文件
 * author:pengzhiyuan
 * Created on:2016/6/7.
 */
public class MD5Util {

    /**
     * 字符串生成MD5
     * @param content
     * @return
     */
    public static String md5String(String content) {
        return DigestUtils.md5Hex(content);
    }

    /**
     * 对文件进行MD5编码
     * @param file
     * @return
     */
    public static String md5File(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            String md5 = DigestUtils.md5Hex(fileInputStream);
            fileInputStream.close();
            return md5;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对文件进行MD5编码
     * @param filePath
     * @return
     */
    public static String md5File(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            String md5 = DigestUtils.md5Hex(fileInputStream);
            fileInputStream.close();
            return md5;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String md5InputStream(InputStream inputStream) {
        try {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[]args) {
//        long t = System.currentTimeMillis();
//        System.out.println(md5File("D:\\开发工具\\staruml-5.0-with-cm.rar"));
//        System.out.println(System.currentTimeMillis()-t);
    }
}
