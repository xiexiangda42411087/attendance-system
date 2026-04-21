package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.StudentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if (attendance.getStudent() == null || attendance.getStudent().getStudentId() == null) {
            throw new RuntimeException("学生信息不能为空");
        }
        Student student = studentRepository.findById(attendance.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("学生不存在！"));

        attendance.setStudent(student);
        attendance.setStudentName(student.getName());
        attendance.setCreateTime(LocalDateTime.now());

        // 判断迟到（8:00前正常，之后迟到，供打卡时调用）
        if (attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(LocalDateTime.now());
        }
        if (attendance.getStatus() == null) {
            attendance.setStatus("NORMAL");
        }

        attendanceRepository.save(attendance);
        return "考勤记录创建成功";
    }

    @Override
    public List<Attendance> getAttendanceByStudentId(String studentId) {
        return attendanceRepository.findByStudentStudentId(studentId);
    }

    @Override
    public Page<Attendance> getAttendancePage(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable) {
        return attendanceRepository.findByStudentStudentId(studentId, pageable);
    }

    @Override
    public Page<Attendance> getAttendanceByConditions(
            String studentId, String studentName, String courseId,
            String status, LocalDateTime startTime, LocalDateTime endTime,
            Pageable pageable) {

        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null && !studentId.isEmpty()) {
                predicates.add(cb.equal(root.get("student").get("studentId"), studentId));
            }
            if (studentName != null && !studentName.isEmpty()) {
                predicates.add(cb.like(root.get("studentName"), "%" + studentName + "%"));
            }
            if (courseId != null && !courseId.isEmpty()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return attendanceRepository.findAll(spec, pageable);
    }
}