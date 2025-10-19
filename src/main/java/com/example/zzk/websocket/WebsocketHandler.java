package com.example.zzk.websocket;



import com.alibaba.fastjson2.JSON;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for managing client connections and message routing
 * 
 * This handler is responsible for:
 * - Managing WebSocket sessions for this instance
 * - Handling incoming messages from clients
 * - Publishing messages to Redis for cross-instance distribution
 * - Receiving messages from Redis and broadcasting to local clients
 * 
 * Key Design: Each application instance maintains its own session map.
 * Redis pub/sub is used to synchronize messages across instances.
 */
@Component
@Slf4j
public class WebsocketHandler extends TextWebSocketHandler {

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Session storage structure:
     * Outer Map - Key: userId, Value: Map of sessions for that user
     * Inner Map - Key: sessionId, Value: WebSocketSession
     * 
     * This allows multi-device support where one user can have multiple active sessions.
     * Each instance only manages connections made to itself.
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    /**
     * Handle incoming text messages from WebSocket clients
     * 
     * @param session The WebSocket session that sent the message
     * @param message The text message received
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String originalUser = (String) session.getAttributes().get("originalUser");
        String instancePort = (String) session.getAttributes().get("instancePort");

        // Debug logging: Display current session map content
        log.info("========================= sessionMap content =========================");
        for (Map.Entry<String, ConcurrentHashMap<String, WebSocketSession>> outerEntry : sessionMap.entrySet()) {
            String outerKey = outerEntry.getKey();
            log.info("Outer Key: {}", outerKey);

            ConcurrentHashMap<String, WebSocketSession> innerMap = outerEntry.getValue();
            for (Map.Entry<String, WebSocketSession> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                WebSocketSession wsSession = innerEntry.getValue();
                log.info("  Inner Key: {}, Session ID: {}", innerKey, wsSession.getId());
            }
        }
        log.info("======================================================================");
        
        // Handle simple ping-pong for connection testing
        if ("ping".equalsIgnoreCase(payload)) {
            JSONObject response = new JSONObject();
            response.put("type", "pong");
            response.put("fromUser", originalUser);
            response.put("fromInstance", serverPort);
            response.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(response.toJSONString()));
        } else {
            // Echo any other message back to the client
            JSONObject response = new JSONObject();
            response.put("type", "echo");
            response.put("message", "收到: " + payload);
            response.put("fromUser", originalUser);
            response.put("fromInstance", serverPort);
            response.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(response.toJSONString()));

        }
    }

    // ========================================
    // Public API: Publish messages to Redis
    // These methods are called by controllers/services to initiate message distribution
    // ========================================

    /**
     * Broadcast message to all connected clients across all instances
     * 
     * @param action The message action type
     * @param data The message data payload
     */
    public void sendMsgToAllClient(WsMsgTypeEnum action, Object data) {
        String dataJson = JSONObject.toJSONString(data, JSONWriter.Feature.NullAsDefaultValue);
        log.info("Publishing broadcast message to Redis: action={}, data={}", action.toString(), dataJson);
        
        // Create simplified DTO for internal use
        SimpleWebSocketMessageDTO message = new SimpleWebSocketMessageDTO(
                action.toString(),
                dataJson,
                false,
                null,
                null,
                "BROADCAST"
        );
        
        // Convert to legacy DTO format for Redis compatibility
        WebSocketMessageDTO legacyMessage = new WebSocketMessageDTO(
                action.toString(),
                dataJson,
                false,
                null,
                null,
                WebSocketMessageDTO.MessageBroadcastType.BROADCAST
        );
        
        log.info("Publishing to Redis: {}", JSONObject.toJSONString(legacyMessage));
        redisMessagePublisher.publish(legacyMessage);
    }

