package com.chenyangxu.FeedbackAnalyzer.service;

import com.itheima.bigevent.pojo.entity.Article;
import com.itheima.bigevent.pojo.entity.PageBean;

import java.util.ArrayList;

public interface ArticleService {
    //新增文章
    void add(Article article);

    //分页查询
    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);

    //获取文章详情
    Article findById(Integer id);
    //更新文章
    void update(Article article);

    //删除文章
    void delete(Integer id);

    //根据categoryId查询文章
    ArrayList<Article> getByCategoryId(Integer id);
}
