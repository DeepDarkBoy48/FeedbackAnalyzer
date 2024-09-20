package com.chenyangxu.FeedbackAnalyzer.controller;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.*;
import com.chenyangxu.FeedbackAnalyzer.service.FeedbackResultService;
import com.chenyangxu.FeedbackAnalyzer.service.MyAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/feedBack")
public class FeedbackResultController {

    @Autowired
    private FeedbackResultService feedbackResultService;
    @Autowired
    private MyAiService myAiService;

    //分页查询course
    @GetMapping("/course")
    public Result<PageBean<Course>> list(Integer pageNum, Integer pageSize,
                                          @RequestParam(required = false) String keyword){
        PageBean<Course> pd = feedbackResultService.list(pageNum,pageSize,keyword);
        return Result.success(pd);
    }

    //删除course
    @DeleteMapping("/course")
    public Result delete(Integer id){
        feedbackResultService.delete(id);
        return Result.success();
    }

    //查询course item
    @GetMapping("/courseItem")
    public Result<List<CourseItem>> courseitemlist(Integer courseId){
        return Result.success(feedbackResultService.getcourseItem(courseId));
    }

    //查询couresItem的feedbacks
    @GetMapping("/courseItemFeedback")
    public Result<List<feedback>> courseItemFeedbackList(@RequestParam List<Integer> courseItemId){
        List<feedback> feedbacks = feedbackResultService.getcourseItemFeedback(courseItemId);
        return Result.success(feedbacks);
    }


}
