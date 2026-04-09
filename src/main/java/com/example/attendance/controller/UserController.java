package com.example.attendance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.attendance.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. 新增教师用户（POST 请求）
    @PostMapping("/teacher")
    public ResponseEntity<String> addTeacher(@RequestBody User user) {
        try {
            userService.addTeacher(user);
            return new ResponseEntity<>("教师新增成功", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. 根据 ID 查询用户
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Integer id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    // 3. 登录验证（根据用户名查询）
    @GetMapping("/username/{username}")
    public ResponseEntity<User> findByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    // 4. 查询所有教师
    @GetMapping("/teachers")
    public ResponseEntity<List<User>> findAllTeachers() {
        List<User> teachers = userService.findAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    // 5. 更新用户
    @PutMapping
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        try {
            userService.updateUser(user);
            return new ResponseEntity<>("用户更新成功", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 6. 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return new ResponseEntity<>("用户删除成功", HttpStatus.OK);
    }
}

