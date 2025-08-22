package com.example.zzk.result;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private T data;  // 泛型，可以是 InfoAddVo 或其他类型

}