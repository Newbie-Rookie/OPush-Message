package com.lin.opush.service;

import com.lin.opush.domain.ChannelAccount;

import java.util.List;

/**
 * 渠道账号管理接口
 */
public interface ChannelAccountService {

    ChannelAccount queryByName(String name);

    List<ChannelAccount> queryByChannelType(Integer channelType);
}
