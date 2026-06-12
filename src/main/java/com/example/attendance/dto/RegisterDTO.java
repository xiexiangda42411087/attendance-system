package com.example.attendance.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String studentId;
    private String realName;
    private String password;
}