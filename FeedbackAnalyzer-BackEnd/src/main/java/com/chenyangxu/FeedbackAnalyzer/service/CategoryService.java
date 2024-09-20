package com.chenyangxu.FeedbackAnalyzer.service;

import com.itheima.bigevent.pojo.entity.Category;

import java.util.List;

public interface CategoryService {
    //新增分类
    void add(Category category);

    //查询所有分类
    List<Category> list();

    //根据id查询分类信息
    Category findById(Integer id);

    //更新分类
    void update(Category category);


    //删除分类
    void delete(Integer id);
}
