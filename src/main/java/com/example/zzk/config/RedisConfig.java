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

/**
 * Redis Configuration for WebSocket Message Distribution
 * 
 * This configuration sets up Redis pub/sub for distributing WebSocket messages
 * across multiple application instances. All instances subscribe to the same
 * Redis channel and publish messages to it for cross-instance communication.
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Configure a dedicated Redis template for WebSocket messages
     * Uses String serialization for both keys and values (messages sent as JSON strings)
     * 
     * @param connectionFactory Redis connection factory
     * @return Configured RedisTemplate for WebSocket messaging
     */
    @Bean
    public RedisTemplate<String, String> websocketRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use StringRedisSerializer for all serialization operations
        // This ensures messages are transmitted as plain JSON strings
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setDefaultSerializer(stringSerializer);

        return template;
    }

    /**
     * Configure Redis message listener container
     * Subscribes to the WebSocket messages channel and forwards messages to the subscriber
     * 
     * @param connectionFactory Redis connection factory
     * @param redisMessageSubscriber The subscriber that handles incoming messages
     * @return Configured listener container
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisMessageSubscriber redisMessageSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Add message listener with custom handler
        container.addMessageListener((message, pattern) -> {
            try {
                // Decode message body as UTF-8 string
                String messageJson = new String(message.getBody(), StandardCharsets.UTF_8);
                redisMessageSubscriber.receiveMessage(messageJson);
            } catch (Exception e) {
                log.error("Error processing Redis message: {}",
                        new String(message.getBody(), StandardCharsets.UTF_8), e);
            }
        }, new PatternTopic("ws:messages"));  // Subscribe to WebSocket messages channel

        return container;
    }
}