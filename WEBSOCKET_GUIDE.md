# WebSocket Implementation Guide

## Overview

This guide provides detailed information about the WebSocket implementation in this project, focusing on the distributed architecture and Redis-based message broadcasting.

## Table of Contents

1. [WebSocket Basics](#websocket-basics)
2. [Project Architecture](#project-architecture)
3. [Implementation Details](#implementation-details)
4. [Message Flow](#message-flow)
5. [Code Examples](#code-examples)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## WebSocket Basics

### What is WebSocket?

WebSocket is a protocol that provides full-duplex communication channels over a single TCP connection. Unlike HTTP, which follows a request-response pattern, WebSocket allows:

- **Bidirectional Communication:** Both client and server can send messages independently
- **Persistent Connection:** Connection stays open until explicitly closed
- **Low Latency:** No need to establish new connections for each message
- **Real-time Updates:** Perfect for chat, notifications, live data feeds

### WebSocket vs HTTP

| Feature | HTTP | WebSocket |
|---------|------|-----------|
| Connection | Request-Response | Persistent |
| Latency | Higher (new connection per request) | Lower (single connection) |
| Overhead | Header overhead per request | Minimal after handshake |
| Direction | Client-initiated only | Bidirectional |
| Use Cases | REST APIs, static content | Chat, gaming, live feeds |

---

## Project Architecture

### Distributed WebSocket Challenge

When running multiple application instances behind a load balancer, a challenge arises:

**Problem:**
```
User Alice connects to Instance 1
User Bob connects to Instance 2
How does Alice receive messages sent to Instance 2?
```

**Solution: Redis Pub/Sub**

This project solves the problem using Redis as a message broker:

```
Instance 1 → Redis Pub/Sub → Instance 1 (local sessions)
                           → Instance 2 (local sessions)
                           → Instance 3 (local sessions)
```

Each instance:
1. Maintains only its own WebSocket sessions
2. Publishes messages to Redis when broadcasting is needed
3. Subscribes to Redis and forwards messages to local sessions

---

## Implementation Details

### Component Overview

#### 1. WebSocketServerConfigure

**Purpose:** Configure WebSocket endpoint and register handler

```java
@Configuration
@EnableWebSocket
public class WebSocketServerConfigure implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler, "/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
```

**Key Points:**
- Endpoint: `/ws`
- Interceptor: Extracts user information from handshake
- CORS: Currently allows all origins (adjust for production)

---

#### 2. HandshakeInterceptor

**Purpose:** Intercept WebSocket handshake to validate and extract user information

```java
@Component
public class HandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        
        // Extract user parameter from URL
        String query = request.getURI().getQuery();
        String user = extractUserFromQuery(query);
        
        if (user == null) {
            return false; // Reject connection
        }
        
        // Store in session attributes
        attributes.put("user", user);
        attributes.put("originalUser", user);
        attributes.put("instancePort", serverPort);
        
        return true; // Accept connection
    }
}
```

**Execution Flow:**
1. Client connects: `ws://localhost:8080/ws?user=alice`
2. Interceptor extracts `user=alice`
3. Stores in session attributes
4. Returns `true` to accept or `false` to reject

---

#### 3. WebsocketHandler

**Purpose:** Main handler for WebSocket connections and messages

**Session Management:**

```java
// Structure: Map<UserId, Map<SessionId, WebSocketSession>>
private final ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> sessionMap 
    = new ConcurrentHashMap<>();
```

**Why this structure?**
- **Outer Map:** Group sessions by user ID
- **Inner Map:** Multiple sessions per user (multi-device support)
- **ConcurrentHashMap:** Thread-safe access

**Lifecycle Methods:**

##### Connection Established
```java
@Override
public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String user = (String) session.getAttributes().get("user");
    
    // Store session
    sessionMap.computeIfAbsent(user, k -> new ConcurrentHashMap<>())
              .put(session.getId(), session);
    
    // Send welcome message
    session.sendMessage(new TextMessage(welcomeJson));
}
```

##### Handle Incoming Message
```java
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    
    if ("ping".equalsIgnoreCase(payload)) {
        // Send pong response
        session.sendMessage(new TextMessage(pongJson));
    } else {
        // Echo message
        session.sendMessage(new TextMessage(echoJson));
    }
}
```

##### Connection Closed
```java
@Override
public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    String user = (String) session.getAttributes().get("user");
    
    ConcurrentHashMap<String, WebSocketSession> userSessions = sessionMap.get(user);
    if (userSessions != null) {
        userSessions.remove(session.getId());
        
        // Clean up empty user entry
        if (userSessions.isEmpty()) {
            sessionMap.remove(user);
        }
    }
}
```

---

#### 4. Redis Integration

##### RedisConfig

**Purpose:** Configure Redis template and message listener

```java
@Bean
public RedisTemplate<String, String> websocketRedisTemplate(
        RedisConnectionFactory connectionFactory) {
    
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    
    // Use String serializer for all operations
    StringRedisSerializer serializer = new StringRedisSerializer();
    template.setKeySerializer(serializer);
    template.setValueSerializer(serializer);
    
    return template;
}

@Bean
public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        RedisMessageSubscriber subscriber) {
    
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    
    // Subscribe to WebSocket messages topic
    container.addMessageListener(
        (message, pattern) -> {
            String messageJson = new String(message.getBody(), UTF_8);
            subscriber.receiveMessage(messageJson);
        },
        new PatternTopic("ws:messages")
    );
    
    return container;
}
```

**Key Configuration:**
- **Channel:** `ws:messages`
- **Serializer:** String-based (JSON strings)
- **Listener:** Automatic message delivery to subscriber

---

##### RedisMessagePublisher

**Purpose:** Publish messages to Redis for distribution

```java
@Component
public class RedisMessagePublisher {
    
    public static final String WEBSOCKET_TOPIC = "ws:messages";
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public void publish(WebSocketMessageDTO message) {
        String jsonMessage = JSONObject.toJSONString(message);
        redisTemplate.convertAndSend(WEBSOCKET_TOPIC, jsonMessage);
        log.info("Published to Redis: {}", jsonMessage);
    }
}
```

---

##### RedisMessageSubscriber

**Purpose:** Receive messages from Redis and distribute to local WebSocket sessions

```java
@Component
public class RedisMessageSubscriber {
    
    @Autowired
    private WebsocketHandler websocketHandler;
    
    public void receiveMessage(String messageJson) {
        WebSocketMessageDTO message = JSONObject.parseObject(
            messageJson, 
            WebSocketMessageDTO.class
        );
        
        switch (message.getBroadcastType()) {
            case BROADCAST:
                if (message.isExcludeSelf()) {
                    websocketHandler.sendMsgToLocalClients(
                        message.getAction(),
                        message.getData(),
                        true,
                        message.getSourceUserId()
                    );
                } else {
                    websocketHandler.sendMsgToLocalClients(
                        message.getAction(),
                        message.getData(),
                        false,
                        null
                    );
                }
                break;
                
            case SINGLE_USER:
                websocketHandler.sendMsgToLocalUser(
                    message.getAction(),
                    message.getData(),
                    message.getTargetUserId()
                );
                break;
        }
    }
}
```

---

## Message Flow

### Scenario 1: Broadcast to All Users

```
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Trigger Broadcast                                   │
│ Controller calls:                                           │
│   websocketHandler.sendMsgToAllClient(action, data)        │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│ Step 2: Publish to Redis                                    │
│ RedisMessagePublisher.publish(message)                      │
│ → Redis channel: "ws:messages"                              │
└─────────────────┬───────────────────────────────────────────┘
                  │
        ┌─────────┼─────────┬─────────┐
        │         │         │         │
        ▼         ▼         ▼         ▼
    Instance  Instance  Instance  Instance
       1         2         3         4
        │         │         │         │
        ▼         ▼         ▼         ▼
┌───────────────────────────────────────────────────────────┐
│ Step 3: Each Instance Receives from Redis                 │
│ RedisMessageSubscriber.receiveMessage()                   │
└─────────────────┬─────────────────────────────────────────┘
                  │
┌─────────────────▼─────────────────────────────────────────┐
│ Step 4: Broadcast to Local Sessions                       │
│ websocketHandler.sendMsgToLocalClients()                  │
│ → Iterate through sessionMap                              │
│ → Send to each open WebSocketSession                      │
└───────────────────────────────────────────────────────────┘
```

### Scenario 2: Send to Specific User

```
┌─────────────────────────────────────────────────────────────┐
│ Trigger: Send to User "alice"                              │
│   websocketHandler.sendMsgToOneUser(action, data, "alice") │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│ Publish to Redis with targetUserId="alice"                  │
└─────────────────┬───────────────────────────────────────────┘
                  │
        ┌─────────┼─────────┬─────────┐
        │         │         │         │
        ▼         ▼         ▼         ▼
    Instance  Instance  Instance  Instance
       1         2         3         4
        │         │         │         │
        ▼         ▼         ▼         ▼
┌───────────────────────────────────────────────────────────┐
│ Each instance checks if it has sessions for "alice"       │
│                                                            │
│ Instance 1: No alice sessions → Skip                      │
│ Instance 2: Has alice sessions → Send to all her sessions │
│ Instance 3: No alice sessions → Skip                      │
│ Instance 4: Has alice sessions → Send to all her sessions │
└───────────────────────────────────────────────────────────┘
```

**Note:** This supports multi-device scenarios where Alice is connected from both mobile and desktop.

---

## Code Examples

### Example 1: Simple WebSocket Client

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Client</title>
</head>
<body>
    <h1>WebSocket Test</h1>
    <div id="status">Disconnected</div>
    <div id="messages"></div>
    <input id="input" type="text" placeholder="Send message">
    <button onclick="send()">Send</button>
    <button onclick="connect()">Connect</button>
    <button onclick="disconnect()">Disconnect</button>

    <script>
        let ws;
        const userId = 'user_' + Date.now();

        function connect() {
            ws = new WebSocket(`ws://localhost:8080/ws?user=${userId}`);
            
            ws.onopen = () => {
                document.getElementById('status').textContent = 'Connected';
                console.log('WebSocket connected');
            };
            
            ws.onmessage = (event) => {
                const message = JSON.parse(event.data);
                addMessage(JSON.stringify(message, null, 2));
            };
            
            ws.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
            
            ws.onclose = () => {
                document.getElementById('status').textContent = 'Disconnected';
                console.log('WebSocket closed');
            };
        }

        function disconnect() {
            if (ws) {
                ws.close();
            }
        }

        function send() {
            const input = document.getElementById('input');
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(input.value);
                input.value = '';
            }
        }

        function addMessage(msg) {
            const div = document.createElement('div');
            div.textContent = msg;
            document.getElementById('messages').appendChild(div);
        }
    </script>
</body>
</html>
```

### Example 2: Multi-Device Simulation

```javascript
// Simulate user with multiple devices
class MultiDeviceUser {
    constructor(userId) {
        this.userId = userId;
        this.connections = [];
    }

    connectDevice(deviceName) {
        const ws = new WebSocket(`ws://localhost:8080/ws?user=${this.userId}`);
        
        ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            console.log(`[${deviceName}] Received:`, message);
        };
        
        this.connections.push({ name: deviceName, ws });
        return ws;
    }

    disconnectDevice(deviceName) {
        const index = this.connections.findIndex(c => c.name === deviceName);
        if (index !== -1) {
            this.connections[index].ws.close();
            this.connections.splice(index, 1);
        }
    }

    disconnectAll() {
        this.connections.forEach(c => c.ws.close());
        this.connections = [];
    }
}

// Usage
const alice = new MultiDeviceUser('alice');
alice.connectDevice('Desktop');
alice.connectDevice('Mobile');
alice.connectDevice('Tablet');

// When a message is sent to alice, all 3 devices receive it
```

### Example 3: Broadcasting from Server

```java
@RestController
@RequestMapping("/api/broadcast")
public class BroadcastController {
    
    @Autowired
    private WebsocketHandler websocketHandler;
    
    // Broadcast to all users
    @PostMapping("/all")
    public void broadcastToAll(@RequestBody Map<String, Object> data) {
        websocketHandler.sendMsgToAllClient(
            WsMsgTypeEnum.SYSTEM_MESSAGE,
            data
        );
    }
    
    // Send to specific user
    @PostMapping("/user/{userId}")
    public void sendToUser(
            @PathVariable String userId,
            @RequestBody Map<String, Object> data) {
        
        websocketHandler.sendMsgToOneUser(
            WsMsgTypeEnum.PRIVATE_MESSAGE,
            data,
            userId
        );
    }
    
    // Broadcast excluding sender
    @PostMapping("/all-except/{userId}")
    public void broadcastExcept(
            @PathVariable String userId,
            @RequestBody Map<String, Object> data) {
        
        websocketHandler.sendMsgToAllClientExcludeSelf(
            WsMsgTypeEnum.USER_ACTIVITY,
            data,
            userId
        );
    }
}
```

---

## Best Practices

### 1. Connection Management

**DO:**
- Always include user identifier in connection URL
- Implement reconnection logic on client side
- Close connections gracefully
- Handle connection errors

**DON'T:**
- Leave connections open indefinitely without activity
- Create unlimited connections per user
- Ignore connection errors

### 2. Message Handling

**DO:**
- Validate message format before processing
- Use structured message format (JSON)
- Include message types/actions for routing
- Log important messages for debugging

**DON'T:**
- Send sensitive data without encryption
- Send very large messages (>10KB)
- Ignore message parsing errors

### 3. Error Handling

```java
@Override
public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("WebSocket transport error for session {}", session.getId(), exception);
    try {
        session.close(CloseStatus.SERVER_ERROR);
    } catch (IOException e) {
        log.error("Error closing session", e);
    }
}
```

### 4. Heartbeat/Ping-Pong

Implement regular ping-pong to detect dead connections:

```javascript
// Client-side heartbeat
setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send('ping');
    }
}, 30000); // Every 30 seconds
```

### 5. Reconnection Strategy

```javascript
class WebSocketClient {
    constructor(url) {
        this.url = url;
        this.reconnectInterval = 1000;
        this.maxReconnectInterval = 30000;
        this.connect();
    }

