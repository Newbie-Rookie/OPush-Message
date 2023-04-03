package com.lin.opush.dao;

import com.lin.opush.domain.ChannelAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 渠道账号 Dao
 */
public interface ChannelAccountDao extends JpaRepository<ChannelAccount, Long> {
    /**
     * 根据渠道账号名获取渠道账号【未删除】
     * @param name 渠道账号名
     * @return 渠道账号
     */
    ChannelAccount findByNameEqualsAndIsDeletedEquals(String name, Integer isDeleted);

    /**
     * 根据发送渠道类型标识获取渠道账号【未删除】
     * @param sendChannel 发送渠道（枚举值）
     * @return 渠道账号列表
     */
    List<ChannelAccount> findAllBySendChannelEqualsAndIsDeletedEquals(Integer sendChannel, Integer isDeleted);

    /**
     * 查询未删除且由个人提供的渠道账号列表【分页】
     * @param creator 创建者
     * @return
     */
    List<ChannelAccount> findAllByCreatorEqualsAndIsDeletedEquals(String creator, Integer isDeleted);
}
