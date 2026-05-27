package com.example.attendance.service;

import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {
    String createAttendance(Attendance attendance);
    List<Attendance> getAttendanceByStudentId(String studentId);
    Page<Attendance> getAttendancePage(Pageable pageable);
    Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable);
    Page<Attendance> getAttendanceByConditions(
            String studentId, String studentName, String courseId,
            String status, LocalDateTime startTime, LocalDateTime endTime,
            Pageable pageable);

    // 从 MultipartFile 导入
    ImportResult importFromExcel(MultipartFile file);

    // 学生考勤统计
    AttendanceStatisticsDTO getStudentStatistics(String studentId);

    // 学生时间段统计
    AttendanceStatisticsDTO getStudentStatisticsByDateRange(String studentId,
                                                            LocalDateTime startTime,
                                                            LocalDateTime endTime);

    // 课程统计
    AttendanceStatisticsDTO getCourseStatistics(String courseId,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime);
}