package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course")
public class Course {
    @Id
    @Column(name = "course_id", length = 20, nullable = false)
    private String courseId;

    @Column(name = "course_name", length = 100, nullable = false)
    private String courseName;

    @Column(name = "class_name", length = 50, nullable = false)
    private String className;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "classroom_name", length = 50)
    private String classroomName;

    @Column(name = "start_time")
    private java.time.LocalTime startTime;

    @Column(name = "end_time")
    private java.time.LocalTime endTime;

    @Column(name = "weekday")
    private Integer weekday;        // 1-7 对应周一到周日

    @Column(name = "start_week")
    private Integer startWeek;

    @Column(name = "end_week")
    private Integer endWeek;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}