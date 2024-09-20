package com.chenyangxu.FeedbackAnalyzer.pojo.vo;

import lombok.Data;

@Data
public class SentimentVo {
    private Integer Negative;
    private Integer SomewhatNegative;
    private Integer Neutral;
    private Integer SomewhatPositive;
    private Integer Positive;
}
