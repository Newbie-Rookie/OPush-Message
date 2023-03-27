package com.lin.opush.chain;

import java.util.List;

/**
 * 消息业务的执行链（将执行链的各执行动作串连起来）
 */
public class ExecutionChain {
    /**
     * 消息业务的执行链中的执行列表
     */
    private List<ExecutionAction> executionList;

    public List<ExecutionAction> getExecutionList() {
        return executionList;
    }

    public void setExecutionList(List<ExecutionAction> processList) {
        this.executionList = processList;
    }
}