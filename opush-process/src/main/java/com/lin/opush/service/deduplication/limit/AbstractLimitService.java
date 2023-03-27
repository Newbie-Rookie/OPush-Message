package com.lin.opush.service.deduplication.limit;

import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.deduplicationService.AbstractDeduplicationService;

import java.util.ArrayList;
import java.util.List;

/**
 * 真正实现去重的限流服务抽象类
 */
public abstract class AbstractLimitService implements LimitService {
    /**
     * 获取所有接收者的对应去重服务的去重key
     * @param service  去重服务
     * @param taskInfo 任务信息
     * @return 所有去重key
     */
    protected List<String> getAllDeduplicationKey(AbstractDeduplicationService service, TaskInfo taskInfo) {
        List<String> result = new ArrayList(taskInfo.getReceiver().size());
        // 获取所有接收者的对应去重服务的去重key
        for (String receiver : taskInfo.getReceiver()) {
            String key = getSingleDeduplicationKey(service, taskInfo, receiver);
            result.add(key);
        }
        return result;
    }

    /**
     * 获取一个接收者的对应去重服务的去重key
     * @param service 去重服务
     * @param taskInfo 任务信息
     * @param receiver 接收者
     * @return 一个去重key
     */
    protected String getSingleDeduplicationKey(AbstractDeduplicationService service, TaskInfo taskInfo, String receiver) {
        // 构建一个接收者的对应去重服务的去重key
        return service.buildSingleDeDuplicationKey(taskInfo, receiver);
    }
}
