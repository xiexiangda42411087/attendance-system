package com.example.attendance.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int successCount = 0;
    private int failCount = 0;
    private int totalCount = 0;
    private List<String> errorDetails = new ArrayList<>();

    public void incrementSuccess() {
        this.successCount++;
        this.totalCount++;
    }

    public void incrementFail(String rowInfo, String reason) {
        this.failCount++;
        this.totalCount++;
        this.errorDetails.add("第" + rowInfo + "行: " + reason);
    }

    public boolean hasErrors() {
        return failCount > 0;
    }
}