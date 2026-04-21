package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "student")
public class Student {
    @Id
    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "class_name", length = 30)
    private String className;

    // ========== 新增字段 ==========
    @Column(name = "gender", length = 10)
    private String gender;              // 性别：男/女

    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;        // 出生日期

    @Column(name = "phone", length = 20)
    private String phone;               // 联系方式

    @Column(name = "email", length = 100)
    private String email;               // 邮箱
}