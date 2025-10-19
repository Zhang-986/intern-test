# Internship Laboratory Code - Spring Boot WebSocket Demo

[中文文档](README_CN.md)

## Project Overview

This is a comprehensive Spring Boot demonstration project developed during an internship, showcasing various enterprise-level Java technologies and architectural patterns. The project implements a distributed WebSocket messaging system with Redis pub/sub for multi-instance communication.

## Key Technologies

- **Spring Boot 3.3.3** - Main application framework
- **WebSocket** - Real-time bidirectional communication
- **Redis** - Message broker for distributed WebSocket sessions
- **MyBatis Plus** - Database ORM framework
- **OpenFeign** - Declarative REST client for microservices
- **FastJSON2** - High-performance JSON processing
- **Lombok** - Reduce boilerplate code
- **Hutool** - Java utility library
- **MapStruct Plus** - Bean mapping framework

## Architecture Highlights

### Distributed WebSocket Implementation
The project implements a production-ready distributed WebSocket solution where multiple application instances can share WebSocket connections through Redis pub/sub messaging.

**Key Features:**
- Multi-instance WebSocket session management
- Redis-based message broadcasting across instances
- User session tracking per instance
- Support for broadcast, single-user, and exclude-self messaging patterns

### Database Integration
- MyBatis Plus for simplified CRUD operations
- MySQL database connectivity
- Custom mapper XML configurations

### Microservice Communication
- OpenFeign client for communicating with external Go services
- RESTful API design patterns

## Project Structure

```
src/main/java/com/example/zzk/
├── config/              # Configuration classes
│   ├── RedisConfig.java       # Redis setup for WebSocket messaging
│   ├── CorsConfig.java        # Cross-Origin Resource Sharing config
│   └── WebConfig.java         # Web MVC configuration
├── controller/          # REST API endpoints
│   ├── JsonTest.java          # WebSocket ping controller
│   ├── UserController.java    # User CRUD operations
│   └── WebSocketTestController.java
├── websocket/           # WebSocket implementation
│   ├── WebsocketHandler.java      # Main WebSocket handler
│   ├── WebSocketServerConfigure.java
│   ├── HandshakeInterceptor.java  # WebSocket handshake interceptor
│   └── dto/                        # WebSocket message DTOs
├── redis/               # Redis pub/sub implementation
│   ├── RedisMessagePublisher.java
│   └── RedisMessageSubscriber.java
├── model/               # Domain models and DTOs
├── mapper/              # MyBatis mapper interfaces
├── service/             # Business logic layer
├── feign/               # OpenFeign clients
├── utils/               # Utility classes
└── result/              # API response wrappers
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Zhang-986/intern-test.git
   cd intern-test
   ```

2. **Configure Database**
   Update `src/main/resources/application.yml` with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC
       username: your_username
       password: your_password
   ```

3. **Configure Redis**
   Update Redis connection in `application.yml`:
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### User Management
- `POST /api/users` - Create a new user
- `GET /api/users/count` - Get total user count

### WebSocket Testing
- `GET /test/json/websocket/ping` - Trigger WebSocket broadcast to all connected clients

### WebSocket Connection
- `ws://localhost:8080/ws?user={userId}` - Connect to WebSocket endpoint

## WebSocket Features

### Message Types
The system supports three broadcasting patterns:

1. **BROADCAST** - Send to all connected clients
2. **SINGLE_USER** - Send to specific user's all sessions
3. **BROADCAST_EXCLUDE_SELF** - Send to all except the sender

### Example WebSocket Messages

**Client Ping:**
```json
"ping"
```

**Server Pong Response:**
```json
{
  "type": "pong",
  "fromUser": "user123",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

**Broadcast Message:**
```json
{
  "action": "DRILL_START",
  "data": {
    "action": "ping",
    "instancePort": 8080
  },
  "fromUser": "user123",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

## Testing

### WebSocket Testing Page
Open `websocket-test.html` in your browser to test WebSocket functionality:
- Multiple user connections simulation
- Message broadcasting
- Instance-specific messaging

### Running Unit Tests
```bash
mvn test
```

## Key Implementation Details

### Distributed Session Management
Each application instance maintains its own WebSocket sessions in memory using `ConcurrentHashMap`. When a message needs to be broadcast:
1. Publisher calls `WebsocketHandler.sendMsgToAllClient()`
2. Message is published to Redis channel `ws:messages`
3. All instances (including sender) receive the message via `RedisMessageSubscriber`
4. Each instance broadcasts to its local WebSocket connections

### Thread Safety
- Uses `ConcurrentHashMap` for session storage
- Synchronous message handling to prevent race conditions
- Proper session lifecycle management

### Error Handling
- Transport error handling in WebSocket connections
- Session validation before sending messages
- Graceful connection closure handling

## Dependencies

Key dependencies and their purposes:

- `spring-boot-starter-web` - Web application support
- `spring-boot-starter-websocket` - WebSocket support
- `spring-boot-starter-data-redis` - Redis integration
- `mybatis-plus-spring-boot3-starter` - MyBatis Plus ORM
- `spring-cloud-starter-openfeign` - Feign client
- `fastjson2` - JSON processing
- `hutool-core` & `hutool-extra` - Utility functions
- `mapstruct-plus` - Object mapping

## Configuration Files

- `application.yml` - Main application configuration
- `pom.xml` - Maven dependencies and build configuration
- `mapper/*.xml` - MyBatis SQL mapping files

## Development Notes

### Code Style
- Uses Lombok annotations to reduce boilerplate
- Follows Spring Boot best practices
- Implements proper logging with SLF4J

### Design Patterns Used
- Singleton (Spring beans)
- Factory (RedisTemplate)
- Observer (Redis pub/sub)
- DTO (Data Transfer Objects)

## Troubleshooting

### WebSocket Connection Issues
- Ensure Redis is running and accessible
- Check CORS configuration in `CorsConfig.java`
- Verify WebSocket endpoint URL includes user parameter

### Database Connection Issues
- Verify MySQL server is running
- Check database credentials in `application.yml`
- Ensure database `testdb` exists

### Redis Connection Issues
- Verify Redis server is running: `redis-cli ping`
- Check Redis host and port configuration
- Ensure no firewall blocking Redis port

## Further Documentation

- [Architecture Design](ARCHITECTURE.md) - Detailed system architecture
- [API Documentation](API_DOCUMENTATION.md) - Complete API reference
- [WebSocket Guide](WEBSOCKET_GUIDE.md) - WebSocket implementation details
- [Setup Guide](SETUP_GUIDE.md) - Detailed setup instructions

## License

This is an educational/internship project for learning purposes.

## Contact

For questions or issues, please open an issue on GitHub.
