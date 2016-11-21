package com.yealink.ims.fileshare.util;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控，cpu 内存 io 磁盘
 * author:pengzhiyuan
 * Created on:2016/7/26.
 */
public class SysMonitorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SysMonitorUtil.class);
    private Sigar sigar = null;

    public SysMonitorUtil() {
        this.sigar = new Sigar();
    }

    public Sigar getSigar() {
        return sigar;
    }

    /**
     * 关闭资源
     */
    public void close() {
        sigar.close();
    }

    /**
     * 获取服务器总体CPU的情况
     * @return
     */
    public Map<String, Object> getTotalCpuInfo() {
        Map<String, Object> cpuMap = new HashMap<String, Object>();

        CpuPerc cpu = null;
        try {
            cpu = sigar.getCpuPerc();

            cpuMap.put("cpuUserTime", CpuPerc.format(cpu.getUser())); //用户使用率
            cpuMap.put("cpuSysTime", CpuPerc.format(cpu.getSys())); // 系统使用率
            cpuMap.put("cpuIdleTime", CpuPerc.format(cpu.getIdle())); //当前cpu空闲率
            cpuMap.put("cpuWaitTime", CpuPerc.format(cpu.getWait())); //当前等待率
            cpuMap.put("cpuNiceTime", CpuPerc.format(cpu.getNice())); //当前错误率
            cpuMap.put("cpuCombined", CpuPerc.format(cpu.getCombined()));//当前cpu占用率
            cpuMap.put("cpuIrqTime", CpuPerc.format(cpu.getIrq()));
            if (SigarLoader.IS_LINUX) {
                cpuMap.put("cpuSoftIrqTime", CpuPerc.format(cpu.getSoftIrq()));
                cpuMap.put("cpuStolenTime", CpuPerc.format(cpu.getStolen()));
            }
        } catch (SigarException e) {
            e.printStackTrace();
        }
        return cpuMap;
    }

    /**
     * 获取内存使用情况 单位k
     * @return
     */
    public Map<String, Object> getTotalMemInfo() {
        Map<String, Object> memMap = new HashMap<String, Object>();
        try {
            Mem mem = sigar.getMem();
            long total = format(mem.getTotal());
            long used = format(mem.getUsed());
            long free = format(mem.getFree());

            memMap.put("memTotal", total);
            memMap.put("memUsed", used);
            memMap.put("memFree", free);
            float memRate = used*100/total;
            DecimalFormat df = new DecimalFormat("0.00");
            memMap.put("serverMemRate", df.format(memRate)+"%");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memMap;
    }

    /**
     * 转化为k
     * @param value
     * @return
     */
    private static Long format(long value) {
        return new Long(value / 1024);
    }

    /**
     * 根据传入的目录路径获取对应磁盘使用情况
     * @param path
     */
    public Map<String, Object> getFileSystemInfo(String path) {
        Map<String, Object> diskMap = new HashMap<String, Object>();
        try {
            FileSystemUsage usage = sigar.getFileSystemUsage(path);
            // 文件系统总大小
            diskMap.put("diskTotal", usage.getTotal() + "KB");
            // 文件系统剩余大小
            diskMap.put("diskFree", usage.getFree() + "KB");
            // 文件系统可用大小
            diskMap.put("diskAvail", usage.getAvail() + "KB");
            // 文件系统已经使用量
            diskMap.put("diskUsed", usage.getUsed() + "KB");
            double usePercent = usage.getUsePercent() * 100D;
            // 文件系统资源的利用率
            diskMap.put("diskUsage", usePercent + "%");

            // 磁盘读取情况 后续可以
            diskMap.put("diskReadBytes", usage.getDiskReadBytes());
            diskMap.put("diskWriteBytes", usage.getDiskWriteBytes());
            diskMap.put("diskReads", usage.getDiskReads());
            diskMap.put("diskWrites", usage.getDiskWrites());

        } catch (SigarException e) {
            e.printStackTrace();
            LOG.debug("getFileSystemInfo error:",e);
        }
        LOG.debug("diskmap:"+diskMap);
        return diskMap;
    }

//    public static void main(String[]args) {
//        SysMonitorUtil sysMonitorUtil = new SysMonitorUtil();
//        System.out.println(sysMonitorUtil.getTotalCpuInfo());
//
//        System.out.println(sysMonitorUtil.getTotalMemInfo());
//
//        System.out.println(sysMonitorUtil.getFileSystemInfo("e:/temp/"));
//        sysMonitorUtil.close();
//    }
}
