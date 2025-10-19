# Architecture Documentation

## System Architecture Overview

This document provides a detailed explanation of the system architecture for the distributed WebSocket messaging application.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Browser    │  │   Browser    │  │   Browser    │     │
│  │   (User 1)   │  │   (User 2)   │  │   (User 3)   │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼────────────┘
          │ WebSocket        │ WebSocket        │ WebSocket
          │                  │                  │
┌─────────┼──────────────────┼──────────────────┼────────────┐
│         ▼                  ▼                  ▼             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐  │
│  │  Instance 1  │   │  Instance 2  │   │  Instance 3  │  │
│  │  (Port 8080) │   │  (Port 8081) │   │  (Port 8082) │  │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘  │
│         │                  │                  │            │
│         └──────────────────┼──────────────────┘            │
│                Application Layer                           │
└────────────────────────────┼───────────────────────────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
          ┌─────────▼────────┐ ┌─────▼──────────┐
          │  Redis Pub/Sub   │ │  MySQL DB      │
          │  (Port 6379)     │ │  (Port 3306)   │
          └──────────────────┘ └────────────────┘
                Data Layer
```

## Component Architecture

### 1. WebSocket Layer

#### WebsocketHandler
**Purpose:** Main WebSocket connection handler that manages client sessions and message routing.

**Key Responsibilities:**
- Maintain WebSocket sessions per instance
- Handle incoming WebSocket messages from clients
- Route outgoing messages to connected clients
- Publish messages to Redis for cross-instance communication

**Session Management:**
```java
// Structure: Map<UserId, Map<SessionId, WebSocketSession>>
ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> sessionMap
```

Each instance maintains its own sessions. When User1 connects to Instance1, only Instance1 stores that session.

#### HandshakeInterceptor
**Purpose:** Intercept WebSocket handshake to extract user information and instance details.

**Flow:**
1. Client connects: `ws://localhost:8080/ws?user=user123`
2. Interceptor extracts `user` parameter
3. Stores user info in session attributes
4. Adds instance port information
5. Allows or denies connection based on validation

### 2. Redis Pub/Sub Layer

#### Message Flow
```
┌──────────────────────────────────────────────────────────┐
│  Instance 1: User1 sends message                         │
│  ┌────────────────────────────────────────────────────┐ │
│  │  1. WebsocketHandler.sendMsgToAllClient()          │ │
│  │  2. RedisMessagePublisher.publish()                │ │
│  └────────────────────────────────────────────────────┘ │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
            ┌────────────────┐
            │  Redis Channel │
            │  "ws:messages" │
            └────────┬───────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌───────────┐ ┌───────────┐ ┌───────────┐
│Instance 1 │ │Instance 2 │ │Instance 3 │
│Subscriber │ │Subscriber │ │Subscriber │
└─────┬─────┘ └─────┬─────┘ └─────┬─────┘
      │             │             │
      ▼             ▼             ▼
  Local         Local         Local
  Sessions      Sessions      Sessions
```

#### RedisMessagePublisher
**Purpose:** Publish WebSocket messages to Redis channel for distribution.

**Key Features:**
- Serializes messages to JSON
- Publishes to `ws:messages` topic
- Thread-safe message publishing

#### RedisMessageSubscriber
**Purpose:** Subscribe to Redis channel and distribute messages to local WebSocket sessions.

**Process:**
1. Receive message from Redis
2. Deserialize JSON to WebSocketMessageDTO
3. Determine message type (BROADCAST, SINGLE_USER, etc.)
4. Call appropriate handler method
5. Send to local WebSocket sessions

### 3. Message Types & Broadcasting Patterns

#### Pattern 1: BROADCAST
Send message to all connected users across all instances.

```
User1 (Instance1) → Redis → All Instances → All Users
```

**Use Case:** System announcements, global notifications

#### Pattern 2: SINGLE_USER
Send message to specific user's all sessions (multi-device support).

```
System → Redis → Instance with Target User → User's All Sessions
```

**Use Case:** Private notifications, user-specific updates

#### Pattern 3: BROADCAST_EXCLUDE_SELF
Send to all users except the sender.

```
User1 → Redis → All Instances → All Users (except User1)
```

**Use Case:** Activity notifications (e.g., "User1 joined the chat")

### 4. Configuration Layer

#### RedisConfig
**Purpose:** Configure Redis template and message listeners.

**Key Beans:**
- `websocketRedisTemplate`: String-based Redis template for WebSocket messages
- `redisMessageListenerContainer`: Container for Redis message listeners

**Serialization Strategy:**
- Uses `StringRedisSerializer` for both keys and values
- Messages serialized as JSON strings
- UTF-8 encoding for message bodies

#### CorsConfig
**Purpose:** Configure Cross-Origin Resource Sharing for WebSocket connections.

**Settings:**
- Allows all origins (for development)
- Allows credentials
- Configures allowed methods and headers

#### WebConfig
**Purpose:** Web MVC configuration and general web settings.

### 5. Controller Layer

#### JsonTest Controller
**Endpoints:**
- `GET /test/json/websocket/ping` - Trigger broadcast message to all clients

**Functionality:**
- Creates ping message with instance information
- Broadcasts via WebSocket handler
- Used for testing distributed messaging

#### UserController
**Endpoints:**
- `POST /api/users` - Create new user
- `GET /api/users/count` - Get user count

**Integration:**
- Uses MyBatis Plus for database operations
- Demonstrates basic CRUD patterns

### 6. Data Layer

#### MyBatis Plus Integration
**Components:**
- `JsonMapper`: MyBatis mapper interface for User table
- `User` model with `@TableName` annotation
- XML mapper for complex SQL queries

