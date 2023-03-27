package com.lin.opush.service.flowControl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.enums.FlowControlStrategy;
import com.lin.opush.service.ConfigService;
import com.lin.opush.service.flowControl.annotations.LocalFlowControlStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流量控制工厂【进行流量控制前做一些前置准备和处理】
 *      (1)初始化流量控制策略枚举值和该流量控制策略对应的具体实现类
 *      (2)获取本地配置，对比初始限流值与本地配置限流值，以配置限流值为准
 *      (3)调用流量控制方法进行流量控制
 */
@Service
@Slf4j
public class FlowControlFactory {
    /**
     * 流量控制规则配置key
     */
    private static final String FLOW_CONTROL_KEY = "flowControlRule";

    /**
     * 流量控制各项规则配置前缀
     */
    private static final String FLOW_CONTROL_PREFIX = "flow_control_";

    /**
     * 存储流量控制策略枚举值和该流量控制策略对应的具体实现类
     */
    private final Map<FlowControlStrategy, FlowControlStrategyService> flowControlStrategyServiceMap = new ConcurrentHashMap();

    /**
     * 配置服务
     */
    @Autowired
    private ConfigService config;

    /**
     * 应用上下文
     */
    @Autowired
    private ApplicationContext context;

    /**
     * 初始化flowControlStrategyServiceMap<流量控制策略枚举值, 流量控制策略具体实现类>
     */
    @PostConstruct
    private void init() {
        // 获取@LocalFlowControlStrategy注解标记的类存入map<类名, 对象>
        Map<String, Object> serviceMap = this.context.getBeansWithAnnotation(LocalFlowControlStrategy.class);
        serviceMap.forEach((name, service) -> {
            // 判断是否为流量控制策略服务实现类
            if (service instanceof FlowControlStrategyService) {
                // 获取标记该流量控制策略服务实现类的注解
                LocalFlowControlStrategy localFlowControlStrategy = AopUtils.getTargetClass(service).getAnnotation(LocalFlowControlStrategy.class);
                // 获取该实现类属于哪种流量控制策略
                FlowControlStrategy flowControlStrategy = localFlowControlStrategy.flowControlStrategy();
                // <流量控制策略枚举值, 该流量控制策略对应的具体实现类>
                flowControlStrategyServiceMap.put(flowControlStrategy, (FlowControlStrategyService) service);
            }
        });
    }

    /**
     * 根据发送渠道类型编码值获取对应渠道的流量控制配置（暂不使用远程配置服务Apollo、Nacos，使用本地配置）
     * @param channelTypeCode
     */
    private Double getflowControlConfig(Integer channelTypeCode) {
        String flowControlConfigJSON = config.getProperty(FLOW_CONTROL_KEY, CommonConstant.EMPTY_JSON_OBJECT);
        JSONObject flowControlConfig = JSON.parseObject(flowControlConfigJSON);
        // 判断是否存在该渠道对应的流量控制配置
        if (Objects.isNull(flowControlConfig.getDouble(FLOW_CONTROL_PREFIX + channelTypeCode))) {
            return null;
        }
        return flowControlConfig.getDouble(FLOW_CONTROL_PREFIX + channelTypeCode);
    }

    /**
     * 流量控制前置处理方法
     *      (1)对比初始限流值与本地配置限流值，以配置限流值为准
     *      (2)调用流量控制服务实现
     * @param taskInfo 任务信息
     * @param flowControlParam 流量控制参数
     */
    public void flowControlPreProcess(TaskInfo taskInfo, FlowControlParam flowControlParam) {
        // 流量控制器
        RateLimiter rateLimiter;
        // 流量控制器初始限流大小
        Double rateLimitInitValue = flowControlParam.getRateLimitInitValue();
        // 对比初始限流值与本地配置限流值，以配置限流值为准
        // 获取对应渠道的本地限流配置
        Double flowControlConfigValue = getflowControlConfig(taskInfo.getSendChannel());
        // 判断该渠道的本地限流配置是否存在，存在是否与初始化配置相同【相同则跳过】
        if (Objects.nonNull(flowControlConfigValue) && !rateLimitInitValue.equals(flowControlConfigValue)) {
            flowControlParam.setRateLimitInitValue(flowControlConfigValue);
            rateLimiter = RateLimiter.create(flowControlConfigValue);
            flowControlParam.setRateLimiter(rateLimiter);
        }
        // 根据流量控制策略枚举值获取对应流量控制策略服务具体实现对象
        FlowControlStrategyService flowControlStrategyService = flowControlStrategyServiceMap.get(flowControlParam.getFlowControlStrategy());
        if (Objects.isNull(flowControlStrategyService)) {
            log.error("未找到对应的单机限流策略");
            return;
        }
        // 调用流量控制策略服务具体实现并返回
        double costTime = flowControlStrategyService.flowControl(taskInfo, flowControlParam);
        if (costTime > 0) {
            log.info("consumer {} flow control time {}",
                    ChannelType.getEnumByCode(taskInfo.getSendChannel()).getDescription(), costTime);
        }
    }
}
