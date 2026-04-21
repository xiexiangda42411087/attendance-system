package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import java.util.List;

public interface AttendanceService {
    String createAttendance(Attendance attendance);
    List<Attendance> getAttendanceByStudentId(String studentId);
}
