package com.chenyangxu.FeedbackAnalyzer.pojo;

import lombok.Data;

@Data
public class AiMessageForSummary {
    private String courseid;
    private String tool;
    private String[] summaryItem;
}
