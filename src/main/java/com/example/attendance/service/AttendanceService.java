package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {
    String createAttendance(Attendance attendance);
    List<Attendance> getAttendanceByStudentId(String studentId);
    Page<Attendance> getAttendancePage(Pageable pageable);
    Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable);

    // 适配多条件查询的方法签名
    Page<Attendance> getAttendanceByConditions(
            String studentId,
            String studentName,
            String courseId,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable);
}
