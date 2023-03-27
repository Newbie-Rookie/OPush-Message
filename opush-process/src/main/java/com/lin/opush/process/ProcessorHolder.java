package com.lin.opush.process;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储不同发送渠道和处理不同发送渠道处理器的映射关系
 */
@Component
public class ProcessorHolder {
    /**
     * 存储处理不同发送渠道的处理器
     */
    private Map<Integer, Processor> processors = new HashMap(128);

    public void putProcessor(Integer channelTypeCode, Processor processor) {
        processors.put(channelTypeCode, processor);
    }

    public Processor route(Integer channelTypeCode) {
        return processors.get(channelTypeCode);
    }
}
