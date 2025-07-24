package com.example.zzk.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "go-service", url = "http://localhost:9086")

public interface GoFeign {
    @GetMapping("/go/api")
    public String getGoApi(@RequestParam("info") List<String> infoList);
}
