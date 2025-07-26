package com.example.zzk.controller;

import com.example.zzk.model.DataRequest;
import com.example.zzk.model.ProcessedData;
import com.example.zzk.result.Result;
import com.example.zzk.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceTestController {
    
    private final DataProcessingService dataProcessingService;

    /**
     * 串行处理接口 - 模拟原始的慢接口
     */
    @PostMapping("/process/serial")
    public Result<ProcessedData> processDataSerial(@RequestBody DataRequest request) {
        log.info("收到串行处理请求，ID: {}", request.getId());
        
        try {
            ProcessedData result = dataProcessingService.processDataSerial(request);
            return Result.success("串行处理完成", result);
        } catch (Exception e) {
            log.error("串行处理失败", e);
            return Result.error("串行处理失败: " + e.getMessage());
        }
    }

    /**
     * 并行处理接口 - 使用CompletableFuture优化后的接口
     */
    @PostMapping("/process/parallel")
    public Result<ProcessedData> processDataParallel(@RequestBody DataRequest request) {
        log.info("收到并行处理请求，ID: {}", request.getId());
        
        try {
            ProcessedData result = dataProcessingService.processDataParallel(request);
            return Result.success("并行处理完成", result);
        } catch (Exception e) {
            log.error("并行处理失败", e);
            return Result.error("并行处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取测试数据
     */
    @GetMapping("/test-data")
    public Result<DataRequest> getTestData() {
        DataRequest testData = new DataRequest();
        testData.setId(1001L);
        testData.setName("测试数据");
        testData.setCategory("性能测试");
        testData.setAmount(1000.0);
        
        return Result.success("测试数据生成成功", testData);
    }
}
