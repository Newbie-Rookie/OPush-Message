package com.lin.opush.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 渠道账号列表请求参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelAccountParam {
    /**
     * 当前页码（>0）
     */
    @NotNull
    private Integer page = 1;
    /**
     * 当前页大小（>0, 5、10、20、50、100）
     */
    @NotNull
    private Integer perPage = 10;
    /**
     * 发送渠道检索
     */
    private String sendChannel;
    /**
     * 当前登录用户【创建者】
     */
    private String creator;
}
