package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // 新增考勤
    @PostMapping
    public String create(@RequestBody Attendance attendance) {
        return attendanceService.createAttendance(attendance);
    }

    // 根据学生学号查考勤（不分页）
    @GetMapping("/student/{studentId}")
    public List<Attendance> getByStudent(@PathVariable String studentId) {
        return attendanceService.getAttendanceByStudentId(studentId);
    }

    // 分页查询所有考勤
    @GetMapping("/page")
    public Page<Attendance> getAttendancePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {

        // 1. 设置默认值，避免空指针
        if (sortField == null || sortField.isEmpty()) {
            // 适配你的字段，默认按考勤时间倒序
            sortField = "checkInTime";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }

        // 2. 只允许实体中存在的字段排序，防止报错
        if (!"id".equals(sortField) &&
                !"checkInTime".equals(sortField) &&
                !"createTime".equals(sortField) &&
                !"student.studentId".equals(sortField) &&
                !"studentName".equals(sortField) &&
                !"status".equals(sortField)) {
            sortField = "checkInTime";
        }

        // 3. 构建排序和分页对象
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return attendanceService.getAttendancePage(pageable);
    }

    // 按学生学号分页查询考勤
    @GetMapping("/page/student/{studentId}")
    public Page<Attendance> getAttendancePageByStudentId(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {

        if (sortField == null || sortField.isEmpty()) {
            sortField = "checkInTime";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        if (!"id".equals(sortField) &&
                !"checkInTime".equals(sortField) &&
                !"createTime".equals(sortField) &&
                !"status".equals(sortField)) {
            sortField = "checkInTime";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return attendanceService.getAttendancePageByStudentId(studentId, pageable);
    }

    // 多条件动态查询
    @GetMapping("/search")
    public Page<Attendance> searchAttendance(
            // 筛选条件
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            // 分页参数
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // 排序参数
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {

        // 处理排序参数
        if (sortField == null || sortField.isEmpty()) {
            sortField = "checkInTime";
        }
        if (sortDirection == null || sortDirection.isEmpty()) {
            sortDirection = "desc";
        }
        if (!"id".equals(sortField) &&
                !"checkInTime".equals(sortField) &&
                !"createTime".equals(sortField) &&
                !"student.studentId".equals(sortField) &&
                !"studentName".equals(sortField) &&
                !"status".equals(sortField)) {
            sortField = "checkInTime";
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return attendanceService.getAttendanceByConditions(studentId, studentName, courseId, status, startTime, endTime, pageable);
    }
}