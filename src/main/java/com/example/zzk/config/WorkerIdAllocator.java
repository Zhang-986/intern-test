package com.example.zzk.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
@Slf4j
@Component
public class WorkerIdAllocator {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_WORKER_ID = 1024;
    private static final int TTL_SECONDS = 30; // 心跳超时时间
    private static final int HEARTBEAT_INTERVAL = 10; // 心跳间隔
    
    private int allocatedWorkerId = -1;

    /**
     * 启动时自动分配workerId
     * 核心原理：尝试SETNX所有可能的workerId，直到成功
     */
    @PostConstruct
    public void allocateWorkerId() {
        String instanceId = getInstanceId(); // 本机唯一标识
        
        for (int workerId = 0; workerId < MAX_WORKER_ID; workerId++) {
            String key = "snowflake:worker:" + workerId;
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, instanceId, TTL_SECONDS, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                this.allocatedWorkerId = workerId;
                log.info("✓ Successfully allocated workerId: {}", workerId);
                
                // 启动心跳线程
                startHeartbeat(key, instanceId);
                return;
            }
        }
        
        // 所有workerId都被占用了（基本不会发生）
        throw new RuntimeException("Failed to allocate workerId: all slots are occupied");
    }
    
    /**
     * 心跳续期机制
     * 每10秒刷新一次TTL，保证Redis中的记录不会过期
     */
    private void startHeartbeat(String key, String instanceId) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WorkerIdHeartbeat");
            t.setDaemon(true);
            return t;
        });
        
        executor.scheduleAtFixedRate(() -> {
            try {
                // Lua脚本保证原子性：只有自己的心跳才能续期
                String luaScript = 
                    "if redis.call('GET', KEYS[1]) == ARGV[1] then\n" +
                    "  return redis.call('EXPIRE', KEYS[1], ARGV[2])\n" +
                    "else\n" +
                    "  return 0\n" +
                    "end";
                
                Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(key),
                    instanceId,
                    String.valueOf(TTL_SECONDS)
                );
                
                if (result == 0) {
                    log.error("✗ Heartbeat failed: workerId was taken by another instance!");
                    System.exit(1); // 致命错误，立即退出
                }
            } catch (Exception e) {
                log.error("Heartbeat error", e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * 优雅关闭：主动释放workerId
     * 让出资源给其他实例快速使用
     */
    @PreDestroy
    public void releaseWorkerId() {
        if (allocatedWorkerId < 0) return;
        
        try {
            String key = "snowflake:worker:" + allocatedWorkerId;
            redisTemplate.delete(key);
            log.info("✓ WorkerId {} released", allocatedWorkerId);
        } catch (Exception e) {
            log.error("Failed to release workerId", e);
        }
    }
    
    /**
     * 获取实例的唯一标识
     * 支持：Docker容器名 > 主机名 > IP地址
     */
    private String getInstanceId() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }
        
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
                return UUID.randomUUID().toString();
            }
        }
    }
    
    public int getWorkerId() {
        if (allocatedWorkerId < 0) {
            throw new RuntimeException("WorkerId not allocated yet");
        }
        return allocatedWorkerId;
    }
}