package com.shopjoy.util;

import com.shopjoy.dao.CategoryDAO;
import com.shopjoy.dao.InventoryDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.Category;
import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;
import com.shopjoy.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Test utilities for setting up and tearing down database state during tests.
 */
public class DatabaseTestUtil {

    public static void printTestHeader(String testName) {
        System.out.println("========== Testing: " + testName + " ==========");
    }

    public static void printTestResult(String operation, boolean success) {
        if (success) System.out.println("\u2713 " + operation + " - PASSED");
        else System.out.println("\u2717 " + operation + " - FAILED");
    }

    /**
     * Delete all records from tables in order respecting FK constraints.
     */
    public static void clearAllTables() {
        String[] tables = new String[]{
                "order_items",
                "orders",
                "reviews",
                "addresses",
                "inventory",
                "products",
                "categories",
                "users"
        };

        for (String table : tables) {
            try (Connection conn = DatabaseConfig.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table)) {
                int affected = ps.executeUpdate();
                System.out.println("Cleared table " + table + ", rows deleted: " + affected);
            } catch (SQLException e) {
                System.err.println("clearAllTables: failed to clear " + table + ": " + e.getMessage());
            } finally {
                // release handled in DAO/DatabaseConfig if needed
            }
        }
    }

    public static User insertTestUser() {
        UserDAO dao = new UserDAO();
        User u = new User();
        u.setUsername("testuser");
        u.setEmail("test@example.com");
        // UserDAO.save expects plain password in passwordHash field which it will hash
        u.setPasswordHash("password123");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        try {
            User saved = dao.save(u);
            System.out.println("Inserted test user id=" + (saved != null ? saved.getUserId() : "null"));
            return saved;
        } catch (SQLException e) {
            System.err.println("insertTestUser: " + e.getMessage());
            return null;
        }
    }

    public static Category insertTestCategory() {
        CategoryDAO dao = new CategoryDAO();
        Category c = new Category();
        c.setCategoryName("Test Category");
        c.setDescription("Test Description");
        c.setCreatedAt(LocalDateTime.now());
        try {
            Category saved = dao.save(c);
            System.out.println("Inserted test category id=" + (saved != null ? saved.getCategoryId() : "null"));
            return saved;
        } catch (SQLException e) {
            System.err.println("insertTestCategory: " + e.getMessage());
            return null;
        }
    }

    public static Product insertTestProduct(int categoryId) {
        ProductDAO dao = new ProductDAO();
        Product p = new Product();
        p.setProductName("Test Product");
        p.setDescription("Test product for integration tests");
        p.setCategoryId(categoryId);
        p.setPrice(99.99);
        p.setCostPrice(50.0);
        p.setSku("TEST-SKU-001");
        p.setBrand("TestBrand");
        p.setImageUrl(null);
        p.setActive(true);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        try {
            Product saved = dao.save(p);
            System.out.println("Inserted test product id=" + (saved != null ? saved.getProductId() : "null"));
            return saved;
        } catch (SQLException e) {
            System.err.println("insertTestProduct: " + e.getMessage());
            return null;
        }
    }

    public static Inventory insertTestInventory(int productId, int quantity) {
        InventoryDAO dao = new InventoryDAO();
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setQuantityInStock(quantity);
        inv.setReorderLevel(10);
        inv.setWarehouseLocation("MAIN");
        inv.setLastRestocked(LocalDateTime.now());
        inv.setUpdatedAt(LocalDateTime.now());
        try {
            Inventory saved = dao.save(inv);
            System.out.println("Inserted test inventory id=" + (saved != null ? saved.getInventoryId() : "null"));
            return saved;
        } catch (SQLException e) {
            System.err.println("insertTestInventory: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reset database: clear tables and insert minimal test data.
     */
    public static void resetDatabase() {
        System.out.println("Resetting test database...");
        clearAllTables();
        Category cat = insertTestCategory();
        User user = insertTestUser();
        if (cat != null) {
            Product prod = insertTestProduct(cat.getCategoryId());
            if (prod != null) insertTestInventory(prod.getProductId(), 100);
        }
        System.out.println("Database reset complete.");
    }
}
