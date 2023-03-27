package com.lin.opush.dao;

import com.lin.opush.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户 Dao
 */
public interface UserDao extends JpaRepository<User, Long> {
    /**
     * 根据手机号获取用户
     * @param Phone 手机号
     * @return
     */
    User findUserByPhoneEquals(String Phone);
}
