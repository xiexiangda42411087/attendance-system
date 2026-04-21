package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    // 根据学生学号查考勤
    @GetMapping("/student/{studentId}")
    public List<Attendance> getByStudent(@PathVariable String studentId) {
        return attendanceService.getAttendanceByStudentId(studentId);
    }
}
