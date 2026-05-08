package com.pasan.websocket.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pasan.util.HttpClientUtil;
import com.pasan.util.JwtUtil;
import com.pasan.util.SpringContextUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/chat/{inviteId}")
@Slf4j
public class ChatWebSocketServer {

    private static final Map<Long, Set<Session>> roomMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("inviteId") Long inviteId) {
        try {
            JwtUtil jwtUtil = SpringContextUtil.getBean(JwtUtil.class);
            String query = session.getQueryString();
            String token = null;
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && "token".equals(kv[0])) {
                        token = kv[1];
                        break;
                    }
                }
            }
            if (token == null) {
                session.close();
                return;
            }
            Claims claims = jwtUtil.parsePayload(token);
            Long userId = Long.valueOf(claims.get("userId").toString());

            session.getUserProperties().put("userId", userId);
            session.getUserProperties().put("inviteId", inviteId);
            sessionUserMap.put(session.getId(), userId);

            roomMap.computeIfAbsent(inviteId, k -> ConcurrentHashMap.newKeySet()).add(session);

            log.info("用户 {} 加入邀约 {} 的聊天室", userId, inviteId);
        } catch (Exception e) {
            log.error("WebSocket 连接失败", e);
            try { session.close(); } catch (Exception ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Long inviteId = (Long) session.getUserProperties().get("inviteId");
        Long userId = (Long) session.getUserProperties().get("userId");

        JSONObject msg;
        try {
            msg = JSON.parseObject(message);
        } catch (Exception e) {
            log.warn("收到非法JSON消息: {}", message);
            sendError(session, "消息格式错误，请发送JSON");
            return;
        }

        String type = msg.getString("type");
        if (type == null || inviteId == null || userId == null) return;

        try {
            switch (type) {
                case "TEXT" -> {
                    String content = msg.getString("content");
                    String senderName = msg.getString("senderName");
                    if (content == null || content.isEmpty()) return;

                    // 持久化到 lb-invitation
                    Map<String, String> params = new HashMap<>();
                    params.put("inviteId", inviteId.toString());
                    params.put("senderId", userId.toString());
                    params.put("senderName", senderName);
                    params.put("type", "0");
                    params.put("content", content);
                    try {
                        HttpClientUtil.doPost4Json("http://localhost:8085/chat/message", params);
                    } catch (Exception e) {
                        log.error("消息持久化失败", e);
                    }

                    // 广播给房间内所有人（id转字符串防JS精度丢失）
                    JSONObject broadcast = new JSONObject();
                    broadcast.put("type", "TEXT");
                    broadcast.put("inviteId", inviteId.toString());
                    broadcast.put("senderId", userId.toString());
                    broadcast.put("senderName", senderName);
                    broadcast.put("content", content);
                    broadcast.put("createTime", LocalDateTime.now().toString());
                    log.info("广播消息到房间 {}: {}", inviteId, content);
                    sendToRoom(inviteId, broadcast.toJSONString());
                }
                case "SYSTEM" -> {
                    JSONObject sys = new JSONObject();
                    sys.put("type", "SYSTEM");
                    sys.put("inviteId", inviteId.toString());
                    sys.put("content", msg.getString("content"));
                    sendToRoom(inviteId, sys.toJSONString());
                }
                case "EXTEND_REQUEST" -> {
                    JSONObject sys = new JSONObject();
                    sys.put("type", "EXTEND_REQUEST");
                    sys.put("inviteId", inviteId.toString());
                    sys.put("senderId", userId.toString());
                    sys.put("content", "用户 " + userId + " 申请延长邀约时间");
                    sendToRoom(inviteId, sys.toJSONString());
                }
                default -> log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("消息处理失败", e);
            sendError(session, "消息处理失败");
        }
    }

    private void sendError(Session session, String msg) {
        try {
            if (session.isOpen()) {
                JSONObject err = new JSONObject();
                err.put("type", "ERROR");
                err.put("content", msg);
                session.getBasicRemote().sendText(err.toJSONString());
            }
        } catch (Exception ignored) {}
    }

    @OnClose
    public void onClose(Session session) {
        Long inviteId = (Long) session.getUserProperties().get("inviteId");
        Long userId = (Long) session.getUserProperties().get("userId");
        if (inviteId != null) {
            Set<Session> room = roomMap.get(inviteId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    roomMap.remove(inviteId);
                }
            }
        }
        sessionUserMap.remove(session.getId());
        log.info("用户 {} 断开连接", userId);
    }

    private void sendToRoom(Long inviteId, String message) {
        Set<Session> room = roomMap.get(inviteId);
        if (room == null) return;
        for (Session s : room) {
            try {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
        }
    }
}
