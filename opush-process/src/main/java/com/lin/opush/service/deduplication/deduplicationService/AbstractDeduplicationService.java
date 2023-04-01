package com.lin.opush.service.deduplication.deduplicationService;

import cn.hutool.core.collection.CollUtil;
import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.DeduplicationParam;
import com.lin.opush.service.deduplication.ParamAndServiceHolder;
import com.lin.opush.service.deduplication.limit.LimitService;
import com.lin.opush.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * 去重服务抽象类（统一抽象为X时间段内达到了Y阈值）
 * 目前支持：
 *      (1) N分钟相同用户相同内容达到N次去重
 *      (2) 一天内相同用户N次相同渠道频次去重
 */
@Slf4j
public abstract class AbstractDeduplicationService implements DeduplicationService {
    /**
     * 去重类型对应编码值
     */
    protected Integer deduplicationTypeCode;

    /**
     * 去重参数和去重服务持有者
     */
    @Autowired
    private ParamAndServiceHolder paramAndServiceHolder;

    /**
     * 结合任务信息、去重参数和去重服务真正实现去重的限流服务
     */
    protected LimitService limitService;

    /**
     * 将去重服务交由paramAndServiceHolder管理
     */
    @PostConstruct
    private void init() {
        paramAndServiceHolder.putDeduplicationService(deduplicationTypeCode, this);
    }

    /**
     * 日志工具类
     */
    @Autowired
    private LogUtils logUtils;

    /**
     * 去重方法（模板方法模式，该方法为模板方法）
     * @param deduplicationParam 去重参数
     */
    @Override
    public void deduplication(DeduplicationParam deduplicationParam) {
        // 任务信息
        TaskInfo taskInfo = deduplicationParam.getTaskInfo();
        // 去重并获取需要去重的接收者
        Set<String> deduplicationReceiver = limitService.limit(this, taskInfo, deduplicationParam);
        // 剔除符合去重条件的用户
        if (CollUtil.isNotEmpty(deduplicationReceiver)) {
            // 去重用户
            taskInfo.getReceiver().removeAll(deduplicationReceiver);
            // 记录日志（业务id + 哪些用户被去重 + 去重类型）
            logUtils.print(AnchorInfo.builder().businessId(taskInfo.getBusinessId())
                                                            .ids(deduplicationReceiver)
                                                            .state(deduplicationParam.getAnchorState().getCode())
                                                            .creator(taskInfo.getCreator()).build());
        }
    }

    /**
     * 构建单个去重Key
     * 由于不同去重服务所需key不同，因此交由不同去重服务对应子类进行实现
     * @param taskInfo
     * @param receiver
     * @return
     */
    public abstract String buildSingleDeDuplicationKey(TaskInfo taskInfo, String receiver);
}
