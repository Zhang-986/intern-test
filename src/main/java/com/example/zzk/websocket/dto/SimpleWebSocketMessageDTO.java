package com.example.zzk.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简化的WebSocket消息DTO，避免序列化问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleWebSocketMessageDTO {
    
    private String action;
    private String dataJson;
    private boolean excludeSelf;
    private String sourceUserId;
    private String targetUserId;
    private String broadcastType;  // 使用字符串代替枚举
    
    public boolean isBroadcast() {
        return "BROADCAST".equals(broadcastType);
    }
    
    public boolean isSingleUser() {
        return "SINGLE_USER".equals(broadcastType);
    }
}
