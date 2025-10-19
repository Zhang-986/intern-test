package com.example.zzk.redis;


import com.alibaba.fastjson2.JSONObject;
import com.example.zzk.websocket.WebsocketHandler;
import com.example.zzk.websocket.dto.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Redis Message Subscriber for WebSocket Messages
 * 
 * This component receives messages from the Redis pub/sub channel
 * and distributes them to local WebSocket sessions. Each application
 * instance has its own subscriber that processes messages for its
 * connected clients.
 */
@Slf4j
@Component
public class RedisMessageSubscriber {

    @Autowired
    private WebsocketHandler websocketHandler;

    /**
     * Process incoming message from Redis
     * Called by RedisMessageListenerContainer when a message arrives
     * 
     * @param messageJson The message as JSON string
     */
    public void receiveMessage(String messageJson) {
        try {
            log.info("Received Redis message: {}", messageJson);

            // Deserialize message from JSON
            WebSocketMessageDTO dto = JSONObject.parseObject(messageJson, WebSocketMessageDTO.class);

            // Route message based on broadcast type
            switch (dto.getBroadcastType()) {
                case BROADCAST:
                    // Send to all local clients (optionally excluding sender)
                    websocketHandler.sendMsgToLocalClients(
                            dto.getAction(),
                            dto.getDataJson(),
                            dto.isExcludeSelf(),
                            dto.getSourceUserId()
                    );
                    break;
                case SINGLE_USER:
                    // Send to specific user if connected to this instance
                    websocketHandler.sendMsgToLocalUser(
                            dto.getAction(),
                            dto.getDataJson(),
                            dto.getTargetUserId()
                    );
                    break;
                default:
                    log.warn("Unknown broadcast type: {}", dto.getBroadcastType());
            }

        } catch (Exception e) {
            log.error("Failed to process Redis message: {}", messageJson, e);
        }
    }
}