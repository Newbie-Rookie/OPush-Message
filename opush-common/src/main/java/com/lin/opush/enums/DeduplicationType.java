package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 去重类型枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum DeduplicationType {
    /**
     * 相同内容去重
     */
    CONTENT(10, "N分钟内相同用户收到相同内容去重"),

    /**
     * 渠道接受消息频次去重
     */
    FREQUENCY(20, "一天内相同用户收到N次相同渠道去重"),
    ;

    /**
     * 编码值
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 获取去重类型对应的编码列表
     * @return 去重类型编码列表
     */
    public static List<Integer> getDeduplicationTypeCodeList() {
        List<Integer> deduplicationTypeCodeList = new ArrayList();
        for (DeduplicationType value : DeduplicationType.values()) {
            deduplicationTypeCodeList.add(value.getCode());
        }
        return deduplicationTypeCodeList;
    }
}
