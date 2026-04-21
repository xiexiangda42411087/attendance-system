package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/login-page", "/register-page",
                                "/api/register", "/api/auth/username/**",
                                "/css/**", "/js/**", "/images/**",
                                "/student/list", "/student/add", "/student/edit/**",
                                "/student/save", "/student/delete/**", "/student/batch-delete",
                                "/attendance/checkIn",          // 打卡页面
                                "/attendance/list"              // 考勤记录列表
                        ).permitAll()

                        // 教师管理接口：仅 ADMIN 可操作（新增、删除、更新教师）
                        .requestMatchers(
                                "/api/users/teacher",    // POST 新增教师
                                "/api/users/{id}",        // DELETE 删除用户
                                "/api/users"              // PUT 更新用户
                        ).hasRole("ADMIN")

                        // 查询接口：ADMIN 和 TEACHER 可查看
                        .requestMatchers(
                                "/api/users/teachers",    // GET 查询所有教师
                                "/api/users/{id}"         // GET 按ID查询（与DELETE同路径但方法不同）
                        ).hasAnyRole("ADMIN", "TEACHER")

                        // 学生接口
                        .requestMatchers("/api/student/**").hasAnyRole("ADMIN", "TEACHER", "USER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((req, res, authentication) -> {
                            res.setContentType("application/json;charset=utf-8");
                            res.setStatus(200);
                            String username = authentication.getName();
                            String role = authentication.getAuthorities().iterator().next().getAuthority();
                            res.getWriter().write(
                                    "{\"code\":200,\"msg\":\"登录成功\",\"username\":\""
                                            + username + "\",\"role\":\"" + role + "\"}"
                            );
                        })
                        .failureHandler((req, res, ex) -> {
                            res.setContentType("application/json;charset=utf-8");
                            res.setStatus(401);
                            res.getWriter().write("{\"code\":401,\"msg\":\"用户名或密码错误\"}");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setContentType("application/json;charset=utf-8");
                            res.setStatus(200);
                            res.getWriter().write("{\"code\":200,\"msg\":\"登出成功\"}");
                        })
                        .permitAll()
                );

        return http.build();
    }
}