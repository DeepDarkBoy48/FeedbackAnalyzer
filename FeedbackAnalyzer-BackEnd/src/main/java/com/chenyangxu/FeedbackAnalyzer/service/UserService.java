package com.chenyangxu.FeedbackAnalyzer.service;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.User;

public interface UserService {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    User findByUserName(String username);


    /**
     * 注册
     *
     * @param username
     * @param password
     * @param role
     */
    void register(String username, String password, Integer role);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    void update(User user);

    /**
     * 更新用户头像
     * @param avatarUrl
     * @return
     */
    void updateAvatar(String avatarUrl);

    /**
     * 更换密码
     * @return
     */
    void updatePwd(String newPwd);
}
