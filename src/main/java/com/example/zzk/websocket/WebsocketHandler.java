package com.example.zzk.websocket;



import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.example.zzk.redis.RedisMessagePublisher;
import com.example.zzk.websocket.dto.SimpleWebSocketMessageDTO;
import com.example.zzk.websocket.dto.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebsocketHandler extends TextWebSocketHandler {

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Value("${server.port:8080}")
    private String serverPort;

    // Key: userId, Value: Map<sessionId, WebSocketSession>
    // 每个实例只管理连接到自己的会话
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String originalUser = (String) session.getAttributes().get("originalUser");
        String instancePort = (String) session.getAttributes().get("instancePort");
        
        log.info("收到来自用户 {} (实例:{}) 的消息: {}", originalUser, instancePort, payload);

        // 示例：简单回显，包含实例信息
        if ("ping".equalsIgnoreCase(payload)) {
            JSONObject response = new JSONObject();
            response.put("type", "pong");
            response.put("fromUser", originalUser);
            response.put("fromInstance", instancePort);
            response.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(response.toJSONString()));
        } else {
            JSONObject response = new JSONObject();
            response.put("type", "echo");
            response.put("message", "收到: " + payload);
            response.put("fromUser", originalUser);
            response.put("fromInstance", instancePort);
            response.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(response.toJSONString()));
        }
    }

    // --- 公共 API：发布消息到 Redis ---

    /**
     * 向所有客户端广播消息
     */
    public void sendMsgToAllClient(WsMsgTypeEnum action, Object data) {
        String dataJson = JSONObject.toJSONString(data, JSONWriter.Feature.NullAsDefaultValue);
        log.info("发布广播消息到Redis: action={}, data={}", action.toString(), dataJson);
        
        SimpleWebSocketMessageDTO message = new SimpleWebSocketMessageDTO(
                action.toString(),
                dataJson,
                false,
                null,
                null,
                "BROADCAST"
        );
        
        // 将SimpleWebSocketMessageDTO转换为原始的WebSocketMessageDTO
        WebSocketMessageDTO legacyMessage = new WebSocketMessageDTO(
                action.toString(),
                dataJson,
                false,
                null,
                null,
                WebSocketMessageDTO.MessageBroadcastType.BROADCAST
        );
        
        log.info("发布到Redis的消息: {}", JSONObject.toJSONString(legacyMessage));
        redisMessagePublisher.publish(legacyMessage);
    }

    /**
     * 广播消息，但排除发起用户自己
     */
    public void sendMsgToAllClientExcludeSelf(WsMsgTypeEnum action, Object data, String sourceUserId) {
        String dataJson = JSONObject.toJSONString(data, JSONWriter.Feature.NullAsDefaultValue);
        log.info("发布排除自己的广播消息到Redis: action={}, sourceUserId={}", action.toString(), sourceUserId);
        
        WebSocketMessageDTO legacyMessage = new WebSocketMessageDTO(
                action.toString(),
                dataJson,
                true,
                sourceUserId,
                null,
                WebSocketMessageDTO.MessageBroadcastType.BROADCAST
        );
        
        redisMessagePublisher.publish(legacyMessage);
    }

    /**
     * 向单个用户的所有会话发送消息
     */
    public void sendMsgToOneUser(WsMsgTypeEnum action, Object data, String targetUserId) {
        String dataJson = JSONObject.toJSONString(data, JSONWriter.Feature.NullAsDefaultValue);
        log.info("发布单用户消息到Redis: action={}, targetUserId={}", action.toString(), targetUserId);
        
        WebSocketMessageDTO legacyMessage = new WebSocketMessageDTO(
                action.toString(),
                dataJson,
                false,
                null,
                targetUserId,
                WebSocketMessageDTO.MessageBroadcastType.SINGLE_USER
        );
        
        redisMessagePublisher.publish(legacyMessage);
    }


    // --- 内部方法：被 Redis 订阅者调用，用于向本实例的客户端发消息 ---
    /**
     * 向连接到【本实例】的所有客户端推送广播消息
     * (由 RedisMessageSubscriber 调用)
     */
    public void sendMsgToLocalClients(String action, String dataJson, boolean excludeSelf, String sourceUserId) {
        log.info("开始向本地客户端广播消息: action={}, excludeSelf={}, sourceUserId={}", action, excludeSelf, sourceUserId);
        log.info("当前实例sessionMap中的用户: {}", sessionMap.keySet());
        
        if (sessionMap.isEmpty()) {
            log.warn("当前实例没有任何WebSocket连接");
            return;
        }

        int sentCount = 0;
        sessionMap.forEach((userId, userSessions) -> {
            log.info("检查用户: {}, 会话数: {}", userId, userSessions.size());
            
            if (excludeSelf && userId.equals(sourceUserId)) {
                log.info("跳过发起者自己: {}", userId);
                return; // 跳过发起者自己的所有会话
            }
            
            userSessions.values().forEach(session -> {
                if (session.isOpen()) {
                    // 获取原始用户ID和实例信息
                    String originalUser = (String) session.getAttributes().get("originalUser");
                    String instancePort = (String) session.getAttributes().get("instancePort");
                    
                    log.info("准备向用户 {} (实例:{}) 发送消息", originalUser, instancePort);
                    
                    TextMessage textMessage = buildTextMessage(action, dataJson, originalUser, instancePort);
                    sendMessage(session, textMessage);
                } else {
                    log.warn("会话已关闭，跳过发送: {}", session.getId());
                }
            });
        });
        
        log.info("广播消息发送完成，实际发送数量: {}", sentCount);
    }

    /**
     * 向连接到【本实例】的单个用户推送消息
     * (由 RedisMessageSubscriber 调用)
     */
    public void sendMsgToLocalUser(String action, String dataJson, String targetUserId) {
        ConcurrentHashMap<String, WebSocketSession> userSessions = sessionMap.get(targetUserId);
        if (userSessions != null && !userSessions.isEmpty()) {
            log.info("Sending local message to user {}, action: {}", targetUserId, action);
            userSessions.values().forEach(session -> {
                // 获取原始用户ID和实例信息
                String originalUser = (String) session.getAttributes().get("originalUser");
                String instancePort = (String) session.getAttributes().get("instancePort");
                
                TextMessage textMessage = buildTextMessage(action, dataJson, originalUser, instancePort);
                sendMessage(session, textMessage);
            });
        }
    }

    // --- 连接管理 ---
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 attributes 获取 user（之前由 HandshakeInterceptor 存入）
        String user = (String) session.getAttributes().get("user");
        String originalUser = (String) session.getAttributes().get("originalUser");
        String instancePort = (String) session.getAttributes().get("instancePort");

        if (user != null) {
            // 将 session 存入本实例的 map 中，按 user 分组
            sessionMap.computeIfAbsent(user, k -> new ConcurrentHashMap<>())
                    .put(session.getId(), session);
            log.info("WebSocket 连接已建立. User: {}, OriginalUser: {}, Instance: {}, SessionId: {}", 
                     user, originalUser, instancePort, session.getId());
            
            // 输出当前所有会话信息用于调试
            log.info("当前实例 {} 的所有会话: {}", instancePort, sessionMap.keySet());
            
            // 发送连接成功消息
            JSONObject welcomeMsg = new JSONObject();
            welcomeMsg.put("type", "welcome");
            welcomeMsg.put("message", "WebSocket连接成功");
            welcomeMsg.put("userId", originalUser);
            welcomeMsg.put("instance", instancePort);
            welcomeMsg.put("timestamp", System.currentTimeMillis());
            
            session.sendMessage(new TextMessage(welcomeMsg.toJSONString()));
            
        } else {
            log.warn("WebSocket 连接失败，未提供 user 标识符");
            session.close(CloseStatus.BAD_DATA); // 关闭连接
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String user = (String) session.getAttributes().get("user");
        if (user != null) {
            ConcurrentHashMap<String, WebSocketSession> userSessions = sessionMap.get(user);
            if (userSessions != null) {
                userSessions.remove(session.getId());
                log.info("WebSocket 连接已关闭. User: {}, SessionId: {}, Reason: {}", user, session.getId(), status);

                // 如果该用户没有其他会话，移除空条目
                if (userSessions.isEmpty()) {
                    sessionMap.remove(user);
                }
            }
        }
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }


    // --- Helper Methods ---

    private TextMessage buildTextMessage(String action, String dataJson, String user, String instancePort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", action);
        // dataJson 已经是字符串，需要解析回对象再放入，以避免双重转义
        jsonObject.put("data", JSONObject.parse(dataJson));
        
        // 添加实例和用户信息
        jsonObject.put("fromUser", user);
        jsonObject.put("fromInstance", instancePort);
        jsonObject.put("timestamp", System.currentTimeMillis());
        
        return new TextMessage(jsonObject.toJSONString());
    }

    // 重载方法保持兼容性
    private TextMessage buildTextMessage(String action, String dataJson) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", action);
        jsonObject.put("data", JSONObject.parse(dataJson));
        jsonObject.put("fromInstance", serverPort);
        jsonObject.put("timestamp", System.currentTimeMillis());
        return new TextMessage(jsonObject.toJSONString());
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        if (session.isOpen()) {
            try {
                log.info("向会话 {} 发送消息: {}", session.getId(), message.getPayload());
                session.sendMessage(message);
                log.info("消息发送成功到会话: {}", session.getId());
            } catch (IOException e) {
                log.error("向会话 {} 发送消息失败", session.getId(), e);
            }
        } else {
            log.warn("会话 {} 已关闭，无法发送消息", session.getId());
        }
    }
}