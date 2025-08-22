package com.example.zzk.websocket;


import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * @author Andy
 * @date 2022-03-17
 */
@Configuration
@EnableWebSocket
public class WebSocketServerConfigure implements WebSocketConfigurer {

    private static final int MAX_MESSAGE_SIZE = 32 * 1024 * 1000; //32M

    //最大空闲时间(毫秒)
    private static final long MAX_IDLE = 60 * 1000L;

    @Autowired
    private WebsocketHandler websocketHandler;

    /**
     * 注入拦截器
     */
    @Resource
    private HandshakeInterceptor handshakeInterceptor;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler, "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(handshakeInterceptor);
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(MAX_MESSAGE_SIZE);
        container.setMaxBinaryMessageBufferSize(MAX_MESSAGE_SIZE * 2);
        container.setMaxSessionIdleTimeout(MAX_IDLE);
        return container;
    }


}
