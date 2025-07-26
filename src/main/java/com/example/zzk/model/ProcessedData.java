package com.example.zzk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedData {
    private Long id;
    private String status;
    private ValidationResult validation;
    private List<DatabaseRecord> databaseRecords;
    private ApiResponse apiResponse;
    private Map<String, Object> calculationResults;
    private Long processingTimeMs;
    private String processingType; // "SERIAL" or "PARALLEL"
}


