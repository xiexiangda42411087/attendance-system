package com.example.attendance.controller;

import com.example.attendance.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.attendance.service.UserService;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

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
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSafeMap(user));
    }

    // 4. 查询所有教师
    @GetMapping("/teachers")
    public ResponseEntity<List<Map<String, Object>>> findAllTeachers() {
        List<User> teachers = userService.findAllTeachers();
        List<Map<String, Object>> result = teachers.stream()
                .map(this::toSafeMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
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

    // 将 User 转为不含密码的安全 Map
    private Map<String, Object> toSafeMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("role", user.getRole());
        map.put("createTime", user.getCreateTime());
        return map;
    }
}

