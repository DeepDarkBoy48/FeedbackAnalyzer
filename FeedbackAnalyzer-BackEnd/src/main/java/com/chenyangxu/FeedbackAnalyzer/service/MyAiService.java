package com.chenyangxu.FeedbackAnalyzer.service;

import java.util.List;
import java.util.Map;

public interface MyAiService {
    String sendRequestToOpenAI(String text);

    Map<String,String> AiSentiment(List<String> feedbacks);

    String AiChat(String sid,String courseid, String message);
}
