package com.lin.opush.service.deduplication.deduplicationParam;

import cn.hutool.core.date.DateUtil;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.enums.DeduplicationType;
import com.lin.opush.service.deduplication.DeduplicationParam;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * 频次去重参数构建器
 */
@Service
public class FrequencyDeduplicationParamBuilder extends AbstractDeduplicationParamBuilder implements DeduplicationParamBuilder {
    /**
     * 指定去重类型对应编码值
     */
    public FrequencyDeduplicationParamBuilder() {
        deduplicationTypeCode = DeduplicationType.FREQUENCY.getCode();
    }

    /**
     * 构建频次去重参数
     * @param deduplicationRule 所有去重规则（JSON字符串）
     * @param taskInfo          任务信息
     * @return 去重参数
     */
    @Override
    public DeduplicationParam build(String deduplicationRule, TaskInfo taskInfo) {
        // 根据去重类型编码值获取对应去重规则构建去重参数
        DeduplicationParam deduplicationParam = getDeduplicationParam(deduplicationTypeCode, deduplicationRule, taskInfo);
        if (Objects.isNull(deduplicationParam)) {
            return null;
        }
        // 设置频次去重的时间段（设置从现在开始到当天23:59:59的时间差为去重时间段 → 过期时间，单位s）
        deduplicationParam.setDeduplicationTimeQuantum((DateUtil.endOfDay(new Date()).getTime() - DateUtil.current()) / 1000);
        // 设置埋点信息（暂时设置为消息已被频次去重）
        deduplicationParam.setAnchorState(AnchorState.FREQUENCY_DEDUPLICATION);
        return deduplicationParam;
    }
}