    connect() {
        this.ws = new WebSocket(this.url);
        
        this.ws.onclose = () => {
            console.log('Connection closed, reconnecting...');
            setTimeout(() => {
                this.reconnectInterval = Math.min(
                    this.reconnectInterval * 2,
                    this.maxReconnectInterval
                );
                this.connect();
            }, this.reconnectInterval);
        };
        
        this.ws.onopen = () => {
            this.reconnectInterval = 1000; // Reset interval
            console.log('Connected');
        };
    }
}
```

---

## Troubleshooting

### Issue 1: WebSocket Connection Refused

**Symptoms:**
- Connection fails immediately
- Error: "WebSocket connection failed"

**Solutions:**
1. Check if server is running
2. Verify WebSocket endpoint URL
3. Ensure user parameter is included
4. Check firewall settings

### Issue 2: Messages Not Received

**Symptoms:**
- Connection successful but no messages received
- Messages sent but not delivered

**Solutions:**
1. Check Redis is running: `redis-cli ping`
2. Verify Redis configuration in application.yml
3. Check logs for publish/subscribe errors
4. Ensure message format is correct

### Issue 3: Memory Leak

**Symptoms:**
- Memory usage grows over time
- OutOfMemoryError after extended use

**Solutions:**
1. Ensure sessions are removed on disconnect
2. Implement connection limits
3. Add session timeout
4. Monitor sessionMap size

### Issue 4: High Latency

**Symptoms:**
- Messages arrive with significant delay
- Slow broadcast performance

**Solutions:**
1. Check Redis network latency
2. Reduce message size
3. Optimize JSON serialization
4. Use connection pooling
5. Consider Redis clustering for high load

---

## Performance Optimization

### 1. Connection Pooling

Configure Redis connection pool:

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 50
          max-idle: 20
          min-idle: 5
          max-wait: 2000ms
```

