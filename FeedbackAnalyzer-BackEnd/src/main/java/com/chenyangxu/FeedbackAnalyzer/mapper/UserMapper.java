package com.chenyangxu.FeedbackAnalyzer.mapper;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    @Select("select * from user where id = #{id};")
    User getByUserId(String id);
    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username};")
    User getByUserName(String username);

    /**
     * 注册
     *
     * @param username
     * @param md5String
     * @param role
     */
    @Insert("insert into user (username,password,create_time,update_time,role) " +
            "values (#{username},#{md5String},#{createTime},#{updateTIme},#{role})")
    void add(String username, String md5String, LocalDateTime createTime, LocalDateTime updateTIme, Integer role);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @Update("update user set nickname = #{nickname}, email=#{email} , update_time = #{updateTime} where id = #{id}")
    void update(User user);

    /**
     * 更新用户头像
     * @param avatarUrl
     * @return
     */
    @Update("update user set user_pic = #{avatarUrl}, update_time = now() where id = #{id}")
    void updateAvatar(String avatarUrl,Integer id);

    /**
     * 更换密码
     * @return
     */
    @Update("update user set password=#{md5String},update_time=now() where id=#{id}")
    void updatePwd(String md5String, Integer id);
}
