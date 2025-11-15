package com.example.zzk.config;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisDistributedIdGenerator implements IdentifierGenerator {
    @Autowired
    private WorkerIdAllocator workerIdAllocator;
    
    private SnowflakeGenerator snowflakeWorker;
    
    @PostConstruct
    public void init() {
        int workerId = workerIdAllocator.getWorkerId();
        int datacenterId = 0; // 如果有多个数据中心，可以从配置读取
        this.snowflakeWorker = new SnowflakeGenerator(workerId, datacenterId);
    }
    
    @Override
    public Long nextId(Object entity) {
        return snowflakeWorker.next();
    }
}