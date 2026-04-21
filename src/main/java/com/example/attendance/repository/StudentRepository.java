package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByClassName(String className);

    @Query("SELECT s FROM Student s WHERE " +
            "s.studentId LIKE %:keyword% OR " +
            "s.name LIKE %:keyword% OR " +
            "s.className LIKE %:keyword%")
    Page<Student> search(@Param("keyword") String keyword, Pageable pageable);
}