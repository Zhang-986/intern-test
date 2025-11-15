package com.example.zzk.controller;

import com.baomidou.mybatisplus.extension.ddl.history.IDdlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("/id/test")
public class IdTest {
    @GetMapping("/getSnowId")
    public String getSnowId() {
        return "snowId: " ;
    }
}
