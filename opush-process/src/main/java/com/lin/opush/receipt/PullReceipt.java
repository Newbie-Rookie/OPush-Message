package com.lin.opush.receipt;

import com.google.common.base.Throwables;
import com.lin.opush.config.SupportThreadPoolConfig;
import com.lin.opush.receipt.stater.PullReceiptStater;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 拉取回执入口
 */
@Component
@Slf4j
public class PullReceipt {
    /**
     * 拉取回执启动器实现类列表【拉取不同渠道消息回执】
     */
    @Autowired
    private List<PullReceiptStater> pullReceiptStaterList;

    @PostConstruct
    private void init() {
        // 获取线程池执行任务【拉取回执】
        SupportThreadPoolConfig.getThreadPool().execute(() -> {
            // 项目启动后就一直拉取消息回执入库
            while (true) {
                try {
                    for (PullReceiptStater pullReceiptStater : pullReceiptStaterList) {
                        pullReceiptStater.start();
                    }
                } catch (Exception e) {
                    log.error("PullReceipt#init fail:{}", Throwables.getStackTraceAsString(e));
                }
            }
        });
    }
}
