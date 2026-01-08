package com.shopjoy.test;

import com.shopjoy.dao.OrderDAO;
import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.Order;
import com.shopjoy.model.OrderStatus;
import com.shopjoy.model.PaymentStatus;
import com.shopjoy.model.User;
import com.shopjoy.util.DatabaseTestUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========== TESTING ORDER DAO ==========");
        OrderDAO orderDAO = new OrderDAO();
        UserDAO userDAO = new UserDAO();

            DatabaseTestUtil.printTestHeader("Create Order");
            testCreateOrder(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find Order By ID");
            testFindOrderById(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find All Orders");
            testFindAllOrders(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Update Order");
            testUpdateOrder(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find By UserId");
            testFindByUserId(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find By Status");
            testFindByStatus(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find By Date Range");
            testFindByDateRange(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find Recent Orders");
            testFindRecentOrders(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Get Total Revenue");
            testGetTotalRevenue(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Update Order Status");
            testUpdateOrderStatus(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Update Payment Status");
            testUpdatePaymentStatus(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Find Pending Orders");
            testFindPendingOrders(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Count Orders By User");
            testCountOrdersByUser(orderDAO, userDAO);

            DatabaseTestUtil.printTestHeader("Delete Order");
            testDeleteOrder(orderDAO, userDAO);

    }

    private static User createTestUser(UserDAO userDAO, String username, String email) throws SQLException {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash("password");
        u.setFirstName("T");
        u.setLastName("U");
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return userDAO.save(u);
    }

    private static Order createTestOrder(OrderDAO dao, int userId, OrderStatus status, PaymentStatus paymentStatus, double amount, LocalDateTime dateTime) throws SQLException {
        Order o = new Order();
        o.setUserId(userId);
        o.setOrderDate(dateTime != null ? dateTime : LocalDateTime.now());
        o.setTotalAmount(amount);
        o.setStatus(status);
        o.setShippingAddress("123 Main St, City, State");
        o.setPaymentMethod("Credit Card");
        o.setPaymentStatus(paymentStatus);
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        return dao.save(o);
    }

    private static void testCreateOrder(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "orderuser", "order@test.com");
            Order o = createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 499.99, LocalDateTime.now());
            boolean ok = o != null && o.getOrderId() > 0;
            DatabaseTestUtil.printTestResult("testCreateOrder", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCreateOrder exception:");
            e.printStackTrace();
        }
    }

    private static void testFindOrderById(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "findorder", "findorder@test.com");
            Order created = createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 100.0, LocalDateTime.now());
            Order found = dao.findById(created.getOrderId());
            boolean ok = found != null && found.getOrderId() == created.getOrderId();
            DatabaseTestUtil.printTestResult("testFindOrderById", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindOrderById exception:");
            e.printStackTrace();
        }
    }

    private static void testFindAllOrders(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "allorders", "allorders@test.com");
            for (int i = 0; i < 4; i++) createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 10 + i, LocalDateTime.now());
            List<Order> list = dao.findAll();
            boolean ok = list != null && list.size() == 4;
            DatabaseTestUtil.printTestResult("testFindAllOrders", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindAllOrders exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdateOrder(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "updorder", "updorder@test.com");
            Order o = createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 20.0, LocalDateTime.now());
            o.setStatus(OrderStatus.PROCESSING);
            o.setPaymentStatus(PaymentStatus.PAID);
            dao.update(o);
            Order r = dao.findById(o.getOrderId());
            boolean ok = r != null && r.getStatus() == OrderStatus.PROCESSING && r.getPaymentStatus() == PaymentStatus.PAID;
            DatabaseTestUtil.printTestResult("testUpdateOrder", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdateOrder exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByUserId(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u1 = createTestUser(udao, "u1", "u1@test.com");
            User u2 = createTestUser(udao, "u2", "u2@test.com");
            for (int i = 0; i < 3; i++) createTestOrder(dao, u1.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 10 + i, LocalDateTime.now());
            for (int i = 0; i < 2; i++) createTestOrder(dao, u2.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 20 + i, LocalDateTime.now());
            List<Order> list = dao.findByUserId(u1.getUserId());
            boolean ok = list != null && list.size() == 3;
            DatabaseTestUtil.printTestResult("testFindByUserId", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByUserId exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByStatus(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "suser", "suser@test.com");
            // 2 PENDING
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 1, LocalDateTime.now());
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 2, LocalDateTime.now());
            // 2 PROCESSING
            createTestOrder(dao, u.getUserId(), OrderStatus.PROCESSING, PaymentStatus.UNPAID, 3, LocalDateTime.now());
            createTestOrder(dao, u.getUserId(), OrderStatus.PROCESSING, PaymentStatus.UNPAID, 4, LocalDateTime.now());
            // 1 SHIPPED
            createTestOrder(dao, u.getUserId(), OrderStatus.SHIPPED, PaymentStatus.UNPAID, 5, LocalDateTime.now());

            List<Order> pending = dao.findByStatus(OrderStatus.PENDING);
            boolean ok = pending != null && pending.size() == 2;
            DatabaseTestUtil.printTestResult("testFindByStatus", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByStatus exception:");
            e.printStackTrace();
        }
    }

    private static void testFindByDateRange(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "druser", "druser@test.com");
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 1, LocalDateTime.now().minusDays(5));
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 2, LocalDateTime.now().minusDays(3));
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 3, LocalDateTime.now());
            LocalDateTime start = LocalDateTime.now().minusDays(4);
            LocalDateTime end = LocalDateTime.now();
            List<Order> list = dao.findByDateRange(start, end);
            boolean ok = list != null && list.size() == 2;
            DatabaseTestUtil.printTestResult("testFindByDateRange", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindByDateRange exception:");
            e.printStackTrace();
        }
    }

    private static void testFindRecentOrders(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "ruser", "ruser@test.com");
            List<Order> created = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                created.add(createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, i + 1, LocalDateTime.now().minusDays(10 - i)));
            }
            List<Order> recent = dao.findRecentOrders(5);
            boolean ok = recent != null && recent.size() == 5;
            if (ok) {
                // verify sorted descending
                boolean sorted = true;
                for (int i = 1; i < recent.size(); i++) {
                    if (recent.get(i - 1).getOrderDate().isBefore(recent.get(i).getOrderDate())) { sorted = false; break; }
                }
                ok = sorted;
            }
            DatabaseTestUtil.printTestResult("testFindRecentOrders", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindRecentOrders exception:");
            e.printStackTrace();
        }
    }

    private static void testGetTotalRevenue(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "revuser", "revuser@test.com");
            // paid orders
            createTestOrder(dao, u.getUserId(), OrderStatus.DELIVERED, PaymentStatus.PAID, 100.00, LocalDateTime.now());
            createTestOrder(dao, u.getUserId(), OrderStatus.DELIVERED, PaymentStatus.PAID, 200.00, LocalDateTime.now());
            createTestOrder(dao, u.getUserId(), OrderStatus.DELIVERED, PaymentStatus.PAID, 150.00, LocalDateTime.now());
            // unpaid
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 300.00, LocalDateTime.now());
            createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 400.00, LocalDateTime.now());

            double total = dao.getTotalRevenue();
            boolean ok = Math.abs(total - 450.00) < 0.001;
            DatabaseTestUtil.printTestResult("testGetTotalRevenue", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testGetTotalRevenue exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdateOrderStatus(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "ustatus", "ustatus@test.com");
            Order o = createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 10.0, LocalDateTime.now());
            boolean updated = dao.updateOrderStatus(o.getOrderId(), OrderStatus.PROCESSING);
            Order r = dao.findById(o.getOrderId());
            boolean ok = updated && r != null && r.getStatus() == OrderStatus.PROCESSING;
            DatabaseTestUtil.printTestResult("testUpdateOrderStatus", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdateOrderStatus exception:");
            e.printStackTrace();
        }
    }

    private static void testUpdatePaymentStatus(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "upay", "upay@test.com");
            Order o = createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 10.0, LocalDateTime.now());
            boolean updated = dao.updatePaymentStatus(o.getOrderId(), PaymentStatus.PAID);
            Order r = dao.findById(o.getOrderId());
            boolean ok = updated && r != null && r.getPaymentStatus() == PaymentStatus.PAID;
            DatabaseTestUtil.printTestResult("testUpdatePaymentStatus", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testUpdatePaymentStatus exception:");
            e.printStackTrace();
        }
    }

    private static void testFindPendingOrders(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "pending", "pending@test.com");
            for (int i = 0; i < 3; i++) createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, i + 1, LocalDateTime.now());
            for (int i = 0; i < 2; i++) createTestOrder(dao, u.getUserId(), OrderStatus.SHIPPED, PaymentStatus.PAID, i + 10, LocalDateTime.now());
            List<Order> pending = dao.findPendingOrders();
            boolean ok = pending != null && pending.size() == 3;
            DatabaseTestUtil.printTestResult("testFindPendingOrders", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testFindPendingOrders exception:");
            e.printStackTrace();
        }
    }

    private static void testCountOrdersByUser(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "countord", "countord@test.com");
            for (int i = 0; i < 5; i++) createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, i + 1, LocalDateTime.now());
            int cnt = dao.countOrdersByUser(u.getUserId());
            boolean ok = cnt == 5;
            DatabaseTestUtil.printTestResult("testCountOrdersByUser", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testCountOrdersByUser exception:");
            e.printStackTrace();
        }
    }

    private static void testDeleteOrder(OrderDAO dao, UserDAO udao) {
        try {
            DatabaseTestUtil.clearAllTables();
            User u = createTestUser(udao, "dorder", "dorder@test.com");
            Order o = createTestOrder(dao, u.getUserId(), OrderStatus.PENDING, PaymentStatus.UNPAID, 1.0, LocalDateTime.now());
            boolean del = dao.delete(o.getOrderId());
            Order found = dao.findById(o.getOrderId());
            boolean ok = del && found == null;
            DatabaseTestUtil.printTestResult("testDeleteOrder", ok);
            if (ok) passed++; else failed++;
        } catch (Exception e) {
            failed++;
            System.err.println("testDeleteOrder exception:");
            e.printStackTrace();
        }
    }
}
