package com.lin.opush.receiver.kafka;

import com.lin.opush.constants.MessageQueueType;
import com.lin.opush.utils.GroupIdMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 启动消费者
 *      创建多个消费者组（不同渠道的不同消息类型为一个消费者组）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "opush.mq.type", havingValue = MessageQueueType.KAFKA)
public class StartKafkaReceiver {
    /**
     * 应用上下文
     */
    @Autowired
    private ApplicationContext context;

    /**
     * receiver类的消费方法常量：KafkaReceiver.consumer
     */
    private static final String RECEIVER_METHOD_NAME = "KafkaReceiver.consumer";

    /**
     * 获取得到所有的groupId
     */
    private static List<String> groupIds = GroupIdMappingUtils.generateGroupIds();

    /**
     * 下标(用于迭代groupIds位置)
     */
    private static Integer index = 0;

    /**
     * @PostConstruct表示在项目启动时执行该方法（在spring容器初始化时执行该方法）
     * 为不同渠道（短信、邮件...）的不同的消息类型（通知类、营销类、验证码）创建一个Receiver对象
     */
    @PostConstruct
    public void init() {
        for (int i = 0; i < groupIds.size(); i++) {
            context.getBean(KafkaReceiver.class);
        }
    }

    /**
     * 后置处理器(BeanPostProcessor)：
     *      在bean初始化完成之后执行相应的操作
     * 监听器注解后置处理器(KafkaListenerAnnotationBeanPostProcessor)：
     *      负责获取所有bean上使用@KafkaListener注解标记的方法
     * 切面：给每个Receiver对象的consumer方法@KafkaListener赋值相应的groupId
     */
    @Bean
    public static KafkaListenerAnnotationBeanPostProcessor.AnnotationEnhancer groupIdEnhancer() {
        // AnnotationAttributes：存储注解（@KafkaListener）的属性及对应属性值
        //                      （为每个消费者的消费方法上的@KafkaListener中的groupId属性赋值）
        // Element：元素（注解标注的内容 → Recevier.consumer方法）
        return (annotationAttributes, element) -> {
            // 元素是否为方法
            if (element instanceof Method) {
                // 判断是否为Receiver类的consumer方法
                String name = ((Method) element).getDeclaringClass().getSimpleName() + "." + ((Method) element).getName();
                if (RECEIVER_METHOD_NAME.equals(name)) {
                    // 给@KafkaListener中的groupId属性赋值不同渠道的不同消息类型对应的groupId
                    annotationAttributes.put("groupId", groupIds.get(index++));
                }
            }
            return annotationAttributes;
        };
    }
}
