package com.example.attendance.service;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
        // 检查学号是否已存在
        if (studentRepository.existsById(student.getStudentId())) {
            throw new RuntimeException("学号已存在！");
        }
        studentRepository.save(student);
        return "创建成功";
    }

    @Override
    public Student getStudentById(String studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }

    @Override
    public List<Student> getStudentsByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ==================== 新增方法实现 ====================

    @Override
    public String updateStudent(Student student) {
        // 检查学生是否存在
        if (!studentRepository.existsById(student.getStudentId())) {
            throw new RuntimeException("学生不存在！");
        }
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        studentRepository.save(student);
        return "更新成功";
    }

    @Override
    public String deleteStudent(String studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("学生不存在！");
        }
        studentRepository.deleteById(studentId);
        return "删除成功";
    }

    @Override
    public String batchDelete(List<String> studentIds) {
        int count = 0;
        for (String id : studentIds) {
            if (studentRepository.existsById(id)) {
                studentRepository.deleteById(id);
                count++;
            }
        }
        return "成功删除 " + count + " 名学生";
    }

    @Override
    public Page<Student> findAllWithSearch(String keyword, String sortField, String sortDir,
                                           int page, int size) {
        // 构建排序对象，默认降序时传 "desc"
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // 如果有搜索关键字，走模糊查询；否则查全部
        if (StringUtils.hasText(keyword)) {
            return studentRepository.search(keyword, pageable);
        }
        return studentRepository.findAll(pageable);
    }
}