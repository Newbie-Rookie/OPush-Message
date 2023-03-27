package com.lin.opush.service.flowControl.annotations;

import com.lin.opush.enums.FlowControlStrategy;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 单机流量控制注解【标记流量控制策略，便于获取这些流量控制策略具体实现类的bean】
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface LocalFlowControlStrategy {
    FlowControlStrategy flowControlStrategy() default FlowControlStrategy.REQUEST_NUM_RATE_LIMIT;
}
