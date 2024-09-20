package com.chenyangxu.FeedbackAnalyzer.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.bigevent.mapper.ArticleMapper;
import com.itheima.bigevent.pojo.entity.Article;
import com.itheima.bigevent.pojo.entity.PageBean;
import com.itheima.bigevent.service.ArticleService;
import com.itheima.bigevent.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;

    //新增文章
    @Override
    public void add(Article article) {
        //补充属性值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        Integer userId = CommonUtils.getId();
        article.setCreateUser(userId);
        articleMapper.add(article);
    }

    //分页查询
    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state) {
        //1.创建PageBean对象
        PageBean<Article> pb = new PageBean<>();

        //2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);

        //3.调用mapper
        Integer userId = CommonUtils.getId();
        List<Article> as = articleMapper.list(userId,categoryId,state);
        //Page中提供了方法,可以获取PageHelper分页查询后 得到的总记录条数和当前页数据
        Page<Article> p = (Page<Article>) as;

        //把数据填充到PageBean对象中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }

    //获取文章详情
    @Override
    public Article findById(Integer id) {
        Article article = articleMapper.findById(id);
        return article;
    }


    //更新文章
    @Override
    public void update(Article article) {
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.update(article);
    }

    //删除文章
    @Override
    public void delete(Integer id) {
        articleMapper.delete(id);
    }

    //根据categoryId查询文章
    @Override
    public ArrayList<Article> getByCategoryId(Integer id) {
        ArrayList<Article> articles = articleMapper.getByCategoryId(id);
        return articles;
    }

}
