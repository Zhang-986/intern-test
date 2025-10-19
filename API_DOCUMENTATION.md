# API Documentation

## Overview

This document provides comprehensive API documentation for the WebSocket messaging application, including REST endpoints and WebSocket protocol specifications.

## Base URL

```
http://localhost:8080
```

## REST API Endpoints

### User Management APIs

#### Create User

Creates a new user in the database.

**Endpoint:** `POST /api/users`

**Request Body:**
```json
{
  "id": 123456789,
  "name": "Alice Smith"
}
```

**Request Headers:**
```
Content-Type: application/json
```

**Response:**
```json
{
  "id": 123456789,
  "name": "Alice Smith"
}
```

**Status Codes:**
- `200 OK` - User created successfully
- `400 Bad Request` - Invalid request body
- `500 Internal Server Error` - Database error

**Example (cURL):**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Smith"}'
```

**Example (JavaScript):**
```javascript
fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: 'Alice Smith'
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

---

#### Get User Count

Returns the total number of users in the database.

**Endpoint:** `GET /api/users/count`

**Response:**
```
42
```

**Response Type:** `text/plain` (number)

**Status Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Database error

**Example (cURL):**
```bash
curl http://localhost:8080/api/users/count
```

**Example (JavaScript):**
```javascript
fetch('http://localhost:8080/api/users/count')
  .then(response => response.text())
  .then(count => console.log('User count:', count));
```

---

### WebSocket Testing APIs

#### Trigger WebSocket Ping

Broadcasts a ping message to all connected WebSocket clients across all instances.

**Endpoint:** `GET /test/json/websocket/ping`

**Response:** No content (204)

**Side Effects:**
- All connected WebSocket clients receive a ping message
- Message includes the instance port number that received the request

**Status Codes:**
- `200 OK` - Ping broadcast initiated
- `500 Internal Server Error` - Redis or WebSocket error

**Example (cURL):**
```bash
curl http://localhost:8080/test/json/websocket/ping
```

**WebSocket Message Received by Clients:**
```json
{
  "action": "DRILL_START",
  "data": {
    "action": "ping",
    "instancePort": 8080
  },
  "fromUser": "system",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

---

## WebSocket Protocol

### Connection

#### WebSocket Endpoint

**URL:** `ws://localhost:8080/ws`

**Query Parameters:**
- `user` (required) - Unique identifier for the user

**Example Connection URLs:**
```
ws://localhost:8080/ws?user=alice
ws://localhost:8080/ws?user=bob123
ws://localhost:8080/ws?user=user-456
```

**Connection Example (JavaScript):**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws?user=alice');

ws.onopen = (event) => {
  console.log('WebSocket connected');
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('Received:', message);
};

ws.onerror = (error) => {
  console.error('WebSocket error:', error);
};

