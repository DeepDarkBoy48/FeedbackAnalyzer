package com.chenyangxu.FeedbackAnalyzer.pojo.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Course {
    @NotNull
    private Integer id;
    private String title;
    private String prompt;
    private Integer userId;
    private LocalDateTime createTime;
    private String airesult;
}
