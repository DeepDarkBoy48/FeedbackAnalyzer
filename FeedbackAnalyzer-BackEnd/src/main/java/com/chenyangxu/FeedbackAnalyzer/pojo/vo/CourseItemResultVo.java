package com.chenyangxu.FeedbackAnalyzer.pojo.vo;

import lombok.Data;

@Data
public class CourseItemResultVo {
    private String title;
    private ScoreVo scoreVo;
    private SentimentVo sentimentVo;

}
