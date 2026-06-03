package com.example.attendance.service;

import com.example.attendance.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();
    Course getCourseById(String courseId);
    Course saveCourse(Course course);
    void deleteCourse(String courseId);
    List<Course> getByTeacherId(Long teacherId);      // 按教师查询
    List<Course> getByClassName(String className);    // 按班级查询
}