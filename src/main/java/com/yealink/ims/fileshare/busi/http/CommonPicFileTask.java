package com.yealink.ims.fileshare.busi.http;

import com.yealink.ims.fileshare.busi.FileDealerProcessManager;
import com.yealink.ims.fileshare.busi.FileServerProvider;
import com.yealink.ims.fileshare.util.CommonUtil;
import com.yealink.ims.fileshare.util.XmppDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 通用图片 处理任务
 * 
 * 处理掉没有使用的图片数据
 * 
 * @author pzy
 *
 */
@Component
public class CommonPicFileTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(CommonPicFileTask.class);

	@Override
	public void run() {
		FileServerProvider fileServerProvider = new FileServerProvider();
		
		while(true) {
			LOG.debug("start delete invalid common pic file...");
			try {
				// 取1天前的无效图片数据进行删除处理
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_YEAR, -1);
				String endDate = XmppDateUtil.formatDate(calendar.getTime());
				// 查找无效的图片数据
				List<Map<String,Object>> commonPicFileList = fileServerProvider.queryCommonPicFileByFlag(endDate,false);
				
				if (commonPicFileList != null && commonPicFileList.size() > 0) {
					for (Map<String,Object> picMap : commonPicFileList) {
						String id = CommonUtil.getString(picMap.get("_id"));
						String savePath = CommonUtil.getString(picMap.get("savePath"));
						
						// 删除文件系统数据
						FileDealerProcessManager.getInstance().getFileStore().deleteFile(savePath);
						//删除数据库记录
						fileServerProvider.deleteCommonPicFileById(id);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// 休眠1小时
            try {
                Thread.sleep(60 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
			
		}

	}

}
