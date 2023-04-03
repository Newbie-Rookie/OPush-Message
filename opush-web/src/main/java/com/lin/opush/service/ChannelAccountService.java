package com.lin.opush.service;

import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.domain.MessageTemplate;

import java.util.List;

/**
 * 渠道账号管理接口
 */
public interface ChannelAccountService {
    /**
     * 根据渠道标识获取渠道账号列表
     * @param channelType 渠道类型
     * @return 渠道账号列表
     */
    List<ChannelAccount> queryByChannelType(Integer channelType);

    /**
     * 根据渠道账号id查询渠道账号信息
     * @param id 渠道账号id
     * @return 渠道账号
     */
    ChannelAccount queryById(Long id);

    /**
     * 保存/修改渠道账号信息
     * @param channelAccount 渠道账号
     * @return
     */
    ChannelAccount save(ChannelAccount channelAccount);

    /**
     * 查询未删除且由个人提供的渠道账号列表【分页】
     * @param creator 创建者
     * @return 渠道账号列表
     */
    List<ChannelAccount> list(String creator);

    /**
     * 软删除【is_deleted = 1】
     * @param ids 单条/批量删除【删除消息模板对应id列表】
     */
    void deleteByIds(List<Long> ids);
}
