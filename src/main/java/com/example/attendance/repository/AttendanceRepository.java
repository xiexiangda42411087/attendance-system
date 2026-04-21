package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
     //关联查询：根据学生学号查询该学生所有考勤记录
    List<Attendance> findByStudentStudentId(String studentId);
}
