package com.shopjoy.service;

import com.shopjoy.dao.InventoryDAO;
import com.shopjoy.dao.OrderDAO;
import com.shopjoy.dao.OrderItemDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for order management and business rules.
 */
public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final InventoryDAO inventoryDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.inventoryDAO = new InventoryDAO();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Create an order with items. Performs stock checks and decrements inventory.
     * If any step fails, attempts to restore decremented inventory.
     */
    public Order createOrder(int userId, List<OrderItem> items, String shippingAddress, String paymentMethod) {
        if (userId <= 0 || items == null || items.isEmpty() || shippingAddress == null || shippingAddress.trim().isEmpty() || paymentMethod == null || paymentMethod.trim().isEmpty()) {
            System.err.println("createOrder: invalid input");
            return null;
        }

        List<OrderItem> createdItems = new ArrayList<>();
        List<int[]> decremented = new ArrayList<>(); // pairs of productId, amount
        try {
            // Validate products and stock
            double total = 0.0;
            for (OrderItem it : items) {
                Product p = productDAO.findById(it.getProductId());
                if (p == null) {
                    System.err.println("createOrder: product not found: " + it.getProductId());
                    return null;
                }
                boolean ok = inventoryDAO.checkStockAvailability(it.getProductId(), it.getQuantity());
                if (!ok) {
                    System.err.println("createOrder: insufficient stock for product " + it.getProductId());
                    return null;
                }
                double unit = p.getPrice();
                it.setUnitPrice(unit);
                it.setSubtotal(unit * it.getQuantity());
                it.setCreatedAt(LocalDateTime.now());
                total += it.getSubtotal();
            }

            // Create order
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(total);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setShippingAddress(shippingAddress);
            order.setPaymentMethod(paymentMethod);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            Order savedOrder = orderDAO.save(order);
            if (savedOrder == null || savedOrder.getOrderId() == 0) {
                System.err.println("createOrder: failed to save order");
                return null;
            }

            // Save items and decrement inventory; if failure, restore decremented quantities
            for (OrderItem it : items) {
                it.setOrderId(savedOrder.getOrderId());
                OrderItem savedItem = orderItemDAO.save(it);
                if (savedItem == null) {
                    // rollback
                    for (int[] d : decremented) inventoryDAO.incrementStock(d[0], d[1]);
                    System.err.println("createOrder: failed to save order item");
                    return null;
                }
                createdItems.add(savedItem);
                boolean dec = inventoryDAO.decrementStock(it.getProductId(), it.getQuantity());
                if (!dec) {
                    // rollback
                    for (int[] d : decremented) inventoryDAO.incrementStock(d[0], d[1]);
                    // also restore those we decremented earlier
                    for (OrderItem ci : createdItems) {
                        orderItemDAO.delete(ci.getOrderItemId());
                    }
                    System.err.println("createOrder: failed to decrement inventory");
                    return null;
                }
                decremented.add(new int[]{it.getProductId(), it.getQuantity()});
            }

            // All good
            return savedOrder;
        } catch (SQLException e) {
            // Attempt to restore inventory for any decrements that succeeded
            try {
                for (int[] d : decremented) inventoryDAO.incrementStock(d[0], d[1]);
            } catch (SQLException ex) {
                System.err.println("createOrder rollback failed: " + ex.getMessage());
            }
            System.err.println("createOrder SQLException: " + e.getMessage());
            return null;
        }
    }

    public Order getOrderById(int orderId) {
        if (orderId <= 0) return null;
        try {
            return orderDAO.findById(orderId);
        } catch (SQLException e) {
            System.err.println("getOrderById SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Order> getOrdersByUser(int userId) {
        if (userId <= 0) return null;
        try {
            return orderDAO.findByUserId(userId);
        } catch (SQLException e) {
            System.err.println("getOrdersByUser SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Order> getAllOrders() {
        try {
            return orderDAO.findAll();
        } catch (SQLException e) {
            System.err.println("getAllOrders SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) return null;
        try {
            return orderDAO.findByStatus(status);
        } catch (SQLException e) {
            System.err.println("getOrdersByStatus SQLException: " + e.getMessage());
            return null;
        }
    }

    public boolean updateOrderStatus(int orderId, OrderStatus newStatus) {
        if (orderId <= 0 || newStatus == null) return false;
        try {
            Order ord = orderDAO.findById(orderId);
            if (ord == null) return false;
            OrderStatus current = ord.getStatus();
            if (current == null || !current.canTransitionTo(newStatus)) {
                System.err.println("updateOrderStatus: invalid status transition from " + current + " to " + newStatus);
                return false;
            }
            return orderDAO.updateOrderStatus(orderId, newStatus);
        } catch (SQLException e) {
            System.err.println("updateOrderStatus SQLException: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelOrder(int orderId) {
        try {
            Order ord = orderDAO.findById(orderId);
            if (ord == null) return false;
            OrderStatus s = ord.getStatus();
            if (s != OrderStatus.PENDING && s != OrderStatus.PROCESSING) {
                System.err.println("cancelOrder: cannot cancel order in status " + s);
                return false;
            }
            List<OrderItem> items = orderItemDAO.findByOrderId(orderId);
            if (items != null) {
                for (OrderItem it : items) {
                    inventoryDAO.incrementStock(it.getProductId(), it.getQuantity());
                }
            }
            return orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELLED);
        } catch (SQLException e) {
            System.err.println("cancelOrder SQLException: " + e.getMessage());
            return false;
        }
    }

    public List<OrderItem> getOrderItems(int orderId) {
        if (orderId <= 0) return null;
        try {
            return orderItemDAO.findByOrderId(orderId);
        } catch (SQLException e) {
            System.err.println("getOrderItems SQLException: " + e.getMessage());
            return null;
        }
    }

    public double calculateOrderTotal(int orderId) {
        try {
            return orderItemDAO.calculateOrderTotal(orderId);
        } catch (SQLException e) {
            System.err.println("calculateOrderTotal SQLException: " + e.getMessage());
            return 0.0;
        }
    }

    public OrderSummary getOrderSummary(int orderId) {
        try {
            Order o = orderDAO.findById(orderId);
            if (o == null) return null;
            List<OrderItem> items = orderItemDAO.findByOrderId(orderId);
            User customer = userDAO.findById(o.getUserId());
            List<Product> products = new ArrayList<>();
            if (items != null) {
                for (OrderItem it : items) {
                    Product p = productDAO.findById(it.getProductId());
                    products.add(p);
                }
            }
            return new OrderSummary(o, items, customer, products);
        } catch (SQLException e) {
            System.err.println("getOrderSummary SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Order> getRecentOrders(int limit) {
        if (limit <= 0) return null;
        try {
            return orderDAO.findRecentOrders(limit);
        } catch (SQLException e) {
            System.err.println("getRecentOrders SQLException: " + e.getMessage());
            return null;
        }
    }

    public List<Order> getPendingOrders() {
        try {
            return orderDAO.findPendingOrders();
        } catch (SQLException e) {
            System.err.println("getPendingOrders SQLException: " + e.getMessage());
            return null;
        }
    }

    public double getTotalRevenue() {
        try {
            return orderDAO.getTotalRevenue();
        } catch (SQLException e) {
            System.err.println("getTotalRevenue SQLException: " + e.getMessage());
            return 0.0;
        }
    }

    public double getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) return 0.0;
        try {
            return orderDAO.getTotalRevenueByDateRange(startDate, endDate);
        } catch (SQLException e) {
            System.err.println("getRevenueByDateRange SQLException: " + e.getMessage());
            return 0.0;
        }
    }

    public boolean markOrderAsPaid(int orderId) {
        try {
            return orderDAO.updatePaymentStatus(orderId, PaymentStatus.PAID);
        } catch (SQLException e) {
            System.err.println("markOrderAsPaid SQLException: " + e.getMessage());
            return false;
        }
    }

    public int getOrderCountByUser(int userId) {
        try {
            return orderDAO.countOrdersByUser(userId);
        } catch (SQLException e) {
            System.err.println("getOrderCountByUser SQLException: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Wrapper for order details used in admin views.
     */
    public static class OrderSummary {
        private final Order order;
        private final List<OrderItem> orderItems;
        private final User customer;
        private final List<Product> products;

        public OrderSummary(Order order, List<OrderItem> orderItems, User customer, List<Product> products) {
            this.order = order;
            this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
            this.customer = customer;
            this.products = products != null ? products : new ArrayList<>();
        }

        public Order getOrder() { return order; }
        public List<OrderItem> getOrderItems() { return orderItems; }
        public User getCustomer() { return customer; }
        public List<Product> getProducts() { return products; }

        public int getTotalItems() {
            int sum = 0;
            for (OrderItem it : orderItems) sum += it.getQuantity();
            return sum;
        }

        public String getFormattedTotal() {
            return String.format("$%,.2f", order.getTotalAmount());
        }
    }
}
