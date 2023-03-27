package com.lin.opush.domain;

import com.lin.opush.dto.model.ContentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 发送任务信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfo {
    /**
     * 消息模板Id
     */
    private Long messageTemplateId;

    /**
     * 业务Id（数据追踪使用）
     * 生成逻辑：com.lin.opush.utils.TaskInfoUtils#generateBusinessId
     */
    private Long businessId;

    /**
     * 接收者id类型【手机号、邮箱地址...】
     */
    private Integer idType;
    /**
     * 接收者【接收者id类型对应的单个/多个值】
     */
    private Set<String> receiver;

    /**
     * 发送渠道【短信、邮件...】
     */
    private Integer sendChannel;

    /**
     * 模板类型【定时、实时】
     */
    private Integer templateType;

    /**
     * 消息类型【通知类、营销类、验证码】
     */
    private Integer msgType;

    /**
     * 发送文案模型
     * 【不同渠道的所需发送文案不同，所以每个渠道有不同的发送文案模型】
     * 【所有发送文案模型继承ContentMode】
     */
    private ContentModel contentModel;

    /**
     * 渠道账号【标识统一发送渠道下的不同渠道账号】
     * 【邮件、短信有多个渠道账号...】
     */
    private Long sendAccount;

    /**
     * 下发者
     */
    private String creator;
}
