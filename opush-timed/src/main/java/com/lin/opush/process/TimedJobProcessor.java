package com.lin.opush.process;

import com.dtp.core.thread.DtpExecutor;
import com.lin.opush.config.TimedThreadPoolConfig;
import com.lin.opush.service.ProcessTimedJob;
import com.lin.opush.utils.ThreadPoolUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 定时任务处理器
 */
@Service
@Slf4j
public class TimedJobProcessor {
    /**
     * 处理定时任务
     */
    @Autowired
    private ProcessTimedJob processTimedJob;

    /**
     * 线程池工具类
     */
    @Autowired
    private ThreadPoolUtils threadPoolUtils;

    /**
     * 异步处理xxl-job-admin请求的线程池
     * 由于读取文件和远程调用发送接口是相对耗时的，使用线程池做异步处理，及时返回xxl-job-admin，避免定时任务超时
     */
    private DtpExecutor dtpExecutor;

    /**
     * 初始化异步处理xxl-job-admin请求的线程池
     */
    @PostConstruct
    public void init(){
        dtpExecutor = TimedThreadPoolConfig.getAsyncProcessXxlRequestExecutor();
        threadPoolUtils.register(dtpExecutor);
    }

    /**
     * 处理定时任务
     * 【将定时任务交由线程池进行异步执行，及时响应请求，避免定时任务超时】
     */
    @XxlJob("opushJob")
    public void processTimedJob() {
        // 获取创建定时任务时保存在定时任务参数中【模板创建者:模板id】
        String creator = XxlJobHelper.getJobParam().split(":")[0];
        Long messageTemplateId = Long.valueOf(XxlJobHelper.getJobParam().split(":")[1]);
        log.info("TimedJobProcessor#processTimedJob TimedJobCreator:{} and messageTemplateId:{} TimedJob exec!", creator, messageTemplateId);
        // 交由异步处理xxl-job-admin请求的线程池执行定时任务，立即响应xxl-job-admin
        dtpExecutor.execute(() -> processTimedJob.process(creator, messageTemplateId));
    }
}