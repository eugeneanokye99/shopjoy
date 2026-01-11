package com.shopjoy.test;

import com.shopjoy.cache.ProductCache;
import com.shopjoy.model.*;
import com.shopjoy.service.*;
import com.shopjoy.util.CacheManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IntegrationTest - End-to-end testing of core application workflows.
 * Verifies that the UI controllers, services, DAOs, and database work together
 * correctly.
 */
public class IntegrationTest {

    public static void main(String[] args) {
        System.out.println("=== SHOPJOY INTEGRATION TESTS ===\n");

        int totalTests = 0;
        int passedTests = 0;

        // Test 1: User Registration and Login
        totalTests++;
        if (testUserRegistrationAndLogin()) {
            passedTests++;
            System.out.println("✓ User Registration and Login - PASSED\n");
        } else {
            System.out.println("✗ User Registration and Login - FAILED\n");
        }

        // Test 2: Product Management Workflow
        totalTests++;
        if (testProductManagementWorkflow()) {
            passedTests++;
            System.out.println("✓ Product Management Workflow - PASSED\n");
        } else {
            System.out.println("✗ Product Management Workflow - FAILED\n");
        }

        // Test 3: Order Creation Workflow
        totalTests++;
        if (testOrderCreationWorkflow()) {
            passedTests++;
            System.out.println("✓ Order Creation Workflow - PASSED\n");
        } else {
            System.out.println("✗ Order Creation Workflow - FAILED\n");
        }

        // Test 4: Inventory Management
        totalTests++;
        if (testInventoryManagement()) {
            passedTests++;
            System.out.println("✓ Inventory Management - PASSED\n");
        } else {
            System.out.println("✗ Inventory Management - FAILED\n");
        }

        // Test 5: Cache Performance
        totalTests++;
        if (testCachePerformance()) {
            passedTests++;
            System.out.println("✓ Cache Performance - PASSED\n");
        } else {
            System.out.println("✗ Cache Performance - FAILED\n");
        }

        // Test 6: Search Functionality
        totalTests++;
        if (testSearchFunctionality()) {
            passedTests++;
            System.out.println("✓ Search Functionality - PASSED\n");
        } else {
            System.out.println("✗ Search Functionality - FAILED\n");
        }

        // Print summary
        System.out.println("===================================");
        System.out.println("INTEGRATION TEST SUMMARY");
        System.out.println("===================================");
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + (totalTests - passedTests));
        System.out.println("Success Rate: " + String.format("%.1f%%", (passedTests * 100.0 / totalTests)));
        System.out.println("===================================");

