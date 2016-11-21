package com.yealink.ims.fileshare.store;

import com.yealink.ims.fileshare.util.CommonUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

/**
 * FTP 客户端操作
 * author:pengzhiyuan
 * Created on:2016/6/6.
 */
@Component
public class FtpClientManager {
    private static final Logger LOG = LoggerFactory.getLogger(FtpClientManager.class);
    private FTPClient ftpClient = new FTPClient();
    /**
     * ftp服务器地址
     */
    @Value("${fs.ftp.host}")
    private String host;
    /**
     * ftp服务器端口
     */
    @Value("${fs.ftp.port}")
    private String port;
    /**
     * 登录名
     */
    @Value("${fs.ftp.username}")
    private String userName;
    /**
     * 登录密码
     */
    @Value("${fs.ftp.password}")
    private String password;
    /**
     * 需要访问的远程目录
     */
    @Value("${fs.ftp.rootdir}")
    private String ftpServerDir;

    /**
     * 是否是二进制格式传输文件
     */
    private boolean binaryTransfer = true;

    public FtpClientManager() {
    }

    /**
     * 是否以二进制的格式传输文件
     * @return
     */
    public boolean isBinaryTransfer() {
        return binaryTransfer;
    }

    /**
     * 设置以二进制的格式传输文件
     * @param binaryTransfer
     */
    public void setBinaryTransfer(boolean binaryTransfer) {
        this.binaryTransfer = binaryTransfer;
    }

    /**
     * 获取FTP服务器IP地址
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置FTP服务器IP地址
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取端口
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * 设置端口
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * 获取登陆FTP服务器用户名
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置登陆FTP服务器用户名
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取登陆FTP服务器密码
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置登陆FTP服务器密码
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取FTP服务文件目录
     * @return
     */
    public String getFtpServerDir() {
        return ftpServerDir;
    }

    /**
     * 设置FTP服务文件目录
     * @param ftpServerDir
     */
    public void setFtpServerDir(String ftpServerDir) {
        this.ftpServerDir = ftpServerDir;
    }

