package com.chenyangxu.FeedbackAnalyzer.service;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.feedback;

import java.util.List;

public interface PublicService {




    List<feedback> getcourseItemFeedback(List<Integer> courseItemId);

    List<feedback> getcourseItemFeedbackBySentiment(List<Integer> courseItemId, List<String> sentiment);
}
