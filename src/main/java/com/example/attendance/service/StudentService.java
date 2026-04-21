package com.example.attendance.service;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import java.util.List;

public interface StudentService {
    String createStudent(Student student);
    Student getStudentById(String studentId);
    List<Student> getStudentsByClassName(String className);
    List<Student> getAllStudents();

    // ========== 新增方法 ==========
    String updateStudent(Student student);                        // 更新学生
    String deleteStudent(String studentId);                      // 删除学生
    String batchDelete(List<String> studentIds);                 // 批量删除
    Page<Student> findAllWithSearch(String keyword, String sortField,
                                    String sortDir, int page, int size);  // 分页+搜索+排序
}