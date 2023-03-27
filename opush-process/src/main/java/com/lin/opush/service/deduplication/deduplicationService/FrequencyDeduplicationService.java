package com.lin.opush.service.deduplication.deduplicationService;

import cn.hutool.core.util.StrUtil;

import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.DeduplicationType;
import com.lin.opush.service.deduplication.limit.LimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 频次去重服务【一天内相同的用户如果已经收到某渠道内容5次去重】
 */
@Service
public class FrequencyDeduplicationService extends AbstractDeduplicationService {
    /**
     * 注入去重类型编码值和去重服务对应的限流服务（普通计数去重）
     * 由于存在多个去重服务，需使用@Qualifier精准注入
     * @param limitService
     */
    @Autowired
    public FrequencyDeduplicationService(@Qualifier("SimpleCountLimitService") LimitService limitService) {
        this.limitService = limitService;
        deduplicationTypeCode = DeduplicationType.FREQUENCY.getCode();
    }

    /**
     * 标记去重类型的前缀
     */
    private static final String PREFIX = "FREQUENCY";

    /**
     * 构建单个频次去重key【receiver + templateId + sendChannel】
     * 一天内相同用户只能收到相同渠道的消息N次
     * @param taskInfo 任务信息
     * @param receiver 接收者
     * @return 唯一key
     */
    @Override
    public String buildSingleDeDuplicationKey(TaskInfo taskInfo, String receiver) {
        return PREFIX + StrUtil.C_UNDERLINE + receiver + StrUtil.C_UNDERLINE + taskInfo.getMessageTemplateId() + StrUtil.C_UNDERLINE + taskInfo.getSendChannel();
    }
}
