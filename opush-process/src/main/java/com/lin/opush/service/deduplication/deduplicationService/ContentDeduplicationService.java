package com.lin.opush.service.deduplication.deduplicationService;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.DeduplicationType;
import com.lin.opush.service.deduplication.limit.LimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 内容去重服务【5分钟相同的文案发给相同的用户去重】
 */
@Service
public class ContentDeduplicationService extends AbstractDeduplicationService {
    /**
     * 注入去重类型编码值和去重服务对应的限流服务（滑动窗口去重）
     * 由于存在多个去重服务，需使用@Qualifier精准注入
     * @param limitService 限流服务
     */
    @Autowired
    public ContentDeduplicationService(@Qualifier("SlideWindowLimitService") LimitService limitService) {
        deduplicationTypeCode = DeduplicationType.CONTENT.getCode();
        this.limitService = limitService;
    }

    /**
     * 构建单个内容去重key【md5(templateId + receiver + content)】
     * 相同模板相同内容短时间内发给同一用户
     * @param taskInfo 任务信息
     * @param receiver 接收者
     * @return 唯一key
     */
    @Override
    public String buildSingleDeDuplicationKey(TaskInfo taskInfo, String receiver) {
        return DigestUtil.md5Hex(taskInfo.getMessageTemplateId() + receiver + JSON.toJSONString(taskInfo.getContentModel()));
    }
}
