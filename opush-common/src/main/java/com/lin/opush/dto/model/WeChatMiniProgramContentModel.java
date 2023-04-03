package com.lin.opush.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 微信小程序订阅信息内容模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeChatMiniProgramContentModel extends ContentModel {
    /**
     * 模板Id
     */
    private String templateId;
    /**
     * 模板消息发送的数据
     */
    Map<String, String> miniProgramParam;
    /**
     * 跳转链接
     */
    private String page;
}
