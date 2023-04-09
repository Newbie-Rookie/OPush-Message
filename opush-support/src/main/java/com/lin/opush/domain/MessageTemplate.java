package com.lin.opush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 消息模板（前端传来）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tb_message_template")
// 开启链式访问，即生成setter方法由返回void变为返回this
@Accessors(chain = true)
public class MessageTemplate implements Serializable {
    /**
     * 模板Id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板标题
     */
    private String name;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 工单ID（审核模板走工单）
     */
    private String flowId;

    /**
     * 消息状态
     */
    private Integer msgStatus;

    /**
     * 定时任务Id【由xxl-job-admin返回】
     */
    private Integer timedJobId;

    /**
     * 定时发送的人群的文件路径
     */
    private String timedSendCrowdFilePath;

    /**
     * 接收者id类型
     */
    private Integer idType;

    /**
     * 发送渠道
     */
    private Integer sendChannel;

    /**
     * 模板类型
     */
    private Integer templateType;

    /**
     * 消息类型
     */
    private Integer msgType;

    /**
     * 推送消息的时间
     * 0：未指定cron表达式，即立即执行【自动配置延迟5秒的cron表达式】
     * else：cron表达式
     */
    private String expectPushTime;

    /**
     * 消息内容 {$var} 为占位符
     */
    private String msgContent;

    /**
     * 发送账号（邮件下可有多个发送账号、短信可有多个发送账号..）
     */
    private Long sendAccount;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 更新者
     */
    private String updator;

    /**
     * 审核者
     */
    private String auditor;

    /**
     * 业务方团队
     */
    private String team;

    /**
     * 业务方
     */
    private String proposer;

    /**
     * 创建时间（单位s）
     */
    private Integer created;

    /**
     * 更新时间（单位s）
     */
    private Integer updated;
}
