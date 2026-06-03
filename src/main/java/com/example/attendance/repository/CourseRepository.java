package com.example.attendance.repository;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByClassName(String className);

    List<Course> findByWeekday(Integer weekday);
}