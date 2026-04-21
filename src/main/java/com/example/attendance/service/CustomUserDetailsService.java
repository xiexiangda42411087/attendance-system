package com.example.attendance.service;

import com.example.attendance.entity.User;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 2. 构建UserDetails对象，Spring Security 会自动匹配密码
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword()) // 数据库中存储的是BCrypt加密后的密码
                .roles(user.getRole()) // 角色会自动加 ROLE_ 前缀，数据库存ADMIN，这里会变成ROLE_ADMIN
                .build();
    }
}
