package com.example.zzk.controller;

import com.example.zzk.mapper.JsonMapper;
import com.example.zzk.model.JsonEntity;
import com.example.zzk.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController()
@RequestMapping("/test/json")
@RequiredArgsConstructor

public class JsonTest {
    private final JsonMapper jsonMapper;

    @PostMapping("/append/json")
    public Result<String> appendJson(@RequestBody JsonEntity jsonEntity) {
        jsonMapper.insert(jsonEntity);
        return Result.success("JSON数据已成功插入到数据库", jsonEntity.getId().toString());
    }


}
