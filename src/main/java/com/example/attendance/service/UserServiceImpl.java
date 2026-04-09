package com.example.attendance.service;

import com.example.attendance.controller.User;
import com.example.attendance.controller.UserDao;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void addTeacher(User user) {
        // 可添加业务校验：比如用户名重复校验
        if (userDao.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在！");
        }
        // 新增用户（角色强制为 TEACHER）
        user.setRole("TEACHER");
        userDao.insert(user);
    }

    @Override
    public User findById(Integer id) {
        return userDao.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> findAllTeachers() {
        return userDao.findAllTeachers();
    }

    @Override
    public void updateUser(User user) {
        // 校验用户是否存在
        if (userDao.findById(user.getId()) == null) {
            throw new RuntimeException("用户不存在！");
        }
        userDao.update(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userDao.deleteById(id);
    }
}

