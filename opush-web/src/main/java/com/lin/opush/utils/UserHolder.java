package com.lin.opush.utils;

import com.lin.opush.domain.User;

/**
 * 将各个用户信息保存在ThreadLocal中，不互相干扰
 */
public class UserHolder {
    private static final ThreadLocal<User> tl = new ThreadLocal<>();

    public static void saveUser(User user){
        tl.set(user);
    }

    public static User getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
