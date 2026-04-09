package com.example.attendance.controller;

public interface StudentService {
    String createStudent(Student student);
    Student getStudentById(String studentId);
}
