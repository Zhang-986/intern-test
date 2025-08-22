// WebSocketMessageDTO.java
package com.example.zzk.websocket.dto; // 注意包路径


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO implements Serializable {
    private static final long serialVersionUID = 2L; // 更新 serialVersionUID

    // action (消息类型)
    private String action;

    // 消息数据 (JSON 字符串)
    private String dataJson;

    // --- 广播逻辑 ---
    // 是否排除自己
    private boolean excludeSelf;
    // 消息来源的用户ID (用于排除自己)
    private String sourceUserId;

    // --- 单发逻辑 ---
    // 目标用户ID
    private String targetUserId;

    // 消息广播类型
    private MessageBroadcastType broadcastType;

    public enum MessageBroadcastType {
        BROADCAST, // 广播 (包括 excludeSelf)
        SINGLE_USER // 发给单个用户
    }
}