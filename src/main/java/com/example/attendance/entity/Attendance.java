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
    @Column(name = "id")
    private Long id;

    // 关联学生外键，和student表student_id对应
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "seat_row")
    private Integer seatRow;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
