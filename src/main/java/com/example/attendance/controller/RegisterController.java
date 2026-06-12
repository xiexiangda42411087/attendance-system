package com.example.attendance.controller;

import com.example.attendance.dto.RegisterDTO;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.StudentService;
import com.example.attendance.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
public class RegisterController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;

    public RegisterController(UserService userService,
                              PasswordEncoder passwordEncoder,
                              StudentService studentService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<String> register(@RequestBody RegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("学号不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("密码至少6位");
        }

        // 校验学号是否在 student 表中存在
        Student student = studentService.getStudentById(dto.getStudentId());
        if (student == null) {
            return ResponseEntity.badRequest().body("学号不存在，请先联系管理员添加学生信息");
        }

        // 检查用户名是否已存在
        User existUser = userService.findByUsername(dto.getUsername());
        if (existUser != null) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRealName(dto.getRealName() != null ? dto.getRealName() : student.getName());
        user.setRole("USER");
        user.setStudentId(dto.getStudentId());

        userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("注册成功");
    }
}