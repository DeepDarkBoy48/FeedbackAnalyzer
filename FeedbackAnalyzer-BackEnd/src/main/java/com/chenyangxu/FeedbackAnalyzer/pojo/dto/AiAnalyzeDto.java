package com.chenyangxu.FeedbackAnalyzer.pojo.dto;

import lombok.Data;

@Data
public class AiAnalyzeDto {
//    {
//        "articleId": 101,
//            "courseItemId": 1,
//            "prompt": "Please provide your feedback on this course.",
//            "selectedEmotions": ["positive", "neutral"]
//    }

    private Integer articleId;
    private Integer courseItemId;
    private String prompt;
    private String[] selectedEmotions;
}
