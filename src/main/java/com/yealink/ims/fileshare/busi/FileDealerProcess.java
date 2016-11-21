package com.yealink.ims.fileshare.busi;

import com.yealink.ims.fileshare.event.EventType;
import com.yealink.ims.fileshare.event.FsMsg;
import com.yealink.ims.fileshare.util.FileShareThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 文件处理 线程
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class FileDealerProcess implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FileDealerProcess.class);

    // 任务队列
    private ConcurrentLinkedQueue<FsMsg> workQueue = new ConcurrentLinkedQueue<FsMsg>();

    private volatile boolean isRunning = false;

    /**
     * 添加到任务队列
     * @param fsMsg
     */
    public boolean putFsMsgQueue(FsMsg fsMsg) {
        if (isRunning) {
            synchronized (workQueue) {
                boolean added = workQueue.add(fsMsg);
                if (added) {
                    workQueue.notifyAll();
                }
                return added;
            }
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        isRunning = true;
        LOG.info("file deal process thread start...");

        while (true) {
            try {
                FsMsg msg = null;
                synchronized (workQueue) {
                    msg = workQueue.poll();
                    if(msg == null){
                        workQueue.wait();
                        continue;
                    }
                }

                // 任务类型
                EventType event = msg.getCmd();
                if (event == null) {
                    LOG.info("event type is null...");
                    continue;
                }

                final IDealer dealer = FileDealerProcessManager.getInstance().getDealer(event);
                if (dealer == null) {
                    LOG.info("event is:{} without the file dealer...", event.getCmd());
                    continue;
                }

                LOG.debug("{}=======start deal the file cmd:{}", dealer.toString(), event.getCmd());
                // 处理客户端请求
                final FsMsg fsMsg = msg;
                FileShareThreadExecutor.getInstance().getBusiDealthreadPool().execute(new Runnable(){
					@Override
					public void run() {
						dealer.deal(fsMsg);
					}
                });

//                dealer.deal(fsMsg);

            } catch (Exception e) {
                LOG.error("file deal process exception", e);
            }
        }
    }
}
