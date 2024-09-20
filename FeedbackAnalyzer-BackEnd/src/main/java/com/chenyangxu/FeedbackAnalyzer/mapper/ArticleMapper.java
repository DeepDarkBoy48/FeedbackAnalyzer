package com.chenyangxu.FeedbackAnalyzer.mapper;


import com.itheima.bigevent.pojo.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface ArticleMapper {

    //新增
    @Insert("insert into article(title,content,cover_img,state,category_id,create_user,create_time,update_time) " +
            "values(#{title},#{content},#{coverImg},#{state},#{categoryId},#{createUser},#{createTime},#{updateTime})")
    void add(Article article);

    //分页查询
    List<Article> list(Integer userId, Integer categoryId, String state);

    //获取文章详情
    @Select("select * from article where id = #{id};")
    Article findById(Integer id);

    //更新文章
    @Update("update article set title = #{title}, content = #{content}, cover_img = #{coverImg}, state = #{state}, category_id = #{categoryId}, update_time = #{updateTime} where id = #{id};")
    void update(Article article);

    //删除文章
    @Delete("delete from article where id = #{id};")
    void delete(Integer id);

    //根据categoryId查询文章
    @Select("select * from article where category_id = #{id};")
    ArrayList<Article> getByCategoryId(Integer id);
}
