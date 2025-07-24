package com.example.zzk.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "test_json_entity" ,autoResultMap = true)
public class JsonEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    private Integer age;

    @TableField("is_active")
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Double price;
    private Float discount;
    private String description;

    // 修改为MyBatis Plus内置的Jackson处理器
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags; // 改为List接收
}