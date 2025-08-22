package com.example.zzk.redis;


import com.alibaba.fastjson2.JSONObject;
import com.example.zzk.websocket.dto.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisMessagePublisher {

    public static final String WEBSOCKET_TOPIC = "ws:messages";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisMessagePublisher(@Qualifier("websocketRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(WebSocketMessageDTO message) {
        // 关键修改：手动序列化成JSON字符串
        String jsonMessage = JSONObject.toJSONString(message);

        // 现在传入的是String类型，与RedisTemplate的声明一致
        redisTemplate.convertAndSend(WEBSOCKET_TOPIC, jsonMessage);
        log.info("已发送WebSocket消息到Redis: {}", jsonMessage);
    }
}