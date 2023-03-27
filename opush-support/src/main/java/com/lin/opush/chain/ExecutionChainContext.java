package com.lin.opush.chain;

import com.lin.opush.vo.BasicResultVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 执行链上下文
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class ExecutionChainContext<T extends ExecutionChainDataModel> {
    /**
     * 不同消息业务类型的标识 → 普通发送业务【send】，撤回业务【recall】
     */
    private String code;
    /**
     * 执行链上下文数据的模型
     */
    private T executionChainDataModel;
    /**
     * 执行链中断的标识
     */
    private Boolean needBreak;
    /**
     * 消息业务流程处理的结果
     */
    BasicResultVO response;
}
