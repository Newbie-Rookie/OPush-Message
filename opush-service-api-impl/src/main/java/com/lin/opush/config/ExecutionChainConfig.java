package com.lin.opush.config;

import com.lin.opush.action.AssembleParamAction;
import com.lin.opush.action.SendMqAction;
import com.lin.opush.enums.BusinessCode;
import com.lin.opush.chain.ExecutionChain;
import com.lin.opush.chain.ExecutionController;
import com.lin.opush.action.AfterParamCheckAction;
import com.lin.opush.action.PreParamCheckAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置不同消息业务类型对应的执行链
 */
@Configuration
public class ExecutionChainConfig {
    /**
     * 前置参数校验
     */
    @Autowired
    private PreParamCheckAction preParamCheckAction;
    /**
     * 组装参数
     */
    @Autowired
    private AssembleParamAction assembleParamAction;
    /**
     * 后置参数校验
     */
    @Autowired
    private AfterParamCheckAction afterParamCheckAction;
    /**
     * 发送消息至MQ
     */
    @Autowired
    private SendMqAction sendMqAction;

    /**
     * 普通发送执行流程：前置参数校验 → 组装参数 → 后置参数校验 → 发送消息至MQ
     * @return
     */
    @Bean("commonSendTemplate")
    public ExecutionChain commonSendTemplate() {
        ExecutionChain executionChain = new ExecutionChain();
        executionChain.setExecutionList(Arrays.asList(preParamCheckAction, assembleParamAction,
                afterParamCheckAction, sendMqAction));
        return executionChain;
    }

    /**
     * 消息撤回执行流程：组装参数 → 发送至MQ
     * @return
     */
    @Bean("recallMessageTemplate")
    public ExecutionChain recallMessageTemplate() {
        ExecutionChain executionChain = new ExecutionChain();
        executionChain.setExecutionList(Arrays.asList(assembleParamAction, sendMqAction));
        return executionChain;
    }

    /**
     * 消息业务执行的全流程控制器
     *      扩展则增加BusinessCode（消息业务类型）和ProcessChain（消息业务类型对应执行链）
     * @return 消息业务执行的全流程控制器
     */
    @Bean
    public ExecutionController processController() {
        ExecutionController executionController = new ExecutionController();
        // 存储不同消息业务类型对应的执行链
        Map<String, ExecutionChain> executionChainConfig = new HashMap<>(4);
        // 设置消息业务类型与不同消息业务类型对应执行链之间的映射
        executionChainConfig.put(BusinessCode.SEND.getCode(), commonSendTemplate());
        executionChainConfig.put(BusinessCode.RECALL.getCode(), recallMessageTemplate());
        executionController.setExecutionChainConfig(executionChainConfig);
        return executionController;
    }
}
