package com.example.zzk.websocket;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;

/**
 * @author Andy
 * @date 2020-12-17
 */
@Component
@Slf4j
public class HandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 握手之前，若返回false，则不建立链接
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = serverHttpRequest.getServletRequest();
        
        // 先从 Header 获取 user
        String user = servletRequest.getHeader("user");
        
        // 如果Header中没有，则从URL参数中获取
        if (user == null) {
            user = servletRequest.getParameter("user");
        }
        
        // 如果都没有，使用默认值
        if (user == null) {
            user = "anonymous";
        }

        // 在用户ID后面添加实例标识，格式：user@port
        String userWithInstance = user + "@" + serverPort;

        log.info("WebSocket连接尝试建立，原始user={}，带实例标识user={}", user, userWithInstance);

        // 将原始用户ID和带实例标识的用户ID都存入 attributes
        attributes.put("user", userWithInstance);
        attributes.put("originalUser", user);
        attributes.put("instancePort", serverPort);
        return true; // 允许连接
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.info("WebSocket afterHandshake");
        if (exception != null) {
            log.error("WebSocket握手异常", exception);
        } else {
            log.info("WebSocket握手成功");
        }
    }
}