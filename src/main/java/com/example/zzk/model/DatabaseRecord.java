package com.example.zzk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseRecord {
    private Long id;
    private String name;
    private String data;
    private Long processingTimeMs;
}
