package com.chenyangxu.FeedbackAnalyzer.pojo;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
public class sentiment {
    @Description("Positive, Negative, Neutral")
    private String sentimentType;
    private String ReasonForSentiment;
}
