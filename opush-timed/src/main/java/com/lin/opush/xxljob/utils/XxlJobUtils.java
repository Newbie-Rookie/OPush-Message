package com.lin.opush.xxljob.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.vo.BasicResultVO;
import com.lin.opush.xxljob.constants.XxlJobConstant;
import com.lin.opush.xxljob.domain.XxlJobInfo;
import com.lin.opush.xxljob.domain.XxlJobGroup;
import com.lin.opush.xxljob.enums.*;
import com.lin.opush.xxljob.service.XxlJobService;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * xxlJob工具类
 */
@Component
public class XxlJobUtils {
    /**
     * 执行器appname【标识执行器分组】
     */
    @Value("${xxl.job.executor.appname}")
    private String appName;

    /**
     * 执行器名【增加appname可读性】
     */
    @Value("${xxl.job.executor.title}")
    private String title;

    /**
     * 定时任务服务
     */
    @Autowired
    private XxlJobService timedTaskService;

    /**
     * 根据执行器appname和title获取执行器id列表【目前只有1个，故返回该执行器id】，没有则创建执行器
     * @return 执行器id
     */
    private Integer queryJobGroupId() {
        BasicResultVO basicResultVO = timedTaskService.getJobGroupId(appName, title);
        // 若执行器不存在则创建执行器
        if (Objects.isNull(basicResultVO.getData())) {
            // 执行器信息
            XxlJobGroup xxlJobGroup = XxlJobGroup.builder().appname(appName).title(title)
                                                .addressType(CommonConstant.FALSE).build();
            // 判断是否创建成功
            if (RespStatusEnum.SUCCESS.getCode().equals(timedTaskService.createXxlJobGroup(xxlJobGroup).getStatus())) {
                return (Integer) timedTaskService.getJobGroupId(appName, title).getData();
            }
        }
        return (Integer) basicResultVO.getData();
    }

    /**
     * 构建定时任务信息
     * @param messageTemplate 模板信息
     * @return 定时任务信息
     */
    public XxlJobInfo buildXxlJobInfo(MessageTemplate messageTemplate) {
        // 期望推送消息的时间【0：立即推送，else：cron表达式】
        String expectPushTime = messageTemplate.getExpectPushTime();
        // expectPushTime为0，即未指定cron表达式，即立即执行【自动配置延迟8秒的cron表达式】
        if (expectPushTime.equals(String.valueOf(CommonConstant.FALSE))) {
            // 自动配置延迟8秒的cron表达式
            expectPushTime = DateUtil.format(DateUtil.offsetSecond(new Date(), XxlJobConstant.DELAY_TIME), CommonConstant.CRON_FORMAT);
        }
        // 构建定时任务信息
        XxlJobInfo xxlJobInfo = XxlJobInfo.builder()
                                        // 执行器主键、任务描述为模板名、负责人为模板创建者、报警邮件【""】
                                        .jobGroup(queryJobGroupId()).jobDesc(messageTemplate.getName())
                                        .author(messageTemplate.getCreator()).alarmEmail(StrUtil.EMPTY)
                                        // 调度类型【Cron】、调度配置【推送时间】
                                        .scheduleType(ScheduleTypeEnum.CRON.name()).scheduleConf(expectPushTime)
                                        // 运行模式【BEAN】、GLUE源代码【""】、GLUE备注【""】
                                        .glueType(GlueTypeEnum.BEAN.name()).glueSource(StrUtil.EMPTY).glueRemark(StrUtil.EMPTY)
                                        // 任务处理器名【opushJob】、任务参数【模板创建者 + ":" + 消息模板id】
                                        .executorHandler(XxlJobConstant.JOB_HANDLER_NAME)
                                        .executorParam(messageTemplate.getCreator() + ":" + messageTemplate.getId())
                                        // 执行器路由策略【一致性Hash】
                                        .executorRouteStrategy(ExecutorRouteStrategyEnum.CONSISTENT_HASH.name())
                                        // 子任务id【""】
                                        .childJobId(StrUtil.EMPTY)
                                        // 调度过期策略【忽略】
                                        .misfireStrategy(ScheduleOverdueStrategyEnum.DO_NOTHING.name())
                                        // 阻塞处理策略【单机串行】
                                        .executorBlockStrategy(ExecutorBlockStrategyEnum.SERIAL_EXECUTION.name())
                                        // 任务执行超时时间【120s】、任务失败重试次数【0】、调度状态【0 → 停止】
                                        .executorTimeout(XxlJobConstant.TIME_OUT)
                                        .executorFailRetryCount(XxlJobConstant.RETRY_COUNT)
                                        .triggerStatus(CommonConstant.FALSE).build();
        // 若定时任务已存在调度中心，则消息模板中定时任务id不为空【即定时任务启动过】
        if (Objects.nonNull(messageTemplate.getTimedJobId())) {
            xxlJobInfo.setId(messageTemplate.getTimedJobId());
        }
        return xxlJobInfo;
    }
}
