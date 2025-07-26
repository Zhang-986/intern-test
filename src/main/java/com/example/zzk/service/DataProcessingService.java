package com.example.zzk.service;

import com.example.zzk.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class DataProcessingService {
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 串行处理数据 - 原始方式
     */
    public ProcessedData processDataSerial(DataRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始串行处理数据，ID: {}", request.getId());
        
        // 1. 数据验证 - 模拟100ms
        ValidationResult validation = validateData(request);
        
        // 2. 数据库查询 - 模拟200ms
        List<DatabaseRecord> dbRecords = queryDatabase(request.getId());
        
        // 3. 外部API调用 - 模拟300ms
        ApiResponse apiResponse = callExternalApi(request);
        
        // 4. 数据计算 - 模拟150ms
        Map<String, Object> calculations = performCalculations(request, dbRecords);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        log.info("串行处理完成，总耗时: {}ms", totalTime);
        
        return new ProcessedData(
            request.getId(),
            "SUCCESS",
            validation,
            dbRecords,
            apiResponse,
            calculations,
            totalTime,
            "SERIAL"
        );
    }

    /**
     * 并行处理数据 - CompletableFuture方式
     */
    public ProcessedData processDataParallel(DataRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始并行处理数据，ID: {}", request.getId());
        
        // 并行执行多个任务
        CompletableFuture<ValidationResult> validationFuture = 
            CompletableFuture.supplyAsync(() -> validateData(request), executorService);
        
        CompletableFuture<List<DatabaseRecord>> dbFuture = 
            CompletableFuture.supplyAsync(() -> queryDatabase(request.getId()), executorService);
        
        CompletableFuture<ApiResponse> apiFuture = 
            CompletableFuture.supplyAsync(() -> callExternalApi(request), executorService);
        
        // 等待前三个任务完成，然后执行计算（计算依赖数据库查询结果）
        CompletableFuture<ProcessedData> resultFuture = CompletableFuture.allOf(
            validationFuture, dbFuture, apiFuture
        ).thenApply(v -> {
            try {
                ValidationResult validation = validationFuture.join();
                List<DatabaseRecord> dbRecords = dbFuture.join();
                ApiResponse apiResponse = apiFuture.join();
                
                // 数据计算（这个可能依赖于数据库查询结果）
                Map<String, Object> calculations = performCalculations(request, dbRecords);
                
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                
                log.info("并行处理完成，总耗时: {}ms", totalTime);
                
                return new ProcessedData(
                    request.getId(),
                    "SUCCESS",
                    validation,
                    dbRecords,
                    apiResponse,
                    calculations,
                    totalTime,
                    "PARALLEL"
                );
            } catch (Exception e) {
                log.error("并行处理出错", e);
                throw new RuntimeException("处理失败", e);
            }
        });
        
        return resultFuture.join();
    }

    // 模拟数据验证 - 100ms
    private ValidationResult validateData(DataRequest request) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(100); // 模拟验证耗时
            boolean valid = request.getId() != null && request.getName() != null;
            long time = System.currentTimeMillis() - start;
            log.debug("数据验证完成，耗时: {}ms", time);
            return new ValidationResult(valid, valid ? "验证通过" : "验证失败", time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("验证被中断", e);
        }
    }

    // 模拟数据库查询 - 200ms
    private List<DatabaseRecord> queryDatabase(Long id) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(200); // 模拟数据库查询耗时
            List<DatabaseRecord> records = Arrays.asList(
                new DatabaseRecord(id, "Record1", "Data1", 0L),
                new DatabaseRecord(id + 1, "Record2", "Data2", 0L)
            );
            long time = System.currentTimeMillis() - start;
            log.debug("数据库查询完成，耗时: {}ms", time);
            records.forEach(record -> record.setProcessingTimeMs(time));
            return records;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("数据库查询被中断", e);
        }
    }

    // 模拟外部API调用 - 300ms
    private ApiResponse callExternalApi(DataRequest request) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(300); // 模拟API调用耗时
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("external_id", UUID.randomUUID().toString());
            responseData.put("category", request.getCategory());
            responseData.put("processed", true);
            
            long time = System.currentTimeMillis() - start;
            log.debug("外部API调用完成，耗时: {}ms", time);
            return new ApiResponse("SUCCESS", responseData, time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API调用被中断", e);
        }
    }

    // 模拟数据计算 - 150ms
    private Map<String, Object> performCalculations(DataRequest request, List<DatabaseRecord> dbRecords) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(150); // 模拟计算耗时
            Map<String, Object> results = new HashMap<>();
            results.put("total_records", dbRecords.size());
            results.put("calculated_value", request.getAmount() != null ? request.getAmount() * 1.1 : 0);
            results.put("complexity_score", Math.random() * 100);
            
            long time = System.currentTimeMillis() - start;
            log.debug("数据计算完成，耗时: {}ms", time);
            results.put("calculation_time_ms", time);
            return results;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("计算被中断", e);
        }
    }
}
