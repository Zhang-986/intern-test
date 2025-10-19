package com.example.zzk.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.zzk.mapper.JsonMapper;

import com.example.zzk.websocket.WebsocketHandler;
import com.example.zzk.websocket.WsMsgTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Slf4j
@RestController()
@RequestMapping("/test/json")
@RequiredArgsConstructor

public class JsonTest {
    private final JsonMapper jsonMapper;
    private final WebsocketHandler websocketHandler;

    @Value("${server.port}")
    private Integer serverPort;

    @GetMapping("/websocket/ping")
    public void ping(SessionStatus sessionStatus) {
        log.info("Controller raging ==============================");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "ping");
        jsonObject.put("instancePort", serverPort);

        websocketHandler.sendMsgToAllClient(WsMsgTypeEnum.DRILL_START, jsonObject);
    }


}
