package com.example.attendance.service;

import com.example.attendance.entity.Course;
import com.example.attendance.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(String courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    @Override
    public Course saveCourse(Course course) {
        if (course.getCreateTime() == null) {
            course.setCreateTime(LocalDateTime.now());
        }
        log.info("保存课程: {}", course.getCourseId());
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(String courseId) {
        log.info("删除课程: {}", courseId);
        courseRepository.deleteById(courseId);
    }

    @Override
    public List<Course> getByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<Course> getByClassName(String className) {
        return courseRepository.findByClassName(className);
    }
}