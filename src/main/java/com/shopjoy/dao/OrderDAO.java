package com.shopjoy.dao;

import com.shopjoy.model.Order;
import com.shopjoy.model.OrderStatus;
import com.shopjoy.model.PaymentStatus;
import com.shopjoy.util.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Order entity. Implements CRUD and order-specific queries.
 */
public class OrderDAO implements GenericDAO<Order, Integer> {

    @Override
    public Order findById(Integer orderId) throws SQLException {
        if (orderId == null) return null;

        String sql = "SELECT order_id, user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes, created_at, updated_at FROM orders WHERE order_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToOrder(rs) : null;
            }
        }
    }

    @Override
    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT order_id, user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes, created_at, updated_at FROM orders ORDER BY order_date DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToOrder(rs));
        }
        return list;
    }

    @Override
    public Order save(Order order) throws SQLException {
        if (order == null) return null;

        String sql = "INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING order_id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, order.getUserId());
            ps.setObject(2, order.getOrderDate());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus() != null ? order.getStatus().toString().toLowerCase() : null);
            ps.setString(5, order.getShippingAddress());
            ps.setString(6, order.getPaymentMethod());
            ps.setString(7, order.getPaymentStatus() != null ? order.getPaymentStatus().toString().toLowerCase() : null);
            ps.setString(8, order.getNotes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) order.setOrderId(rs.getInt("order_id"));
            }
        }
        return order;
    }

    @Override
    public Order update(Order order) throws SQLException {
        if (order == null) return null;

        String sql = "UPDATE orders SET status = ?, payment_status = ?, notes = ? WHERE order_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getStatus() != null ? order.getStatus().toString().toLowerCase() : null);
            ps.setString(2, order.getPaymentStatus() != null ? order.getPaymentStatus().toString().toLowerCase() : null);
            ps.setString(3, order.getNotes());
            ps.setInt(4, order.getOrderId());
            ps.executeUpdate();
        }
        return order;
    }

    @Override
    public boolean delete(Integer orderId) throws SQLException {
        if (orderId == null) return false;

        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM orders";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // --- Custom methods ---

    public List<Order> findByUserId(int userId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT order_id, user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes, created_at, updated_at FROM orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public List<Order> findByStatus(OrderStatus status) throws SQLException {
        List<Order> list = new ArrayList<>();
        if (status == null) return list;

        String sql = "SELECT order_id, user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes, created_at, updated_at FROM orders WHERE status = ? ORDER BY order_date DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Order> list = new ArrayList<>();
        if (startDate == null || endDate == null) return list;

        String sql = "SELECT order_id, user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes, created_at, updated_at FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, startDate);
            ps.setObject(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public List<Order> findRecentOrders(int limit) throws SQLException {
        List<Order> list = new ArrayList<>();
        if (limit <= 0) return list;

        String sql = "SELECT order_id, user_id, order_date, total_amount, status, shipping_address, " +
                "payment_method, payment_status, notes, created_at, updated_at FROM orders ORDER BY order_date DESC LIMIT ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) AS total FROM orders WHERE payment_status = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PaymentStatus.PAID.toString().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("total");
                    return rs.wasNull() ? 0.0 : val;
                }
                return 0.0;
            }
        }
    }

    public double getTotalRevenueByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        if (start == null || end == null) return 0.0;

        String sql = "SELECT SUM(total_amount) AS total FROM orders WHERE payment_status = ? AND order_date BETWEEN ? AND ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PaymentStatus.PAID.toString().toLowerCase());
            ps.setObject(2, start);
            ps.setObject(3, end);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("total");
                    return rs.wasNull() ? 0.0 : val;
                }
                return 0.0;
            }
        }
    }

    public boolean updateOrderStatus(int orderId, OrderStatus newStatus) throws SQLException {
        if (newStatus == null) return false;

        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.toString().toLowerCase());
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePaymentStatus(int orderId, PaymentStatus newStatus) throws SQLException {
        if (newStatus == null) return false;

        String sql = "UPDATE orders SET payment_status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.toString().toLowerCase());
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Order> findPendingOrders() throws SQLException {
        return findByStatus(OrderStatus.PENDING);
    }

    public int countOrdersByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM orders WHERE user_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));

        Timestamp od = rs.getTimestamp("order_date");
        if (od != null) o.setOrderDate(od.toLocalDateTime());

        o.setTotalAmount(rs.getDouble("total_amount"));

        String status = rs.getString("status");
        o.setStatus(status != null ? OrderStatus.fromString(status) : null);

        o.setShippingAddress(rs.getString("shipping_address"));
        o.setPaymentMethod(rs.getString("payment_method"));

        String pstat = rs.getString("payment_status");
        o.setPaymentStatus(pstat != null ? PaymentStatus.fromString(pstat) : null);

        o.setNotes(rs.getString("notes"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) o.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) o.setUpdatedAt(updated.toLocalDateTime());

        return o;
    }
}
