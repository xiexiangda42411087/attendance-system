package com.example.attendance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceStatisticsDTO {
    private long totalCount;        // 总考勤次数
    private long normalCount;       // 正常次数
    private long lateCount;         // 迟到次数
    private long earlyCount;        // 早退次数
    private long absentCount;       // 缺勤次数
    private double attendanceRate;  // 出勤率（百分比）
}