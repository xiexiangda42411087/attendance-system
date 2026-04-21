package com.example.attendance.dao;

import com.example.attendance.entity.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 新增用户（不再硬编码角色，由 Service 层传入）
    public int insert(User user) {
        String sql = "INSERT INTO \"user\" (username, password, real_name, role, create_time) VALUES (?, ?, ?, ?, NOW())";
        return jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole()  // 使用实体中的角色，而非硬编码 TEACHER
        );
    }

    // 2. 根据 ID 查询
    public User findById(Integer id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);
        return users.isEmpty() ? null : users.get(0);  // 防止查不到抛异常
    }

    // 3. 根据用户名查询（登录验证）
    public User findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), username);
        return users.isEmpty() ? null : users.get(0);  // 防止查不到抛异常
    }

    // 4. 查询所有教师
    public List<User> findAllTeachers() {
        String sql = "SELECT * FROM \"user\" WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    // 5. 更新用户（补充 username 字段更新）
    public int update(User user) {
        String sql = "UPDATE \"user\" SET username = ?, password = ?, real_name = ?, role = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                user.getUsername(),   // 补充用户名更新
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getId()
        );
    }

    // 6. 根据 ID 删除
    public int deleteById(Integer id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 行映射器
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