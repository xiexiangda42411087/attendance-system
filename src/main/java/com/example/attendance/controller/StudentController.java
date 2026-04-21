package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 创建学生
    @PostMapping
    public String create(@RequestBody Student student) {
        return studentService.createStudent(student);
    }

    // 根据学号查询
    @GetMapping("/{studentId}")
    public Student getById(@PathVariable String studentId) {
        return studentService.getStudentById(studentId);
    }

    // 根据班级查询
    @GetMapping("/class")
    public List<Student> getByClass(@RequestParam String className) {
        return studentService.getStudentsByClassName(className);
    }

    // 查询所有
    @GetMapping
    public List<Student> getAll() {
        return studentService.getAllStudents();
    }
}
