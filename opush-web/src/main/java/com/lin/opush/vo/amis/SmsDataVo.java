package com.lin.opush.vo.amis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 短信下发记录对应VO【适配Amis前端】
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 开启链式访问，即生成setter方法由返回void变为返回this
@Accessors(chain = true)
public class SmsDataVo {
    /**
     * items
     */
    private List<ItemsVO> items;

    /**
     * 总条数
     */
    private Long count;

    /**
     * ItemsVO
     */
    @Data
    @Builder
    public static class ItemsVO {
        /**
         * 业务ID【模板Id】
         */
        private String businessId;

        /**
         * 渠道商名
         */
        private String supplierName;

        /**
         * 接收者【手机号】
         */
        private Long phone;

        /**
         * 发送内容
         */
        private String content;

        /**
         * 发送状态
         */
        private String sendType;

        /**
         * 回执状态
         */
        private String receiveType;

        /**
         * 回执报告
         */
        private String receiveContent;

        /**
         * 发送时间
         */
        private String sendTime;

        /**
         * 回执时间
         */
        private String receiveTime;
    }
}
