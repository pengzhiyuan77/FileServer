package com.yealink.ims.fileshare.util;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具类 author:pengzhiyuan Created on:2016/6/3.
 */
public class CommonUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CommonUtil.class);
    private static Map<String, MessageDigest> digests = new ConcurrentHashMap<>();

    /**
     * 计算请求块总数
     *
     * @param requestSize
     * @param size
     * @return
     */
    public static long calChunkNumber(int requestSize, long size) {
        if (requestSize > size) {
            return 1;
        } else {
            return size / requestSize + 1;
        }
    }

    /**
     * 生成临时文件序列号 总共16位，不足左边补0
     *
     * @param seq
     * @return
     */
    public static String makeFileSeq(long seq) {
        String strSeq = String.valueOf(seq);
        String orignStr = "0000000000000000";
        strSeq = orignStr.substring(0, 16-strSeq.length()) + strSeq;
        return strSeq;
    }

    /**
     * 去掉空格
     * @param string
     * @return
     */
    public static String getTrimString(String string){
        if(string==null){
            return string;
        }
        return string.trim();
    }

    /**
     * 处理字符串,将null转为""
     * @param object
     * @return String
     */
    public static String getString(Object object) {
        String string = String.valueOf(object);
        string = (string == null||string.equals("null")) ? "" : string;
        return getTrimString(string);
    }

    /**
     * 把字符串按照分隔符串 分隔后放到List里面
     * @param longString
     * @param splitString
     * @return
     */
    public static List getSpiltStringList(String longString, String splitString) {
        List<String> list=new ArrayList<String>();
        longString = getString(longString);
        splitString = getString(splitString);
        if(longString.equals("")){
            return null;
        }
        String[] splitArray=longString.split(splitString);
        if(splitArray==null){
            return null;
        }
        int length=splitArray.length;
        for(int i=0;i<length;i++){
            list.add(splitArray[i]);
        }
        return list;
    }

    /**
     * 将路径中的"\"全部替换成"/"
     *
     * @param path
     * @return String
     */
    public static String getFormatPath(String path) {
        path = path.replaceAll("\\\\", "/");
        path = path.replaceAll("//", "/");
        return path;
    }

    /**
     * 转化16进制字符串
     * @param bytes
     * @return
     */
    public static String encodeHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    /**
     * 执行shell脚本文件 or shell命令
     * @param shellString
     */
    public static List<String> runShell(String shellString) {
        List<String> resultList = new ArrayList<String>();
        Runtime rc = Runtime.getRuntime();
        try {
            String[] command = { "/bin/sh", "-c", shellString };

            Process pcs = rc.exec(command);
//            DataOutputStream os = new DataOutputStream(pcs.getOutputStream());
//            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(shellPath));
//            byte[] data = new byte[8192];
//            int len=0;
//            while ((len=inputStream.read(data)) != -1) {
//                LOG.debug("len="+len);
//                os.write(data, 0, len);
//            }
//            os.writeBytes("\n");
//            os.flush();

            BufferedReader errorBr = new BufferedReader(new InputStreamReader(pcs.getErrorStream()));
            LOG.debug("Run shell error: "+errorBr.readLine());
            errorBr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(pcs.getInputStream()));
            String line="";
            while((line = br.readLine()) != null) {
                resultList.add(line);
            }
            try {
                // 等待进程执行完毕
                pcs.waitFor();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            br.close();
            int ret = pcs.exitValue();
            LOG.debug("shell:"+shellString+",run ret="+ret);
            pcs.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 对指定图片文件 指定宽度 高度进行缩略图处理
     * 如果宽度 高度 参数为0，则采用默认大小
     * @param file
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    public static byte[] toThumbnail(File file, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (width <= 0) {
            width = FileShareConstant.DEFAULT_IMAGE_THUMB_WIDTH;
        }
        if (height <= 0) {
            height = FileShareConstant.DEFAULT_IMAGE_THUMB_HEIGHT;
        }
        Thumbnails.of(file).size(width, height)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 对UTF-8进行中文解码
     * @param string
     * @return String
     */
    public static String getStringFromUtf8(String string){
        try {
            string= URLDecoder.decode(string,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace() ;
        }
        return string;
    }

    /**
     * 对中文进行UTF-8编码
     * @param string
     * @return String
     */
    public static String getUtf8String(String string){
        try {
            string= URLEncoder.encode(string, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

    /**
     *  生成sha-1编码
     * @param sessionID
     * @param sender
     * @param target
     * @return
     */
    public static String createSha1Digest(final String sessionID, final String sender, final String target) {
        return hash(sessionID + sender + target, "SHA-1");
    }

    public static String hash(String data, String algorithm) {
        return hash(data.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    public static String hash(byte[] bytes, String algorithm) {
        synchronized (algorithm.intern()) {
            MessageDigest digest = digests.get(algorithm);
            if (digest == null) {
                try {
                    digest = MessageDigest.getInstance(algorithm);
                    digests.put(algorithm, digest);
                }
                catch (NoSuchAlgorithmException nsae) {
                    LOG.error("Failed to load the " + algorithm + " MessageDigest. " +
                            "Jive will be unable to function normally.", nsae);
                    return null;
                }
            }
            // Now, compute hash.
            digest.update(bytes);
            return encodeHex(digest.digest());
        }
    }

}
