package com.example.zzk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Spring Boot Application Class
 * 
 * This is the entry point for the WebSocket messaging application.
 * 
 * Key Features:
 * - Distributed WebSocket messaging with Redis pub/sub
 * - RESTful API endpoints for user management
 * - MyBatis Plus integration for database operations
 * - OpenFeign client for microservice communication
 * 
 * @EnableFeignClients - Enables Feign declarative REST clients
 * @SpringBootApplication - Auto-configuration and component scanning
 * @MapperScan - Scans for MyBatis mapper interfaces
 */
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.example.zzk.mapper")
public class JsonTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonTestApplication.class, args);
    }

}
