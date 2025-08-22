package com.example.zzk.feign;

import com.example.zzk.model.InfoAddVo;
import com.example.zzk.result.ApiResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "go-service", url = "http://localhost:9086")

public interface GoFeign {
    @GetMapping("/go/api")
    ApiResponse<String>getGoApi(@RequestParam("info") List<String> infoList);

    @PostMapping("/go/api/save")
    ApiResponse<InfoAddVo> postInfo(@RequestBody InfoAddVo info);

    @PostMapping("/go/api/solve")
    ApiResponse<Integer> solveNumber(@RequestBody List<Integer> numbers);
}
