package com.lin.opush.dao;

import com.lin.opush.domain.ChannelAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 渠道账号 Dao
 */
public interface ChannelAccountDao extends JpaRepository<ChannelAccount, Long> {

    /**
     * 根据渠道账号名获取渠道账号
     * @param name 渠道账号名
     * @return 渠道账号
     */
    ChannelAccount findByNameEquals(String name);

    /**
     * 根据发送渠道类型标识获取渠道账号
     * @param sendChannel 发送渠道（枚举值）
     * @return 渠道账号列表
     */
    List<ChannelAccount> findAllBySendChannelEquals(Integer sendChannel);
}
