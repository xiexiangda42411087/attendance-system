package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leave_application")
public class LeaveApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "status", length = 20, nullable = false)
    private String status;  // PENDING/APPROVED/REJECTED

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "approver_id")
    private Integer approverId;

    @Column(name = "approver_remark")
    private String approverRemark;
}