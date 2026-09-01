package com.syslog.api.model.repository;

import com.syslog.api.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<User> MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getTimestamp("updated_at"));

    public Optional<User> findByEmail(String email) {
        return jdbc.query("""
                SELECT id, name, email, username, password, updated_at
                FROM user_entity
                WHERE email = ?
                """, MAPPER, email).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        return jdbc.query("""
                SELECT id, name, email, username, password, updated_at
                FROM user_entity
                WHERE id = ?
                """, MAPPER, id).stream().findFirst();
    }

    public Long insertAndReturnId(User user) {
        return jdbc.queryForObject("""
                INSERT INTO user_entity (name, email, username, password, updated_at)
                VALUES (?, ?, ?, ?, current_timestamp)
                RETURNING id
                """, Long.class, user.getName(), user.getEmail(), user.getUsername(), user.getPassword());
    }

    public int updatePassword(Long id, String password) {
        return jdbc.update("""
                UPDATE user_entity
                SET password = ?, updated_at = current_timestamp
                WHERE id = ?
                """, password, id);
    }

    public int updateUser(User user) {
        return jdbc.update("""
                UPDATE user_entity
                SET name = ?, email = ?, username = ?, password = ?, updated_at = current_timestamp
                WHERE id = ?
                """, user.getName(), user.getEmail(), user.getUsername(), user.getPassword(), user.getId());
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM user_entity WHERE id = ?", id);
    }
}
