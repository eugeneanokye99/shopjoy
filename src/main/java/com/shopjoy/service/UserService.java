package com.shopjoy.service;

import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.User;
import com.shopjoy.model.UserType;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service handling user registration, authentication and profile management.
 */
public class UserService {

    private final UserDAO userDAO;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new customer user.
     * Returns saved User or null on validation/error.
     */
    public User registerUser(String username, String email, String password, String firstName, String lastName, String phone) {
        try {
            if (username == null || username.trim().isEmpty() || !isValidUsername(username)) {
                System.err.println("registerUser: invalid username");
                return null;
            }
            if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
                System.err.println("registerUser: invalid email");
                return null;
            }
            if (password == null || !isValidPassword(password)) {
                System.err.println("registerUser: invalid password");
                return null;
            }
            if (firstName == null) firstName = "";
            if (lastName == null) lastName = "";

            if (userDAO.usernameExists(username)) {
                System.err.println("registerUser: username exists");
                return null;
            }
            if (userDAO.emailExists(email)) {
                System.err.println("registerUser: email exists");
                return null;
            }

            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setPhone(phone);
            u.setUserType(UserType.CUSTOMER);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());

            // set plain password temporarily in the object; DAO will hash it
            u.setPasswordHash(password);
            return userDAO.save(u);
        } catch (SQLException e) {
            System.err.println("registerUser SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Authenticate user by username and password. Returns User on success, null otherwise.
     */
    public User authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("authenticateUser: missing credentials");
            return null;
        }
        try {
            User u = userDAO.authenticate(username, password);
            if (u == null) System.err.println("authenticateUser: failed login for " + username);
            return u;
        } catch (SQLException e) {
            System.err.println("authenticateUser SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user by id.
     */
    public User getUserById(int userId) {
        if (userId <= 0) return null;
        try {
            return userDAO.findById(userId);
        } catch (SQLException e) {
            System.err.println("getUserById SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user by username.
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        try {
            return userDAO.findByUsername(username);
        } catch (SQLException e) {
            System.err.println("getUserByUsername SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user by email.
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        try {
            return userDAO.findByEmail(email);
        } catch (SQLException e) {
            System.err.println("getUserByEmail SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update user profile. Returns true if update succeeded.
     */
    public boolean updateUserProfile(User user) {
        if (user == null || user.getUserId() <= 0) return false;
        try {
            // If email changed, ensure uniqueness
            User existing = userDAO.findById(user.getUserId());
            if (existing == null) return false;
            if (user.getEmail() != null && !user.getEmail().equalsIgnoreCase(existing.getEmail())) {
                if (userDAO.emailExists(user.getEmail())) {
                    System.err.println("updateUserProfile: email already in use");
                    return false;
                }
            }
            user.setUpdatedAt(LocalDateTime.now());
            userDAO.update(user);
            return true;
        } catch (SQLException e) {
            System.err.println("updateUserProfile SQLException: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change password for a user after verifying old password.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (userId <= 0 || oldPassword == null || newPassword == null) return false;
        if (!isValidPassword(newPassword)) {
            System.err.println("changePassword: new password does not meet requirements");
            return false;
        }
        try {
            User u = userDAO.findById(userId);
            if (u == null) return false;
            // verify old password using stored hash
            if (!BCrypt.checkpw(oldPassword, u.getPasswordHash())) {
                System.err.println("changePassword: old password incorrect");
                return false;
            }
            userDAO.changePassword(userId, newPassword);
            return true;
        } catch (SQLException e) {
            System.err.println("changePassword SQLException: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllCustomers() {
        try {
            return userDAO.findByUserType(UserType.CUSTOMER);
        } catch (SQLException e) {
            System.err.println("getAllCustomers SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<User> getAllAdmins() {
        try {
            return userDAO.findByUserType(UserType.ADMIN);
        } catch (SQLException e) {
            System.err.println("getAllAdmins SQLException: " + e.getMessage());
            return null;
        }
    }

    public boolean isAdmin(int userId) {
        try {
            User u = userDAO.findById(userId);
            return u != null && u.getUserType() == UserType.ADMIN;
        } catch (SQLException e) {
            System.err.println("isAdmin SQLException: " + e.getMessage());
            return false;
        }
    }

    public boolean promoteToAdmin(int userId) {
        try {
            User u = userDAO.findById(userId);
            if (u == null) return false;
            u.setUserType(UserType.ADMIN);
            userDAO.update(u);
            return true;
        } catch (SQLException e) {
            System.err.println("promoteToAdmin SQLException: " + e.getMessage());
            return false;
        }
    }

    public int getTotalUserCount() {
        try {
            long cnt = userDAO.count();
            return (int) Math.min(Integer.MAX_VALUE, cnt);
        } catch (SQLException e) {
            System.err.println("getTotalUserCount SQLException: " + e.getMessage());
            return 0;
        }
    }

    // --- Validation helpers ---

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidUsername(String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= 8;
    }
}
