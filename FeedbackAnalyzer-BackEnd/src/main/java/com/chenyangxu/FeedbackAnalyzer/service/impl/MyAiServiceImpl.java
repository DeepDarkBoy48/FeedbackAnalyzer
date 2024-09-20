package com.chenyangxu.FeedbackAnalyzer.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chenyangxu.FeedbackAnalyzer.config.aimodel;
import com.chenyangxu.FeedbackAnalyzer.pojo.AiMessageFormat;
import com.chenyangxu.FeedbackAnalyzer.service.MyAiService;
import com.chenyangxu.FeedbackAnalyzer.websocket.WebSocketServer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.Duration.ofSeconds;

@Service
public class MyAiServiceImpl implements MyAiService {
    @Value("${openai.api.url}")
    static private String openaiApiUrl;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private static ChatLanguageModel model;

    @Autowired
    private aimodel.AiAssistant aiAssistant;

    @Override
    public String sendRequestToOpenAI(String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", "gpt-3.5-turbo");  // 设置模型名称
        jsonObject.put("max_tokens", 200);  // 设置 max_tokens

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", msg);
        messages.add(message);
        jsonObject.put("messages", messages);

        HttpResponse response = HttpRequest.post("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .body(jsonObject.toString())
                .execute();

        if (response.isOk()) {
            //取得响应体
            String body = response.body();
            //解析响应体为JSON对象
            JSONObject json = JSONUtil.parseObj(body);
            //取得choices数组
            JSONArray choices = json.getJSONArray("choices");
            //取得第一个元素
            JSONObject choice1 = choices.getJSONObject(0);
            //取得message对象
            JSONObject message1 = choice1.getJSONObject("message");
            //取得content字段
            String content = message1.getStr("content");
//            System.out.println(content);
            return content;
        } else {
            throw new RuntimeException("Failed to get response from OpenAI: " + response.body());
        }
    }
    @Override
    public Map<String, String> AiSentiment(List<String> feedbacks) {
//        webSocketServer.sendToClient("1", "{\"message\": \"xuziAI\"}");
        enum Sentiment {
            POSITIVE, NEUTRAL, NEGATIVE
        }
        interface SentimentAnalyzer {
            // 定义情感分析接口，支持批量处理
            @UserMessage("Analyze sentiment of {{it}}")
            List<Sentiment> analyzeSentimentOfBatch(List<String> texts);
        }
        SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, model);
        // 批量分析反馈的情感
        List<Sentiment> sentiments = sentimentAnalyzer.analyzeSentimentOfBatch(feedbacks);
        // 创建一个 Map 来存储反馈和相应的情感结果
        Map<String, String> feedbackSentiments = new HashMap<>();
        // 将反馈和对应的情感结果存储到 Map 中
        for (int i = 0; i < feedbacks.size(); i++) {
            feedbackSentiments.put(feedbacks.get(i), sentiments.get(i).name());
        }
        // 返回包含反馈及其情感结果的 Map
        return feedbackSentiments;
    }


    @Override
    public String AiChat(String sid, String courseid,String message) {
        AiMessageFormat aiMessageFormat = new AiMessageFormat();
        aiMessageFormat.setMessage(message);
        aiMessageFormat.setCourseid(courseid);
        if (message.startsWith("@")) {
            Pattern pattern = Pattern.compile("@(\\w+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String tool = matcher.group(1);
                aiMessageFormat.setTool(tool);
            } else {
                aiMessageFormat.setTool("");
            }
        }
        String answer = aiAssistant.chat(sid, aiMessageFormat);
        System.out.println(answer);
        return answer;
    }
}
