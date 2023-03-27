package com.lin.opush.process;

import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.service.flowControl.FlowControlFactory;
import com.lin.opush.service.flowControl.FlowControlParam;
import com.lin.opush.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * 基础处理器【提供模板方法，具体实现交由各发送渠道实现】
 */
public abstract class BaseProcessor implements Processor {
    /**
     * 存储不同发送渠道对应的处理器
     */
    @Autowired
    private ProcessorHolder processorHolder;

    /**
     * 流量控制工厂
     */
    @Autowired
    private FlowControlFactory flowControlFactory;

    /**
     * 标识渠道类型的Code，由基础处理器的子类初始化时指定
     */
    protected Integer channelTypeCode;

    /**
     * 流量控制参数【子类初始化的时候指定】
     */
    protected FlowControlParam flowControlParam;

    /**
     * 日志工具类
     */
    @Autowired
    private LogUtils logUtils;

    /**
     * 初始化不同渠道与处理器的映射关系
     */
    @PostConstruct
    private void init() {
        processorHolder.putProcessor(channelTypeCode, this);
    }

    /**
     * 该方法为模板方法（发送消息方法）
     * 定义处理不同渠道发送的基本处理过程，具体处理交由该类的子类实现
     * @param taskInfo 任务信息
     */
    @Override
    public void send(TaskInfo taskInfo) {
        // 判断是否需要进行流量控制
        isFlowControl(taskInfo);
        // 调用子类实现的处理方法进行真正消息发送
        if (realSend(taskInfo)) {
            logUtils.print(AnchorInfo.builder().state(AnchorState.SEND_SUCCESS.getCode()).businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).build());
            return;
        }
        logUtils.print(AnchorInfo.builder().state(AnchorState.SEND_FAIL.getCode()).businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).build());
    }

    /**
     * 判断是否需要进行流量控制，需要则先进行流量控制前置处理
     * 【调用流量控制工厂的flowControlPreProcess方法进行流量控制前置处理，处理后根据流量控制参数中对应流量控制策略进行流量控制】
     * @param taskInfo 任务信息
     */
    public void isFlowControl(TaskInfo taskInfo) {
        // 只有子类指定了限流参数，才需要限流
        if (Objects.nonNull(flowControlParam)) {
            // 交由流量控制工厂进行流量控制前置处理
            flowControlFactory.flowControlPreProcess(taskInfo, flowControlParam);
        }
    }

    /**
     * 统一发送消息的抽象方法，交由不同渠道的处理类实现
     * @param taskInfo 任务信息
     * @return 发送是否成功
     */
    public abstract boolean realSend(TaskInfo taskInfo);
}