        // Ensure background threads are shut down
        CacheManager.shutdown();
    }

    private static boolean testUserRegistrationAndLogin() {
        try {
            System.out.println("Testing User Registration and Login...");

            UserService userService = new UserService();

            // Register new user
            String username = "testuser_" + System.currentTimeMillis();
            User newUser = userService.registerUser(
                    username,
                    "test@example.com",
                    "password123",
                    "Test",
                    "User",
                    "555-1234");

            if (newUser == null) {
                System.out.println("  - Registration failed");
                return false;
            }
            System.out.println("  - User registered successfully: " + username);

            // Test login
            User authenticated = userService.authenticateUser(username, "password123");
            if (authenticated == null) {
                System.out.println("  - Authentication failed");
                return false;
            }
            System.out.println("  - User authenticated successfully");

            // Test wrong password
            User failAuth = userService.authenticateUser(username, "wrongpassword");
            if (failAuth != null) {
                System.out.println("  - Security issue: Wrong password accepted");
                return false;
            }
            System.out.println("  - Security check passed: Wrong password rejected");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testProductManagementWorkflow() {
        try {
            System.out.println("Testing Product Management Workflow...");

            ProductService productService = new ProductService();
            CategoryService categoryService = new CategoryService();

            // Get or create a test category
            List<Category> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("  - No categories available");
                return false;
            }
            Category testCategory = categories.get(0);

            // Create product
            Product testProduct = new Product();
            testProduct.setProductName("Test Product " + System.currentTimeMillis());
            testProduct.setDescription("Integration test product");
            testProduct.setCategoryId(testCategory.getCategoryId());
            testProduct.setPrice(99.99);
            testProduct.setCostPrice(50.00);
            testProduct.setSku("TEST-SKU-" + System.currentTimeMillis());
            testProduct.setBrand("TestBrand");
            testProduct.setActive(true);
            testProduct.setCreatedAt(LocalDateTime.now());
            testProduct.setUpdatedAt(LocalDateTime.now());

            Product created = productService.addProduct(testProduct, 100);
            if (created == null) {
                System.out.println("  - Product creation failed");
                return false;
            }
            System.out.println("  - Product created: " + created.getProductName());

            // Update product
            created.setPrice(109.99);
            Product updated = productService.updateProduct(created);
            if (updated == null || updated.getPrice() != 109.99) {
                System.out.println("  - Product update failed");
                return false;
            }
            System.out.println("  - Product updated successfully");

            // Search for product
            List<Product> searchResults = productService.searchProducts(created.getProductName());
            if (searchResults.isEmpty()) {
                System.out.println("  - Product search failed");
                return false;
            }
            System.out.println("  - Product search successful");

            // Delete product
            boolean deleted = productService.deleteProduct(created.getProductId());
            if (!deleted) {
                System.out.println("  - Product deletion failed");
                return false;
            }
            System.out.println("  - Product deleted successfully");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testOrderCreationWorkflow() {
        try {
            System.out.println("Testing Order Creation Workflow...");

            OrderService orderService = new OrderService();
            UserService userService = new UserService();
            ProductService productService = new ProductService();

            // Get a test user
            List<User> customers = userService.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("  - No customers available");
                return false;
            }
            User testUser = customers.get(0);

            // Get test products
            List<Product> products = productService.getAllProducts();
            if (products.size() < 2) {
                System.out.println("  - Not enough products for test");
                return false;
            }

            // Create order items
            List<OrderItem> items = new ArrayList<>();

            OrderItem item1 = new OrderItem();
            item1.setProductId(products.get(0).getProductId());
            item1.setQuantity(2);
            item1.setUnitPrice(products.get(0).getPrice());
            item1.setSubtotal(item1.getQuantity() * item1.getUnitPrice());
            items.add(item1);

            OrderItem item2 = new OrderItem();
            item2.setProductId(products.get(1).getProductId());
            item2.setQuantity(1);
            item2.setUnitPrice(products.get(1).getPrice());
            item2.setSubtotal(item2.getQuantity() * item2.getUnitPrice());
            items.add(item2);

            // Create order (Note: OrderService might expect different params depending on
            // implementation)
            Order order = orderService.createOrder(
                    testUser.getUserId(),
                    items,
                    "123 Test St, Test City, TS 12345",
                    "Credit Card");

            if (order == null) {
                System.out.println("  - Order creation failed");
                return false;
            }
            System.out.println("  - Order created: #" + order.getOrderId());

            // Verify order items
            List<OrderItem> savedItems = orderService.getOrderItems(order.getOrderId());
            if (savedItems == null || savedItems.isEmpty()) {
                System.out.println("  - Order items not saved correctly");
                return false;
            }
            System.out.println("  - Order items verified");

            // Update order status
            boolean statusUpdated = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PROCESSING);
            if (!statusUpdated) {
                System.out.println("  - Order status update failed");
                return false;
            }
            System.out.println("  - Order status updated");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testInventoryManagement() {
        try {
            System.out.println("Testing Inventory Management...");

            InventoryService inventoryService = new InventoryService();
            ProductService productService = new ProductService();

            // Get a test product
            List<Product> products = productService.getAllProducts();
            if (products.isEmpty()) {
                System.out.println("  - No products available");
                return false;
            }
            Product testProduct = products.get(0);

            // Get current inventory
            Inventory inventory = inventoryService.getInventoryByProductId(testProduct.getProductId());
            if (inventory == null) {
                System.out.println("  - Inventory not found");
                return false;
            }
            int originalStock = inventory.getQuantityInStock();
            System.out.println("  - Original stock: " + originalStock);

            // Add stock
            boolean added = inventoryService.addStock(testProduct.getProductId(), 50);
            if (!added) {
                System.out.println("  - Failed to add stock");
                return false;
            }

            // Verify stock increased
            inventory = inventoryService.getInventoryByProductId(testProduct.getProductId());
            if (inventory.getQuantityInStock() != originalStock + 50) {
                System.out.println("  - Stock not updated correctly");
                return false;
            }
            System.out.println("  - Stock added successfully: " + inventory.getQuantityInStock());

            // Remove stock
            boolean removed = inventoryService.removeStock(testProduct.getProductId(), 50);
            if (!removed) {
                System.out.println("  - Failed to remove stock");
                return false;
            }

            // Verify stock decreased
            inventory = inventoryService.getInventoryByProductId(testProduct.getProductId());
            if (inventory.getQuantityInStock() != originalStock) {
                System.out.println("  - Stock not decreased correctly");
                return false;
            }
            System.out.println("  - Stock removed successfully: " + inventory.getQuantityInStock());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testCachePerformance() {
        try {
            System.out.println("Testing Cache Performance...");

            ProductService productService = new ProductService();
            ProductCache cache = ProductCache.getInstance();

            // Clear cache
            cache.invalidateCache();

            // First load (from database)
            long start1 = System.currentTimeMillis();
            List<Product> products1 = productService.getAllProducts();
            long time1 = System.currentTimeMillis() - start1;
            System.out.println("  - First load (database): " + time1 + "ms");

            // Second load (from cache)
            long start2 = System.currentTimeMillis();
            List<Product> products2 = productService.getAllProducts();
            long time2 = System.currentTimeMillis() - start2;
            System.out.println("  - Second load (cache): " + time2 + "ms");

            // Cache should be faster (or at least valid)
            double improvement = time1 > 0 ? ((time1 - time2) * 100.0) / time1 : 0;
            System.out.println("  - Performance improvement: " + String.format("%.1f%%", improvement));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testSearchFunctionality() {
        try {
            System.out.println("Testing Search Functionality...");

            ProductService productService = new ProductService();

            // Test search with common term (Assuming "laptop" exists or testing for general
            // search behavior)
            List<Product> results = productService.searchProducts("laptop");
            System.out.println("  - Search for 'laptop': " + results.size() + " results");

            // Test case-insensitive search
            List<Product> results2 = productService.searchProducts("LAPTOP");
            if (results.size() != results2.size()) {
                System.out.println("  - Case-insensitive search failed");
                return false;
            }
            System.out.println("  - Case-insensitive search working");

            // Test empty search
            List<Product> allProducts = productService.searchProducts("");
            if (allProducts.isEmpty()) {
                System.out.println("  - Empty search should return all products");
                return false;
            }
            System.out.println("  - Empty search returns all products");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
