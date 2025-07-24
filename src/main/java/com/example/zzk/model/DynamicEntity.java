package com.example.zzk.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
@TableName(value = "dynamic_table")
public class DynamicEntity {

    // Getter和Setter方法
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> stringFields;

}