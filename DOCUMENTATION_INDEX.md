# Documentation Index

Welcome to the Internship Laboratory Code documentation! This index will help you navigate through all available documentation.

## 📚 Documentation Overview

This repository contains comprehensive English documentation for a Spring Boot WebSocket demonstration project. All documentation has been written to help developers understand, set up, and extend the project.

## 🚀 Getting Started

**New to the project?** Start here:

1. **[README.md](README.md)** - Project overview and quick start guide
   - Technology stack overview
   - Key features
   - Quick installation steps
   - Basic usage examples

2. **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Detailed installation and configuration
   - Prerequisites and software requirements
   - Step-by-step installation
   - Database and Redis setup
   - Multi-instance configuration
   - Docker deployment
   - Production deployment guidelines

## 🏗️ Understanding the Architecture

3. **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design and architecture
   - High-level architecture diagram
   - Component responsibilities
   - Message flow diagrams
   - Distributed session management
   - Scalability considerations
   - Design patterns used
   - Future enhancements

## 🔌 API Reference

4. **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference
   - REST API endpoints
   - WebSocket protocol specification
   - Message types and formats
   - Request/response examples
   - Error handling
   - CORS configuration
   - Usage examples with cURL and JavaScript

5. **[WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)** - WebSocket implementation deep dive
   - WebSocket basics and benefits
   - Distributed WebSocket challenges and solutions
   - Component detailed explanation
   - Message flow scenarios
   - Code examples and best practices
   - Security considerations
   - Testing strategies
   - Performance optimization

## 🌏 Chinese Documentation

6. **[README_CN.md](README_CN.md)** - Chinese version of the README
   - 项目概述 (Project overview in Chinese)
   - 核心技术栈 (Technology stack)
   - 快速开始 (Quick start guide)
   - 中文说明 (Chinese explanations)

## 📖 Documentation by Use Case

### I want to...

#### ...get the project running quickly
→ Read: [README.md](README.md) → [SETUP_GUIDE.md](SETUP_GUIDE.md)

#### ...understand how WebSocket works in this project
→ Read: [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md) → [ARCHITECTURE.md](ARCHITECTURE.md)

#### ...use the API endpoints
→ Read: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

#### ...deploy to production
→ Read: [SETUP_GUIDE.md](SETUP_GUIDE.md) (Production Deployment section)

#### ...run multiple instances
→ Read: [SETUP_GUIDE.md](SETUP_GUIDE.md) (Multi-Instance Setup) → [ARCHITECTURE.md](ARCHITECTURE.md) (Distributed Architecture)

#### ...understand the code structure
→ Read: [ARCHITECTURE.md](ARCHITECTURE.md) → Check inline code comments in Java files

#### ...troubleshoot issues
→ Read: [SETUP_GUIDE.md](SETUP_GUIDE.md) (Troubleshooting) → [API_DOCUMENTATION.md](API_DOCUMENTATION.md) (Error Responses)

#### ...extend or modify the project
→ Read: [ARCHITECTURE.md](ARCHITECTURE.md) → [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md) → Code comments

## 💡 Key Concepts

### Distributed WebSocket Architecture
The project uses Redis pub/sub to enable WebSocket communication across multiple application instances. Each instance maintains its own sessions but can communicate with sessions on other instances through Redis.

**Learn more:** [ARCHITECTURE.md](ARCHITECTURE.md) → [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)

### Message Broadcasting Patterns
The system supports three types of message distribution:
1. **Broadcast** - Send to all users
2. **Single User** - Send to specific user's all devices
3. **Exclude Self** - Send to all except sender

**Learn more:** [API_DOCUMENTATION.md](API_DOCUMENTATION.md) → [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)

### Session Management
Multi-device support through nested ConcurrentHashMap structure, tracking multiple sessions per user.

**Learn more:** [ARCHITECTURE.md](ARCHITECTURE.md) → Code comments in `WebsocketHandler.java`

## 🔧 Technical Details

### Technology Stack
- **Backend:** Spring Boot 3.3.3, Java 17
- **WebSocket:** Spring WebSocket
- **Database:** MySQL 8.0, MyBatis Plus
- **Cache/Messaging:** Redis 6.0+
- **JSON Processing:** FastJSON2
- **Utilities:** Lombok, Hutool, MapStruct Plus
- **Microservices:** OpenFeign

### Project Structure
```
src/main/java/com/example/zzk/
├── config/         - Configuration classes
├── controller/     - REST API endpoints
├── websocket/      - WebSocket handlers and DTOs
├── redis/          - Redis pub/sub implementation
├── model/          - Domain models
├── mapper/         - MyBatis mappers
├── service/        - Business logic
├── feign/          - Feign clients
└── utils/          - Utility classes
```

## 📝 Code Comments

All major Java files include comprehensive English comments:

- `WebsocketHandler.java` - Main WebSocket handler with detailed comments
- `RedisConfig.java` - Redis configuration explanation
- `RedisMessagePublisher.java` - Publisher with usage notes
- `RedisMessageSubscriber.java` - Subscriber with routing logic
- Controller classes - Endpoint documentation
- Main application class - Feature overview

## 🎯 Quick Links

| I need to... | Go to... |
|--------------|----------|
| Install the project | [SETUP_GUIDE.md](SETUP_GUIDE.md) |
| Understand the architecture | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Use the API | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) |
| Learn about WebSocket | [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md) |
| See quick start | [README.md](README.md) |
| Read in Chinese | [README_CN.md](README_CN.md) |

## 📊 Documentation Statistics

- **Total documentation files:** 6
- **Total pages (approx.):** 90+
- **Code files with enhanced comments:** 7+
- **Diagrams and examples:** 15+
- **Topics covered:** 50+

## 🤝 Contributing

If you find any issues in the documentation or want to improve it:

1. Check the specific documentation file
2. Review inline code comments
3. Open an issue on GitHub
4. Suggest improvements

## 📮 Getting Help

If you can't find what you're looking for:

1. Check the [Troubleshooting sections](SETUP_GUIDE.md#troubleshooting) in SETUP_GUIDE.md
2. Review [Common Issues](API_DOCUMENTATION.md#common-issues) in API_DOCUMENTATION.md
3. Search through code comments in the relevant Java files
4. Open an issue on GitHub with details about what you're trying to accomplish

## 🔄 Documentation Updates

This documentation was created as part of the internship project to provide comprehensive English explanations of the codebase. It covers:

- ✅ Complete project setup and installation
- ✅ Architecture and design patterns
- ✅ API and WebSocket protocols
- ✅ Code-level documentation
- ✅ Deployment guidelines
- ✅ Best practices and security
- ✅ Troubleshooting guides

---

**Note:** All documentation is written in English to make the internship laboratory code accessible to international developers. A Chinese version of the main README is also available for reference.

**Last Updated:** 2024-01-19

**Project Version:** 1.0.0-SNAPSHOT
