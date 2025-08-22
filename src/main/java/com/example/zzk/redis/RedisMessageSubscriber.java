package com.example.zzk.redis;


import com.alibaba.fastjson2.JSONObject;
import com.example.zzk.websocket.WebsocketHandler;
import com.example.zzk.websocket.dto.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisMessageSubscriber {

    @Autowired
    private WebsocketHandler websocketHandler;

    public void receiveMessage(String messageJson) {
        try {
            log.info("收到Redis消息: {}", messageJson);

            WebSocketMessageDTO dto = JSONObject.parseObject(messageJson, WebSocketMessageDTO.class);

            switch (dto.getBroadcastType()) {
                case BROADCAST:
                    websocketHandler.sendMsgToLocalClients(
                            dto.getAction(),
                            dto.getDataJson(),
                            dto.isExcludeSelf(),
                            dto.getSourceUserId()
                    );
                    break;
                case SINGLE_USER:
                    websocketHandler.sendMsgToLocalUser(
                            dto.getAction(),
                            dto.getDataJson(),
                            dto.getTargetUserId()
                    );
                    break;
                default:
                    log.warn("未知的广播类型: {}", dto.getBroadcastType());
            }

        } catch (Exception e) {
            log.error("处理Redis消息失败，原始数据: {}", messageJson, e);
        }
    }
}