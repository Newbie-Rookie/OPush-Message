package com.lin.opush.constants;

/**
 * Redis常量
 */
public class RedisConstant {
    // 登录、注册
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_CODE_VERIFY_KEY = "login:code:verify:";
    public static final Long LOGIN_CODE_VERIFY_TTL = 2L;
    public static final Long LOGIN_CODE_TIMESOUT_TTL = 0L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final Long LOGIN_USER_TIMESOUT_TTL = 0L;

}
