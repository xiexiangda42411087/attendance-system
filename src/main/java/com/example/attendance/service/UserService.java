package com.example.attendance.service;

import com.example.attendance.entity.User;
import java.util.List;

public interface UserService {
    void addTeacher(User user);
    User findById(Integer id);
    User findByUsername(String username);
    List<User> findAllTeachers();
    void updateUser(User user);
    void deleteUser(Integer id);
    void register(User user);
}

