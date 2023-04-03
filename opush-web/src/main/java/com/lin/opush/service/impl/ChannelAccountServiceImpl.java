package com.lin.opush.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.service.ChannelAccountService;
import com.lin.opush.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * 根据渠道标识获取渠道账号列表
     * @param channelType 渠道类型
     * @return
     */
    @Override
    public List<ChannelAccount> queryByChannelType(Integer channelType) {
        return channelAccountDao.findAllBySendChannelEqualsAndIsDeletedEquals(channelType, CommonConstant.FALSE);
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
     * 保存/修改渠道账号信息
     * @param channelAccount 渠道账号
     * @return
     */
    @Override
    public ChannelAccount save(ChannelAccount channelAccount) {
        // 判断渠道账号是否已存在
        if (Objects.isNull(channelAccount.getId())) {
            channelAccount.setCreated(Math.toIntExact(DateUtil.currentSeconds()));
            channelAccount.setIsDeleted(CommonConstant.FALSE);
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
     * 查询未删除且由个人提供的渠道账号列表【分页】
     * @param creator 创建者
     * @return
     */
    @Override
    public List<ChannelAccount> list(String creator) {
        List<ChannelAccount> encodeAccounts = channelAccountDao
                .findAllByCreatorEqualsAndIsDeletedEquals(creator, CommonConstant.FALSE);
        List<ChannelAccount> decodeAccounts = new ArrayList<>(encodeAccounts.size());
        // 将账号配置信息解密【aes】
        byte[] key = JSONArray.parseObject(config.getProperty(ENCRYPT_AND_DECRYPT_KEY,
                                CommonConstant.EMPTY_VALUE_JSON_ARRAY), byte[].class);
        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DESede, key);
        for (ChannelAccount encodeAccount : encodeAccounts) {
            ChannelAccount decodeAccount = ChannelAccount.builder()
                                            .id(encodeAccount.getId())
                                            .name(encodeAccount.getName())
                                            .sendChannel(encodeAccount.getSendChannel())
                                            .accountConfig(des.decryptStr(encodeAccount.getAccountConfig()))
                                            .build();
            decodeAccounts.add(decodeAccount);
        }
        return decodeAccounts;
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
