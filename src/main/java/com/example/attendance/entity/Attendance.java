package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 匹配数据库外键 student_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "student_name", length = 50, nullable = false)
    private String studentName;

    @Column(name = "course_id", length = 20, nullable = false)
    private String courseId;

    @Column(name = "course_name", length = 100)
    private String courseName;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "seat_row")
    private Integer seatRow;

    @Column(name = "seat_col")
    private Integer seatCol;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "remark")
    private String remark;

    @Column(name = "ip", length = 15)
    private String ip;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}