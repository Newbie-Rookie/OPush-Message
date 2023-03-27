package com.lin.opush.service.deduplication.deduplicationParam;

import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.enums.DeduplicationType;
import com.lin.opush.service.deduplication.DeduplicationParam;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 内容去重参数构建器
 */
@Service
public class ContentDeduplicationParamBuilder extends AbstractDeduplicationParamBuilder implements DeduplicationParamBuilder {
    /**
     * 指定去重类型对应编码值
     */
    public ContentDeduplicationParamBuilder() {
        deduplicationTypeCode = DeduplicationType.CONTENT.getCode();
    }

    /**
     * 构建内容去重参数
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
        // 设置埋点信息（暂时设置为消息已被内容去重）
        deduplicationParam.setAnchorState(AnchorState.CONTENT_DEDUPLICATION);
        return deduplicationParam;
    }
}
