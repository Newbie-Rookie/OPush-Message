package com.lin.opush.xxljob.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务详细信息【基础配置、触发配置、任务配置、高级配置】
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XxlJobInfo implements Serializable {
    // 基础配置
    /**
     * 任务主键id
     */
    private Integer id;
    /**
     * 执行器主键id
     */
    private int jobGroup;
    /**
     * 任务描述
     */
    private String jobDesc;
    /**
     * 添加时间
     */
    private Date addTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 负责人
     */
    private String author;
    /**
     * 报警邮件
     */
    private String alarmEmail;

    // 触发配置
    /**
     * 调度类型【无、Cron、固定速度】
     */
    private String scheduleType;
    /**
     * 调度配置【""、Cron表达式、秒数】
     */
    private String scheduleConf;

    // 任务配置
    /**
     * 运行模式【GlueTypeEnum，主要分为BEAN和GLUE模式】
     * 【GLUE模式目前支持Java、Shell、Python、PHP、NodeJs、PowerShell】
     */
    private String glueType;
    /**
     * GLUE源代码【运行模式为GLUE时使用】
     */
    private String glueSource;
    /**
     * GLUE备注【运行模式为GLUE时使用】
     */
    private String glueRemark;
    /**
     * GLUE更新时间【运行模式为GLUE时使用】
     */
    private Date glueUpdatetime;
    /**
     * 任务处理器名【JobHandler】
     * 【运行模式为BEAN模式时生效，对应@XxlJob的value属性值】
     * 【xxl.job.executor.jobHandlerName】
     */
    private String executorHandler;
    /**
     * 任务参数【执行器调用任务处理器时传入的参数】
     */
    private String executorParam;

    // 高级配置
    /**
     * 路由策略【执行器集群部署时，选择在哪个执行器执行该任务】
     * 【第一个、最后一个、轮询、随机、一致性Hash、最不经常使用、最近最久未使用、故障转移、忙碌传播、分片广播】
     */
    private String executorRouteStrategy;
    /**
     * 子任务ID
     * 【每个任务都拥有唯一任务ID，当本任务成功执行结束时，
     * 将会触发子任务ID所对应的任务的一次主动调度【多个子任务用逗号分隔】】
     */
    private String childJobId;
    /**
     * 调度过期策略【忽略、立即执行一次】
     */
    private String misfireStrategy;
    /**
     * 阻塞处理策略【单机串行、丢弃后续调度、覆盖之前调度】
     */
    private String executorBlockStrategy;
    /**
     * 任务执行超时时间
     * 【自定义任务执行超时时间，任务运行超时将会主动中断任务【单位秒，大于0生效】】
     */
    private int executorTimeout;
    /**
     * 任务失败重试次数
     * 【自定义任务失败重试次数，任务失败时将按预设的失败重试次数主动进行重试【大于0生效】】
     */
    private int executorFailRetryCount;

    /**
     * 调度状态【0：停止，1：运行】
     */
    private int triggerStatus;
    /**
     * 上次调度时间
     */
    private long triggerLastTime;
    /**
     * 下次调度时间
     */
    private long triggerNextTime;
}
