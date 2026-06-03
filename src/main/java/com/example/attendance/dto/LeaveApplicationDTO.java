package com.example.attendance.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveApplicationDTO {
    private String studentId;
    private String courseId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
}