package com.chenyangxu.FeedbackAnalyzer.service.impl;

import com.itheima.bigevent.mapper.CategoryMapper;
import com.itheima.bigevent.pojo.entity.Category;
import com.itheima.bigevent.service.CategoryService;
import com.itheima.bigevent.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    //新增分类
    @Override
    public void add(Category category) {
        //补充属性值
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        Integer userId = CommonUtils.getId();
        category.setCreateUser(userId);
        categoryMapper.add(category);
    }

    //查询所有分类
    @Override
    public List<Category> list() {
        Integer userId = CommonUtils.getId();
        return categoryMapper.list(userId);
    }

    //根据id查询分类信息
    @Override
    public Category findById(Integer id) {
        Category c = categoryMapper.findById(id);
        return c;
    }

    //更新分类
    @Override
    public void update(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    //删除分类
    @Override
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }
}
