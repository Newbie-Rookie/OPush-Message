package com.lin.opush.xxljob.service;

import com.lin.opush.vo.BasicResultVO;
import com.lin.opush.xxljob.domain.XxlJobInfo;
import com.lin.opush.xxljob.domain.XxlJobGroup;

/**
 * 定时任务服务接口
 */
public interface XxlJobService {
    /**
     * 新增/修改定时任务
     * @param timedTaskInfo 定时任务信息
     * @return 新增时返回任务Id，修改时无返回
     */
    BasicResultVO saveTimedJob(XxlJobInfo timedTaskInfo);

    /**
     * 启动定时任务
     * @param timedJobId 定时任务id
     * @return 启动结果
     */
    BasicResultVO startTimedJob(Integer timedJobId);

    /**
     * 暂停定时任务
     * @param timedJobId 定时任务id
     * @return 暂停结果
     */
    BasicResultVO stopTimedJob(Integer timedJobId);

    /**
     * 删除定时任务
     * 【删除模板时、该模板启动并创建过定时任务，需删除对应定时任务】
     * @param timedJobId 定时任务id
     * @return 删除结果
     */
    BasicResultVO deleteTimedJob(Integer timedJobId);

    /**
     * 根据执行器的appname和title获取执行器id
     * @param appName 执行器appname
     * @param title 执行器名
     * @return 获取结果
     */
    BasicResultVO getJobGroupId(String appName, String title);

    /**
     * 创建执行器
     * @param xxlJobGroup 执行器信息
     * @return 创建结果
     */
    BasicResultVO createXxlJobGroup(XxlJobGroup xxlJobGroup);
}