    /**
     * 登陆FTP服务器
     * @return
     */
    public boolean connectFtpServer() {
        try {
            int reply;
            ftpClient.connect(host ,Integer.parseInt(port));
            reply = ftpClient.getReplyCode();

            if (FTPReply.isPositiveCompletion(reply)) {
                if (ftpClient.login(userName, password)) {
                    ftpClient.enterLocalPassiveMode();
                    return true;
                }
            } else {
                ftpClient.disconnect();
                LOG.info("FTP server refused connection.");
            }
        } catch (IOException e) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException f) {
                }
            }
            LOG.info("Could not connect to server." + e);
        }
        return false;
    }

    /**
     * 创建目录
     * @param path -目录路径 比如a/b/c/d
     * @return
     *    创建成功 返回true
     *    创建失败 返回false
     */
    public boolean createDir(String path) {
        path = CommonUtil.getFormatPath(path);
        List<String> dirList= CommonUtil.getSpiltStringList(path, "/");
        int length=dirList.size();
        String theDir="";
        try{
            for(int i=0;i<length;i++){
                String dir=dirList.get(i);
                theDir=theDir+"/"+dir;
                ftpClient.makeDirectory(theDir);
            }
        }catch(Exception e){
            LOG.error("create ftp dir {} error"+e, path);
            return false;
        }
        return true;
    }

    /**
     * 上传远程指定文件
     *
     * @param remoteAbsoluteFile
     *            远程文件名(包括完整路径)
     * @param input
     * @return 成功时，返回true，失败返回false
     */
    public boolean upload(String remoteAbsoluteFile, InputStream input) {
        try {
            // 设置文件传输类型
            if (binaryTransfer) {
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            }
            ftpClient.storeFile(remoteAbsoluteFile, input);
            return true;
        } catch (FileNotFoundException e) {
            LOG.error("upload file{} faile!"+e, remoteAbsoluteFile);
        } catch (IOException e1) {
            LOG.error("upload file{} faile!"+e1, remoteAbsoluteFile);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e2) {
            }
        }
        return false;
    }

    /**
     * 上传一个本地文件到远程指定文件
     *
     * @param remotePath
     *            远程文件路径(指定的FTPSERVERDIR之后的部分，不包括文件名)
     * @param fileName
     *            文件名
     * @param input
     * @return 成功时，返回true，失败返回false
     */
    public boolean upload(String remotePath, String fileName, InputStream input) {
        try {
            // //设置文件传输类型
            if (binaryTransfer) {
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            }
            String fullPath = ftpServerDir+remotePath;
            createDir(fullPath);
            ftpClient.storeFile(fullPath+fileName, input);
            return true;
        } catch (FileNotFoundException e) {
            LOG.error("upload file{} faile!"+e, fileName);
        } catch (IOException e1) {
            LOG.error("upload file{} faile!"+e1, fileName);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e2) {
            }
        }

        return false;
    }

    /**
     * 获取文件输入流
     *
     * @param filePath
     *            文件名 不包含根路径
     * @return 成功时，返回true，失败返回false
     */
    public InputStream get(String filePath) {
        InputStream input = null;
        try {
            // 设置文件传输类型
            if (binaryTransfer) {
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            }
            // 处理传输
            String remote = ftpServerDir + filePath;
            input=ftpClient.retrieveFileStream(remote);
        } catch (FileNotFoundException e) {
            LOG.error("Get ftp File{} data fail!"+e, filePath);
        } catch (IOException e1) {
            LOG.error("Get ftp File{} data fail!"+e1, filePath);
        }
        return input;
    }

    /**
     * 下载一个远程文件到本地的指定文件
     *
     * @param remoteAbsoluteFile
     *            远程文件名(包括完整路径)
     * @param localAbsoluteFile
     *            本地文件名(包括完整路径)
     * @param delFile - 是否删除远程文件
     * @return 成功时，返回true，失败返回false
     */
    private boolean get(String remoteAbsoluteFile, String localAbsoluteFile,
                        boolean delFile) {
        OutputStream output = null;
        try {
            // 设置文件传输类型
            if (binaryTransfer) {
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            }
            // 处理传输
            output = new FileOutputStream(localAbsoluteFile);
            ftpClient.retrieveFile(remoteAbsoluteFile, output);
            output.close();
            if (delFile) { // 删除远程文件
                ftpClient.deleteFile(remoteAbsoluteFile);
            }
            return true;
        } catch (FileNotFoundException e) {
            LOG.error("Get ftp File{} data fail!"+e, remoteAbsoluteFile);
        } catch (IOException e1) {
            LOG.error("Get ftp File{} data fail!"+e1, remoteAbsoluteFile);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e2) {
            }
        }
        return false;
    }

    /**
     * 下载一个文件到默认的本地路径中
     *
     * @param fileName
     *            文件名称(不含根路径)
     * @param delFile
     *            成功后是否删除该文件
     * @return
     */
    public boolean getFiltToLoal(String fileName, String localAbsoluteFil, boolean delFile) {
        String remote = ftpServerDir + fileName;
        return get(remote, localAbsoluteFil, delFile);
    }

    /**
     * 删除FTP服务器上的文件
     * @param fileName -包含路径
     * @return
     */
    public boolean del(String fileName){
        String remote = ftpServerDir + fileName;
        try {
            return ftpClient.deleteFile(remote);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除目录
     * @param path
     * @return
     */
    public boolean removeDir(String path) {
        try {
            path = ftpServerDir + path;
            return ftpClient.removeDirectory(path);
        } catch (IOException e) {
            LOG.error("Delete ftp dir{} data fail!"+e, path);
        }
        return false;
    }

    /**
     * 列出远程目录下所有的文件
     *
     * @param remotePath
     *            远程目录名
     * @return 远程目录下所有文件名的列表，目录不存在或者目录下没有文件时返回0长度的数组
     */
    public String[] listNames(String remotePath) {
        String[] fileNames = null;
        try {
            FTPFile[] remotefiles = ftpClient.listFiles(remotePath);
            fileNames = new String[remotefiles.length];
            for (int i = 0; i < remotefiles.length; i++) {
                fileNames[i] = remotefiles[i].getName();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    /**
     * 断开ftp连接
     */
    public void disconnect() {
        try {
            ftpClient.logout();
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FtpClientManager webFtp=new FtpClientManager();
		webFtp.setHost("127.0.0.1");
		webFtp.setPort("2221");
		webFtp.setBinaryTransfer(true);
		webFtp.setUserName("admin");
		webFtp.setPassword("123456");
		webFtp.setFtpServerDir("/test");
		webFtp.connectFtpServer();

		//上传文件到FTP服务器
//		try {
//			webFtp.upload("/1111/", "122.xml", new FileInputStream("E:\\pengzhiyuan\\dev\\git\\of\\Odin-OF\\src\\.idea\\misc.xml"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//        webFtp.getFiltToLoal("/1111/122.xml", "e:/122.xml", false);
//
//        webFtp.del("/111/12.xml");

//        webFtp.removeDir("/111");
        webFtp.disconnect();
    }
}
