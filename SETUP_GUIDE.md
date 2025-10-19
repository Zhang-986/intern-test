# Setup and Installation Guide

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation Steps](#installation-steps)
3. [Configuration](#configuration)
4. [Running the Application](#running-the-application)
5. [Testing the Setup](#testing-the-setup)
6. [Multi-Instance Setup](#multi-instance-setup)
7. [Docker Deployment](#docker-deployment)
8. [Production Deployment](#production-deployment)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

#### 1. Java Development Kit (JDK)

**Version Required:** Java 17 or higher

**Installation:**

**On Windows:**
```bash
# Using Chocolatey
choco install openjdk17

# Or download from https://adoptium.net/
```

**On macOS:**
```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

**On Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

**Verify Installation:**
```bash
java -version
# Should output: openjdk version "17.x.x" or higher
```

---

#### 2. Maven

**Version Required:** Maven 3.6 or higher

**Installation:**

**On Windows:**
```bash
# Using Chocolatey
choco install maven
```

**On macOS:**
```bash
# Using Homebrew
brew install maven
```

**On Linux:**
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven
```

**Verify Installation:**
```bash
mvn -version
# Should output: Apache Maven 3.6.x or higher
```

---

#### 3. MySQL

**Version Required:** MySQL 8.0 or higher

**Installation:**

**On Windows:**
1. Download MySQL Installer from https://dev.mysql.com/downloads/installer/
2. Run installer and select "MySQL Server"
3. Choose "Developer Default" setup type
4. Set root password during installation

**On macOS:**
```bash
# Using Homebrew
brew install mysql

# Start MySQL service
brew services start mysql

# Secure installation
mysql_secure_installation
```

**On Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Secure installation
sudo mysql_secure_installation
```

**Verify Installation:**
```bash
mysql -u root -p
# Enter password and you should see MySQL prompt
```

---

#### 4. Redis

**Version Required:** Redis 6.0 or higher

**Installation:**

**On Windows:**
```bash
# Using Chocolatey
choco install redis-64

# Or download from https://github.com/microsoftarchive/redis/releases
```

**On macOS:**
```bash
# Using Homebrew
brew install redis

# Start Redis service
brew services start redis
```

**On Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install redis-server

# Start Redis service
sudo systemctl start redis
sudo systemctl enable redis

# CentOS/RHEL
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis
```

**Verify Installation:**
```bash
redis-cli ping
# Should output: PONG
```

---

#### 5. Git (Optional but Recommended)

**Installation:**

**On Windows:**
```bash
# Using Chocolatey
choco install git
```

**On macOS:**
```bash
# Using Homebrew
brew install git
```

**On Linux:**
```bash
# Ubuntu/Debian
sudo apt install git

# CentOS/RHEL
sudo yum install git
```

---

## Installation Steps

### Step 1: Clone the Repository

```bash
git clone https://github.com/Zhang-986/intern-test.git
cd intern-test
```

**Alternative (without Git):**
- Download ZIP from https://github.com/Zhang-986/intern-test
- Extract to a directory
- Navigate to the directory in terminal

---

### Step 2: Configure Database

#### Create Database

```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE testdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Create user (optional, for production)
CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON testdb.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;

# Exit MySQL
exit;
```

#### Create Tables

```sql
USE testdb;

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add indexes if needed
CREATE INDEX idx_name ON user(name);
```

---

### Step 3: Configure Application Properties

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    # Update with your database credentials
    url: jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root  # or 'appuser' if you created one
    password: your_password_here
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  application:
    name: json-test
    
  data:
    redis:
      # Update if Redis is running on different host/port
      host: localhost
      port: 6379
      # password: your_redis_password  # Uncomment if Redis has password
      
server:
  port: 8080
  address: 0.0.0.0
  
logging:
  level:
    com.example.zzk: DEBUG  # Change to INFO in production
    org.springframework.web: INFO
    org.mybatis: INFO
```

**Security Note:** Never commit sensitive credentials to version control. Consider using environment variables:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/testdb}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
```

Then set environment variables:
```bash
export DB_PASSWORD=your_password_here
```

---

### Step 4: Build the Project

```bash
# Clean and build
mvn clean install

# Or skip tests if they're failing
mvn clean install -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30.123 s
```

**Common Build Issues:**

1. **Dependency Download Failures:**
   ```bash
   # Clear Maven cache and retry
   rm -rf ~/.m2/repository
   mvn clean install
   ```

2. **Java Version Mismatch:**
   ```bash
   # Verify Java version
   java -version
   mvn -version
   ```

---

## Running the Application

### Development Mode

**Option 1: Using Maven**
```bash
mvn spring-boot:run
```

**Option 2: Using Java directly**
```bash
# Build first
mvn clean package

# Run the JAR
java -jar target/json-test-0.0.1-SNAPSHOT.jar
```

**Option 3: Using IDE**
- Open project in IntelliJ IDEA or Eclipse
- Run `JsonTestApplication.java` main method

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.3.3)

2024-01-15 10:30:45.123  INFO 12345 --- [main] c.e.zzk.JsonTestApplication : Starting JsonTestApplication
...
2024-01-15 10:30:48.456  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
2024-01-15 10:30:48.789  INFO 12345 --- [main] c.e.zzk.JsonTestApplication : Started JsonTestApplication in 3.666 seconds
```

**Application is now running on:** `http://localhost:8080`

---

## Testing the Setup

### 1. Test REST API

```bash
# Test health (if health endpoint exists)
curl http://localhost:8080/actuator/health

# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User"}'

# Get user count
curl http://localhost:8080/api/users/count
```

### 2. Test WebSocket Connection

**Option 1: Using Browser Console**

Open browser console (F12) and run:
```javascript
const ws = new WebSocket('ws://localhost:8080/ws?user=testuser');

ws.onopen = () => console.log('Connected');
ws.onmessage = (event) => console.log('Received:', event.data);
ws.onerror = (error) => console.error('Error:', error);

// Send ping
ws.send('ping');
```

**Option 2: Using Test HTML Page**

Open `websocket-test.html` in your browser:
1. Enter a user ID (e.g., "alice")
2. Click "Connect"
3. Send a "ping" message
4. Check for "pong" response

### 3. Test WebSocket Broadcast

```bash
# Trigger broadcast from another terminal
curl http://localhost:8080/test/json/websocket/ping
```

All connected WebSocket clients should receive the broadcast message.

### 4. Verify Redis Connection

```bash
# Connect to Redis CLI
redis-cli

# Monitor messages
SUBSCRIBE ws:messages

# In another terminal, trigger a broadcast
curl http://localhost:8080/test/json/websocket/ping

# You should see the message in Redis monitor
```

---

## Multi-Instance Setup

For testing distributed WebSocket functionality, run multiple instances:

### Step 1: Create Multiple Configurations

**Instance 1 (Port 8080):** Use default `application.yml`

**Instance 2 (Port 8081):**
```bash
# Run with different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

**Instance 3 (Port 8082):**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

### Step 2: Create Profile-Specific Configurations (Alternative)

Create `application-instance2.yml`:
```yaml
server:
  port: 8081
```

Create `application-instance3.yml`:
```yaml
server:
  port: 8082
```

Run with profiles:
```bash
# Instance 2
mvn spring-boot:run -Dspring-boot.run.profiles=instance2

# Instance 3
mvn spring-boot:run -Dspring-boot.run.profiles=instance3
```

### Step 3: Test Multi-Instance Messaging

1. Connect WebSocket client to Instance 1 (port 8080)
2. Connect another client to Instance 2 (port 8081)
3. Trigger broadcast:
   ```bash
   curl http://localhost:8080/test/json/websocket/ping
   ```
4. Both clients should receive the message

---

## Docker Deployment

### Step 1: Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy Maven build
COPY target/json-test-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 2: Create Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: testdb
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - app-network

  app-instance1:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: rootpassword
      SPRING_DATA_REDIS_HOST: redis
      SERVER_PORT: 8080
    depends_on:
      - mysql
      - redis
    networks:
      - app-network

  app-instance2:
    build: .
    ports:
      - "8081:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: rootpassword
      SPRING_DATA_REDIS_HOST: redis
      SERVER_PORT: 8080
    depends_on:
      - mysql
      - redis
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  mysql-data:
```

### Step 3: Build and Run

```bash
# Build the application
mvn clean package -DskipTests

# Build and start Docker containers
docker-compose up -d

# View logs
docker-compose logs -f

# Stop containers
docker-compose down
```

---

## Production Deployment

### 1. Security Hardening

**Update `application.yml` for production:**

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      ssl: true

server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12

logging:
  level:
    com.example.zzk: INFO
    root: WARN
```

### 2. CORS Configuration

Update `CorsConfig.java` to restrict origins:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins(
                "https://your-production-domain.com",
                "https://www.your-production-domain.com"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
}
```

### 3. Database Connection Pooling

Add to `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 4. Production Build

```bash
# Build optimized JAR
mvn clean package -Pprod -DskipTests

# Run with production profile
java -jar target/json-test-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5. Systemd Service (Linux)

Create `/etc/systemd/system/websocket-app.service`:

```ini
[Unit]
Description=WebSocket Application
After=syslog.target mysql.service redis.service

[Service]
User=appuser
ExecStart=/usr/bin/java -jar /opt/websocket-app/json-test.jar
SuccessExitStatus=143
Environment="JAVA_OPTS=-Xmx512m -Xms256m"
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable websocket-app
sudo systemctl start websocket-app
sudo systemctl status websocket-app
```

---

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

#### 2. Database Connection Failed

**Error:**
```
java.sql.SQLNonTransientConnectionException: Could not connect to address
```

**Solutions:**
1. Verify MySQL is running:
   ```bash
   sudo systemctl status mysql  # Linux
   brew services list  # macOS
   ```

2. Test connection:
   ```bash
   mysql -h localhost -u root -p testdb
   ```

3. Check credentials in `application.yml`

4. Verify database exists:
   ```sql
   SHOW DATABASES;
   ```

#### 3. Redis Connection Failed

**Error:**
```
Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException
```

**Solutions:**
1. Verify Redis is running:
   ```bash
   redis-cli ping  # Should return PONG
   ```

2. Start Redis if stopped:
   ```bash
   redis-server  # Direct start
   sudo systemctl start redis  # Linux service
   brew services start redis  # macOS
   ```

3. Check Redis configuration in `application.yml`

#### 4. WebSocket Connection Refused

**Error (Browser Console):**
```
WebSocket connection to 'ws://localhost:8080/ws' failed
```

**Solutions:**
1. Ensure application is running
2. Check CORS configuration
3. Verify user parameter is provided
4. Check browser console for detailed error

#### 5. Build Failures

**Error:**
```
Failed to execute goal on project json-test: Could not resolve dependencies
```

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Update Maven dependencies
mvn clean install -U
```

### Getting Help

If you encounter issues not listed here:

1. Check application logs in `logs/` directory
2. Enable DEBUG logging in `application.yml`
3. Review [Architecture Documentation](ARCHITECTURE.md)
4. Check [API Documentation](API_DOCUMENTATION.md)
5. Open an issue on GitHub with:
   - Error message
   - Steps to reproduce
   - Environment details (OS, Java version, etc.)

---

## Next Steps

After successful setup:

1. Read [WebSocket Guide](WEBSOCKET_GUIDE.md) to understand WebSocket implementation
2. Review [API Documentation](API_DOCUMENTATION.md) for available endpoints
3. Study [Architecture Documentation](ARCHITECTURE.md) for system design
4. Explore the code and experiment with modifications
5. Consider implementing additional features

---

## Environment Variables Reference

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | Database JDBC URL | `jdbc:mysql://localhost:3306/testdb` | No |
| `DB_USERNAME` | Database username | `root` | No |
| `DB_PASSWORD` | Database password | - | Yes (prod) |
| `REDIS_HOST` | Redis host | `localhost` | No |
| `REDIS_PORT` | Redis port | `6379` | No |
| `REDIS_PASSWORD` | Redis password | - | No |
| `SERVER_PORT` | Application port | `8080` | No |

---

## Maintenance

### Backup Database

```bash
# Backup
mysqldump -u root -p testdb > backup_$(date +%Y%m%d).sql

# Restore
mysql -u root -p testdb < backup_20240115.sql
```

### Monitor Application

```bash
# View logs
tail -f logs/spring.log

# Monitor system resources
top -p $(pgrep -f json-test)

# Check active connections
netstat -an | grep :8080 | grep ESTABLISHED
```

### Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild
mvn clean package

# Restart service
sudo systemctl restart websocket-app
```

---

## Conclusion

You should now have a fully functional WebSocket application running. The setup supports:

- ✅ Single instance development
- ✅ Multi-instance distributed messaging
- ✅ Docker containerization
- ✅ Production deployment

For more details on specific features, refer to the other documentation files.
