package com.chenyangxu.FeedbackAnalyzer.pojo.FunctionCallingEntity;

import lombok.Data;

import java.util.Map;

@Data
public class FunctionCallingFormat {
    private String ToolName;
    private Map<String, String> data;

    public FunctionCallingFormat(String toolName, Map<String, String> data) {
        ToolName = toolName;
        this.data = data;
    }
}
