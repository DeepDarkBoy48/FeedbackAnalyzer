package com.chenyangxu.FeedbackAnalyzer.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseFormDto {
    private String title;
    private String prompt;
    private List<CourseItemsDto> courseItems;
}
