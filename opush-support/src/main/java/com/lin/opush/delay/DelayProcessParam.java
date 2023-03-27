package com.lin.opush.delay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * 延迟处理参数【阻塞队列实现类、数量阈值、时间阈值、消费线程池实例】
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class DelayProcessParam<T> {
    /**
     * 阻塞队列实现类【必填】
     */
    private BlockingQueue<T> queue;

    /**
     * batch【批量】触发执行的数量阈值【必填】
     */
    private Integer numberThreshold;

    /**
     * batch【批量】触发执行的时间阈值，单位毫秒【必填】
     */
    private Long timeThreshold;

    /**
     * 消费线程池实例【必填】
     */
    protected ExecutorService executorService;
}