ws.onclose = (event) => {
  console.log('WebSocket closed:', event.code, event.reason);
};
```

---

### Message Protocol

#### Client to Server Messages

##### 1. Ping Message

**Purpose:** Test connection and get pong response from server

**Message:**
```
ping
```

**Server Response:**
```json
{
  "type": "pong",
  "fromUser": "alice",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

##### 2. Echo Message

**Purpose:** Send any text and receive echo response

**Message:**
```
Hello, Server!
```

**Server Response:**
```json
{
  "type": "echo",
  "message": "收到: Hello, Server!",
  "fromUser": "alice",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

---

#### Server to Client Messages

All server-to-client messages follow this general structure:

```json
{
  "action": "ACTION_TYPE",
  "data": { /* action-specific data */ },
  "fromUser": "user_id",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

##### Message Types

###### 1. Welcome Message

Sent immediately after successful connection.

```json
{
  "type": "welcome",
  "message": "WebSocket连接成功",
  "userId": "alice",
  "instance": "8080",
  "timestamp": 1697234567890
}
```

###### 2. Pong Message

Response to client ping.

```json
{
  "type": "pong",
  "fromUser": "alice",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

###### 3. Echo Message

Echo of client message.

```json
{
  "type": "echo",
  "message": "收到: [client_message]",
  "fromUser": "alice",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

###### 4. Broadcast Messages

System-wide broadcast messages triggered by server actions.

**DRILL_START Action:**
```json
{
  "action": "DRILL_START",
  "data": {
    "action": "ping",
    "instancePort": 8080
  },
  "fromUser": "system",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

---

### Message Broadcasting Patterns

The system supports three types of message broadcasting:

#### 1. Broadcast to All

**Purpose:** Send message to all connected users across all instances

**Usage:**
```java
websocketHandler.sendMsgToAllClient(
    WsMsgTypeEnum.DRILL_START, 
    messageData
);
```

**Recipients:** All connected users on all instances

**Example Scenario:**
- System announcements
- Global notifications
- Emergency alerts

---

#### 2. Broadcast Excluding Sender

**Purpose:** Send message to all users except the one who triggered the action

**Usage:**
```java
websocketHandler.sendMsgToAllClientExcludeSelf(
    WsMsgTypeEnum.USER_JOINED, 
    messageData, 
    "alice"
);
```

**Recipients:** All users except "alice"

**Example Scenario:**
- User activity notifications: "Alice joined the room"
- Collaborative editing: "Bob updated the document"
- Status changes: "Charlie went offline"

---

#### 3. Single User Message

**Purpose:** Send message to specific user's all sessions (multi-device support)

**Usage:**
```java
websocketHandler.sendMsgToOneUser(
    WsMsgTypeEnum.PRIVATE_MESSAGE, 
    messageData, 
    "alice"
);
```

**Recipients:** All sessions belonging to "alice" (e.g., desktop + mobile)

**Example Scenario:**
- Private messages
- Personal notifications
- User-specific updates

---

## WebSocket Message Actions

### Available Actions (WsMsgTypeEnum)

```java
public enum WsMsgTypeEnum {
    DRILL_START,        // Drill/test started
    DRILL_STOP,         // Drill/test stopped
    USER_JOINED,        // User joined
    USER_LEFT,          // User left
    PRIVATE_MESSAGE,    // Private message
    SYSTEM_MESSAGE      // System message
}
```

---

## Error Responses

### WebSocket Connection Errors

#### Missing User Parameter

**Close Code:** `1002` (Protocol Error)
**Close Reason:** "user parameter is required"

**Example:**
```javascript
ws://localhost:8080/ws  // Missing ?user=xxx
```

#### Invalid Session

**Close Code:** `1011` (Internal Server Error)
**Close Reason:** "Session error"

---

### REST API Error Responses

All REST API errors follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request body",
  "path": "/api/users"
}
```

---

## Rate Limiting

**Current Implementation:** No rate limiting

**Recommendations for Production:**
- Implement rate limiting per user/IP
- Suggested limits:
  - REST API: 100 requests/minute
  - WebSocket messages: 50 messages/minute
  - WebSocket connections: 10 connections/user

---

## Authentication & Authorization

**Current Implementation:** Basic user identification via query parameter (no authentication)

**Production Recommendations:**

### JWT Authentication Example

**1. Obtain Token:**
```bash
POST /api/auth/login
{
  "username": "alice",
  "password": "secret"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

**2. Connect with Token:**
```javascript
const token = 'eyJhbGciOiJIUzI1NiIs...';
const ws = new WebSocket(`ws://localhost:8080/ws?token=${token}`);
```

**3. Validate in HandshakeInterceptor:**
```java
String token = request.getParameter("token");
// Validate JWT token
// Extract user from token
// Store in session attributes
```

---

## CORS Configuration

**Current Settings:**
- Allowed Origins: `*` (all)
- Allowed Methods: `GET, POST, PUT, DELETE, OPTIONS`
- Allowed Headers: `*`
- Allow Credentials: `true`

**Production Recommendation:**
```yaml
cors:
  allowed-origins:
    - https://app.example.com
    - https://www.example.com
  allowed-methods:
    - GET
    - POST
  allow-credentials: true
```

---

## Complete Usage Examples

### Example 1: Simple Chat Client

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Chat</title>
</head>
<body>
    <div id="messages"></div>
    <input id="messageInput" type="text" placeholder="Type a message">
    <button onclick="sendMessage()">Send</button>

    <script>
        const userId = 'user_' + Math.random().toString(36).substr(2, 9);
        const ws = new WebSocket(`ws://localhost:8080/ws?user=${userId}`);

        ws.onopen = () => {
            console.log('Connected to WebSocket');
            addMessage('System', 'Connected to chat');
        };

        ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            if (message.type === 'welcome') {
                addMessage('System', message.message);
            } else if (message.action) {
                addMessage('Server', JSON.stringify(message.data));
            }
        };

        function sendMessage() {
            const input = document.getElementById('messageInput');
            ws.send(input.value);
            input.value = '';
        }

        function addMessage(sender, text) {
            const div = document.createElement('div');
            div.textContent = `${sender}: ${text}`;
            document.getElementById('messages').appendChild(div);
        }
    </script>
</body>
</html>
```

### Example 2: Multi-Instance Testing

```javascript
// Connect to multiple instances for testing
const connections = [];

// Connect to 3 instances (assuming they run on ports 8080, 8081, 8082)
[8080, 8081, 8082].forEach(port => {
    const ws = new WebSocket(`ws://localhost:${port}/ws?user=alice`);
    
    ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        console.log(`Instance ${port} received:`, message);
    };
    
    connections.push(ws);
});

// Trigger broadcast from server
fetch('http://localhost:8080/test/json/websocket/ping')
    .then(() => console.log('Ping broadcast triggered'));

// Expected: All 3 connections receive the ping message
```

### Example 3: User Management Integration

```javascript
// Create a user
async function createUser(name) {
    const response = await fetch('http://localhost:8080/api/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name })
    });
    return response.json();
}

