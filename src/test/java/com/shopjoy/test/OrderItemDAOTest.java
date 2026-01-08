//package com.shopjoy.test;
//
//import com.shopjoy.dao.CategoryDAO;
//import com.shopjoy.dao.OrderDAO;
//import com.shopjoy.dao.OrderItemDAO;
//import com.shopjoy.dao.ProductDAO;
//import com.shopjoy.dao.UserDAO;
//import com.shopjoy.model.Category;
//import com.shopjoy.model.Order;
//import com.shopjoy.model.OrderItem;
//import com.shopjoy.model.Product;
//import com.shopjoy.model.User;
//import com.shopjoy.util.DatabaseConfig;
//import com.shopjoy.util.DatabaseTestUtil;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//public class OrderItemDAOTest {
//    private static int passed = 0;
//    private static int failed = 0;
//
//    public static void main(String[] args) {
//        System.out.println("===== TESTING ORDER ITEM DAO =====");
//
//        OrderItemDAO oiDao = new OrderItemDAO();
//        OrderDAO orderDao = new OrderDAO();
//        ProductDAO productDao = new ProductDAO();
//        CategoryDAO catDao = new CategoryDAO();
//        UserDAO userDao = new UserDAO();
//
//        try {
//            DatabaseTestUtil.printTestHeader("Create OrderItem");
//            testCreateOrderItem(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Find OrderItem By ID");
//            testFindOrderItemById(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Find By OrderId");
//            testFindByOrderId(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Find By ProductId");
//            testFindByProductId(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Save OrderItems (batch)");
//            testSaveOrderItems(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Calculate Order Total");
//            testCalculateOrderTotal(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Get Total Quantity Sold");
//            testGetTotalQuantitySold(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Update OrderItem");
//            testUpdateOrderItem(oiDao, orderDao, productDao, catDao, userDao);
//
//            DatabaseTestUtil.printTestHeader("Delete OrderItem");
//            testDeleteOrderItem(oiDao, orderDao, productDao, catDao, userDao);
//
//        } finally {
//            System.out.println("Tests complete. Passed: " + passed + ", Failed: " + failed);
//            DatabaseConfig.getInstance().closeAllConnections();
//        }
//    }
//
//    private static User createTestUser(UserDAO userDAO, String username, String email) throws Exception {
//        User u = new User();
//        u.setUsername(username);
//        u.setEmail(email);
//        u.setPasswordHash("password");
//        u.setFirstName("T");
//        u.setLastName("U");
//        u.setCreatedAt(LocalDateTime.now());
//        u.setUpdatedAt(LocalDateTime.now());
//        return userDAO.save(u);
//    }
//
//    private static Category createTestCategory(CategoryDAO catDao, String name) throws Exception {
//        Category c = new Category();
//        c.setCategoryName(name);
//        c.setDescription("desc");
//        return catDao.save(c);
//    }
//
//    private static Product createTestProduct(ProductDAO pDao, int categoryId, String name, double price) throws Exception {
//        Product p = new Product();
//        p.setProductName(name);
//        p.setDescription("desc");
//        p.setCategoryId(categoryId);
//        p.setPrice(price);
//        p.setSku("SKU" + System.currentTimeMillis());
//        p.setCreatedAt(LocalDateTime.now());
//        p.setUpdatedAt(LocalDateTime.now());
//        return pDao.save(p);
//    }
//
//    private static Order createTestOrder(OrderDAO orderDao, int userId) throws Exception {
//        Order o = new Order();
//        o.setUserId(userId);
//        o.setOrderDate(LocalDateTime.now());
//        o.setTotalAmount(0.0);
//        o.setStatus(com.shopjoy.model.OrderStatus.PENDING);
//        o.setShippingAddress("123 Main St");
//        o.setPaymentMethod("Card");
//        o.setPaymentStatus(com.shopjoy.model.PaymentStatus.UNPAID);
//        o.setCreatedAt(LocalDateTime.now());
//        o.setUpdatedAt(LocalDateTime.now());
//        return orderDao.save(o);
//    }
//
//    private static OrderItem createTestOrderItem(OrderItemDAO oiDao, int orderId, int productId, int qty, double price) throws Exception {
//        OrderItem it = new OrderItem();
//        it.setOrderId(orderId);
//        it.setProductId(productId);
//        it.setQuantity(qty);
//        it.setUnitPrice(price);
//        it.setSubtotal(qty * price);
//        return oiDao.save(it);
//    }
//
//    private static void testCreateOrderItem(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "oiuser", "oi@test.com");
//            Category c = createTestCategory(cDao, "cat1");
//            Product p = createTestProduct(pDao, c.getCategoryId(), "prod1", 49.99);
//            Order o = createTestOrder(orderDao, u.getUserId());
//            OrderItem it = createTestOrderItem(oiDao, o.getOrderId(), p.getProductId(), 2, 49.99);
//            boolean ok = it != null && it.getOrderItemId() > 0;
//            DatabaseTestUtil.printTestResult("testCreateOrderItem", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testCreateOrderItem exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindOrderItemById(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "fiuser", "fi@test.com");
//            Category c = createTestCategory(cDao, "cat2");
//            Product p = createTestProduct(pDao, c.getCategoryId(), "prod2", 20.0);
//            Order o = createTestOrder(orderDao, u.getUserId());
//            OrderItem it = createTestOrderItem(oiDao, o.getOrderId(), p.getProductId(), 1, 20.0);
//            OrderItem found = oiDao.findById(it.getOrderItemId());
//            boolean ok = found != null && found.getOrderItemId() == it.getOrderItemId();
//            DatabaseTestUtil.printTestResult("testFindOrderItemById", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindOrderItemById exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindByOrderId(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "foiuser", "foi@test.com");
//            Category c = createTestCategory(cDao, "cat3");
//            Product p1 = createTestProduct(pDao, c.getCategoryId(), "prod3", 10.0);
//            Product p2 = createTestProduct(pDao, c.getCategoryId(), "prod4", 5.0);
//            Order o = createTestOrder(orderDao, u.getUserId());
//            createTestOrderItem(oiDao, o.getOrderId(), p1.getProductId(), 1, 10.0);
//            createTestOrderItem(oiDao, o.getOrderId(), p2.getProductId(), 2, 5.0);
//            List<OrderItem> list = oiDao.findByOrderId(o.getOrderId());
//            boolean ok = list != null && list.size() == 2;
//            DatabaseTestUtil.printTestResult("testFindByOrderId", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testFindByOrderId exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testFindByProductId(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "fpuser", "fp@test.com");
//            Category c = createTestCategory(cDao, "cat4");
//            Product p = createTestProduct(pDao, c.getCategoryId(), "prod5", 15.0);
//            Order o1 = createTestOrder(orderDao, u.getUserId());
//            Order o2 = createTestOrder(orderDao, u.getUserId());
//            createTestOrderItem(oiDao, o1.getOrderId(), p.getProductId(), 1, 15.0);
//            createTestOrderItem(oiDao, o2.getOrderId(), p.getProductId(), 3, 15.0);
//            List<OrderItem> list = oiDao.findByProductId(p.getProductId());
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
//    private static void testSaveOrderItems(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "batchuser", "batch@test.com");
//            Category c = createTestCategory(cDao, "catbatch");
//            Order o = createTestOrder(orderDao, u.getUserId());
//            List<OrderItem> items = new ArrayList<>();
//            for (int i = 0; i < 5; i++) {
//                Product p = createTestProduct(pDao, c.getCategoryId(), "batchprod" + i, 10 + i);
//                OrderItem it = new OrderItem();
//                it.setOrderId(o.getOrderId());
//                it.setProductId(p.getProductId());
//                it.setQuantity(i + 1);
//                it.setUnitPrice(p.getPrice());
//                it.setSubtotal(it.getQuantity() * it.getUnitPrice());
//                items.add(it);
//            }
//            oiDao.saveOrderItems(items);
//            List<OrderItem> saved = oiDao.findByOrderId(o.getOrderId());
//            boolean ok = saved != null && saved.size() == 5;
//            DatabaseTestUtil.printTestResult("testSaveOrderItems", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testSaveOrderItems exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testCalculateOrderTotal(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "totuser", "tot@test.com");
//            Category c = createTestCategory(cDao, "cattot");
//            Product p1 = createTestProduct(pDao, c.getCategoryId(), "tprod1", 50.0);
//            Product p2 = createTestProduct(pDao, c.getCategoryId(), "tprod2", 75.0);
//            Product p3 = createTestProduct(pDao, c.getCategoryId(), "tprod3", 25.0);
//            Order o = createTestOrder(orderDao, u.getUserId());
//            createTestOrderItem(oiDao, o.getOrderId(), p1.getProductId(), 2, 50.0); // 100
//            createTestOrderItem(oiDao, o.getOrderId(), p2.getProductId(), 1, 75.0); // 75
//            createTestOrderItem(oiDao, o.getOrderId(), p3.getProductId(), 3, 25.0); // 75
//            double total = oiDao.calculateOrderTotal(o.getOrderId());
//            boolean ok = Math.abs(total - 250.0) < 0.001;
//            DatabaseTestUtil.printTestResult("testCalculateOrderTotal", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testCalculateOrderTotal exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testGetTotalQuantitySold(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "qtyuser", "qty@test.com");
//            Category c = createTestCategory(cDao, "catqty");
//            Product p = createTestProduct(pDao, c.getCategoryId(), "qtyprod", 9.99);
//            Order o1 = createTestOrder(orderDao, u.getUserId());
//            Order o2 = createTestOrder(orderDao, u.getUserId());
//            Order o3 = createTestOrder(orderDao, u.getUserId());
//            createTestOrderItem(oiDao, o1.getOrderId(), p.getProductId(), 2, 9.99);
//            createTestOrderItem(oiDao, o2.getOrderId(), p.getProductId(), 5, 9.99);
//            createTestOrderItem(oiDao, o3.getOrderId(), p.getProductId(), 3, 9.99);
//            int totalQty = oiDao.getTotalQuantitySold(p.getProductId());
//            boolean ok = totalQty == 10;
//            DatabaseTestUtil.printTestResult("testGetTotalQuantitySold", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testGetTotalQuantitySold exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testUpdateOrderItem(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "upituser", "upit@test.com");
//            Category c = createTestCategory(cDao, "catup");
//            Product p = createTestProduct(pDao, c.getCategoryId(), "upprod", 12.5);
//            Order o = createTestOrder(orderDao, u.getUserId());
//            OrderItem it = createTestOrderItem(oiDao, o.getOrderId(), p.getProductId(), 1, 12.5);
//            it.setQuantity(4);
//            it.setSubtotal(4 * it.getUnitPrice());
//            oiDao.update(it);
//            OrderItem r = oiDao.findById(it.getOrderItemId());
//            boolean ok = r != null && r.getQuantity() == 4 && Math.abs(r.getSubtotal() - 50.0) < 0.001;
//            DatabaseTestUtil.printTestResult("testUpdateOrderItem", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testUpdateOrderItem exception:");
//            e.printStackTrace();
//        }
//    }
//
//    private static void testDeleteOrderItem(OrderItemDAO oiDao, OrderDAO orderDao, ProductDAO pDao, CategoryDAO cDao, UserDAO uDao) {
//        try {
//            DatabaseTestUtil.clearAllTables();
//            User u = createTestUser(uDao, "delituser", "delit@test.com");
//            Category c = createTestCategory(cDao, "catdel");
//            Product p = createTestProduct(pDao, c.getCategoryId(), "delprod", 7.5);
//            Order o = createTestOrder(orderDao, u.getUserId());
//            OrderItem it = createTestOrderItem(oiDao, o.getOrderId(), p.getProductId(), 2, 7.5);
//            boolean del = oiDao.delete(it.getOrderItemId());
//            OrderItem found = oiDao.findById(it.getOrderItemId());
//            boolean ok = del && found == null;
//            DatabaseTestUtil.printTestResult("testDeleteOrderItem", ok);
//            if (ok) passed++; else failed++;
//        } catch (Exception e) {
//            failed++;
//            System.err.println("testDeleteOrderItem exception:");
//            e.printStackTrace();
//        }
//    }
//}
