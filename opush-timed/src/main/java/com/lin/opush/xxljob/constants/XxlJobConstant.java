package com.lin.opush.xxljob.constants;

/**
 * xxl-job常量信息
 */
public class XxlJobConstant {
    /**
     * 主控制器【IndexController】
     *      登录xxl-job-admin：/login【POST】
     *           通过登录接口获取cookie，由于不知道cookie对应的session何时过期
     *           其他接口在调用时，都需先获取cookie【为避免该过程失败，允许最多重试3次】
     */
    public static final String LOGIN_URL = "/login";

    /**
     * 执行器控制器【JobGroupController】→ 执行器控制器接口
     *      条件查询【分页】执行器列表：/jobgroup/pageList
     *      添加执行器：/jobgroup/save
     */
    public static final String JOBGROUP_PAGELIST_URL = "/jobgroup/pageList";
    public static final String JOBGROUP_SAVE_URL = "/jobgroup/save";

    /**
     * 任务信息控制器【JobInfoController】→ 任务信息接口
     *      添加任务：/jobinfo/add
     *      更新任务：/jobinfo/update
     *      删除任务：/jobinfo/remove
     *      启动任务：/jobinfo/start
     *      停止任务：/jobinfo/stop
     */
    public static final String ADD_JOB_URL = "/jobinfo/add";
    public static final String UPDATE_JOB_URL = "/jobinfo/update";
    public static final String DELETE_JOB_URL = "/jobinfo/remove";
    public static final String START_JOB_URL = "/jobinfo/start";
    public static final String STOP_JOB_URL = "/jobinfo/stop";

    /**
     * 任务处理器名称
     */
    public static final String JOB_HANDLER_NAME = "opushJob";

    /**
     * 任务执行超时时间
     */
    public static final Integer TIME_OUT = 120;

    /**
     * 任务失败重试次数
     */
    public static final Integer RETRY_COUNT = 0;

    /**
     * 立即执行
     * 【模板中expectPushTime为0】的任务延迟时间【秒】
     */
    public static final Integer DELAY_TIME = 8;
}
