package com.example.zzk.redis;


import com.alibaba.fastjson2.JSONObject;
import com.example.zzk.websocket.dto.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Message Publisher for WebSocket Messages
 * 
 * This component publishes WebSocket messages to a Redis channel.
 * All application instances subscribe to this channel, enabling
 * cross-instance message distribution.
 */
@Slf4j
@Component
public class RedisMessagePublisher {

    /** Redis channel name for WebSocket messages */
    public static final String WEBSOCKET_TOPIC = "ws:messages";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisMessagePublisher(@Qualifier("websocketRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Publish a WebSocket message to Redis
     * 
     * @param message The message DTO to publish
     */
    public void publish(WebSocketMessageDTO message) {
        // Serialize message to JSON string manually
        String jsonMessage = JSONObject.toJSONString(message);

        // Publish to Redis channel (convertAndSend uses configured serializers)
        redisTemplate.convertAndSend(WEBSOCKET_TOPIC, jsonMessage);
        log.info("Published WebSocket message to Redis: {}", jsonMessage);
    }
}