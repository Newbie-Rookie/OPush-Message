package com.lin.opush.controller;

import cn.hutool.core.util.StrUtil;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.service.ChannelAccountService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.amis.CommonAmisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 渠道账号控制器
 */
@RestController
@RequestMapping("/account")
public class ChannelAccountController {
    @Autowired
    private ChannelAccountService channelAccountService;

    /**
     * 根据渠道标识获取渠道账号列表
     * 目前短信、邮件渠道账号由官方提供，微信小程序等其他渠道账号由个人提供
     * @param channelType 渠道类型
     * @return
     */
    @GetMapping("/queryByChannelType/{channelType}")
    public List<CommonAmisVo> query(@PathVariable("channelType") Integer channelType) {
        List<ChannelAccount> channelAccounts = channelAccountService.queryByChannelType(channelType);
        return Convert4Amis.getChannelAccountVo(channelAccounts, channelType);
    }

    /**
     * 根据渠道账号id查询渠道账号信息
     * @param id 渠道账号id
     * @return
     */
    @GetMapping("/queryById/{id}")
    public Map<String, Object> query(@PathVariable("id") Long id) {
        return Convert4Amis.flatSingleMap(channelAccountService.queryById(id));
    }

    /**
     * 若渠道账号Id存在，则修改，否则保存
     * @param channelAccount 渠道账号
     * @return 渠道账号
     */
    @PostMapping("/save")
    public ChannelAccount saveOrUpdate(@RequestBody ChannelAccount channelAccount) {
        return channelAccountService.save(channelAccount);
    }

    /**
     * 获取个人创建的渠道账号列表
     * @param creator 创建者
     * @return
     */
    @GetMapping("/list")
    public List<ChannelAccount> list(String creator) {
        return channelAccountService.list(creator);
    }

    /**
     * 根据Id删除单个/批量渠道账号【多个id中间逗号隔开】
     * @param id 渠道账号id
     */
    @DeleteMapping("/delete/{id}")
    public void deleteByIds(@PathVariable("id") String id) {
        if (StrUtil.isNotBlank(id)) {
            List<Long> idList = Arrays.stream(id.split(StrUtil.COMMA)).map(Long::valueOf).collect(Collectors.toList());
            channelAccountService.deleteByIds(idList);
        }
    }

//    /**
//     * 根据渠道标识查询渠道账号相关的信息
//     * 后续可能会推出toB版本，由用户添加自己渠道账号
//     */
//    @GetMapping("/query")
//    public List<CommonAmisVo> query(Integer channelType, String creator) {
//        creator = StrUtil.isBlank(creator) ? OpushConstant.DEFAULT_CREATOR : creator;
//        List<ChannelAccount> channelAccounts = channelAccountService.queryByChannelType(channelType, creator);
//        return Convert4Amis.getChannelAccountVo(channelAccounts, channelType);
//    }
}
