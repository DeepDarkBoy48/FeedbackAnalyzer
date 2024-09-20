package com.chenyangxu.FeedbackAnalyzer.pojo.entity;

import lombok.Data;

@Data
public class CourseItem {
    private Integer id;
    private Integer courseId;
    private String title;
    private String prompt;
    private String chart;
    private Integer positive;
    private Integer somewhatpositive;
    private Integer neutral;
    private Integer somewhatnegative;
    private Integer negative;
    private Integer good;
    private Integer bad;
    private Integer normal;
    private String airesult;
    private String keyword;
}
