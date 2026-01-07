package com.shopjoy.dao;

import com.shopjoy.model.User;
import com.shopjoy.model.UserType;
import com.shopjoy.util.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for User entity. Uses DatabaseConfig for connections
 * and BCrypt for secure password hashing.
 */
public class UserDAO implements GenericDAO<User, Integer> {

    @Override
    public User findById(Integer userId) throws SQLException {
        if (userId == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT user_id, username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToUser(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findById SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT user_id, username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at FROM users ORDER BY username";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            System.out.println("findAll SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public User save(User user) throws SQLException {
        if (user == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            // Hash password before storing if provided in passwordHash field
            String plain = user.getPasswordHash();
            String hashed = null;
            if (plain != null) {
                hashed = BCrypt.hashpw(plain, BCrypt.gensalt());
            }

            String sql = "INSERT INTO users (username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING user_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, hashed);
                ps.setString(4, user.getFirstName());
                ps.setString(5, user.getLastName());
                ps.setString(6, user.getPhone());
                ps.setString(7, user.getUserType() != null ? user.getUserType().toString() : null);
                ps.setObject(8, user.getCreatedAt());
                ps.setObject(9, user.getUpdatedAt());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("user_id");
                        user.setUserId(id);
                        // store hashed password in memory
                        user.setPasswordHash(hashed);
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public User update(User user) throws SQLException {
        if (user == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE users SET email = ?, first_name = ?, last_name = ?, phone = ?, user_type = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getFirstName());
                ps.setString(3, user.getLastName());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getUserType() != null ? user.getUserType().toString() : null);
                ps.setInt(6, user.getUserId());
                ps.executeUpdate();
            }
            return user;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer userId) throws SQLException {
        if (userId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("delete SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public long count() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM users";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("cnt");
                return 0L;
            }
        } catch (SQLException e) {
            System.out.println("count SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    // --- Custom methods ---

    public User findByUsername(String username) throws SQLException {
        if (username == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT user_id, username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToUser(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findByUsername SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public User findByEmail(String email) throws SQLException {
        if (email == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT user_id, username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at FROM users WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToUser(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findByEmail SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) return null;
        User user = findByUsername(username);
        if (user == null) return null;
        String hash = user.getPasswordHash();
        if (hash == null) return null;
        try {
            if (BCrypt.checkpw(password, hash)) return user;
        } catch (Exception e) {
            System.out.println("authenticate error: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        if (email == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM users WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong("cnt") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            System.out.println("emailExists SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        if (username == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong("cnt") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            System.out.println("usernameExists SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<User> findByUserType(UserType userType) throws SQLException {
        List<User> users = new ArrayList<>();
        if (userType == null) return users;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT user_id, username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at FROM users WHERE user_type = ? ORDER BY username";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userType.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) users.add(mapResultSetToUser(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            System.out.println("findByUserType SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean changePassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hashed);
                ps.setInt(2, userId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("changePassword SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Map a ResultSet row to a User object. Handles enum conversion and timestamps.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        // password_hash stored but avoid logging or exposing it; keep in object for authentication
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setPhone(rs.getString("phone"));

        String ut = rs.getString("user_type");
        u.setUserType(ut != null ? UserType.fromString(ut) : null);

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) u.setCreatedAt(createdTs.toLocalDateTime());
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) u.setUpdatedAt(updatedTs.toLocalDateTime());

        return u;
    }
}
