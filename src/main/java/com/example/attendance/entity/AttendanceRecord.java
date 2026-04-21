package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "attendance_record")
public class AttendanceRecord {

    @Id
    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}

