package com.example.zzk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.zzk.mapper")
public class JsonTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonTestApplication.class, args);
    }

}