### 2. Message Batching

For high-frequency updates, batch messages:

```java
private final BlockingQueue<WebSocketMessageDTO> messageQueue = new LinkedBlockingQueue<>();

@Scheduled(fixedDelay = 100) // Every 100ms
public void processBatch() {
    List<WebSocketMessageDTO> batch = new ArrayList<>();
    messageQueue.drainTo(batch, 100);
    
    if (!batch.isEmpty()) {
        // Send batch as single message
        redisMessagePublisher.publish(createBatchMessage(batch));
    }
}
```

### 3. Selective Broadcasting

Only broadcast to relevant users:

```java
public void sendToUsersInRoom(String roomId, WsMsgTypeEnum action, Object data) {
    Set<String> usersInRoom = roomService.getUsersInRoom(roomId);
    
    usersInRoom.forEach(userId -> {
        sendMsgToOneUser(action, data, userId);
    });
}
```

---

## Security Considerations

### 1. Authentication

Implement token-based authentication:

```java
@Override
public boolean beforeHandshake(..., Map<String, Object> attributes) {
    String token = extractToken(request);
    
    if (!jwtValidator.validateToken(token)) {
        return false; // Reject connection
    }
    
    String userId = jwtValidator.getUserId(token);
    attributes.put("user", userId);
    
    return true;
}
```