// Get user count
async function getUserCount() {
    const response = await fetch('http://localhost:8080/api/users/count');
    return response.text();
}

// Use the user ID for WebSocket connection
async function connectWithNewUser(name) {
    const user = await createUser(name);
    const ws = new WebSocket(`ws://localhost:8080/ws?user=${user.id}`);
    return ws;
}

// Example usage
connectWithNewUser('Alice').then(ws => {
    ws.onopen = () => console.log('Alice connected');
    ws.onmessage = (event) => console.log('Alice received:', event.data);
});
```

---

## Testing the API

### Using Postman

**1. Test User Creation:**
```
POST http://localhost:8080/api/users
Body (JSON):
{
  "name": "Test User"
}
```

**2. Test User Count:**
```
GET http://localhost:8080/api/users/count
```

**3. Test WebSocket Ping:**
```
GET http://localhost:8080/test/json/websocket/ping
```

### Using WebSocket Test Page

Open the included `websocket-test.html` file in your browser:
1. Set user ID
2. Click "Connect"
3. Send messages
4. Test broadcast functionality

---

## Performance Considerations

### Connection Limits

**Current Configuration:**
- No explicit connection limit
- Limited by system resources

**Recommended Production Limits:**
- Max connections per instance: 10,000
- Max connections per user: 5 (devices)
- Connection timeout: 5 minutes idle

### Message Size Limits

**Current Configuration:**
- No explicit size limit
- Limited by WebSocket frame size (typically 64KB)

**Recommended Limits:**
- Max message size: 10KB
- Max JSON payload: 5KB

### Throughput

**Expected Performance:**
- Messages per second per instance: ~1,000
- Concurrent connections per instance: ~5,000
- Broadcast latency: <100ms (Redis network dependent)

---

## Monitoring & Debugging

### Enable Debug Logging

In `application.yml`:
```yaml
logging:
  level:
    com.example.zzk.websocket: DEBUG
    com.example.zzk.redis: DEBUG
```

### Monitor Active Connections

Add this endpoint to monitor active sessions:
```java
@GetMapping("/api/websocket/stats")
public Map<String, Object> getStats() {
    return Map.of(
        "totalUsers", sessionMap.size(),
        "totalSessions", sessionMap.values().stream()
            .mapToInt(Map::size).sum()
    );
}
```

---

## Migration Guide

### From v1.x to v2.x (Hypothetical)

If you were using an older version, here's what changed:

**Breaking Changes:**
- User parameter is now required for WebSocket connections
- Message format now includes `fromInstance` field
- Redis serialization changed to String-based

**Migration Steps:**
1. Update WebSocket connection URLs to include `?user=xxx`
2. Update message parsing to handle new fields
3. Update Redis configuration if customized

---

## Support & Troubleshooting

### Common Issues

**Issue:** WebSocket connection fails
**Solution:** Ensure user parameter is provided in URL

**Issue:** Messages not broadcasting across instances
**Solution:** Verify Redis is running and accessible by all instances

**Issue:** High memory usage
**Solution:** Implement connection limits and session cleanup

### Getting Help

For issues or questions:
1. Check the [Architecture Documentation](ARCHITECTURE.md)
2. Review the [WebSocket Guide](WEBSOCKET_GUIDE.md)
3. Open an issue on GitHub

---

## API Versioning

**Current Version:** 1.0

**Future Versioning Strategy:**
- URL-based versioning: `/api/v2/users`
- Header-based versioning: `Accept: application/vnd.api.v2+json`

---

## Changelog

### Version 1.0.0 (Current)
- Initial release
- Basic WebSocket functionality
- User management APIs
- Redis pub/sub integration
- Multi-instance support
