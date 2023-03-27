package com.lin.opush.domain;

import lombok.*;

import javax.persistence.*;

/**
 * 渠道账号信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tb_channel_account")
@ToString
public class ChannelAccount {
    /**
     * 渠道账号ID（自增主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 渠道账号名
     */
    private String name;

    /**
     * 发送渠道（枚举值）
     * 枚举值：com.lin.opush.common.com.lin.opush.enums.ChannelType
     */
    private Integer sendChannel;

    /**
     * 渠道账号配置（JSON格式）
     */
    private String accountConfig;

    /**
     * 渠道账号拥有者（默认opush）
     */
    private String creator;
}
