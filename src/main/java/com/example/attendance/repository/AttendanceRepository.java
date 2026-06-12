package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    List<Attendance> findByStudentStudentId(String studentId);

    Page<Attendance> findByStudentStudentId(String studentId, Pageable pageable);

    // 统计某学生总考勤次数
    long countByStudentStudentId(String studentId);

    // 统计某学生指定状态的次数
    long countByStudentStudentIdAndStatus(String studentId, String status);

    // 时间范围统计
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentId = :studentId " +
            "AND a.checkInTime BETWEEN :startTime AND :endTime")
    long countByStudentIdAndDateRange(@Param("studentId") String studentId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    // 按状态和时间范围统计
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentId = :studentId " +
            "AND a.status = :status AND a.checkInTime BETWEEN :startTime AND :endTime")
    long countByStudentIdAndStatusAndDateRange(@Param("studentId") String studentId,
                                               @Param("status") String status,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    // 检查是否已打卡（同一学生同一课程同一天）
    @Query("SELECT COUNT(a) > 0 FROM Attendance a WHERE a.student.studentId = :studentId " +
            "AND a.courseId = :courseId AND a.checkInTime BETWEEN :startOfDay AND :endOfDay")
    boolean existsByStudentAndCourseAndDate(@Param("studentId") String studentId,
                                            @Param("courseId") String courseId,
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);

    // 班级统计：按课程统计各状态数量
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.courseId = :courseId " +
            "AND a.checkInTime BETWEEN :startTime AND :endTime GROUP BY a.status")
    List<Object[]> countByCourseGroupByStatus(@Param("courseId") String courseId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    // 某学生某课程的统计
    @Query("SELECT a.status, COUNT(a) FROM Attendance a " +
            "WHERE a.student.studentId = :studentId AND a.courseId = :courseId " +
            "GROUP BY a.status")
    List<Object[]> countByStudentAndCourseGroupByStatus(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId);
}