package com.lin.opush.delay;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.lin.opush.config.SupportThreadPoolConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 延迟处理抽象类
 * 【阻塞队列的生产者和消费者实现】
 */
@Slf4j
@Data
public abstract class AbstractDelayProcess<T> {
    /**
     * 延迟消费参数【子类构造方法需初始化该参数】
     */
    protected DelayProcessParam<T> delayProcessParam;

    /**
     * 用于缓存行信息的列表
     */
    private List<T> bufferRowInfos = new ArrayList<>();

    /**
     * 最后一次处理缓存行信息列表的时间
     */
    private Long lastProcessTime = System.currentTimeMillis();

    /**
     * 是否终止线程
     */
    private volatile Boolean stop = false;

    /**
     * 单线程消费阻塞队列的数据
     */
    @PostConstruct
    public void initConsumeBlockingQueue() {
        // 开启线程池
        ExecutorService executorService = SupportThreadPoolConfig.getThreadPool();
        executorService.execute(() -> {
            while (true) {
                try {
                    // 在指定时间【时间阈值】内从阻塞队列中拉取队首对象【在指定时间内，队列一旦有数据可取，则立即返回队列中的数据】
                    T rowInfo = delayProcessParam.getQueue().poll(delayProcessParam.getTimeThreshold(), TimeUnit.MILLISECONDS);
                    if (null != rowInfo) {
                        bufferRowInfos.add(rowInfo);
                    }
                    // 缓存行信息列表不为空且满足处理缓存行信息的条件【数量超限 / 时间超限】
                    if (CollUtil.isNotEmpty(bufferRowInfos) && isCanProcessBufferRowInfos()) {
                        List<T> rowInfos = bufferRowInfos;
                        bufferRowInfos = Lists.newArrayList();
                        lastProcessTime = System.currentTimeMillis();
                        // 具体执行逻辑
                        delayProcessParam.getExecutorService().execute(() -> this.consume(rowInfos));
                    }
                    // 判断是否停止当前线程池【Csv文件处理完成且缓存行信息列表为空】
                    if (stop && CollUtil.isEmpty(bufferRowInfos)) {
                        executorService.shutdown();
                        break;
                    }
                } catch (Exception e) {
                    log.error("DelayProcess#initConsumeBlockingQueue failed:{}", Throwables.getStackTraceAsString(e));
                }
            }
        });
    }

    /**
     * 是否满足处理缓存行信息的条件【数量超限 / 时间超限】
     * @return 是否满足处理条件
     */
    private boolean isCanProcessBufferRowInfos() {
        return bufferRowInfos.size() >= delayProcessParam.getNumberThreshold() ||
                (System.currentTimeMillis() - lastProcessTime >= delayProcessParam.getTimeThreshold());
    }

    /**
     * 将元素【单行信息】放入阻塞队列中
     * @param element 单行信息
     */
    public void put(T element) {
        try {
            delayProcessParam.getQueue().put(element);
        } catch (InterruptedException e) {
            log.error("DelayProcess#put error:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 消费阻塞队列中元素【单行信息组成缓存行信息】
     * @param elementList 单行信息组成缓存行信息
     */
    public void consume(List<T> elementList) {
        if (elementList.isEmpty()) {
            return;
        }
        try {
            realConsume(elementList);
        } catch (Exception e) {
            log.error("DelayProcess#consume failed:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 真正消费阻塞队列中元素【单行信息组成缓存行信息】
     * 【由具体子类实现】
     * @param elementList 单行信息组成缓存行信息
     */
    public abstract void realConsume(List<T> elementList);
}
