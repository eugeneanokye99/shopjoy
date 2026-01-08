package com.shopjoy.test;

import com.shopjoy.dao.CategoryDAO;
import com.shopjoy.dao.InventoryDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.Category;
import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;
import com.shopjoy.model.User;
import com.shopjoy.util.DatabaseTestUtil;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryDAOTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("===== TESTING INVENTORY DAO =====");

        InventoryDAO invDao = new InventoryDAO();
        ProductDAO prodDao = new ProductDAO();
        CategoryDAO catDao = new CategoryDAO();
        UserDAO userDao = new UserDAO();

            DatabaseTestUtil.printTestHeader("Create Inventory");
            testCreateInventory(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Find By ProductId");
            testFindByProductId(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Update Stock");
            testUpdateStock(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Increment Stock");
            testIncrementStock(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Decrement Stock");
            testDecrementStock(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Find Low Stock Items");
            testFindLowStockItems(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Find Out Of Stock Items");
            testFindOutOfStockItems(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Check Stock Availability");
            testCheckStockAvailability(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Update Last Restocked");
            testUpdateLastRestocked(invDao, prodDao, catDao, userDao);

            DatabaseTestUtil.printTestHeader("Delete Inventory");
            testDeleteInventory(invDao, prodDao, catDao, userDao);

    }

    private static User createTestUser(UserDAO udao, String username, String email) throws Exception {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash("password");
        u.setFirstName("T");
        u.setLastName("U");
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return udao.save(u);
    }

    private static Category createTestCategory(CategoryDAO cdao, String name) throws Exception {
        Category c = new Category();
        c.setCategoryName(name);
        c.setDescription("desc");
        return cdao.save(c);
    }

    private static Product createTestProduct(ProductDAO pdao, int categoryId, String name) throws Exception {
        Product p = new Product();
        p.setProductName(name);
        p.setDescription("desc");
        p.setCategoryId(categoryId);
        p.setPrice(9.99);
        p.setSku("SKU" + System.currentTimeMillis());
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return pdao.save(p);
    }

    private static Inventory createTestInventory(InventoryDAO invDao, int productId, int qty, int reorder) throws Exception {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setQuantityInStock(qty);
        inv.setReorderLevel(reorder);
        inv.setWarehouseLocation("WH1");
        inv.setLastRestocked(LocalDateTime.now());
        inv.setUpdatedAt(LocalDateTime.now());
        return invDao.save(inv);
    }

    private static void testCreateInventory(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "invuser", "inv@test.com");
            Category c = createTestCategory(cDao, "icat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "iprod");
            Inventory inv = createTestInventory(invDao, p.getProductId(), 100, 10);
            boolean ok = inv != null && inv.getInventoryId() > 0;
            DatabaseTestUtil.printTestResult("testCreateInventory", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCreateInventory exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByProductId(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "fipuser", "fip@test.com");
            Category c = createTestCategory(cDao, "ficat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "fiprod");
            createTestInventory(invDao, p.getProductId(), 20, 5);
            Inventory inv = invDao.findByProductId(p.getProductId());
            boolean ok = inv != null && inv.getProductId() == p.getProductId();
            DatabaseTestUtil.printTestResult("testFindByProductId", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByProductId exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdateStock(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "upinv", "upinv@test.com");
            Category c = createTestCategory(cDao, "upcat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "upprod");
            createTestInventory(invDao, p.getProductId(), 100, 10);
            boolean updated = invDao.updateStock(p.getProductId(), 50);
            Inventory inv = invDao.findByProductId(p.getProductId());
            boolean ok = updated && inv != null && inv.getQuantityInStock() == 50;
            DatabaseTestUtil.printTestResult("testUpdateStock", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdateStock exception:");
            e.printStackTrace();
        }
    }

    private static void testIncrementStock(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "incinv", "incinv@test.com");
            Category c = createTestCategory(cDao, "inccat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "incprod");
            createTestInventory(invDao, p.getProductId(), 50, 10);
            boolean ok1 = invDao.incrementStock(p.getProductId(), 25);
            Inventory inv = invDao.findByProductId(p.getProductId());
            boolean ok = ok1 && inv != null && inv.getQuantityInStock() == 75;
            DatabaseTestUtil.printTestResult("testIncrementStock", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testIncrementStock exception:");
            e.printStackTrace();
        }
    }

    private static void testDecrementStock(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "decinv", "decinv@test.com");
            Category c = createTestCategory(cDao, "deccat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "decprod");
            createTestInventory(invDao, p.getProductId(), 100, 10);
            boolean ok1 = invDao.decrementStock(p.getProductId(), 30);
            Inventory after1 = invDao.findByProductId(p.getProductId());
            boolean okAfter1 = ok1 && after1 != null && after1.getQuantityInStock() == 70;

            // try to decrement by too much
            boolean ok2 = invDao.decrementStock(p.getProductId(), 100);
            Inventory after2 = invDao.findByProductId(p.getProductId());
            boolean okAfter2 = !ok2 && after2 != null && after2.getQuantityInStock() == 70;

            boolean ok = okAfter1 && okAfter2;
            DatabaseTestUtil.printTestResult("testDecrementStock", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testDecrementStock exception:");
            e.printStackTrace();
        }
    }

    private static void testFindLowStockItems(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "lowinv", "lowinv@test.com");
            Category c = createTestCategory(cDao, "lowcat");
            Product p1 = createTestProduct(pDao, c.getCategoryId(), "low1");
            Product p2 = createTestProduct(pDao, c.getCategoryId(), "low2");
            Product p3 = createTestProduct(pDao, c.getCategoryId(), "low3");
            createTestInventory(invDao, p1.getProductId(), 5, 10); // low
            createTestInventory(invDao, p2.getProductId(), 50, 10); // ok
            createTestInventory(invDao, p3.getProductId(), 8, 10); // low
            List<Inventory> low = invDao.findLowStockItems();
            boolean ok = low != null && low.size() == 2;
            DatabaseTestUtil.printTestResult("testFindLowStockItems", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindLowStockItems exception:");
            e.printStackTrace();
        }
    }

    private static void testFindOutOfStockItems(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "outinv", "outinv@test.com");
            Category c = createTestCategory(cDao, "outcat");
            Product p1 = createTestProduct(pDao, c.getCategoryId(), "out1");
            Product p2 = createTestProduct(pDao, c.getCategoryId(), "out2");
            Product p3 = createTestProduct(pDao, c.getCategoryId(), "out3");
            createTestInventory(invDao, p1.getProductId(), 0, 5); // out
            createTestInventory(invDao, p2.getProductId(), 10, 5); // ok
            createTestInventory(invDao, p3.getProductId(), 0, 5); // out
            List<Inventory> out = invDao.findOutOfStockItems();
            boolean ok = out != null && out.size() == 2;
            DatabaseTestUtil.printTestResult("testFindOutOfStockItems", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindOutOfStockItems exception:");
            e.printStackTrace();
        }
    }

    private static void testCheckStockAvailability(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "chkinv", "chkinv@test.com");
            Category c = createTestCategory(cDao, "chkcat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "chkprod");
            createTestInventory(invDao, p.getProductId(), 50, 5);
            boolean ok1 = invDao.checkStockAvailability(p.getProductId(), 30);
            boolean ok2 = !invDao.checkStockAvailability(p.getProductId(), 100);
            boolean ok = ok1 && ok2;
            DatabaseTestUtil.printTestResult("testCheckStockAvailability", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCheckStockAvailability exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdateLastRestocked(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "restock", "restock@test.com");
            Category c = createTestCategory(cDao, "restcat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "restprod");
            createTestInventory(invDao, p.getProductId(), 10, 5);
            boolean updated = invDao.updateLastRestocked(p.getProductId());
            Inventory inv = invDao.findByProductId(p.getProductId());
            boolean ok = updated && inv != null && inv.getLastRestocked() != null;
            DatabaseTestUtil.printTestResult("testUpdateLastRestocked", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdateLastRestocked exception:");
            e.printStackTrace();
        }
    }

    private static void testDeleteInventory(InventoryDAO invDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(uDao, "delinv", "delinv@test.com");
            Category c = createTestCategory(cDao, "delcat");
            Product p = createTestProduct(pDao, c.getCategoryId(), "delprod");
            Inventory inv = createTestInventory(invDao, p.getProductId(), 15, 5);
            boolean del = invDao.delete(inv.getInventoryId());
            Inventory found = invDao.findByProductId(p.getProductId());
            boolean ok = del && (found == null || found.getInventoryId() != inv.getInventoryId());
            DatabaseTestUtil.printTestResult("testDeleteInventory", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testDeleteInventory exception:");
            e.printStackTrace();
        }
    }
}
