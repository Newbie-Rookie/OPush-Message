package com.lin.opush.domain;

import com.lin.opush.domain.MessageParam;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.chain.ExecutionChainDataModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送消息任务的数据模型
 * 【存储在执行链上下文com.lin.opush.chain.ExecutionChainContext#executionChainDataModel中】
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendTaskModel implements ExecutionChainDataModel {
    /**
     * 消息模板id
     */
    private Long messageTemplateId;

    /**
     * 请求参数列表【接收者、消息内容可变参数】
     */
    private List<MessageParam> messageParamList;

    /**
     * 发送任务的信息【详细信息】
     */
    private List<TaskInfo> taskInfo;

    /**
     * 撤回任务的信息【通过消息模板id进行撤回】
     */
    private MessageTemplate messageTemplate;

    /**
     * 下发者
     */
    private String creator;
}
