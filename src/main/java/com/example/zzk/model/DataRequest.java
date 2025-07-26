package com.example.zzk.model;

import lombok.Data;
import java.util.List;

@Data
public class DataRequest {
    private Long id;
    private String name;
    private String category;
    private List<String> tags;
    private Double amount;
}
