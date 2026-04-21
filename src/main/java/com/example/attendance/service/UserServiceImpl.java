package com.example.attendance.service;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void addTeacher(User user) {
        if (userDao.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在！");
        }
        user.setRole("TEACHER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        User existingUser = userDao.findById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在！");
        }

        // 如果传入了新密码且非空，则加密后更新
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 未传入密码或为空，保留原密码
            user.setPassword(existingUser.getPassword());
        }

        // 保留不允许为空的字段
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            user.setUsername(existingUser.getUsername());
        }
        if (user.getRealName() == null) {
            user.setRealName(existingUser.getRealName());
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole(existingUser.getRole());
        }

        userDao.update(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userDao.deleteById(id);
    }

    @Override
    public void register(User user) {
        if (userDao.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在！");
        }
        // 默认注册角色为学生 USER
        user.setRole("USER");
        // 密码加密（统一在此处加密，Controller 无需重复加密）
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.insert(user);
    }
}