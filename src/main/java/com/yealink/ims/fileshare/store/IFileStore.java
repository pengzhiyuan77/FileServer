package com.yealink.ims.fileshare.store;

/**
 * 文件存储接口
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public interface IFileStore {

    /**
     * 临时文件存储接口
     * @param savePath-文件保存的路径
     * @param fileName-临时文件名
     * @param data-文件数据
     */
    boolean saveFile(String savePath, String fileName, byte[]data);

    /**
     * 合并临时文件
     * @param savePath 文件保存的路径
     * @param fileName 最终文件名
     */
    String generateFinalFile(String savePath, String fileName);

    /**
     * 删除文件
     * @param fileNamePath 文件的完整路径 包含文件名
     * @return
     */
    boolean deleteFile(String fileNamePath);

    /**
     * 获取文件数据
     * @param fileNamePath -- 文件的完整路径 包含文件名
     * @param offset
     * @param length
     * @return
     */
    byte[] getFileData(String fileNamePath, long offset, int length);

    /**
     * 获取文件数据
     * @param fileNamePath -- 文件的完整路径 包含文件名
     * @return
     */
    byte[] getFileData(String fileNamePath);

    /**
     * 文件MD5码校验
     * @param fileNamePath -- 文件的完整路径 包含文件名
     * @param md5
     * @return
     */
    boolean verifyFileMd5(String fileNamePath, String md5);

    /**
     * 判断是否已接收到所有临时文件
     * @param savePath 文件保存的路径
     * @param chunkNumber
     * @return
     */
    boolean isGetAllFile(String savePath, long chunkNumber);
}
