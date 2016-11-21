package com.yealink.ims.fileshare.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

/**
 * 字节工具类
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class ByteUtil {
    /**
     * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
     */
    public static byte[] getBitArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * 把byte转为字符串的bit
     */
    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    /**
     * 二进制字符串转byte
     */
    public static byte decodeBinaryString(String byteStr) {
        int re, len;
        if (null == byteStr) {
            return 0;
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {// 4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。
     * 和bytesToInt（）配套使用
     * @param value
     *            要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。
     * 和bytesToInt2()配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占2个字节的无符号byte数组，本方法适用于(高位在前，低位在后)的顺序。
     */
    public static byte[] intToUnsignShortBytes(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value>>8)&0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。
     * 和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF)<<24)
                | ((src[offset+1] & 0xFF)<<16)
                | ((src[offset+2] & 0xFF)<<8)
                | (src[offset+3] & 0xFF));
        return value;
    }

    /**
     * byte数组中取short数值，本方法适用于(低位在后，高位在前)的顺序。
     * @param src
     * @param offset
     * @return
     */
    public static short bytesToShort(byte[] src, int offset) {
        short value;
        value = (short) (((src[offset] & 0xFF)<<8)
                | (src[offset+1] & 0xFF));
        return value;
    }

    /**
     * 2个无符号字节的数组 转化为int整数
     * @param src
     * @param offset
     * @return
     */
    public static int unSignBytesToInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF)<<8)
                | (src[offset+1] & 0xFF));
        return value;
    }

    /**
     * long换成6个字节数组
     * @param value
     * @return
     */
    public static byte[] longToBytes2(long value) {
        byte[] src = new byte[6];
        src[0] = (byte) ((value>>40) & 0xFF);
        src[1] = (byte) ((value>>32) & 0xFF);
        src[2] = (byte) ((value>>24) & 0xFF);
        src[3] = (byte) ((value>>16)& 0xFF);
        src[4] = (byte) ((value>>8)&0xFF);
        src[5] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 数组换成long
     * @param src
     * @param offset
     * @return
     */
    public static long bytesToLong2(byte[] src, int offset) {
        long value;
        value = (long) (((src[offset] & 0xFF)<<40)
                | ((src[offset+1] & 0xFF)<<32)
                | ((src[offset+2] & 0xFF)<<24)
                | ((src[offset+3] & 0xFF)<<16)
                | ((src[offset+4] & 0xFF)<<8)
                | (src[offset+5] & 0xFF));
        return value;
    }

    /**
     * 从已有数组截取一段
     * @param src
     * @param begin-起始点位
     * @param count-截取长度
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) {
            bs[i-begin] = src[i];
        }
        return bs;
    }

    public static void main(String[] args) {
        byte b = 0x35; // 0011 0101
        // 输出 [0, 0, 1, 1, 0, 1, 0, 1]
//        System.out.println(Arrays.toString(getBitArray(b)));
//
//        System.out.println(Arrays.toString(intToBytes2(512)));
//        System.out.println(Arrays.toString(intToBytes(512)));
//        System.out.println(bytesToInt2(intToBytes2(512),0));
//        System.out.println(bytesToInt(intToBytes(512),0));
//
//        System.out.println(Arrays.toString(longToBytes2(100000)));
//        System.out.println(bytesToLong2(longToBytes2(100000), 0));
//
//        ByteBuf frame = Unpooled.buffer(6);
//        frame.writeLong(100000);
//        System.out.println(Arrays.toString(frame.array()));
//        frame.release();

        ByteBuf frame = Unpooled.buffer(2);
        frame.writeBytes(intToUnsignShortBytes(61240));
        System.out.println(Arrays.toString(frame.array()));
        System.out.println(frame.readUnsignedShort());
        System.out.println(unSignBytesToInt(intToUnsignShortBytes(61240),0));
        System.out.println(Arrays.toString(intToUnsignShortBytes(61240)));


//        // 输出 00110101
//        System.out.println(byteToBit(b));
//        // JDK自带的方法，会忽略前面的 0
//        System.out.println(Integer.toBinaryString(0x35));
//        //
//        System.out.println(decodeBinaryString(byteToBit(b)));
    }

}
