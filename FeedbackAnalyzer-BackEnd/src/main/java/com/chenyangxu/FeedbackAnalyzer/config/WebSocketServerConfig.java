package com.chenyangxu.FeedbackAnalyzer.config;

import com.chenyangxu.FeedbackAnalyzer.mapper.UserMapper;
import com.chenyangxu.FeedbackAnalyzer.service.MyAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServerConfig {

    private static MyAiService myAiService;
    private static UserMapper userMapper;

    @Autowired
    public void setMyAiService(MyAiService myAiService, UserMapper userMapper) {
        WebSocketServerConfig.myAiService = myAiService;
        WebSocketServerConfig.userMapper = userMapper;
    }
    public static MyAiService getMyAiService() {
        return myAiService;
    }
    public static UserMapper getUserMapper() {
        return userMapper;
    }
}
