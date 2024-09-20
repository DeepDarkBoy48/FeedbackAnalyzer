package com.chenyangxu.FeedbackAnalyzer.service;

import com.chenyangxu.FeedbackAnalyzer.pojo.NlpData;
import com.chenyangxu.FeedbackAnalyzer.pojo.dto.CourseFormDto;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.CourseItem;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.ScoreVo;

import java.util.List;
import java.util.Map;

public interface AnalyzeService {
    /**
     * 新增信息表
     * @param courseFormDto
     */
    Integer addAnalyzeForm(CourseFormDto courseFormDto);


    /**
     * 根据课程id获取课程项目
     *
     * @param couseId
     * @return
     */
    List<CourseItem> getCourseItemByCourseId(Integer couseId);

    /**
     * 插入情感反馈
     * @param sentimentVoMap
     * @param courseItems
     */
    void insertSentimentFeedback(Map<String, NlpData> sentimentVoMap, List<CourseItem> courseItems);


    /**
     * 插入分数比例
     * @param scoreRatioMap
     * @param couseId
     */
    void insertScoreRatio(Map<String, ScoreVo> scoreRatioMap, Integer couseId);
}
