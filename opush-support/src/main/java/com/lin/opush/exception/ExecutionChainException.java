package com.lin.opush.exception;

import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.chain.ExecutionChainContext;

import java.util.Objects;

/**
 * 业务流程处理异常
 */
public class ExecutionChainException extends RuntimeException {
    /**
     * 执行链上下文
     */
    private final ExecutionChainContext executionChainContext;

    public ExecutionChainException(ExecutionChainContext executionChainContext) {
        super();
        this.executionChainContext = executionChainContext;
    }

    public ExecutionChainException(ExecutionChainContext executionChainContext, Throwable cause) {
        super(cause);
        this.executionChainContext = executionChainContext;
    }

    @Override
    public String getMessage() {
        if (Objects.nonNull(this.executionChainContext)) {
            return this.executionChainContext.getResponse().getMsg();
        }
        return RespStatusEnum.CONTEXT_IS_NULL.getMsg();

    }

    public ExecutionChainContext getExecutionChainContext() {
        return executionChainContext;
    }
}
