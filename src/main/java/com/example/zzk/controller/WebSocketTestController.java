package com.example.zzk.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
@RequestMapping("/websocket")
public class WebSocketTestController {

    @GetMapping("/test")
    public ResponseEntity<String> getTestPage() {
        try {
            // 读取项目根目录下的HTML文件
            String content = Files.readString(Paths.get("websocket-test.html"), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(getDefaultTestPage());
        }
    }

    @GetMapping("")
    public String redirectToTest() {
        return "redirect:/websocket/test";
    }

    private String getDefaultTestPage() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>WebSocket测试页面</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; }
                        .error { background: #f8d7da; padding: 20px; border-radius: 5px; color: #721c24; }
                        .info { background: #d1ecf1; padding: 20px; border-radius: 5px; color: #0c5460; }
                    </style>
                </head>
                <body>
                    <h1>WebSocket分布式测试工具</h1>
                    <div class="error">
                        <h3>页面文件未找到</h3>
                        <p>请确保 websocket-test.html 文件位于项目根目录下。</p>
                    </div>
                    <div class="info">
                        <h3>访问说明</h3>
                        <p><strong>本地访问：</strong></p>
                        <ul>
                            <li>http://localhost:8080/websocket/test</li>
                            <li>http://127.0.0.1:8080/websocket/test</li>
                        </ul>
                        <p><strong>局域网访问：</strong></p>
                        <ul>
                            <li>http://[您的IP地址]:8080/websocket/test</li>
                            <li>例如：http://192.168.1.100:8080/websocket/test</li>
                        </ul>
                        <p><strong>外网访问：</strong></p>
                        <ul>
                            <li>需要配置端口转发和防火墙</li>
                            <li>http://[公网IP]:8080/websocket/test</li>
                        </ul>
                    </div>
                </body>
                </html>
                """;
    }
}
