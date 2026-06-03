package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 查询所有课程
    @GetMapping
    public ResponseEntity<List<Course>> getAll() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // 按ID查询
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getById(@PathVariable String courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    // 创建课程
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Course course) {
        try {
            Course saved = courseService.saveCourse(course);
            log.info("课程创建成功: {}", saved.getCourseId());
            return ResponseEntity.ok(Map.of("code", 200, "msg", "课程创建成功", "data", saved));
        } catch (Exception e) {
            log.error("课程创建失败", e);
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    // 更新课程
    @PutMapping("/{courseId}")
    public ResponseEntity<?> update(@PathVariable String courseId, @RequestBody Course course) {
        course.setCourseId(courseId);
        try {
            Course saved = courseService.saveCourse(course);
            log.info("课程更新成功: {}", saved.getCourseId());
            return ResponseEntity.ok(Map.of("code", 200, "msg", "课程更新成功", "data", saved));
        } catch (Exception e) {
            log.error("课程更新失败", e);
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    // 删除课程
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> delete(@PathVariable String courseId) {
        try {
            courseService.deleteCourse(courseId);
            log.info("课程删除成功: {}", courseId);
            return ResponseEntity.ok(Map.of("code", 200, "msg", "课程删除成功"));
        } catch (Exception e) {
            log.error("课程删除失败", e);
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    // 按教师ID查询
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(courseService.getByTeacherId(teacherId));
    }

    // 按班级查询
    @GetMapping("/class/{className}")
    public ResponseEntity<List<Course>> getByClass(@PathVariable String className) {
        return ResponseEntity.ok(courseService.getByClassName(className));
    }
}