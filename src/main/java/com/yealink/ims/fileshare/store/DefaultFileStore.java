package com.yealink.ims.fileshare.store;

import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * 默认文件存储方式 直接存储在系统磁盘上 author:pengzhiyuan Created on:2016/6/3.
 */
public class DefaultFileStore implements IFileStore {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFileStore.class);
    private String storeDirPath;

    public String getStoreDirPath() {
        return storeDirPath;
    }

    public void setStoreDirPath(String storeDirPath) {
        this.storeDirPath = storeDirPath;
    }

    @Override
    public boolean saveFile(String savePath, String fileName, byte[] data) {
        try {
            File file = new File(storeDirPath + savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(storeDirPath + savePath + File.separator + fileName));
            out.write(data);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String generateFinalFile(String savePath, String fileName) {
        BufferedOutputStream outputStream = null;
        try {
            // 设置保存路径
            String fileNamePath = savePath + File.separator + fileName;

            // 对目前的临时文件进行排序 filename+seq
            File fileDir = new File(storeDirPath + savePath);
            File[] fileList = fileDir.listFiles();
            if (fileList != null && fileList.length > 0) {
                Arrays.sort(fileList);
            }

            // 生成目标文件输出流
            outputStream = new BufferedOutputStream(
                    new FileOutputStream(storeDirPath+fileNamePath));

            for (File file : fileList) {
                BufferedInputStream inputStream = new BufferedInputStream(
                        new FileInputStream(file));
                LOG.debug("{},cur read file name:"+file.getName()+","+file.length(), fileNamePath);
                int len = -1;
                byte[] b = new byte[8192];
                while((len=inputStream.read(b)) != -1) {
                    outputStream.write(b, 0, len);
                }
                outputStream.flush();
                inputStream.close();
                //删除临时文件
                file.delete();
            }
            return fileNamePath;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public boolean deleteFile(String fileNamePath) {
        String filePath = storeDirPath + fileNamePath;
        String fileDir = filePath.substring(0, filePath.lastIndexOf(File.separator));
        File file = new File(fileDir);
        if (file.exists()) {
            return deleteDir(file);
        }
        return false;
    }

    @Override
    public byte[] getFileData(String fileNamePath, long offset, int length) {
        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(storeDirPath+fileNamePath, "r");
            LOG.debug("{}文件大小:"+randomFile.length(), fileNamePath);
            randomFile.seek(offset);
            byte[] fileData = new byte[length];
            int byteread = randomFile.read(fileData);
            LOG.debug("传入想要读取文件长度:"+length);
            LOG.debug("读取到的文件大小:"+byteread);
            if (randomFile != null) {
                randomFile.close();
            }
            return fileData;
        } catch (FileNotFoundException e) {
            LOG.error("读取的文件不存在:"+fileNamePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getFileData(String fileNamePath) {
        File f = new File(storeDirPath+fileNamePath);
        if (!f.exists()) {
            return null;
        }
        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
            }
            return byteBuffer.array();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean verifyFileMd5(String fileNamePath, String md5) {
        md5 = CommonUtil.getString(md5);
        if (md5.equals("")) {
            return  true;
        }
        String filePath = storeDirPath+fileNamePath;
        String fileMd5 = MD5Util.md5File(filePath);
        LOG.debug("generate fileMd5="+fileMd5);
        if (md5.equalsIgnoreCase(fileMd5)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isGetAllFile(String savePath, long chunkNumber) {
        File fileDir = new File(storeDirPath + savePath);
        if (fileDir != null && fileDir.exists()) {
            File[] fileList = fileDir.listFiles();
            if (fileList != null && fileList.length == chunkNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除目录
     * @param dir
     * @return
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
