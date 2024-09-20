package com.chenyangxu.FeedbackAnalyzer.service.impl;

import com.chenyangxu.FeedbackAnalyzer.mapper.AnalyzeMapper;
import com.chenyangxu.FeedbackAnalyzer.mapper.FeedbackResultMapper;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.feedback;
import com.chenyangxu.FeedbackAnalyzer.service.PublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicServiceImpl implements PublicService {

    @Autowired
    private FeedbackResultMapper feedbackResultMapper;

    @Autowired
    private AnalyzeMapper analyzeMapper;



    @Override
    public List<feedback> getcourseItemFeedback(List<Integer> courseItemId) {
        return feedbackResultMapper.getcourseItemFeedback(courseItemId);
    }

    @Override
    public List<feedback> getcourseItemFeedbackBySentiment(List<Integer> courseItemId, List<String> sentiment) {
        return feedbackResultMapper.getcourseItemFeedbackBySentiment(courseItemId,sentiment);
    }
}
