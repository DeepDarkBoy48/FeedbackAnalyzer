package com.chenyangxu.FeedbackAnalyzer.controller;

import com.itheima.bigevent.pojo.entity.Article;
import com.itheima.bigevent.pojo.entity.Category;
import com.itheima.bigevent.pojo.entity.Result;
import com.itheima.bigevent.service.ArticleService;
import com.itheima.bigevent.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleService articleService;
    //新增分类
    @PostMapping
    public Result add(@RequestBody @Validated(Category.Add.class) Category category){
        categoryService.add(category);
        return Result.success();
    }

    //查询所有分类
    @GetMapping
    public Result<List<Category>> list(){
        List<Category> cs = categoryService.list();
        return Result.success(cs);
    }

    //根据id查询分类信息
    @GetMapping("/detail")
    public Result<Category> detail(Integer id){
        Category c = categoryService.findById(id);
        return Result.success(c);
    }

    //更新分类
    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        categoryService.update(category);
        return Result.success();
    }

    //删除分类
    @DeleteMapping
    public Result delete(Integer id){
        ArrayList<Article> articles = new ArrayList<>();
        articles = articleService.getByCategoryId(id);
        if (articles.size()!=0){
            return Result.error("分类已和文章关联无法删除");
        }
        categoryService.delete(id);
        return Result.success();
    }
}
