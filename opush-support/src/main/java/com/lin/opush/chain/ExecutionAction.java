package com.lin.opush.chain;

/**
 * 执行链中各环节的执行动作
 *      执行链中各环节实现该接口并重写execute方法进行具体执行动作的实现
 */
public interface ExecutionAction<T extends ExecutionChainDataModel> {
    /**
     * 执行链中各环节执行动作的具体执行逻辑
     * @param context 执行链上下文
     */
    void execute(ExecutionChainContext<T> context);
}
