package com.lin.opush.constants;

/**
 * Opush常量信息
 */
public class OpushConstant {
    /**
     * businessId默认的长度
     * 生成的逻辑：com.lin.opush.support.utils.TaskInfoUtils#
     *              generateBusinessId(java.lang.Long, java.lang.Integer)
     */
    public final static Integer BUSINESS_ID_LENGTH = 16;

    /**
     * 接口限制最多的人数
     */
    public static final Integer BATCH_RECEIVER_SIZE = 100;

    /**
     * 消息发送给全部人的标识
     * (企业微信 应用消息)
     * (钉钉自定义机器人)
     * (钉钉工作消息)
     */
    public static final String SEND_ALL = "@all";

    /**
     * 默认常量（若新建消息模板时，没传入则用以下常量）
     * 目前项目toC，后续可能会开发为toB，即由企业自己部署并添加渠道账号
     */
    // 默认创建者和更新者
    public static final String DEFAULT_CREATOR = "OPush";
    public static final String DEFAULT_UPDATOR = "OPush";
    // 默认业务方
    public static final String DEFAULT_TEAM = "OPush公众号";
    // 默认审核者
    public static final String DEFAULT_AUDITOR = "OPush";
}
