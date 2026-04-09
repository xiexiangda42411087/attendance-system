package com.example.attendance.controller;

import java.sql.Timestamp;
import lombok.Data; // 需引入 lombok 依赖，也可手动写 get/set

@Data // 自动生成 get/set/toString 等方法
public class User {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private Timestamp createTime;
}
