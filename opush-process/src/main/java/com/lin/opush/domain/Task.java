package com.lin.opush.domain;


import cn.hutool.core.collection.CollUtil;
import com.lin.opush.process.ProcessorHolder;
import com.lin.opush.service.deduplication.DeduplicationServiceEntrance;
import com.lin.opush.service.discard.MessageDiscardService;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 任务（任务执行方法run：消息丢弃 → 消息平台性去重 → 真正发送消息）
 */
@Data
@Accessors(chain = true)
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Task implements Runnable {
    /**
     * 存储不同发送渠道的处理器
     */
    @Autowired
    private ProcessorHolder processorHolder;

    /**
     * 消息丢弃服务
     */
    @Autowired
    private MessageDiscardService messageDiscardService;

    /**
     * 消息平台性去重服务（入口）
     */
    @Autowired
    private DeduplicationServiceEntrance deduplicationServiceEntrance;

    /**
     * 任务信息
     */
    private TaskInfo taskInfo;

    @Override
    public void run() {
        // 消息丢弃【暂时根据模板id丢弃】
        if (messageDiscardService.isDiscard(taskInfo)) {
            return;
        }
        // 消息去重【5分钟相同内容、渠道、用户去重，一天相同渠道、用户去重】
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            deduplicationServiceEntrance.deduplication(taskInfo);
        }
        // 真正发送消息
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            processorHolder.route(taskInfo.getSendChannel()).send(taskInfo);
        }
    }
}