### 2. Authorization

Check user permissions before sending messages:

```java
public void sendMsgToOneUser(WsMsgTypeEnum action, Object data, String targetUserId) {
    // Check if current user has permission to message target user
    if (!authService.canMessageUser(getCurrentUser(), targetUserId)) {
        throw new UnauthorizedException();
    }
    
    // Proceed with sending
    ...
}
```

### 3. Input Validation

Validate all incoming messages:

```java
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();
    
    // Validate message length
    if (payload.length() > MAX_MESSAGE_LENGTH) {
        log.warn("Message too long from user {}", getUser(session));
        return;
    }
    
    // Sanitize input
    payload = sanitize(payload);
    
    // Process message
    ...
}
```

### 4. Rate Limiting

Prevent message flooding:

```java
private final LoadingCache<String, AtomicInteger> rateLimiter = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .build(new CacheLoader<String, AtomicInteger>() {
        public AtomicInteger load(String key) {
            return new AtomicInteger(0);
        }
    });

protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String userId = getUser(session);
    
    if (rateLimiter.getUnchecked(userId).incrementAndGet() > MAX_MESSAGES_PER_MINUTE) {
        log.warn("Rate limit exceeded for user {}", userId);
        return;
    }
    
    // Process message
    ...
}
```

---

## Testing

