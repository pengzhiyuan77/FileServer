package com.yealink.ims.fileshare.store;

import com.yealink.ims.fileshare.busi.FileDealerProcessManager;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.MD5Util;

import java.io.*;

/**
 * FTP存储
 * author:pengzhiyuan
 * Created on:2016/6/6.
 */
public class FtpFileStore extends DefaultFileStore {
    private FtpClientManager ftpClientManager = FileDealerProcessManager.getInstance().getFtpClientManager();

    /**
     * 生成最终文件
     * 从当前服务器临时文件生成最终文件，并上传到FTP服务器上，然后删除当前服务器的最终文件
     * @param savePath
     * @param fileName
     * @return  -- FTP上最终存储路径
     */
    @Override
    public String generateFinalFile(String savePath, String fileName) {
        // 先生成到本地最终文件
        String fileNamePath = super.generateFinalFile(savePath, fileName);
        try {
            File orginFile = new File(super.getStoreDirPath()+fileNamePath);
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(orginFile));

            ftpClientManager.connectFtpServer();

            // 上传到ftp上
            ftpClientManager.upload(File.separator+savePath+File.separator, fileName, inputStream);
            // 删除当前服务器文件
            super.deleteFile(fileNamePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return fileNamePath;
    }

    /**
     * 删除FTP上的文件
     * @param fileNamePath
     * @return
     */
    @Override
    public boolean deleteFile(String fileNamePath) {
        ftpClientManager.connectFtpServer();

        String fileDir = fileNamePath.substring(0, fileNamePath.lastIndexOf(File.separator));
        fileDir = File.separator + fileDir;
        return ftpClientManager.removeDir(fileDir);
    }

    /**
     * 获取ftp上的文件 先下载到本地服务器临时目录，等文件传输完成的时候再删除
     * @param fileNamePath
     * @param offset
     * @param length
     * @return
     */
    @Override
    public byte[] getFileData(String fileNamePath, long offset, int length) {
        String defaultSavePath = super.getStoreDirPath()+fileNamePath;
        File file = new File(defaultSavePath);
        // 本地文件不存在则从ftp服务器上获取
        if (!file.exists()) {
            ftpClientManager.connectFtpServer();
            // 从FTP服务器上获取文件到本地服务器
            ftpClientManager.getFiltToLoal(File.separator+fileNamePath, defaultSavePath, false);
        }
        return super.getFileData(fileNamePath, offset, length);
    }

    /**
     * 获取当前服务器上临时文件的数据
     * @param fileNamePath
     * @return
     */
    @Override
    public byte[] getFileData(String fileNamePath) {
        return super.getFileData(fileNamePath);
    }

    /**
     * 存储临时文件 存放到当前文件服务器上
     * @param savePath
     * @param fileName
     * @param data
     */
    @Override
    public boolean saveFile(String savePath, String fileName, byte[] data) {
        return super.saveFile(savePath, fileName, data);
    }

    @Override
    public boolean verifyFileMd5(String fileNamePath, String md5) {
        md5 = CommonUtil.getString(md5);
        if (md5.equals("")) {
            return true;
        }
        ftpClientManager.connectFtpServer();

        InputStream inputStream = ftpClientManager.get(File.separator+fileNamePath);
        if (inputStream != null) {
            String fileMd5 = MD5Util.md5InputStream(inputStream);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (md5.equalsIgnoreCase(fileMd5)) {
                return true;
            }
        }
        return false;
    }
}
