package com.shopjoy.test;

import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.User;
import com.shopjoy.model.UserType;
import com.shopjoy.util.DatabaseTestUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Simple integration-style test runner for UserDAO CRUD operations.
 * Run as a standard Java application (main method).
 */
public class UserDAOTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========== TESTING USER DAO ==========");
        UserDAO dao = new UserDAO();

            DatabaseTestUtil.printTestHeader("Create User");
            testCreateUser(dao);

            DatabaseTestUtil.printTestHeader("Find User By ID");
            testFindUserById(dao);

            DatabaseTestUtil.printTestHeader("Find All Users");
            testFindAllUsers(dao);

            DatabaseTestUtil.printTestHeader("Update User");
            testUpdateUser(dao);

            DatabaseTestUtil.printTestHeader("Find By Username");
            testFindByUsername(dao);

            DatabaseTestUtil.printTestHeader("Find By Email");
            testFindByEmail(dao);

            DatabaseTestUtil.printTestHeader("Authenticate");
            testAuthenticate(dao);

            DatabaseTestUtil.printTestHeader("Email Exists");
            testEmailExists(dao);

            DatabaseTestUtil.printTestHeader("Username Exists");
            testUsernameExists(dao);

            DatabaseTestUtil.printTestHeader("Change Password");
            testChangePassword(dao);

            DatabaseTestUtil.printTestHeader("Delete User");
            testDeleteUser(dao);

            DatabaseTestUtil.printTestHeader("Count Users");
            testCount(dao);

    }

    private static User createUser(UserDAO dao, String username, String email, String password, String first, String last, UserType type) throws SQLException {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(password); // DAO will hash this field
        u.setFirstName(first);
        u.setLastName(last);
        u.setUserType(type);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return dao.save(u);
    }

    private static void testCreateUser(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createUser(dao, "johndoe", "john@test.com", "password123", "John", "Doe", UserType.CUSTOMER);
            boolean ok = u != null && u.getUserId() > 0 && u.getPasswordHash() != null && !u.getPasswordHash().equals("password123");
            DatabaseTestUtil.printTestResult("testCreateUser", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCreateUser exception:");
            e.printStackTrace();
        }
    }

    private static void testFindUserById(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User created = createUser(dao, "findme", "findme@test.com", "pw123", "Find", "Me", UserType.CUSTOMER);
            User found = dao.findById(created.getUserId());
            boolean ok = found != null && "findme".equals(found.getUsername());
            DatabaseTestUtil.printTestResult("testFindUserById", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindUserById exception:");
            e.printStackTrace();
        }
    }

    private static void testFindAllUsers(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            createUser(dao, "u1", "u1@test.com", "p1", "A", "One", UserType.CUSTOMER);
            createUser(dao, "u2", "u2@test.com", "p2", "B", "Two", UserType.CUSTOMER);
            createUser(dao, "u3", "u3@test.com", "p3", "C", "Three", UserType.CUSTOMER);
            List<User> list = dao.findAll();
            boolean ok = list != null && list.size() == 3;
            System.out.println("Found users:");
            if (list != null) list.forEach(x -> System.out.println(" - " + x.getUsername()));
            DatabaseTestUtil.printTestResult("testFindAllUsers", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindAllUsers exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdateUser(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createUser(dao, "upd", "upd@test.com", "pw", "Old", "Name", UserType.CUSTOMER);
            u.setEmail("updated@test.com");
            u.setFirstName("NewFirst");
            u.setLastName("NewLast");
            dao.update(u);
            User re = dao.findById(u.getUserId());
            boolean ok = re != null && "updated@test.com".equals(re.getEmail()) && "NewFirst".equals(re.getFirstName());
            DatabaseTestUtil.printTestResult("testUpdateUser", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdateUser exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByUsername(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            createUser(dao, "testuser", "tu@test.com", "pw", "T", "U", UserType.CUSTOMER);
            User found = dao.findByUsername("testuser");
            boolean ok = found != null;
            User not = dao.findByUsername("no_such_user");
            ok = ok && not == null;
            DatabaseTestUtil.printTestResult("testFindByUsername", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByUsername exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByEmail(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            createUser(dao, "euser", "test@example.com", "pw", "E", "User", UserType.CUSTOMER);
            User found = dao.findByEmail("test@example.com");
            boolean ok = found != null;
            User not = dao.findByEmail("noemail@none.com");
            ok = ok && not == null;
            DatabaseTestUtil.printTestResult("testFindByEmail", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByEmail exception:");
            e.printStackTrace();
        }
    }

    private static void testAuthenticate(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            createUser(dao, "authuser", "auth@test.com", "password123", "A", "U", UserType.CUSTOMER);
            User okUser = dao.authenticate("authuser", "password123");
            User bad = dao.authenticate("authuser", "wrongpass");
            boolean ok = okUser != null && bad == null;
            DatabaseTestUtil.printTestResult("testAuthenticate", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testAuthenticate exception:");
            e.printStackTrace();
        }
    }

    private static void testEmailExists(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            createUser(dao, "ex1", "exists@test.com", "pw", "X", "One", UserType.CUSTOMER);
            boolean exists = dao.emailExists("exists@test.com");
            boolean not = dao.emailExists("nope@none.com");
            boolean ok = exists && !not;
            DatabaseTestUtil.printTestResult("testEmailExists", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testEmailExists exception:");
            e.printStackTrace();
        }
    }

    private static void testUsernameExists(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            createUser(dao, "existinguser", "exu@test.com", "pw", "E", "U", UserType.CUSTOMER);
            boolean exists = dao.usernameExists("existinguser");
            boolean not = dao.usernameExists("no_such_name");
            boolean ok = exists && !not;
            DatabaseTestUtil.printTestResult("testUsernameExists", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUsernameExists exception:");
            e.printStackTrace();
        }
    }

    private static void testChangePassword(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createUser(dao, "changepw", "cpw@test.com", "oldpassword", "C", "Pw", UserType.CUSTOMER);
            boolean changed = dao.changePassword(u.getUserId(), "newpassword");
            User authNew = dao.authenticate("changepw", "newpassword");
            User authOld = dao.authenticate("changepw", "oldpassword");
            boolean ok = changed && authNew != null && authOld == null;
            DatabaseTestUtil.printTestResult("testChangePassword", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testChangePassword exception:");
            e.printStackTrace();
        }
    }

    private static void testDeleteUser(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createUser(dao, "todelete", "del@test.com", "pw", "D", "E", UserType.CUSTOMER);
            boolean del = dao.delete(u.getUserId());
            User found = dao.findById(u.getUserId());
            boolean ok = del && found == null;
            DatabaseTestUtil.printTestResult("testDeleteUser", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testDeleteUser exception:");
            e.printStackTrace();
        }
    }

    private static void testCount(UserDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            for (int i = 0; i < 5; i++) {
                createUser(dao, "count" + i, "count" + i + "@test.com", "pw", "FN", "LN", UserType.CUSTOMER);
            }
            long cnt = dao.count();
            boolean ok = cnt == 5;
            DatabaseTestUtil.printTestResult("testCount", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCount exception:");
            e.printStackTrace();
        }
    }
}
