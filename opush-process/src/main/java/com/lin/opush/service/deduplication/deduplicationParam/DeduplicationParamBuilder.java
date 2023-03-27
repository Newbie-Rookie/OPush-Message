package com.lin.opush.service.deduplication.deduplicationParam;

import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.DeduplicationParam;

/**
 * 去重参数构建器接口
 */
public interface DeduplicationParamBuilder {
    /**
     * 本地配置中去重规则中不同去重类型的配置前缀
     */
    String DEDUPLICATION_CONFIG_PREFIX = "deduplication_";

    /**
     * 去重参数构建方法
     * 根据本地配置（暂不接入远程配置服务）构建不同去重类型对应参数
     * @param deduplicationRule 所有去重规则（JSON字符串）
     * @param taskInfo 任务信息
     * @return 去重参数
     */
    DeduplicationParam build(String deduplicationRule, TaskInfo taskInfo);
}