### Unit Testing WebSocket Handler

```java
@SpringBootTest
class WebsocketHandlerTest {
    
    @Autowired
    private WebsocketHandler handler;
    
    @Test
    void testSessionManagement() throws Exception {
        // Create mock session
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", "testUser");
        
        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        
        // Test connection
        handler.afterConnectionEstablished(session);
        
        // Verify session stored
        // Test message handling
        // Test disconnection
    }
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    void testWebSocketConnection() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        
        WebSocketSession session = client.doHandshake(
            new TextWebSocketHandler(),
            "ws://localhost:" + port + "/ws?user=testUser"
        ).get();
        
        assertTrue(session.isOpen());
        
        // Test message sending and receiving
        session.sendMessage(new TextMessage("ping"));
        
        // Wait for response
        // Verify response
    }
}
```

---

## Conclusion

This WebSocket implementation provides a robust, scalable solution for real-time communication in distributed environments. Key takeaways:

1. **Redis pub/sub enables cross-instance messaging**
2. **Session management is critical for multi-device support**
3. **Proper error handling prevents cascading failures**
4. **Security measures are essential for production**
5. **Performance optimization ensures scalability**

For more information, refer to:
- [Architecture Documentation](ARCHITECTURE.md)
- [API Documentation](API_DOCUMENTATION.md)
- [Setup Guide](SETUP_GUIDE.md)
