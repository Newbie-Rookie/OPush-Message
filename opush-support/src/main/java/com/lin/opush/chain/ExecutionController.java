package com.lin.opush.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.exception.ExecutionChainException;
import com.lin.opush.vo.BasicResultVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 消息业务执行的全流程控制器
 *    1、不同消息业务对应执行链的组装
 *    2、消息业务类型与不同消息业务类型对应执行链之间的映射
 *    3、对执行链中每个环节进行遍历执行
 */
@Slf4j
@Data
public class ExecutionController {
    /**
     * 不同消息业务的执行链配置
     * 消息业务类型与不同消息业务类型对应执行链之间的映射
     *      send → 【前置参数校验 → 组装参数 → 后置参数校验 → 发送消息至MQ】
     *      recall → 【组装参数 → 发送至MQ】
     */
    private Map<String, ExecutionChain> executionChainConfig = null;

    /**
     * 对执行链中每个环节进行遍历执行
     * @param context 执行链上下文
     * @return 返回上下文内容
     */
    public ExecutionChainContext execute(ExecutionChainContext context) {
        /**
         * 前置检查
         */
        try {
            preCheck(context);
        } catch (ExecutionChainException e) {
            return e.getExecutionChainContext();
        }
        /**
         * 遍历执行链中的执行列表中各环节的执行动作
         */
        List<ExecutionAction> executionList = executionChainConfig.get(context.getCode()).getExecutionList();
        for (ExecutionAction executionAction : executionList) {
            executionAction.execute(context);
            if (context.getNeedBreak()) {
                break;
            }
        }
        return context;
    }

    /**
     * 执行前检查，出错则抛出异常
     * @param context 执行链上下文
     * @throws ExecutionChainException 执行链异常信息
     */
    private void preCheck(ExecutionChainContext context) throws ExecutionChainException {
        // 执行链上下文是否为空
        if (Objects.isNull(context)) {
            context = new ExecutionChainContext();
            context.setResponse(BasicResultVO.fail(RespStatusEnum.CONTEXT_IS_NULL));
            throw new ExecutionChainException(context);
        }
        // 消息业务代码是否为空
        String businessCode = context.getCode();
        if (StrUtil.isBlank(businessCode)) {
            context.setResponse(BasicResultVO.fail(RespStatusEnum.BUSINESS_CODE_IS_NULL));
            throw new ExecutionChainException(context);
        }
        // 消息业务代码对应的执行链配置是否为空
        ExecutionChain executionChain = executionChainConfig.get(businessCode);
        if (Objects.isNull(executionChain)) {
            context.setResponse(BasicResultVO.fail(RespStatusEnum.Execution_Chain_IS_NULL));
            throw new ExecutionChainException(context);
        }
        // 消息业务代码对应的执行链中的执行列表是否为空
        List<ExecutionAction> executionList = executionChain.getExecutionList();
        if (CollUtil.isEmpty(executionList)) {
            context.setResponse(BasicResultVO.fail(RespStatusEnum.Execution_LIST_IS_NULL));
            throw new ExecutionChainException(context);
        }
    }
}
