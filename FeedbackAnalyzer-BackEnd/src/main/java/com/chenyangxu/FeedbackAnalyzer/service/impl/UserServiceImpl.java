package com.chenyangxu.FeedbackAnalyzer.service.impl;

import com.chenyangxu.FeedbackAnalyzer.mapper.UserMapper;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.User;
import com.chenyangxu.FeedbackAnalyzer.service.UserService;
import com.chenyangxu.FeedbackAnalyzer.utils.CommonUtils;
import com.chenyangxu.FeedbackAnalyzer.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Override
    public User findByUserName(String username) {
        User u = userMapper.getByUserName(username);
        return u;
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     * @param role
     */
    @Override
    public void register(String username, String password, Integer role) {
        //加密 md5
        String md5String = Md5Util.getMD5String(password);
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime updateTime = LocalDateTime.now();
        //添加
        userMapper.add(username,md5String,createTime,updateTime,role);
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 更新用户头像
     * @param avatarUrl
     * @return
     */
    @Override
    public void updateAvatar(String avatarUrl) {
        Integer id = CommonUtils.getId();
        userMapper.updateAvatar(avatarUrl,id);
    }

    /**
     * 更换密码
     * @return
     */
    @Override
    public void updatePwd(String newPwd) {
        Integer id = CommonUtils.getId();
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }
}
