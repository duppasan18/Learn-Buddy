package com.pasan.websocket.server;

import com.pasan.util.JwtUtil;
import com.pasan.util.SpringContextUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ServerEndpoint("/ws") // 前端 /ws?token=xx
@Slf4j
public class WebSocketServer {

    private JwtUtil jwtUtil;

    //存放会话对象
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("用户开始连接");
        this.jwtUtil = SpringContextUtil.getBean(JwtUtil.class);
        try {
            // 获取token
            String query = session.getQueryString();
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(a -> a[0], a -> a[1]));
            String token = params.get("token");

            // 解析token
            Claims claims = jwtUtil.parsePayload(token);
            String userId = claims.get("userId").toString();

            // 防止重复登录，以新会话覆盖旧会话
            if (sessionMap.containsKey(userId)) {
                Session oldSession = sessionMap.get(userId);
                try {
                    oldSession.close();
                }
                catch (Exception ignored){}
            }

            // 绑定用户
            session.getUserProperties().put("userId", userId);
            sessionMap.put(userId, session);
            log.info("用户 {} 连接成功:", userId);
        } catch (Exception e){
            try {
                session.close(); // 关闭会话
                log.error("用户连接失败:{}", String.valueOf(e));
            }catch (Exception ignored){

            }
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        System.out.println("收到来自客户端：" + userId + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        System.out.println("连接断开:" + userId);
        sessionMap.remove(userId);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息  
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
