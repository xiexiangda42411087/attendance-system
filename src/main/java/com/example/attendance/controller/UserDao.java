package com.example.attendance.controller;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    // 构造器注入 JdbcTemplate（Spring 4.3+ 可省略 @Autowired）
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 新增教师用户
    public int insert(User user) {
        String sql = "INSERT INTO \"user\" (username, password, real_name, role, create_time) VALUES (?, ?, ?, ?, NOW())";
        return jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getRealName(), user.getRole());
    }

    // 2. 根据 ID 查询
    public User findById(Integer id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
    }

    // 3. 根据用户名查询（登录验证）
    public User findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
    }

    // 4. 查询所有教师
    public List<User> findAllTeachers() {
        String sql = "SELECT * FROM \"user\" WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    // 5. 更新用户
    public int update(User user) {
        String sql = "UPDATE \"user\" SET password = ?, real_name = ?, role = ? WHERE id = ?";
        return jdbcTemplate.update(sql, user.getPassword(), user.getRealName(), user.getRole(), user.getId());
    }

    // 6. 根据 ID 删除
    public int deleteById(Integer id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 行映射器：将 ResultSet 转为 User 对象
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRealName(rs.getString("real_name"));
            user.setRole(rs.getString("role"));
            user.setCreateTime(rs.getTimestamp("create_time"));
            return user;
        }
    }
}
