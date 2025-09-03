package com.stockManagement.dao.interfaces.impl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.stockManagement.dao.interfaces.UserDao;
import com.stockManagement.models.User;

@Repository
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbc;

    public UserDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM tbl_users WHERE username = ?";
        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                return u;
            }
            return null;
        }, username);
    }
}