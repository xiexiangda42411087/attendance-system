package com.example.attendance.config;

import com.example.attendance.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private final UserService userService;

    private final JdbcTemplate jdbcTemplate;

    public SecurityConfig(@Lazy UserService userService, JdbcTemplate jdbcTemplate) {
        this.userService = userService;
        this.jdbcTemplate = jdbcTemplate;
    }

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
                        .requestMatchers("/login", "/login-page", "/register-page",
                                "/api/register", "/api/auth/username/**",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/teacher").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/student/list", "/student/add", "/student/edit/**",
                                "/student/save", "/student/delete/**", "/student/batch-delete",
                                "/users/teacher-page").hasAnyRole("ADMIN")
                        .requestMatchers("/attendance/checkIn", "/attendance/list",
                                "/attendance/import", "/attendance/statistics").hasAnyRole("ADMIN", "TEACHER", "USER")
                        .requestMatchers("/course/list").hasAnyRole("ADMIN")
                        .requestMatchers("/api/course/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/leave/approve/**", "/api/leave/status/**",
                                "/api/leave/pending/count").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/leave/apply", "/api/leave/student/**").hasAnyRole("ADMIN", "TEACHER", "USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((req, res, authentication) -> {
                            res.setContentType("application/json;charset=utf-8");
                            res.setStatus(200);

                            String username = authentication.getName();
                            String role = authentication.getAuthorities().iterator().next().getAuthority();

                            // 从数据库查 studentId，查不到就用空字符串
                            String studentId = "";
                            try {
                                String sql = "SELECT student_id FROM \"user\" WHERE username = ?";
                                List<String> result = jdbcTemplate.query(sql,
                                        (rs, rowNum) -> rs.getString("student_id"), username);
                                if (!result.isEmpty() && result.get(0) != null) {
                                    studentId = result.get(0);
                                }
                            } catch (Exception ignored) {
                            }

                            String json = String.format(
                                    "{\"code\":200,\"msg\":\"登录成功\",\"username\":\"%s\",\"role\":\"%s\",\"studentId\":\"%s\"}",
                                    username, role, studentId
                            );
                            res.getWriter().write(json);
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