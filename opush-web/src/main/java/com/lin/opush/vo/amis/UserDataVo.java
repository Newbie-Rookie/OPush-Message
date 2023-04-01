package com.lin.opush.vo.amis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDataVo {
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
         * 业务ID
         */
        private String businessId;
        /**
         * 模板名称
         */
        private String title;
        /**
         * 发送渠道类型
         */
        private String channelType;
        /**
         * 发送细节
         */
        private String detail;
    }
}
