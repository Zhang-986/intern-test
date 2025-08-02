package com.example.zzk;


import com.example.zzk.mapper.JsonMapper;
import com.example.zzk.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class JsonTestApplicationTests {
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void insertIds() {
        jsonMapper.insert(new User());
    }

    @Test
    public void testConcurrentInsert() throws InterruptedException {
        int threadCount = 50;
        int insertCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Long> allIds = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < insertCount; j++) {
                        User user = new User();
                        user.setName("TestUser_" + Thread.currentThread().getId() + "_" + j);
                        jsonMapper.insert(user);
                        allIds.add(user.getId());
                        log.info("Thread: {}, Insert ID: {}", Thread.currentThread().getId(), user.getId());
                    }
                } catch (Exception e) {
                    log.error("Insert error: ", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // 检测ID重复
        Set<Long> uniqueIds = allIds.stream().collect(Collectors.toSet());
        log.info("Total inserted: {}, Unique IDs: {}", allIds.size(), uniqueIds.size());
        
        if (allIds.size() != uniqueIds.size()) {
            log.error("发现ID重复！总插入数: {}, 唯一ID数: {}", allIds.size(), uniqueIds.size());
            // 找出重复的ID
            Set<Long> duplicates = allIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
            log.error("重复的ID: {}", duplicates);
        } else {
            log.info("所有ID唯一，无重复");
        }
    }

    @Test
    public void testRealTimeDuplicateMonitor() throws InterruptedException {
        log.info("开始实时ID重复监控测试...");
        
        int threadCount = 20;
        int insertCount = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // 使用ConcurrentHashMap来实时监控ID重复
        ConcurrentHashMap<Long, AtomicInteger> idCounter = new ConcurrentHashMap<>();
        AtomicBoolean duplicateFound = new AtomicBoolean(false);
        AtomicLong totalInserted = new AtomicLong(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < insertCount; j++) {
                        User user = new User();
                        user.setName("RealTimeTest_" + threadIndex + "_" + j + "_" + System.currentTimeMillis());
                        jsonMapper.insert(user);
                        
                        Long userId = user.getId();
                        long currentTotal = totalInserted.incrementAndGet();
                        
                        // 实时检测ID重复
                        AtomicInteger count = idCounter.computeIfAbsent(userId, k -> new AtomicInteger(0));
                        int currentCount = count.incrementAndGet();
                        
                        if (currentCount > 1 && !duplicateFound.get()) {
                            duplicateFound.set(true);
                            log.error("🚨 实时检测到ID重复！ID: {}, 出现次数: {}, 总插入数: {}", 
                                userId, currentCount, currentTotal);
                            log.error("重复ID详情 - 线程: {}, 批次: {}", threadIndex, j);
                        }
                        
                        // 每100条记录输出一次统计
                        if (currentTotal % 100 == 0) {
                            log.info("已插入 {} 条记录，唯一ID数: {}", currentTotal, idCounter.size());
                        }
                    }
                } catch (Exception e) {
                    log.error("插入异常 - 线程 {}: ", threadIndex, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        // 最终统计
        long finalTotal = totalInserted.get();
        int uniqueCount = idCounter.size();
        
        log.info("=== 实时监控测试结果 ===");
        log.info("总插入记录数: {}", finalTotal);
        log.info("唯一ID数量: {}", uniqueCount);
        log.info("是否发现重复: {}", duplicateFound.get() ? "是" : "否");
        
        if (duplicateFound.get()) {
            // 列出所有重复的ID
            List<Map.Entry<Long, AtomicInteger>> duplicates = idCounter.entrySet().stream()
                .filter(entry -> entry.getValue().get() > 1)
                .sorted((a, b) -> b.getValue().get() - a.getValue().get())
                .collect(Collectors.toList());
            
            log.error("所有重复ID详情:");
            duplicates.forEach(entry -> 
                log.error("ID: {}, 重复次数: {}", entry.getKey(), entry.getValue().get()));
        }
    }
}
