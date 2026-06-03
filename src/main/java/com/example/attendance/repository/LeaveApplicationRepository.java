package com.example.attendance.repository;

import com.example.attendance.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    // 按学生查询
    Page<LeaveApplication> findByStudentStudentId(String studentId, Pageable pageable);

    // 按状态查询
    Page<LeaveApplication> findByStatus(String status, Pageable pageable);

    // 按学生和状态查询
    Page<LeaveApplication> findByStudentStudentIdAndStatus(String studentId, String status, Pageable pageable);

    // 统计待审批数量
    long countByStatus(String status);

    // 查询某学生某时间段的请假
    @Query("SELECT l FROM LeaveApplication l WHERE l.student.studentId = :studentId " +
            "AND l.status = 'APPROVED' " +
            "AND ((l.startTime BETWEEN :startTime AND :endTime) " +
            "OR (l.endTime BETWEEN :startTime AND :endTime))")
    List<LeaveApplication> findApprovedLeaveByStudentAndDateRange(
            @Param("studentId") String studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}