package com.lin.opush.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lin.opush.dao.UserDao;
import com.lin.opush.domain.User;
import com.lin.opush.domain.sms.LoginUser;
import com.lin.opush.dto.UserDTO;
import com.lin.opush.mq.SendMqService;
import com.lin.opush.service.ChannelAccountService;
import com.lin.opush.service.UserService;
import com.lin.opush.utils.UserHolder;
import com.lin.opush.vo.BasicResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lin.opush.constants.RedisConstant.*;
import static com.lin.opush.constants.RegexConstant.CODE_REGEX;
import static com.lin.opush.constants.RegexConstant.PHONE_REGEX;

/**
 * 用户服务接口实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private ChannelAccountService channelAccountService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SendMqService sendMqService;

    @Value("${opush.business.login.topic.name}")
    private String loginSmsTopic;

    @Override
    public BasicResultVO sendCode(String phone) {
        // 1.校验手机号
        if (StrUtil.isBlank(phone) || !phone.matches(PHONE_REGEX)) {
            // 2.如果不符合，返回错误信息
            return BasicResultVO.fail("手机号格式错误!");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码和验证码验证次数（超过3次验证码失效）到Redis中
        ValueOperations<String, String> opertions = stringRedisTemplate.opsForValue();
        opertions.set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        opertions.set(LOGIN_CODE_VERIFY_KEY + phone, "0", LOGIN_CODE_VERIFY_TTL, TimeUnit.MINUTES);
        // 5.将验证码请求发送给MQ
        String loginUserJson = JSON.toJSONString(LoginUser.builder().phone(phone).code(code).build(),
                                    new SerializerFeature[]{SerializerFeature.WriteClassName});
        // sendMqService.send(loginSmsTopic, loginUserJson);
        // 返回success
        log.info("发送验证码 → " + phone + " : " + code);
        return BasicResultVO.success();
    }

    @Override
    public BasicResultVO login(UserDTO userDTO) {
        // 1.校验手机号和验证码
        String phone = userDTO.getPhone();
        String code = userDTO.getCode();
        if (StrUtil.isBlank(phone) || !phone.matches(PHONE_REGEX)) {
            // 2.如果不符合，返回错误信息
            return BasicResultVO.fail("手机号格式错误!");
        }
        if (StrUtil.isBlank(code) || !code.matches(CODE_REGEX)) {
            // 2.如果不符合，返回错误信息
            return BasicResultVO.fail("验证码格式错误!");
        }
        // 3.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        Integer times = Integer.parseInt(stringRedisTemplate.opsForValue().get(LOGIN_CODE_VERIFY_KEY + phone));
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致，报错
            // 记录手机号验证码验证次数，超过3次则失效
            if(times <= 2){
                stringRedisTemplate.opsForValue().set(LOGIN_CODE_VERIFY_KEY + phone, String.valueOf(++times));
                return BasicResultVO.fail("验证码错误!");
            } else {
                // 验证码失效 → 将验证码和验证次数去除
                stringRedisTemplate.expire(LOGIN_CODE_KEY + phone, LOGIN_CODE_TIMESOUT_TTL, TimeUnit.MINUTES);
                stringRedisTemplate.expire(LOGIN_CODE_VERIFY_KEY + phone, LOGIN_CODE_TIMESOUT_TTL, TimeUnit.MINUTES);
                return BasicResultVO.fail("验证码已失效, 请重新获取!");
            }
        }
        // 4.验证码一致，根据手机号判断用户是否存在
        User user = userDao.findUserByPhoneEquals(phone);
        if (user == null) {
            // 5.用户不存在则创建新用户并保存（注册）
            user = User.builder().phone(phone).build();
            user = userDao.save(user);
        }
        // 6.保存用户信息到redis中
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_VERIFY_KEY + phone, String.valueOf(++times));
        // 6.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 6.2.将User对象转为HashMap存储
        Map<String, Object> userMap = BeanUtil.beanToMap(user, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 6.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 6.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 7.返回token
        userDTO.setToken(token);
        log.info("用户 " + phone + " 登录");
        return BasicResultVO.success(userDTO);
    }

    @Override
    public BasicResultVO logout(String token) {
        // tokenKey
        String tokenKey = LOGIN_USER_KEY + token;
        // 获取phone
        String phone = (String) stringRedisTemplate.opsForHash().get(tokenKey, "phone");
        // 清除redis中的token对应的用户信息
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TIMESOUT_TTL, TimeUnit.MINUTES);
        // 移除ThreadLocal中的用户信息
        UserHolder.removeUser();
        log.info("用户 " + phone + " 登出");
        return BasicResultVO.success();
    }
}
