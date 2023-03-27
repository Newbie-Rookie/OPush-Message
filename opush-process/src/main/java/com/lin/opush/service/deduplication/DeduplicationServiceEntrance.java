package com.lin.opush.service.deduplication;

import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.DeduplicationType;
import com.lin.opush.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 去重服务入口
 */
@Service
public class DeduplicationServiceEntrance {
    /**
     * 本地配置中去重规则KEY（暂不接入远程服务Apollo / Nacos）
     */
    public static final String DEDUPLICATION_RULE_KEY = "deduplicationRule";

    /**
     * 配置服务
     */
    @Autowired
    private ConfigService config;

    /**
     * 去重参数和去重服务持有者
     */
    @Autowired
    private ParamAndServiceHolder paramAndServiceHolder;

    /**
     * 执行去重操作：读取去重规则配置 → 构建去重参数 → 执行去重服务
     * @param taskInfo 任务信息
     */
    public void deduplication(TaskInfo taskInfo) {
        // 配置样例：{"deduplication_10":{"num":1,"time":300},"deduplication_20":{"num":5}}
        // 获取配置的去重规则（JSON字符串）
        String deduplicationRule = config.getProperty(DEDUPLICATION_RULE_KEY, CommonConstant.EMPTY_JSON_OBJECT);
        // 获取去重类型对应的编码列表
        List<Integer> deduplicationTypeCodeList = DeduplicationType.getDeduplicationTypeCodeList();
        // 根据去重类型编码进行去重参数构建并执行去重服务
        for (Integer deduplicationTypeCode : deduplicationTypeCodeList) {
            // 构建去重参数
            DeduplicationParam deduplicationParam = paramAndServiceHolder.getDeduplicationParamBuilder(deduplicationTypeCode)
                                                                                            .build(deduplicationRule, taskInfo);
            if (Objects.nonNull(deduplicationParam)) {
                // 执行去重服务
                paramAndServiceHolder.getDeduplicationService(deduplicationTypeCode).deduplication(deduplicationParam);
            }
        }
    }
}
