package com.example.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentDao studentDao;

    @Override
    public String createStudent(Student student) {
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        studentDao.insert(student);
        return "创建成功";
    }

    @Override
    public Student getStudentById(String studentId) {
        return studentDao.findById(studentId);
    }
}