    /**
     * Broadcast message to all clients except the sender
     * 
     * @param action The message action type
     * @param data The message data payload
     * @param sourceUserId The user ID to exclude from broadcast
     */
    public void sendMsgToAllClientExcludeSelf(WsMsgTypeEnum action, Object data, String sourceUserId) {
        String dataJson = JSONObject.toJSONString(data, JSONWriter.Feature.NullAsDefaultValue);
        log.info("Publishing broadcast message (excluding self) to Redis: action={}, sourceUserId={}", action.toString(), sourceUserId);
        
        WebSocketMessageDTO legacyMessage = new WebSocketMessageDTO(
                action.toString(),
                dataJson,
                true,  // excludeSelf flag
                sourceUserId,
                null,
                WebSocketMessageDTO.MessageBroadcastType.BROADCAST
        );
        
        redisMessagePublisher.publish(legacyMessage);
    }

    /**
     * Send message to a specific user's all sessions (multi-device support)
     * 
     * @param action The message action type
     * @param data The message data payload
     * @param targetUserId The target user ID
     */
    public void sendMsgToOneUser(WsMsgTypeEnum action, Object data, String targetUserId) {
        String dataJson = JSONObject.toJSONString(data, JSONWriter.Feature.NullAsDefaultValue);
        log.info("Publishing single-user message to Redis: action={}, targetUserId={}", action.toString(), targetUserId);
        
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


    // ========================================
    // Internal Methods: Called by Redis Subscriber
    // These methods distribute messages to local WebSocket sessions
    // ========================================
    
    /**
     * Broadcast message to all clients connected to THIS instance
     * Called by RedisMessageSubscriber when a broadcast message is received from Redis
     * 
     * @param action The message action
     * @param dataJson The message data as JSON string
     * @param excludeSelf Whether to exclude the sender from receiving
     * @param sourceUserId The user ID of the sender (used when excludeSelf=true)
     */
    public void sendMsgToLocalClients(String action, String dataJson, boolean excludeSelf, String sourceUserId) {
        log.info("Broadcasting message to local clients: action={}, excludeSelf={}, sourceUserId={}", action, excludeSelf, sourceUserId);
        log.info("Current instance sessionMap users: {}", sessionMap.keySet());
        
        if (sessionMap.isEmpty()) {
            log.warn("No WebSocket connections on this instance");
            return;
        }

        int sentCount = 0;
        sessionMap.forEach((userId, userSessions) -> {
            log.info("Checking user: {}, session count: {}", userId, userSessions.size());
            
            // Skip sender's sessions if excludeSelf is true
            if (excludeSelf && userId.equals(sourceUserId)) {
                log.info("Skipping sender: {}", userId);
                return;
            }
            
            userSessions.values().forEach(session -> {
                if (session.isOpen()) {
                    // Get session attributes for message context
                    String originalUser = (String) session.getAttributes().get("originalUser");
                    String instancePort = (String) session.getAttributes().get("instancePort");
                    
                    log.info("Sending message to user {} (instance:{})", originalUser, instancePort);
                    
                    TextMessage textMessage = buildTextMessage(action, dataJson, originalUser, instancePort);
                    sendMessage(session, textMessage);
                } else {
                    log.warn("Session closed, skipping: {}", session.getId());
                }
            });
        });
        
        log.info("Broadcast completed, messages sent: {}", sentCount);
    }

    /**
     * Send message to a specific user connected to THIS instance
     * Called by RedisMessageSubscriber when a single-user message is received
     * 
     * @param action The message action
     * @param dataJson The message data as JSON string
     * @param targetUserId The target user ID
     */
    public void sendMsgToLocalUser(String action, String dataJson, String targetUserId) {
        ConcurrentHashMap<String, WebSocketSession> userSessions = sessionMap.get(targetUserId);
        if (userSessions != null && !userSessions.isEmpty()) {
            log.info("Sending message to local user {}, action: {}", targetUserId, action);
            userSessions.values().forEach(session -> {
                // Get session attributes for message context
                String originalUser = (String) session.getAttributes().get("originalUser");
                String instancePort = (String) session.getAttributes().get("instancePort");
                
                TextMessage textMessage = buildTextMessage(action, dataJson, originalUser, instancePort);
                sendMessage(session, textMessage);
            });
        }
    }

    // ========================================
    // Connection Lifecycle Management
    // ========================================
    
    /**
     * Called when a new WebSocket connection is established
     * Stores the session and sends a welcome message
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract user information from session attributes (set by HandshakeInterceptor)
        String user = (String) session.getAttributes().get("user");
        String originalUser = (String) session.getAttributes().get("originalUser");
        String instancePort = (String) session.getAttributes().get("instancePort");

        if (user != null) {
            // Store session in instance-local map, grouped by user
            sessionMap.computeIfAbsent(user, k -> new ConcurrentHashMap<>())
                    .put(session.getId(), session);
            log.info("WebSocket connection established. User: {}, OriginalUser: {}, Instance: {}, SessionId: {}", 
                     user, originalUser, instancePort, session.getId());
            
            // Log current sessions for debugging
            log.info("Current sessions on instance {}: {}", instancePort, sessionMap.keySet());
            
            // Send welcome message to client
            JSONObject welcomeMsg = new JSONObject();
            welcomeMsg.put("type", "welcome");
            welcomeMsg.put("message", "WebSocket连接成功");
            welcomeMsg.put("userId", originalUser);
            welcomeMsg.put("instance", instancePort);
            welcomeMsg.put("timestamp", System.currentTimeMillis());
            
            session.sendMessage(new TextMessage(welcomeMsg.toJSONString()));
            
        } else {
            log.warn("WebSocket connection rejected: no user identifier provided");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    /**
     * Called when a WebSocket connection is closed
     * Removes the session from the session map
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String user = (String) session.getAttributes().get("user");
        if (user != null) {
            ConcurrentHashMap<String, WebSocketSession> userSessions = sessionMap.get(user);
            if (userSessions != null) {
                userSessions.remove(session.getId());
                log.info("WebSocket connection closed. User: {}, SessionId: {}, Reason: {}", user, session.getId(), status);

                // Remove empty user entry to prevent memory leaks
                if (userSessions.isEmpty()) {
                    sessionMap.remove(user);
                }
            }
        }
        super.afterConnectionClosed(session, status);
    }

    /**
     * Handle WebSocket transport errors
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }


    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Build a text message for sending to WebSocket client
     * 
     * @param action Message action type
     * @param dataJson Message data as JSON string
     * @param user User ID
     * @param instancePort Instance port number
     * @return TextMessage ready to send
     */
    private TextMessage buildTextMessage(String action, String dataJson, String user, String instancePort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", action);
        // Parse JSON string back to object to avoid double-encoding
        jsonObject.put("data", JSONObject.parse(dataJson));
        
        // Add context information
        jsonObject.put("fromUser", user);
        jsonObject.put("fromInstance", instancePort);
        jsonObject.put("timestamp", System.currentTimeMillis());
        
        return new TextMessage(jsonObject.toJSONString());
    }

    /**
     * Overloaded method for backward compatibility
     */
    private TextMessage buildTextMessage(String action, String dataJson) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", action);
        jsonObject.put("data", JSONObject.parse(dataJson));
        jsonObject.put("fromInstance", serverPort);
        jsonObject.put("timestamp", System.currentTimeMillis());
        return new TextMessage(jsonObject.toJSONString());
    }

    /**
     * Send a message to a WebSocket session with error handling
     * 
     * @param session The target session
     * @param message The message to send
     */
    private void sendMessage(WebSocketSession session, TextMessage message) {
        if (session.isOpen()) {
            try {
                log.info("Sending message to session {}: {}", session.getId(), message.getPayload());
                session.sendMessage(message);
                log.info("Message sent successfully to session: {}", session.getId());
            } catch (IOException e) {
                log.error("Failed to send message to session {}", session.getId(), e);
            }
        } else {
            log.warn("Session {} is closed, cannot send message", session.getId());
        }
    }
}