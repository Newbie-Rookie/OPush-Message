package com.lin.opush.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.alibaba.fastjson.JSONArray;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.constants.OpushConstant;
import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.service.ChannelAccountService;
import com.lin.opush.service.ConfigService;
import com.lin.opush.vo.ChannelAccountParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 渠道账号管理服务接口实现
 */
@Service
public class ChannelAccountServiceImpl implements ChannelAccountService {
    /**
     * 渠道账号 Dao
     */
    @Autowired
    private ChannelAccountDao channelAccountDao;

    /**
     * 配置服务
     */
    @Autowired
    private ConfigService config;

    /**
     * 加解密key
     */
    private static final String ENCRYPT_AND_DECRYPT_KEY = "encryptAndDecryptKey";

    /**
     * 根据渠道标识获取渠道账号列表【未删除】
     * 目前短信、邮件渠道账号由官方【OPush】提供，微信小程序等其他渠道账号由个人【creator】提供
     * @param channelType 渠道类型
     * @param creator 创建者
     * @return
     */
    @Override
    public List<ChannelAccount> queryByChannelType(Integer channelType, String creator) {
        if (ChannelType.SMS.getCode().equals(channelType) || ChannelType.EMAIL.getCode().equals(channelType)) {
            return channelAccountDao.findAllBySendChannelEqualsAndCreatorEquals(
                                        channelType, OpushConstant.DEFAULT_CREATOR);
        }
        return channelAccountDao.findAllBySendChannelEqualsAndCreatorEquals(channelType, creator);
    }

    /**
     * 查找渠道账号id对应渠道账号
     * @param id 渠道账号id
     * @return 渠道账号
     */
    @Override
    public ChannelAccount queryById(Long id) {
        // 渠道账号不存在则返回null
        ChannelAccount account = channelAccountDao.findById(id).orElse(null);
        if (!Objects.isNull(account)) {
            byte[] key = JSONArray.parseObject(config.getProperty(ENCRYPT_AND_DECRYPT_KEY,
                    CommonConstant.EMPTY_VALUE_JSON_ARRAY), byte[].class);
            SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DESede, key);
            account.setAccountConfig(des.decryptStr(account.getAccountConfig()));
        }
        return account;
    }

    /**
     * 查询由个人提供的渠道账号列表【分页】
     * @param param 渠道账号列表请求参数
     * @return 分页渠道账号列表
     */
    @Override
    public Page<ChannelAccount> queryList(ChannelAccountParam param) {
        // 判断页码和是否合理（page > 0, perPage >= 1）并生成分页请求对象
        PageRequest pageRequest = PageRequest.of(param.getPage() - 1, param.getPerPage());
        // 分页查询
        // root: 代表查询的根对象，即MessageTemplate（消息模板）
        // criteriaQuery: 顶层查询对象，sql语句关键字，用于自定义查询方式（基本不用）
        // criteriaBuilder: 查询构造器，封装很多的查询条件
        Page<ChannelAccount> channelAccounts = channelAccountDao
                .findAll((Specification<ChannelAccount>) (root, criteriaQuery, criteriaBuilder) -> {
            // 搜索条件列表
            List<Predicate> predicateList = new ArrayList<>();
            // 添加搜索条件（发送渠道、创建者）
            if (StrUtil.isNotBlank(param.getSendChannel())) {
                predicateList.add(criteriaBuilder.equal(root.get("sendChannel").as(String.class), param.getSendChannel()));
            }
            predicateList.add(criteriaBuilder.equal(root.get("creator").as(String.class), param.getCreator()));
            Predicate[] predicate = new Predicate[predicateList.size()];
            // 执行分页查询
            criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(predicate)));
            // 查询内容按更新时间排序
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("updated")));
            // 返回与where子句限制相对应的predicates，如果未指定限制，则返回null
            return criteriaQuery.getRestriction();
        }, pageRequest);
        // 将账号配置信息解密【aes】
        byte[] key = JSONArray.parseObject(config.getProperty(ENCRYPT_AND_DECRYPT_KEY,
                CommonConstant.EMPTY_VALUE_JSON_ARRAY), byte[].class);
        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DESede, key);
        for (ChannelAccount channelAccount : channelAccounts) {
            channelAccount.setAccountConfig(des.decryptStr(channelAccount.getAccountConfig()));
        }
        return channelAccounts;
    }

    /**
     * 保存/修改渠道账号信息
     * @param channelAccount 渠道账号
     * @return
     */
    @Override
    public ChannelAccount saveOrUpdate(ChannelAccount channelAccount) {
        // 判断渠道账号是否已存在
        if (Objects.isNull(channelAccount.getId())) {
            channelAccount.setCreated(Math.toIntExact(DateUtil.currentSeconds()));
        }
        channelAccount.setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        // 账号配置进行DES加密
        byte[] key = JSONArray.parseObject(config.getProperty(ENCRYPT_AND_DECRYPT_KEY,
                                CommonConstant.EMPTY_VALUE_JSON_ARRAY), byte[].class);
        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DESede, key);
        channelAccount.setAccountConfig(des.encryptHex(channelAccount.getAccountConfig()));
        return channelAccountDao.save(channelAccount);
    }

    /**
     * 根据Id删除单个/批量渠道账号【多个id中间逗号隔开】
     * @param ids 单条/批量删除【删除消息模板对应id列表】
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        channelAccountDao.deleteAllById(ids);
    }
}
