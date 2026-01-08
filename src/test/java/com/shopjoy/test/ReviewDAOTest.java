//package com.shopjoy.test;
//
//import com.shopjoy.dao.CategoryDAO;
//import com.shopjoy.dao.ProductDAO;
//import com.shopjoy.dao.ReviewDAO;
//import com.shopjoy.dao.UserDAO;
//import com.shopjoy.model.Category;
//import com.shopjoy.model.Product;
//import com.shopjoy.model.Review;
//import com.shopjoy.model.User;
//import com.shopjoy.util.DatabaseConfig;
//import com.shopjoy.util.DatabaseTestUtil;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ReviewDAOTest {
//    private static int passed = 0;
//    private static int failed = 0;
//
//    public static void main(String[] args) {
//        System.out.println("===== TESTING REVIEW DAO =====");
//
//        ReviewDAO reviewDao = new ReviewDAO();
//        UserDAO userDao = new UserDAO();
//        ProductDAO productDao = new ProductDAO();
//        CategoryDAO categoryDao = new CategoryDAO();
//
//        try {
//            DatabaseTestUtil.printTestHeader("Create Review");
//            testCreateReview(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Find Review By ID");
//            testFindReviewById(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Find By ProductId");
//            testFindByProductId(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Find By UserId");
//            testFindByUserId(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Get Average Rating");
//            testGetAverageRating(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Get Review Count");
//            testGetReviewCount(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Find Verified Purchase Reviews");
//            testFindVerifiedPurchaseReviews(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Increment Helpful Count");
//            testIncrementHelpfulCount(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("User Has Reviewed");
//            testUserHasReviewed(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Find Recent Reviews");
//            testFindRecentReviews(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Update Review");
//            testUpdateReview(reviewDao, userDao, productDao, categoryDao);
//
//            DatabaseTestUtil.printTestHeader("Delete Review");
//            testDeleteReview(reviewDao, userDao, productDao, categoryDao);
//
//        } finally {
//            System.out.println("Tests complete. Passed: " + passed + ", Failed: " + failed);
//            DatabaseConfig.getInstance().closeAllConnections();
//        }
//    }
//
//    private static User createTestUser(UserDAO udao, String username, String email) throws Exception {
//        User u = new User();
//        u.setUsername(username);
//        u.setEmail(email);
//        u.setPasswordHash("password");
//        u.setFirstName("T");
//        u.setLastName("U");
//        u.setCreatedAt(LocalDateTime.now());
//        u.setUpdatedAt(LocalDateTime.now());
//        return udao.save(u);
//    }
//
//    private static Category createTestCategory(CategoryDAO cdao, String name) throws Exception {
//        Category c = new Category();
//        c.setCategoryName(name);
//        c.setDescription("desc");
//        return cdao.save(c);
//    }
//
//    private static Product createTestProduct(ProductDAO pdao, int categoryId, String name) throws Exception {
//        Product p = new Product();
//        p.setProductName(name);
//        p.setDescription("desc");
//        p.setCategoryId(categoryId);
//        p.setPrice(9.99);
//        p.setSku("SKU" + System.currentTimeMillis());
//        p.setCreatedAt(LocalDateTime.now());
//        p.setUpdatedAt(LocalDateTime.now());
//        return pdao.save(p);
//    }
//
//    private static Review createTestReview(ReviewDAO rdao, int productId, int userId, int rating, boolean verified, String comment, LocalDateTime at) throws Exception {
//        Review r = new Review();
//        r.setProductId(productId);
//        r.setUserId(userId);
//        r.setRating(rating);
//        r.setTitle("T");
//        r.setComment(comment != null ? comment : "comment");
//        r.setVerifiedPurchase(verified);
//        r.setHelpfulCount(0);
//        r.setCreatedAt(at != null ? at : LocalDateTime.now());
//        r.setUpdatedAt(LocalDateTime.now());
//        return rdao.save(r);
//    }
//
//    private static void testCreateReview(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "ruser", "r@test.com");
//            Category c = createTestCategory(cdao, "rcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "rprod");
//            Review r = createTestReview(rdao, p.getProductId(), u.getUserId(), 5, true, "great", LocalDateTime.now());
//            boolean ok = r != null && r.getReviewId() > 0;
//            DatabaseTestUtil.printTestResult("testCreateReview", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testCreateReview exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindReviewById(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "rfuser", "rf@test.com");
//            Category c = createTestCategory(cdao, "rfcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "rfprod");
//            Review created = createTestReview(rdao, p.getProductId(), u.getUserId(), 4, false, "ok", LocalDateTime.now());
//            Review found = rdao.findById(created.getReviewId());
//            boolean ok = found != null && found.getReviewId() == created.getReviewId();
//            DatabaseTestUtil.printTestResult("testFindReviewById", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindReviewById exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindByProductId(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u1 = createTestUser(udao, "puser1", "p1@test.com");
//            User u2 = createTestUser(udao, "puser2", "p2@test.com");
//            Category c = createTestCategory(cdao, "ppcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "pprod");
//            createTestReview(rdao, p.getProductId(), u1.getUserId(), 5, true, "a", LocalDateTime.now());
//            createTestReview(rdao, p.getProductId(), u2.getUserId(), 4, false, "b", LocalDateTime.now());
//            List<Review> list = rdao.findByProductId(p.getProductId());
//            boolean ok = list != null && list.size() == 2;
//            DatabaseTestUtil.printTestResult("testFindByProductId", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindByProductId exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindByUserId(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "uview", "uv@test.com");
//            Category c = createTestCategory(cdao, "uvcat");
//            Product p1 = createTestProduct(pdao, c.getCategoryId(), "up1");
//            Product p2 = createTestProduct(pdao, c.getCategoryId(), "up2");
//            createTestReview(rdao, p1.getProductId(), u.getUserId(), 3, false, "x", LocalDateTime.now());
//            createTestReview(rdao, p2.getProductId(), u.getUserId(), 4, true, "y", LocalDateTime.now());
//            List<Review> list = rdao.findByUserId(u.getUserId());
//            boolean ok = list != null && list.size() == 2;
//            DatabaseTestUtil.printTestResult("testFindByUserId", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindByUserId exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testGetAverageRating(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u1 = createTestUser(udao, "avg1", "avg1@test.com");
//            User u2 = createTestUser(udao, "avg2", "avg2@test.com");
//            User u3 = createTestUser(udao, "avg3", "avg3@test.com");
//            User u4 = createTestUser(udao, "avg4", "avg4@test.com");
//            Category c = createTestCategory(cdao, "avgcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "avgprod");
//            createTestReview(rdao, p.getProductId(), u1.getUserId(), 5, true, "a", LocalDateTime.now());
//            createTestReview(rdao, p.getProductId(), u2.getUserId(), 4, true, "b", LocalDateTime.now());
//            createTestReview(rdao, p.getProductId(), u3.getUserId(), 5, false, "c", LocalDateTime.now());
//            createTestReview(rdao, p.getProductId(), u4.getUserId(), 3, false, "d", LocalDateTime.now());
//            double avg = rdao.getAverageRating(p.getProductId());
//            boolean ok = Math.abs(avg - 4.25) < 0.001;
//            DatabaseTestUtil.printTestResult("testGetAverageRating", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testGetAverageRating exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testGetReviewCount(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u1 = createTestUser(udao, "cnt1", "cnt1@test.com");
//            User u2 = createTestUser(udao, "cnt2", "cnt2@test.com");
//            Category c = createTestCategory(cdao, "cntcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "cntprod");
//            createTestReview(rdao, p.getProductId(), u1.getUserId(), 5, true, "a", LocalDateTime.now());
//            createTestReview(rdao, p.getProductId(), u2.getUserId(), 4, false, "b", LocalDateTime.now());
//            int cnt = rdao.getReviewCount(p.getProductId());
//            boolean ok = cnt == 2;
//            DatabaseTestUtil.printTestResult("testGetReviewCount", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testGetReviewCount exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindVerifiedPurchaseReviews(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u1 = createTestUser(udao, "v1", "v1@test.com");
//            User u2 = createTestUser(udao, "v2", "v2@test.com");
//            Category c = createTestCategory(cdao, "vcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "vprod");
//            createTestReview(rdao, p.getProductId(), u1.getUserId(), 5, true, "a", LocalDateTime.now());
//            createTestReview(rdao, p.getProductId(), u2.getUserId(), 3, false, "b", LocalDateTime.now());
//            List<Review> verified = rdao.findVerifiedPurchaseReviews(p.getProductId());
//            boolean ok = verified != null && verified.size() == 1 && verified.get(0).isVerifiedPurchase();
//            DatabaseTestUtil.printTestResult("testFindVerifiedPurchaseReviews", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindVerifiedPurchaseReviews exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testIncrementHelpfulCount(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "huser", "h@test.com");
//            Category c = createTestCategory(cdao, "hcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "hprod");
//            Review r = createTestReview(rdao, p.getProductId(), u.getUserId(), 4, false, "helpful", LocalDateTime.now());
//            for (int i = 0; i < 3; i++) rdao.incrementHelpfulCount(r.getReviewId());
//            Review updated = rdao.findById(r.getReviewId());
//            boolean ok = updated != null && updated.getHelpfulCount() == 3;
//            DatabaseTestUtil.printTestResult("testIncrementHelpfulCount", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testIncrementHelpfulCount exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testUserHasReviewed(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u1 = createTestUser(udao, "uh1", "uh1@test.com");
//            User u2 = createTestUser(udao, "uh2", "uh2@test.com");
//            Category c = createTestCategory(cdao, "uhcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "uhprod");
//            createTestReview(rdao, p.getProductId(), u1.getUserId(), 5, true, "x", LocalDateTime.now());
//            boolean has = rdao.userHasReviewed(u1.getUserId(), p.getProductId());
//            boolean notHas = rdao.userHasReviewed(u2.getUserId(), p.getProductId());
//            boolean ok = has && !notHas;
//            DatabaseTestUtil.printTestResult("testUserHasReviewed", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testUserHasReviewed exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindRecentReviews(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "rruser", "rr@test.com");
//            Category c = createTestCategory(cdao, "rrcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "rrprod");
//            List<Review> created = new ArrayList<>();
//            for (int i = 0; i < 10; i++) {
//                created.add(createTestReview(rdao, p.getProductId(), u.getUserId(), 5, false, "r" + i, LocalDateTime.now().minusDays(10 - i)));
//            }
//            List<Review> recent = rdao.findRecentReviews(5);
//            boolean ok = recent != null && recent.size() == 5;
//            if (ok) {
//                boolean sorted = true;
//                for (int i = 1; i < recent.size(); i++) {
//                    if (recent.get(i - 1).getCreatedAt().isBefore(recent.get(i).getCreatedAt())) { sorted = false; break; }
//                }
//                ok = sorted;
//            }
//            DatabaseTestUtil.printTestResult("testFindRecentReviews", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindRecentReviews exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testUpdateReview(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "uprev", "uprev@test.com");
//            Category c = createTestCategory(cdao, "upcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "upprod");
//            Review r = createTestReview(rdao, p.getProductId(), u.getUserId(), 2, false, "bad", LocalDateTime.now());
//            r.setRating(4);
//            r.setComment("better");
//            rdao.update(r);
//            Review updated = rdao.findById(r.getReviewId());
//            boolean ok = updated != null && updated.getRating() == 4 && "better".equals(updated.getComment());
//            DatabaseTestUtil.printTestResult("testUpdateReview", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testUpdateReview exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testDeleteReview(ReviewDAO rdao, UserDAO udao, ProductDAO pdao, CategoryDAO cdao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(udao, "delrev", "delrev@test.com");
//            Category c = createTestCategory(cdao, "delcat");
//            Product p = createTestProduct(pdao, c.getCategoryId(), "delprod");
//            Review r = createTestReview(rdao, p.getProductId(), u.getUserId(), 3, false, "x", LocalDateTime.now());
//            boolean del = rdao.delete(r.getReviewId());
//            Review found = rdao.findById(r.getReviewId());
//            boolean ok = del && found == null;
//            DatabaseTestUtil.printTestResult("testDeleteReview", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testDeleteReview exception:");
//            e.printStackTrace();
//        }
//    }
//}
