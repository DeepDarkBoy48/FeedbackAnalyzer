package com.chenyangxu.FeedbackAnalyzer.pojo.entity;

import lombok.Data;

@Data
public class feedback {
    private Integer id;
    private Integer courseitemid;
    private String feedback;
    private String feedbackType;
}
