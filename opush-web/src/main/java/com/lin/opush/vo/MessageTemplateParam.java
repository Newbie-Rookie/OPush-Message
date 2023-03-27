package com.lin.opush.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

/**
 * 消息模板列表请求参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTemplateParam {
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
     * 模板id
     */
    private Long id;

    /**
     * 当前登录用户【下发者】
     */
    private String creator;

    /**
     * 模版名检索
     */
    private String name;

    /**
     * 模板类型检索
     */
    private String templateType;

    /**
     * 发送渠道检索
     */
    private String sendChannel;

    /**
     * 消息类型检索
     */
    private String msgType;

    /**
     * 消息接收者【多个用逗号隔开】
     * 测试发送时使用
     */
    private String receiver;

    /**
     * 下发参数信息
     */
    private String msgContent;
}
