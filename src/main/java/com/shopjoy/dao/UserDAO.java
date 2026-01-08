package com.shopjoy.dao;

import com.shopjoy.model.User;
import com.shopjoy.model.UserType;
import com.shopjoy.util.DbConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for User entity.
 * Uses DbConfig (no pooling) and BCrypt for password hashing.
 */
public class UserDAO implements GenericDAO<User, Integer> {

    @Override
    public User findById(Integer userId) throws SQLException {
        if (userId == null) return null;

        String sql = """
                SELECT user_id, username, email, password_hash,
                       first_name, last_name, phone, user_type,
                       created_at, updated_at
                FROM users
                WHERE user_id = ?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToUser(rs) : null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, email, password_hash,
                       first_name, last_name, phone, user_type,
                       created_at, updated_at
                FROM users
                ORDER BY username
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public User save(User user) throws SQLException {
        if (user == null) return null;

        String plain = user.getPasswordHash();
        String hashed = plain != null ? BCrypt.hashpw(plain, BCrypt.gensalt()) : null;

        String sql = """
                INSERT INTO users
                (username, email, password_hash, first_name, last_name,
                 phone, user_type, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING user_id
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, hashed);
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getUserType() != null
                    ? user.getUserType().toString().toLowerCase()
                    : null);
            ps.setObject(8, user.getCreatedAt());
            ps.setObject(9, user.getUpdatedAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt("user_id"));
                    user.setPasswordHash(hashed);
                }
            }
        }
        return user;
    }

    @Override
    public User update(User user) throws SQLException {
        if (user == null) return null;

        String sql = """
                UPDATE users
                SET email = ?, first_name = ?, last_name = ?,
                    phone = ?, user_type = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getUserType() != null ? user.getUserType().toString().toLowerCase() : null);
            ps.setInt(6, user.getUserId());

            ps.executeUpdate();
        }
        return user;
    }

    @Override
    public boolean delete(Integer userId) throws SQLException {
        if (userId == null) return false;

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM users";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // -------- Custom methods --------

    public User findByUsername(String username) throws SQLException {
        if (username == null) return null;

        String sql = """
                SELECT user_id, username, email, password_hash,
                       first_name, last_name, phone, user_type,
                       created_at, updated_at
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToUser(rs) : null;
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        if (email == null) return null;

        String sql = """
                SELECT user_id, username, email, password_hash,
                       first_name, last_name, phone, user_type,
                       created_at, updated_at
                FROM users
                WHERE email = ?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToUser(rs) : null;
            }
        }
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) return null;

        User user = findByUsername(username);
        if (user == null || user.getPasswordHash() == null) return null;

        return BCrypt.checkpw(password, user.getPasswordHash()) ? user : null;
    }

    public boolean emailExists(String email) throws SQLException {
        if (email == null) return false;

        String sql = "SELECT COUNT(*) AS cnt FROM users WHERE email = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong("cnt") > 0;
            }
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        if (username == null) return false;

        String sql = "SELECT COUNT(*) AS cnt FROM users WHERE username = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong("cnt") > 0;
            }
        }
    }

    public List<User> findByUserType(UserType userType) throws SQLException {
        List<User> users = new ArrayList<>();
        if (userType == null) return users;

        String sql = """
                SELECT user_id, username, email, password_hash,
                       first_name, last_name, phone, user_type,
                       created_at, updated_at
                FROM users
                WHERE user_type = ?
                ORDER BY username
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userType.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    public boolean changePassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null) return false;

        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        String sql = """
                UPDATE users
                SET password_hash = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashed);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // -------- Mapping --------

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setPhone(rs.getString("phone"));

        String ut = rs.getString("user_type");
        u.setUserType(ut != null ? UserType.fromString(ut) : null);

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) u.setUpdatedAt(updated.toLocalDateTime());

        return u;
    }
}