**Database Schema:**
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255)
);
```

### 7. External Service Integration

#### OpenFeign Client
**GoFeign Interface:**
- Communicates with external Go service
- Endpoint: `http://localhost:9086/go/api`
- Demonstrates microservice architecture

## Data Flow Examples

### Example 1: User Connects to WebSocket

```
1. Browser → ws://localhost:8080/ws?user=alice
2. HandshakeInterceptor extracts user="alice"
3. WebsocketHandler.afterConnectionEstablished()
4. Session stored: sessionMap["alice"][sessionId] = session
5. Welcome message sent to client
```

### Example 2: Broadcasting Message

```
1. Controller calls websocketHandler.sendMsgToAllClient(action, data)
2. Message serialized to JSON
3. RedisMessagePublisher.publish(message) → Redis channel
4. All instances receive via RedisMessageSubscriber
5. Each instance calls websocketHandler.sendMsgToLocalClients()
6. Each instance sends to its local WebSocket sessions
7. All connected clients receive the message
```

### Example 3: User-Specific Message

```
1. System calls websocketHandler.sendMsgToOneUser(action, data, "alice")
2. Message published to Redis with target="alice"
3. All instances receive the message
4. Only instance with "alice" session responds
5. Message sent to all of alice's sessions (multi-device)
```

## Thread Safety Considerations

### 1. Session Storage
- Uses `ConcurrentHashMap` for thread-safe session management
- Nested maps for user → sessions mapping
- Atomic operations for add/remove sessions

### 2. Message Publishing
- Redis operations are thread-safe
- RedisTemplate provides synchronized access
- Message ordering guaranteed within single channel

### 3. WebSocket Session Access
- Check `session.isOpen()` before sending
- Synchronized message sending to prevent concurrent writes
- Proper exception handling for closed sessions

## Scalability Considerations

### Horizontal Scaling
- **Supported:** Multiple instances can run simultaneously
- **Load Balancing:** Use sticky sessions or distribute users evenly
- **Limitation:** Redis becomes single point of failure (use Redis Cluster for production)

### Vertical Scaling
- **Memory:** Each instance stores only its sessions
- **CPU:** WebSocket I/O is generally I/O-bound
- **Network:** Redis pub/sub has minimal latency

### Performance Optimization
1. **Connection Pooling:** Redis connection pool configured
2. **Session Cleanup:** Automatic session removal on disconnect
3. **Lazy Initialization:** Sessions created only on connection
4. **Efficient Serialization:** FastJSON2 for high-performance JSON processing

## Error Handling Strategy

### WebSocket Errors
- Transport errors logged and session closed
- Reconnection handled by client
- Invalid messages logged but don't crash handler

### Redis Errors
- Connection failures logged
- Messages may be lost if Redis is down
- Application continues serving local sessions

### Database Errors
- Standard Spring transaction management
- Exceptions propagated to client as error responses
- Connection pool handles transient failures

## Security Considerations

### Current Implementation
- Basic user identification via query parameter
- No authentication/authorization
- Open CORS policy

### Production Recommendations
1. Implement proper authentication (JWT, OAuth2)
2. Validate user identity in HandshakeInterceptor
3. Restrict CORS to specific origins
4. Use TLS/SSL for WebSocket connections (wss://)
5. Implement rate limiting
6. Sanitize user inputs
7. Use secure Redis connection

## Deployment Architecture

### Development
```
Single Instance → Local Redis → Local MySQL
```

### Production (Recommended)
```
┌─────────────┐
│ Load Balancer│
└──────┬──────┘
       │
   ┌───┴────┬────────┬────────┐
   ▼        ▼        ▼        ▼
Instance Instance Instance Instance
   1        2        3        4
   │        │        │        │
   └────┬───┴────┬───┴────┬───┘
        │        │        │
    ┌───▼────────▼────────▼───┐
    │   Redis Cluster (3 nodes)│
    └──────────────────────────┘
    ┌──────────────────────────┐
    │   MySQL Primary/Replica   │
    └──────────────────────────┘
```

## Monitoring & Observability

### Logging
- SLF4J with Logback
- Structured logging for WebSocket events
- Request/response logging for debugging

### Metrics (Recommended)
- Active WebSocket connections per instance
- Messages sent/received per second
- Redis pub/sub latency
- Database query performance

### Health Checks
- WebSocket endpoint availability
- Redis connection health
- Database connection health

## Technology Choices Rationale

### Why Redis Pub/Sub?
- Simple and fast message distribution
- Low latency for real-time messaging
- Built-in support in Spring Boot
- Easy to scale horizontally

### Why ConcurrentHashMap?
- Thread-safe without explicit synchronization
- Better performance than synchronized HashMap
- Good for read-heavy workloads

### Why FastJSON2?
- High performance JSON processing
- Widely used in Chinese tech companies
- Good integration with Spring Boot

### Why MyBatis Plus?
- Simplifies CRUD operations
- Reduces boilerplate code
- Active development and community support
- Popular in Chinese enterprise applications

## Future Enhancements

1. **WebSocket Authentication:** Add JWT-based authentication
2. **Message Persistence:** Store messages in database for offline users
3. **Message Acknowledgment:** Implement delivery confirmation
4. **Clustering:** Add Redis Sentinel or Cluster for high availability
5. **Monitoring Dashboard:** Add real-time connection monitoring
6. **Rate Limiting:** Prevent message flooding
7. **Binary Messages:** Support for file transfers
8. **Compression:** Add message compression for large payloads
9. **Heartbeat Mechanism:** Automatic ping/pong to detect dead connections
10. **Message History:** Store and retrieve message history

## References

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Redis Pub/Sub Documentation](https://redis.io/docs/interact/pubsub/)
- [MyBatis Plus Documentation](https://baomidou.com/)
- [WebSocket Protocol RFC 6455](https://tools.ietf.org/html/rfc6455)
