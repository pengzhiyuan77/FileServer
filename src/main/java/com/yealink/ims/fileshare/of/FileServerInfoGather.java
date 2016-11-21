package com.yealink.ims.fileshare.of;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yealink.ims.fileshare.busi.FileServerProvider;
import com.yealink.ims.fileshare.server.FileServerMonitor;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *  ================  该功能不用了。。。。。。=======================
 *
 * 服务器状态信息收集
 * author:pengzhiyuan
 * Created on:2016/7/27.
 */
@Component
public class FileServerInfoGather implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FileServerInfoGather.class);

    private static final int GATHER_TIME_INTERVAL=1; //单位分钟，每1分钟采集一次数据
    private static final int GATERH_TIME_ALL=1; //单位天, 保留1天的状态收集数据

    @Override
    public void run() {
        FileServerProvider fileServerProvider = new FileServerProvider();
        FileServerMonitor fileServerMonitor = new FileServerMonitor();
        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            try {
                // 获取服务器状态信息
                String fileServerInfo = fileServerMonitor.getFileServerInfo();
                if (fileServerInfo != null) {
                    LOG.debug("fileServerInfo:"+fileServerInfo);
                    Map<String, Object> statMap = new HashMap<String,Object>();

                    Map<String, Object> gatheredMap = objectMapper.readValue(fileServerInfo, Map.class);

                    Object cpuRate = gatheredMap.get("cpuCombined");
                    Object memRate = gatheredMap.get("serverMemRate");
                    Object ioRate = gatheredMap.get("diskIoRate");

                    if (cpuRate != null) {
                        String strcpuRate = CommonUtil.getString(cpuRate).replace("%", "");
                        statMap.put("cpuRate", Float.valueOf(strcpuRate));
                    }
                    if (memRate != null) {
                        String strmemRate = CommonUtil.getString(memRate).replace("%", "");
                        statMap.put("memRate", Float.valueOf(strmemRate));
                    }
                    if (ioRate != null) {
                        String strioRate = CommonUtil.getString(ioRate).replace("%", "");
                        statMap.put("ioRate", Float.valueOf(strioRate));
                    }
                    statMap.put("jid", gatheredMap.get("jid"));
                    // 保存状态信息
                    fileServerProvider.saveFileServerStat(statMap);

                    // 删除1天前数据
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, -GATERH_TIME_ALL);
                    String endDate = XmppDateUtil.formatDate(calendar.getTime());
                    fileServerProvider.deleteFileServerStatByDeadLine(endDate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 休眠1分钟
            try {
                Thread.sleep(GATHER_TIME_INTERVAL * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
