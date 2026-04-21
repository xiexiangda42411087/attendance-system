package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public String createAttendance(Attendance attendance) {
        // 校验学生是否存在
        Student student = studentRepository.findById(attendance.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        attendance.setStudent(student);
        attendanceRepository.save(attendance);
        return "考勤记录创建成功";
    }

    @Override
    public List<Attendance> getAttendanceByStudentId(String studentId) {
        return attendanceRepository.findByStudentStudentId(studentId);
    }
}
