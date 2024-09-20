package com.chenyangxu.FeedbackAnalyzer.websocket;

import cn.hutool.json.JSONObject;
import com.chenyangxu.FeedbackAnalyzer.config.WebSocketServerConfig;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.User;
import com.chenyangxu.FeedbackAnalyzer.service.MyAiService;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * WebSocket服务
 */

@Component
@ServerEndpoint("/chat/{sid}/{CourseId}")

public class WebSocketServer {

    // Store the session of each user
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * English: Method called when a connection is established
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid , @PathParam("CourseId") String CourseId) {
        System.out.println("client:" + sid + " connected");
        System.out.println("itemId: " + CourseId);
        User user = WebSocketServerConfig.getUserMapper().getByUserId(sid);
        String nickname = user.getNickname();
        sessionMap.put(sid, session);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("from", "server");
        jsonResponse.put("message", "<strong>AI:</strong> " + "user <strong>"+nickname+"</strong> connected<br>" +
                "<span>tutorial:</span><br>" +
                "<div>Use the following command to talk to ai</div>" +
                "<span><strong>@keyword</strong> [enter keyword to search relevant feedbacks from the excel file]</span><br>" +
                "<span><strong>@sentiment</strong> [enter a sentence to analyze sentiment]</span><br>" +
                "<span><strong>@summary</strong> [enter single or multiple course module to get ai summary]</span>");
        sendToClient(sid, jsonResponse.toString());
    }
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid, @PathParam("CourseId") String CourseId) {
        System.out.println("receive message from client: " + sid + message);
        // Create a JSON object to store the response message
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("from", "server");
        // Use WebSocketServerConfig to get MyAiService instance
        if (!message.contains("@keyword") && !message.contains("@sentiment") && !message.contains("@summary")) {
            jsonResponse.put("message", "wrong");
        } else {
            //Call AI service
            MyAiService myAiService = WebSocketServerConfig.getMyAiService();
            String s = myAiService.AiChat(sid, CourseId, message);
            jsonResponse.put("message", "<strong>AI:</strong> " + s);
        }
        //Convert the JSON object to a string and send it to the client
        sendToClient(sid, jsonResponse.toString());
    }
    public void sendToClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("user " + sid + " not connected");
        }
    }
    /**
     *  Method called when a connection is closed
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("Connection close:" + sid);
        sessionMap.remove(sid);
    }

    /**
     *  Send a message to a specific user
     * @param sid      ID of the user receiving the message
     * @param message   Message to send
     */


}