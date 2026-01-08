//package com.shopjoy.test;
//
//import com.shopjoy.dao.CategoryDAO;
//import com.shopjoy.dao.ProductDAO;
//import com.shopjoy.model.Category;
//import com.shopjoy.model.Product;
//import com.shopjoy.util.DatabaseConfig;
//import com.shopjoy.util.DatabaseTestUtil;
//
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//import java.util.List;
//
//public class CategoryDAOTest {
//    private static int passed = 0;
//    private static int failed = 0;
//
//    public static void main(String[] args) {
//        System.out.println("========== TESTING CATEGORY DAO ==========");
//        CategoryDAO categoryDAO = new CategoryDAO();
//        ProductDAO productDAO = new ProductDAO();
//
//        try {
//            DatabaseTestUtil.printTestHeader("Create Category");
//            testCreateCategory(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Find Category By ID");
//            testFindCategoryById(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Find All Categories");
//            testFindAllCategories(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Update Category");
//            testUpdateCategory(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Find Top Level Categories");
//            testFindTopLevelCategories(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Find Subcategories");
//            testFindSubcategories(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Find By Name");
//            testFindByName(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Has Products");
//            testHasProducts(categoryDAO, productDAO);
//
//            DatabaseTestUtil.printTestHeader("Get Product Count");
//            testGetProductCount(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Delete Category");
//            testDeleteCategory(categoryDAO);
//
//            DatabaseTestUtil.printTestHeader("Count Categories");
//            testCount(categoryDAO);
//
//        } finally {
//            System.out.println("Tests complete. Passed: " + passed + ", Failed: " + failed);
//            DatabaseConfig.getInstance().closeAllConnections();
//        }
//    }
//
//    private static void testCreateCategory(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = new Category();
//            c.setCategoryName("Electronics");
//            c.setDescription("Electronic items");
//            c.setCreatedAt(LocalDateTime.now());
//            Category saved = dao.save(c);
//            boolean ok = saved != null && saved.getCategoryId() > 0;
//            DatabaseTestUtil.printTestResult("testCreateCategory", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testCreateCategory exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindCategoryById(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = new Category();
//            c.setCategoryName("FindCat");
//            c.setDescription("Desc");
//            c.setCreatedAt(LocalDateTime.now());
//            Category saved = dao.save(c);
//            Category found = dao.findById(saved.getCategoryId());
//            boolean ok = found != null && "FindCat".equals(found.getCategoryName());
//            DatabaseTestUtil.printTestResult("testFindCategoryById", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindCategoryById exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindAllCategories(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            for (int i = 0; i < 4; i++) {
//                Category c = new Category();
//                c.setCategoryName("Cat" + i);
//                c.setDescription("Desc" + i);
//                c.setCreatedAt(LocalDateTime.now());
//                dao.save(c);
//            }
//            List<Category> list = dao.findAll();
//            boolean ok = list != null && list.size() == 4;
//            System.out.println("Categories:");
//            if (list != null) list.forEach(x -> System.out.println(" - " + x.getCategoryName()));
//            DatabaseTestUtil.printTestResult("testFindAllCategories", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindAllCategories exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testUpdateCategory(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = new Category();
//            c.setCategoryName("ToUpdate");
//            c.setDescription("Old");
//            c.setCreatedAt(LocalDateTime.now());
//            Category saved = dao.save(c);
//            saved.setCategoryName("UpdatedName");
//            saved.setDescription("NewDesc");
//            dao.update(saved);
//            Category r = dao.findById(saved.getCategoryId());
//            boolean ok = r != null && "UpdatedName".equals(r.getCategoryName()) && "NewDesc".equals(r.getDescription());
//            DatabaseTestUtil.printTestResult("testUpdateCategory", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testUpdateCategory exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindTopLevelCategories(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            // create 3 top-level
//            for (int i = 0; i < 3; i++) {
//                Category c = new Category();
//                c.setCategoryName("Top" + i);
//                c.setCreatedAt(LocalDateTime.now());
//                dao.save(c);
//            }
//            // create 2 subcategories
//            Category parent = new Category();
//            parent.setCategoryName("Parent");
//            parent.setCreatedAt(LocalDateTime.now());
//            parent = dao.save(parent);
//            for (int i = 0; i < 2; i++) {
//                Category sub = new Category();
//                sub.setCategoryName("Sub" + i);
//                sub.setParentCategoryId(parent.getCategoryId());
//                sub.setCreatedAt(LocalDateTime.now());
//                dao.save(sub);
//            }
//
//            List<Category> top = dao.findTopLevelCategories();
//            boolean ok = top != null && top.size() == 4; // Top0..2 and Parent
//            DatabaseTestUtil.printTestResult("testFindTopLevelCategories", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindTopLevelCategories exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindSubcategories(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category parent = new Category();
//            parent.setCategoryName("Electronics");
//            parent.setCreatedAt(LocalDateTime.now());
//            parent = dao.save(parent);
//            String[] subs = {"Laptops", "Phones", "Tablets"};
//            for (String s : subs) {
//                Category c = new Category();
//                c.setCategoryName(s);
//                c.setParentCategoryId(parent.getCategoryId());
//                c.setCreatedAt(LocalDateTime.now());
//                dao.save(c);
//            }
//            List<Category> list = dao.findSubcategories(parent.getCategoryId());
//            boolean ok = list != null && list.size() == 3;
//            DatabaseTestUtil.printTestResult("testFindSubcategories", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindSubcategories exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindByName(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = new Category();
//            c.setCategoryName("Books");
//            c.setCreatedAt(LocalDateTime.now());
//            dao.save(c);
//            Category found = dao.findByName("Books");
//            boolean ok = found != null;
//            DatabaseTestUtil.printTestResult("testFindByName", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindByName exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testHasProducts(CategoryDAO dao, ProductDAO pdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = DatabaseTestUtil.insertTestCategory();
//            // create product in that category
//            Product p = DatabaseTestUtil.insertTestProduct(c.getCategoryId());
//            boolean has = dao.hasProducts(c.getCategoryId());
//            // create empty category
//            Category empty = new Category();
//            empty.setCategoryName("EmptyCat");
//            empty.setCreatedAt(LocalDateTime.now());
//            empty = dao.save(empty);
//            boolean none = dao.hasProducts(empty.getCategoryId());
//            boolean ok = has && !none;
//            DatabaseTestUtil.printTestResult("testHasProducts", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testHasProducts exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testGetProductCount(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = DatabaseTestUtil.insertTestCategory();
//            for (int i = 0; i < 5; i++) DatabaseTestUtil.insertTestProduct(c.getCategoryId());
//            int cnt = dao.getProductCount(c.getCategoryId());
//            boolean ok = cnt == 5;
//            DatabaseTestUtil.printTestResult("testGetProductCount", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testGetProductCount exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testDeleteCategory(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            Category c = new Category();
//            c.setCategoryName("ToDeleteCat");
//            c.setCreatedAt(LocalDateTime.now());
//            Category saved = dao.save(c);
//            boolean del = dao.delete(saved.getCategoryId());
//            Category found = dao.findById(saved.getCategoryId());
//            boolean ok = del && found == null;
//            DatabaseTestUtil.printTestResult("testDeleteCategory", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testDeleteCategory exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testCount(CategoryDAO dao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            for (int i = 0; i < 6; i++) {
//                Category c = new Category();
//                c.setCategoryName("CntCat" + i);
//                c.setCreatedAt(LocalDateTime.now());
//                dao.save(c);
//            }
//            long cnt = dao.count();
//            boolean ok = cnt == 6;
//            DatabaseTestUtil.printTestResult("testCount", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testCount exception:");
//            e.printStackTrace();
//        }
//    }
//}
