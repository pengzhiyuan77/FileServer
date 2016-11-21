package com.yealink.ims.fileshare.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yealink.ims.fileshare.busi.FileDealerProcessManager;
import com.yealink.ims.fileshare.busi.FileServerProtocalHandler;
import com.yealink.ims.fileshare.cache.FileShareCacheManager;
import com.yealink.ims.fileshare.conn.Connection;
import com.yealink.ims.fileshare.conn.ConnectionManager;
import com.yealink.ims.fileshare.of.FileServerStateManager;
import com.yealink.ims.fileshare.store.DefaultFileStore;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.SysMonitorUtil;
import io.netty.channel.ChannelId;
import org.hyperic.sigar.SigarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件服务器监控信息
 * 给web管理平台用
 * author:pengzhiyuan
 * Created on:2016/7/7.
 */
public class FileServerMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(FileServerMonitor.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 基本服务器信息
     * 通过sigar获取
     * @return
     */
    public String getFileServerInfo() {
        FileServerStateManager fileServerStateManager = FileServerStateManager.getInstance();
        Map<String,Object> valueMap=new HashMap<String,Object>();
        valueMap.put("host", fileServerStateManager.getHost());
        valueMap.put("port", fileServerStateManager.getPort());
        valueMap.put("jid", fileServerStateManager.getFsJid());
        // 当前客户端连接数
        valueMap.put("connNum", ConnectionManager.getInstance().listAllConn().size());

        SysMonitorUtil sysMonitorUtil = new SysMonitorUtil();
        String storeDirPath = ((DefaultFileStore)FileDealerProcessManager.getInstance().getFileStore()).getStoreDirPath();
        // cpu
        valueMap.putAll(sysMonitorUtil.getTotalCpuInfo());
        // 内存
        valueMap.putAll(sysMonitorUtil.getTotalMemInfo());
        // 磁盘
        LOG.debug("storeDirPath="+storeDirPath);
        valueMap.putAll(sysMonitorUtil.getFileSystemInfo(storeDirPath));

        if (SigarLoader.IS_LINUX) {
            float diskIoRate=0.0f; //服务器磁盘IO使用率
            // 获取linux系统下磁盘IO使用率
            diskIoRate = getDiskIoRate();
            valueMap.put("diskIoRate", diskIoRate*100+"%");
        }
        valueMap.put("isLinux", SigarLoader.IS_LINUX);
        sysMonitorUtil.close();

        try {
            return objectMapper.writeValueAsString(valueMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 基本服务器信息
     * shell脚本获取
     * @return
     */
    public String getFileServerInfoByShell() {
        FileServerStateManager fileServerStateManager = FileServerStateManager.getInstance();
        Map<String,Object> valueMap=new HashMap<String,Object>();
        valueMap.put("host", fileServerStateManager.getHost());
        valueMap.put("port", fileServerStateManager.getPort());
        valueMap.put("jid", fileServerStateManager.getFsJid());
        // 当前客户端连接数
        valueMap.put("connNum", ConnectionManager.getInstance().listAllConn().size());

        String SERVER_HOME = System.getProperty("FileServer_Home");
        String fs_pid = SERVER_HOME + File.separator + "bin" + File.separator + "fs_pid";
        //文件服务进程PID
        String pid = null;
        try {
            BufferedReader fsBr = new BufferedReader(new InputStreamReader(new FileInputStream(fs_pid)));
            pid = fsBr.readLine();
            LOG.debug("FileServer proc id = " + pid);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String curProcMem = ""; //当前进程占用内存 KB
        String totalMem = ""; //服务器总内存  KB
        String curProcMemRate = ""; //当前进程内存占用率
        String curProcCpuRate = ""; //当前进程cpu占用率 %
        String serverIdleCpuRate = "";// 服务器cpu空闲百分比 %
        String usedMem = ""; //服务器已使用内存 KB
        String serverMemRate = ""; //服务器内存占用率 %
        float diskIoRate=0.0f; //服务器磁盘IO使用率

        List<String> commExecResList = null;
        if (pid != null) {
            pid = CommonUtil.getString(pid);

            //当前进程占内存(单位KB)
            String curMemCommand = "cat /proc/" + pid + "/status|grep -e VmRSS";
            commExecResList = CommonUtil.runShell(curMemCommand);
            LOG.debug("exec curMemCommand result = " + commExecResList);
            if (commExecResList.size() > 0) {
                curProcMem = commExecResList.get(0).replace("VmRSS:", "").replace("kB","").trim();
            }
            //当前服务器总内存(单位M)
            String totalMemCommand = "free -m | grep Mem | awk '{print $2}'";
            commExecResList = CommonUtil.runShell(totalMemCommand);
            LOG.debug("exec totalMemCommand result = " + commExecResList);
            if (commExecResList.size() > 0) {
                totalMem = commExecResList.get(0).trim();
                try {
                    totalMem = CommonUtil.getString(Integer.parseInt(totalMem)*1000);
                } catch (Exception e) {
                    LOG.error("getServerInfo:",e);
                }
            }
            // 计算当前进程内存占用率
            if (!"".equals(curProcMem) &&
                    !"".equals(totalMem)) {
                try {
                    float memRate = Float.parseFloat(curProcMem)*100/Float.parseFloat(totalMem);
                    DecimalFormat df = new DecimalFormat("0.00");
                    curProcMemRate = df.format(memRate);
                } catch (Exception e) {
                    LOG.error("getServerInfo:",e);
                }
            }
            //当前进程CPU占有率
            String curCpuCommand = "ps aux|grep "+pid+"|grep -v \"grep\"|awk '{print $3}'";
            commExecResList = CommonUtil.runShell(curCpuCommand);
            LOG.debug("exec curCpuCommand result = " + commExecResList);
            if (commExecResList.size() > 0) {
                curProcCpuRate = commExecResList.get(0).trim();
            }
            //当前服务器CPU空闲百分比
            String cpuCommand = "top -b -n 1 | grep Cpu | awk '{print $5}' | cut -f 1 -d \".\"";
            commExecResList = CommonUtil.runShell(cpuCommand);
            LOG.debug("exec cpuCommand result = " + commExecResList);
            if (commExecResList.size() > 0) {
                serverIdleCpuRate = commExecResList.get(0).trim();
            }
            //当前服务器已用内存(单位M)
            String memCommand = "free -m | grep Mem | awk '{print $3}'";
            commExecResList = CommonUtil.runShell(memCommand);
            LOG.debug("exec memCommand result = " + commExecResList);
            if (commExecResList.size() > 0) {
                usedMem = commExecResList.get(0).trim();
                try {
                    usedMem = CommonUtil.getString(Integer.parseInt(usedMem)*1000);
                } catch (Exception e) {
                    LOG.error("getServerInfo:",e);
                }
            }
            // 计算服务器内存使用率
            if (!"".equals(usedMem) &&
                    !"".equals(totalMem)) {
                try {
                    float memRate = Float.parseFloat(usedMem)*100/Float.parseFloat(totalMem);
                    DecimalFormat df = new DecimalFormat("0.00");
                    serverMemRate = df.format(memRate);
                } catch (Exception e) {
                    LOG.error("getServerInfo:",e);
                }
            }
            // 获取linux系统下磁盘IO使用率
            diskIoRate = getDiskIoRate();
        }

        valueMap.put("curProcMem", curProcMem+" KB");
        valueMap.put("totalMem", totalMem+" KB");
        valueMap.put("curProcMemRate", curProcMemRate);
        valueMap.put("curProcCpuRate", curProcCpuRate);
        valueMap.put("serverIdleCpuRate", serverIdleCpuRate);
        valueMap.put("usedMem", usedMem+" KB");
        valueMap.put("serverMemRate", serverMemRate);
        valueMap.put("diskIoRate", diskIoRate);

        try {
            return objectMapper.writeValueAsString(valueMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前所有连接会话信息
     * @return
     */
    public String getAllConnectionInfo(String param) {
        List<Map<String, Object>> connectionList = new ArrayList<Map<String, Object>>();
        Map<String, Object> connMap = null;
        Connection connection = null;

        // 获取当前所有连接
        List<ChannelId> channelIdList = ConnectionManager.getInstance().listAllConn();
        for (ChannelId channelId : channelIdList) {
            connMap = new HashMap<String, Object>();
            connection = ConnectionManager.getInstance().getConn(channelId);

            String digest = connection.getDigest();
            Map<String,Object> valueMap = (Map<String,Object>) FileShareCacheManager.getInstance().getDataFromCache(digest);

            connMap.put("id", connection.getId());
            connMap.put("digest", digest);
            connMap.put("clientSocketAddress", connection.getCtx().channel().localAddress().toString());
            connMap.putAll(valueMap);

            connectionList.add(connMap);
        }
        try {
            // 获取客户端发送的分页信息参数 sortOrder:pageSize:pageNumber
            String[] paramArr = param.split(":");
            final String sortOrder = CommonUtil.getString(paramArr[0]);
            String pageSize = CommonUtil.getString(paramArr[1]);
            String pageNumber = CommonUtil.getString(paramArr[2]);

            //按设置排序
            Collections.sort(connectionList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    String id1 = CommonUtil.getString(o1.get("id"));
                    String id2 = CommonUtil.getString(o2.get("id"));
                    if ("desc".equalsIgnoreCase(sortOrder)) {
                        return id2.compareTo(id1);
                    } else {
                        return id1.compareTo(id2);
                    }
                }
            });

            long endIndex = 0;
            long startIndex = 0;
            // 总记录数
            long total = connectionList.size();

            int intPageNumber = Integer.parseInt(pageNumber);
            int intPageSize = Integer.parseInt(pageSize);
            if (intPageNumber*intPageSize >= total) {
                endIndex=total;
            } else {
                endIndex=intPageNumber*intPageSize;
            }
            if (intPageNumber>1) {
                startIndex=(intPageNumber-1)*intPageSize;
            }
            // 获取分页结果集
            List<Map<String,Object>> rows = connectionList.subList((int)startIndex, (int)endIndex);

            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("rows", rows);
            resultMap.put("total", total);

            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭某个连接会话
     * @param digest
     * @return
     */
    public String closeConnByDigest(String digest) {
        // 返回结果
        Map<String, String> resultMap = new HashMap<String, String>();
        String code = "1"; //返回代码 0-失败 1-成功
        String errorMsg = ""; //返回错误信息
        // 释放资源 关闭连接
        FileServerProtocalHandler.releaseFileRequestInfo(digest);

        resultMap.put("code", code);
        resultMap.put("errorMsg", errorMsg);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(resultMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            code="0";
        }
        return code;
    }

    /**
     * 根据文件路径 删除文件
     * @param path
     * @return
     */
    public String deleteFile(String path) {
        // 返回结果
        Map<String, String> resultMap = new HashMap<String, String>();
        String code = "1"; //返回代码 0-失败 1-成功
        String errorMsg = ""; //返回错误信息

        // 删除文件系统文件
        FileDealerProcessManager.getInstance().getFileStore().deleteFile(path);

        resultMap.put("code", code);
        resultMap.put("errorMsg", errorMsg);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(resultMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            code="0";
        }
        return code;
    }

    /**
     * linux系统下获取磁盘IO使用率
     * @return
     */
    private float getDiskIoRate() {
        float ioUsage = 0.0f;
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "iostat -d -x";
            pro = r.exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            int count =  0;
            while((line=in.readLine()) != null) {
                if(++count >= 4){
                    String[] temp = line.split("\\s+");
                    if(temp.length > 1){
                        float util =  Float.parseFloat(temp[temp.length-1]);
                        ioUsage = (ioUsage>util)?ioUsage:util;
                    }
                }
            }
            if(ioUsage > 0) {
                LOG.debug("cur time io usedrate is: " + ioUsage);
                ioUsage /= 100;
            }
            in.close();
            pro.destroy();
        } catch (IOException e) {
            LOG.error("IoUsage Exception. " + e);
        }
        return ioUsage;
    }

}
