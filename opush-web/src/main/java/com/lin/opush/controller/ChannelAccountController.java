package com.lin.opush.controller;

import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.service.ChannelAccountService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.amis.CommonAmisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 渠道账号控制器
 */
@RestController
@RequestMapping("/account")
public class ChannelAccountController {
    @Autowired
    private ChannelAccountService channelAccountService;

    /**
     * 根据渠道标识查询渠道账号相关的信息
     * 目前渠道账号均由官方提供
     */
    @GetMapping("/queryByChannelType")
    public List<CommonAmisVo> query(Integer channelType) {
        List<ChannelAccount> channelAccounts = channelAccountService.queryByChannelType(channelType);
        return Convert4Amis.getChannelAccountVo(channelAccounts, channelType);
    }

    /**
     * 根据渠道标识查询渠道账号相关的信息
     * 后续可能会推出toB版本，由用户添加自己渠道账号
     */
//    @GetMapping("/queryByChannelType")
//    public List<CommonAmisVo> query(Integer channelType, String creator) {
//
//        creator = StrUtil.isBlank(creator) ? OpushConstant.DEFAULT_CREATOR : creator;
//
//        List<ChannelAccount> channelAccounts = channelAccountService.queryByChannelType(channelType, creator);
//        return Convert4Amis.getChannelAccountVo(channelAccounts, channelType);
//    }
}
