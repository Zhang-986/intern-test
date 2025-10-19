# 实习实验室代码 - Spring Boot WebSocket 演示项目

[English Documentation](README.md)

## 项目概述

这是一个在实习期间开发的综合性 Spring Boot 演示项目，展示了多种企业级 Java 技术和架构模式。该项目实现了一个分布式 WebSocket 消息系统，使用 Redis 发布/订阅模式实现多实例通信。

## 核心技术栈

- **Spring Boot 3.3.3** - 主要应用框架
- **WebSocket** - 实时双向通信
- **Redis** - 分布式 WebSocket 会话的消息代理
- **MyBatis Plus** - 数据库 ORM 框架
- **OpenFeign** - 声明式 REST 客户端
- **FastJSON2** - 高性能 JSON 处理
- **Lombok** - 减少样板代码
- **Hutool** - Java 工具库
- **MapStruct Plus** - Bean 映射框架

## 架构亮点

### 分布式 WebSocket 实现
项目实现了生产级的分布式 WebSocket 解决方案，多个应用实例可以通过 Redis 发布/订阅消息共享 WebSocket 连接。

**主要特性：**
- 多实例 WebSocket 会话管理
- 基于 Redis 的跨实例消息广播
- 每个实例的用户会话跟踪
- 支持广播、单用户和排除自己的消息模式

### 数据库集成
- MyBatis Plus 简化 CRUD 操作
- MySQL 数据库连接
- 自定义 Mapper XML 配置

### 微服务通信
- OpenFeign 客户端与外部 Go 服务通信
- RESTful API 设计模式

## 项目结构

```
src/main/java/com/example/zzk/
├── config/              # 配置类
│   ├── RedisConfig.java       # WebSocket 消息的 Redis 配置
│   ├── CorsConfig.java        # 跨域资源共享配置
│   └── WebConfig.java         # Web MVC 配置
├── controller/          # REST API 端点
│   ├── JsonTest.java          # WebSocket ping 控制器
│   ├── UserController.java    # 用户 CRUD 操作
│   └── WebSocketTestController.java
├── websocket/           # WebSocket 实现
│   ├── WebsocketHandler.java      # 主 WebSocket 处理器
│   ├── WebSocketServerConfigure.java
│   ├── HandshakeInterceptor.java  # WebSocket 握手拦截器
│   └── dto/                        # WebSocket 消息 DTO
├── redis/               # Redis 发布/订阅实现
│   ├── RedisMessagePublisher.java
│   └── RedisMessageSubscriber.java
├── model/               # 领域模型和 DTO
├── mapper/              # MyBatis 映射器接口
├── service/             # 业务逻辑层
├── feign/               # OpenFeign 客户端
├── utils/               # 工具类
└── result/              # API 响应包装器
```

## 快速开始

### 前置要求
- Java 17 或更高版本
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/Zhang-986/intern-test.git
   cd intern-test
   ```

2. **配置数据库**
   在 `src/main/resources/application.yml` 中更新数据库凭据：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC
       username: 你的用户名
       password: 你的密码
   ```

3. **配置 Redis**
   在 `application.yml` 中更新 Redis 连接：
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   ```

4. **构建项目**
   ```bash
   mvn clean install
   ```

5. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

应用将在 `http://localhost:8080` 启动

## API 端点

### 用户管理
- `POST /api/users` - 创建新用户
- `GET /api/users/count` - 获取用户总数

### WebSocket 测试
- `GET /test/json/websocket/ping` - 触发 WebSocket 广播到所有连接的客户端

### WebSocket 连接
- `ws://localhost:8080/ws?user={userId}` - 连接到 WebSocket 端点

## WebSocket 功能

### 消息类型
系统支持三种广播模式：

1. **BROADCAST** - 发送给所有连接的客户端
2. **SINGLE_USER** - 发送给特定用户的所有会话
3. **BROADCAST_EXCLUDE_SELF** - 发送给除发送者外的所有人

### WebSocket 消息示例

**客户端 Ping：**
```json
"ping"
```

**服务器 Pong 响应：**
```json
{
  "type": "pong",
  "fromUser": "user123",
  "fromInstance": "8080",
  "timestamp": 1697234567890
}
```

**广播消息：**
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

## 测试

### WebSocket 测试页面
在浏览器中打开 `websocket-test.html` 来测试 WebSocket 功能：
- 模拟多用户连接
- 消息广播
- 实例特定消息

### 运行单元测试
```bash
mvn test
```

## 关键实现细节

### 分布式会话管理
每个应用实例使用 `ConcurrentHashMap` 在内存中维护自己的 WebSocket 会话。当需要广播消息时：
1. 发布者调用 `WebsocketHandler.sendMsgToAllClient()`
2. 消息被发布到 Redis 频道 `ws:messages`
3. 所有实例（包括发送者）通过 `RedisMessageSubscriber` 接收消息
4. 每个实例广播到其本地 WebSocket 连接

### 线程安全
- 使用 `ConcurrentHashMap` 存储会话
- 同步消息处理以防止竞态条件
- 适当的会话生命周期管理

### 错误处理
- WebSocket 连接中的传输错误处理
- 发送消息前的会话验证
- 优雅的连接关闭处理

## 依赖说明

主要依赖及其用途：

- `spring-boot-starter-web` - Web 应用支持
- `spring-boot-starter-websocket` - WebSocket 支持
- `spring-boot-starter-data-redis` - Redis 集成
- `mybatis-plus-spring-boot3-starter` - MyBatis Plus ORM
- `spring-cloud-starter-openfeign` - Feign 客户端
- `fastjson2` - JSON 处理
- `hutool-core` & `hutool-extra` - 工具函数
- `mapstruct-plus` - 对象映射

## 配置文件

- `application.yml` - 主应用配置
- `pom.xml` - Maven 依赖和构建配置
- `mapper/*.xml` - MyBatis SQL 映射文件

## 开发说明

### 代码风格
- 使用 Lombok 注解减少样板代码
- 遵循 Spring Boot 最佳实践
- 使用 SLF4J 实现适当的日志记录

### 使用的设计模式
- 单例模式（Spring beans）
- 工厂模式（RedisTemplate）
- 观察者模式（Redis pub/sub）
- DTO 模式（数据传输对象）

## 故障排除

### WebSocket 连接问题
- 确保 Redis 正在运行且可访问
- 检查 `CorsConfig.java` 中的 CORS 配置
- 验证 WebSocket 端点 URL 包含 user 参数

### 数据库连接问题
- 验证 MySQL 服务器正在运行
- 检查 `application.yml` 中的数据库凭据
- 确保数据库 `testdb` 存在

### Redis 连接问题
- 验证 Redis 服务器正在运行：`redis-cli ping`
- 检查 Redis 主机和端口配置
- 确保没有防火墙阻止 Redis 端口

## 更多文档

- [架构设计](ARCHITECTURE.md) - 详细的系统架构
- [API 文档](API_DOCUMENTATION.md) - 完整的 API 参考
- [WebSocket 指南](WEBSOCKET_GUIDE.md) - WebSocket 实现细节
- [安装指南](SETUP_GUIDE.md) - 详细的安装说明

## 许可证

这是一个用于学习目的的教育/实习项目。

## 联系方式

如有问题或疑问，请在 GitHub 上提交 issue。
