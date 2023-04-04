package com.lin.opush.service;

import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.vo.ChannelAccountParam;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 渠道账号管理接口
 */
public interface ChannelAccountService {
    /**
     * 根据渠道标识获取渠道账号列表
     * 目前短信、邮件渠道账号由官方【OPush】提供，微信小程序等其他渠道账号由个人【creator】提供
     * @param channelType 渠道类型
     * @param creator 创建者
     * @return 渠道账号列表
     */
    List<ChannelAccount> queryByChannelType(Integer channelType, String creator);

    /**
     * 根据渠道账号id查询渠道账号信息
     * @param id 渠道账号id
     * @return 渠道账号
     */
    ChannelAccount queryById(Long id);

    /**
     * 查询由个人提供的渠道账号列表【分页】
     * @param channelAccountParam 渠道账号列表请求参数
     * @return 分页渠道账号列表
     */
    Page<ChannelAccount> queryList(ChannelAccountParam channelAccountParam);

    /**
     * 保存/修改渠道账号信息
     * @param channelAccount 渠道账号
     * @return
     */
    ChannelAccount saveOrUpdate(ChannelAccount channelAccount);

    /**
     * 根据Id删除单个/批量渠道账号【多个id中间逗号隔开】
     * @param ids 渠道账号id列表
     */
    void deleteByIds(List<Long> ids);
}
