package com.shopjoy.test;

import com.shopjoy.dao.CategoryDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.model.Category;
import com.shopjoy.model.Product;
import com.shopjoy.util.DatabaseTestUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDAOTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========== TESTING PRODUCT DAO ==========");
        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

            DatabaseTestUtil.printTestHeader("Create Product");
            testCreateProduct(productDAO);

            DatabaseTestUtil.printTestHeader("Find Product By ID");
            testFindProductById(productDAO);

            DatabaseTestUtil.printTestHeader("Find All Products");
            testFindAllProducts(productDAO);

            DatabaseTestUtil.printTestHeader("Update Product");
            testUpdateProduct(productDAO);

            DatabaseTestUtil.printTestHeader("Find By Category");
            testFindByCategory(productDAO, categoryDAO);

            DatabaseTestUtil.printTestHeader("Search By Name");
            testSearchByName(productDAO);

            DatabaseTestUtil.printTestHeader("Find Active Products");
            testFindActiveProducts(productDAO);

            DatabaseTestUtil.printTestHeader("Find By Price Range");
            testFindByPriceRange(productDAO);

            DatabaseTestUtil.printTestHeader("Find By SKU");
            testFindBySKU(productDAO);

            DatabaseTestUtil.printTestHeader("Delete Product");
            testDeleteProduct(productDAO);

            DatabaseTestUtil.printTestHeader("Count Products");
            testCount(productDAO);

    }

    private static Product createTestProduct(ProductDAO dao, int categoryId, String name, double price, String sku) throws SQLException {
        Product p = new Product();
        p.setProductName(name);
        p.setDescription("Test product: " + name);
        p.setCategoryId(categoryId);
        p.setPrice(price);
        p.setCostPrice(price * 0.6);
        p.setSku(sku);
        p.setBrand("TestBrand");
        p.setImageUrl(null);
        p.setActive(true);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return dao.save(p);
    }

    private static void testCreateProduct(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            Product p = createTestProduct(dao, cat.getCategoryId(), "Dell Laptop", 1299.99, "DELL-001");
            boolean ok = p != null && p.getProductId() > 0 && "Dell Laptop".equals(p.getProductName()) && p.getPrice() == 1299.99;
            DatabaseTestUtil.printTestResult("testCreateProduct", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCreateProduct exception:");
            e.printStackTrace();
        }
    }

    private static void testFindProductById(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            Product created = createTestProduct(dao, cat.getCategoryId(), "FindMe", 199.99, "FIND-001");
            Product found = dao.findById(created.getProductId());
            boolean ok = found != null && "FindMe".equals(found.getProductName());
            DatabaseTestUtil.printTestResult("testFindProductById", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindProductById exception:");
            e.printStackTrace();
        }
    }

    private static void testFindAllProducts(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            for (int i = 0; i < 5; i++) createTestProduct(dao, cat.getCategoryId(), "P" + i, 10 + i, "SKU" + i);
            List<Product> list = dao.findAll();
            boolean ok = list != null && list.size() == 5;
            System.out.println("Products:");
            if (list != null) list.forEach(x -> System.out.println(" - " + x.getProductName()));
            DatabaseTestUtil.printTestResult("testFindAllProducts", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindAllProducts exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdateProduct(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            Product p = createTestProduct(dao, cat.getCategoryId(), "Updatable", 50.0, "UPD-001");
            p.setPrice(75.0);
            p.setDescription("Updated desc");
            p.setActive(false);
            dao.update(p);
            Product r = dao.findById(p.getProductId());
            boolean ok = r != null && r.getPrice() == 75.0 && !r.isActive();
            DatabaseTestUtil.printTestResult("testUpdateProduct", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdateProduct exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByCategory(ProductDAO dao, CategoryDAO cdao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category c1 = DatabaseTestUtil.insertTestCategory();
            Category c2 = new Category();
            c2.setCategoryName("Cat2");
            c2.setDescription("Second");
            c2.setCreatedAt(LocalDateTime.now());
            c2 = cdao.save(c2);

            for (int i = 0; i < 3; i++) createTestProduct(dao, c1.getCategoryId(), "C1P" + i, 10 + i, "C1SKU" + i);
            for (int i = 0; i < 2; i++) createTestProduct(dao, c2.getCategoryId(), "C2P" + i, 20 + i, "C2SKU" + i);

            List<Product> list = dao.findByCategory(c1.getCategoryId());
            boolean ok = list != null && list.size() == 3;
            DatabaseTestUtil.printTestResult("testFindByCategory", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByCategory exception:");
            e.printStackTrace();
        }
    }

    private static void testSearchByName(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            createTestProduct(dao, cat.getCategoryId(), "Dell Laptop", 100, "S1");
            createTestProduct(dao, cat.getCategoryId(), "HP Laptop", 110, "S2");
            createTestProduct(dao, cat.getCategoryId(), "Apple iPhone", 999, "S3");

            List<Product> laptops = dao.searchByName("laptop");
            List<Product> phones = dao.searchByName("phone");
            boolean ok = laptops != null && laptops.size() == 2 && phones != null && phones.size() == 1;
            DatabaseTestUtil.printTestResult("testSearchByName", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testSearchByName exception:");
            e.printStackTrace();
        }
    }

    private static void testFindActiveProducts(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            for (int i = 0; i < 3; i++) createTestProduct(dao, cat.getCategoryId(), "Active" + i, 10, "A" + i);
            for (int i = 0; i < 2; i++) {
                Product p = createTestProduct(dao, cat.getCategoryId(), "Inactive" + i, 5, "I" + i);
                p.setActive(false);
                dao.update(p);
            }
            List<Product> actives = dao.findActiveProducts();
            boolean ok = actives != null && actives.size() == 3;
            DatabaseTestUtil.printTestResult("testFindActiveProducts", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindActiveProducts exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByPriceRange(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            createTestProduct(dao, cat.getCategoryId(), "P50", 50.00, "PR50");
            createTestProduct(dao, cat.getCategoryId(), "P100", 100.00, "PR100");
            createTestProduct(dao, cat.getCategoryId(), "P150", 150.00, "PR150");
            createTestProduct(dao, cat.getCategoryId(), "P200", 200.00, "PR200");
            createTestProduct(dao, cat.getCategoryId(), "P250", 250.00, "PR250");

            List<Product> range = dao.findByPriceRange(100.00, 200.00);
            boolean ok = range != null && range.size() == 3;
            DatabaseTestUtil.printTestResult("testFindByPriceRange", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByPriceRange exception:");
            e.printStackTrace();
        }
    }

    private static void testFindBySKU(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            createTestProduct(dao, cat.getCategoryId(), "SKUProd", 10.0, "UNIQUE-SKU-001");
            Product found = dao.findBySKU("UNIQUE-SKU-001");
            Product not = dao.findBySKU("NO-SUCH-SKU");
            boolean ok = found != null && not == null;
            DatabaseTestUtil.printTestResult("testFindBySKU", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindBySKU exception:");
            e.printStackTrace();
        }
    }

    private static void testDeleteProduct(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            Product p = createTestProduct(dao, cat.getCategoryId(), "ToDelete", 30.0, "DEL-001");
            boolean del = dao.delete(p.getProductId());
            Product found = dao.findById(p.getProductId());
            boolean ok = del && found == null;
            DatabaseTestUtil.printTestResult("testDeleteProduct", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testDeleteProduct exception:");
            e.printStackTrace();
        }
    }

    private static void testCount(ProductDAO dao) {
        try {
            DatabaseTestUtil.clearAllTables();
            Category cat = DatabaseTestUtil.insertTestCategory();
            for (int i = 0; i < 7; i++) createTestProduct(dao, cat.getCategoryId(), "Cnt" + i, 10.0 + i, "CNSK" + i);
            long cnt = dao.count();
            boolean ok = cnt == 7;
            DatabaseTestUtil.printTestResult("testCount", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCount exception:");
            e.printStackTrace();
        }
    }
}
