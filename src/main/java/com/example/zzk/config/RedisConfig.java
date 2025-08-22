package com.example.zzk.config;


import com.example.zzk.redis.RedisMessageSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class RedisConfig {

    // 专门用于WebSocket消息的Redis模板 - 纯字符串传输
    @Bean
    public RedisTemplate<String, String> websocketRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setDefaultSerializer(stringSerializer);

        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisMessageSubscriber redisMessageSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener((message, pattern) -> {
            try {
                String messageJson = new String(message.getBody(), StandardCharsets.UTF_8);
                redisMessageSubscriber.receiveMessage(messageJson);
            } catch (Exception e) {
                log.error("处理Redis消息时发生错误，原始消息：{}",
                        new String(message.getBody(), StandardCharsets.UTF_8), e);
            }
        }, new PatternTopic("ws:messages"));

        return container;
    }
}