package com.chenyangxu.FeedbackAnalyzer.service;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Course;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.CourseItem;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.PageBean;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.feedback;

import java.util.List;

public interface FeedbackResultService {

    //分页查询course
    PageBean<Course> list(Integer pageNum, Integer pageSize, String keyword);


    //删除course
    void delete(Integer id);

    //查询course item
    List<CourseItem> getcourseItem(Integer courseId);

    //查询所有couresItem的feedbacks
    List<feedback> getcourseItemFeedback(List<Integer> courseItemId);

    List<feedback> getcourseItemFeedbackBySentiment(List<Integer> courseItemId, List<String> sentiment);
}
