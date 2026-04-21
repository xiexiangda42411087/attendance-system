package com.example.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public String createStudent(Student student) {
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        studentRepository.save(student);
        return "创建成功";
    }

    @Override
    public Student getStudentById(String studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }

    // 对应Repository的班级查询
    @Override
    public List<Student> getStudentsByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    // 查询所有学生
    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}
