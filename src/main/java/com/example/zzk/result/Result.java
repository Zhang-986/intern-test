package com.example.zzk.result;

import lombok.Data;


@Data
public class Result<T> {
    private Integer code; // 状态码
    private String msg; // 提示信息
    private T data; // 数据（使用泛型）

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功返回，不带数据
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功返回，带数据
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功返回，自定义消息和数据
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // 失败返回，默认状态码和消息
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }

    // 失败返回，自定义消息
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 失败返回，自定义状态码和消息
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 失败返回，自定义状态码、消息和数据
    public static <T> Result<T> error(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    // 自定义返回
    public static <T> Result<T> of(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}