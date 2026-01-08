package com.shopjoy.dao;

import com.shopjoy.model.OrderItem;
import com.shopjoy.util.DbConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for OrderItem entity. Implements CRUD and order-item-specific queries.
 */
public class OrderItemDAO implements GenericDAO<OrderItem, Integer> {

    @Override
    public OrderItem findById(Integer orderItemId) throws SQLException {
        if (orderItemId == null) return null;

        String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items WHERE order_item_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderItemId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToOrderItem(rs) : null;
            }
        }
    }

    @Override
    public List<OrderItem> findAll() throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToOrderItem(rs));
        }
        return list;
    }

    @Override
    public OrderItem save(OrderItem item) throws SQLException {
        if (item == null) return null;

        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal, created_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING order_item_id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getSubtotal());
            ps.setObject(6, item.getCreatedAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) item.setOrderItemId(rs.getInt("order_item_id"));
            }
        }
        return item;
    }

    @Override
    public OrderItem update(OrderItem item) throws SQLException {
        if (item == null) return null;

        String sql = "UPDATE order_items SET quantity = ?, unit_price = ?, subtotal = ? WHERE order_item_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getQuantity());
            ps.setDouble(2, item.getUnitPrice());
            ps.setDouble(3, item.getSubtotal());
            ps.setInt(4, item.getOrderItemId());

            ps.executeUpdate();
        }
        return item;
    }

    @Override
    public boolean delete(Integer orderItemId) throws SQLException {
        if (orderItemId == null) return false;

        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderItemId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM order_items";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // --- Custom methods ---

    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        return findByColumn("order_id", orderId);
    }

    public List<OrderItem> findByProductId(int productId) throws SQLException {
        return findByColumn("product_id", productId);
    }

    private List<OrderItem> findByColumn(String column, int value) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items WHERE " + column + " = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrderItem(rs));
            }
        }
        return list;
    }

    public boolean saveOrderItems(List<OrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) return false;

        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            for (OrderItem item : items) {
                ps.setInt(1, item.getOrderId());
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getUnitPrice());
                ps.setDouble(5, item.getSubtotal());
                ps.setObject(6, item.getCreatedAt());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int idx = 0;
                while (keys.next() && idx < items.size()) {
                    items.get(idx).setOrderItemId(keys.getInt(1));
                    idx++;
                }
            }

            for (int r : results) {
                if (r == PreparedStatement.EXECUTE_FAILED) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        }
    }

    public double calculateOrderTotal(int orderId) throws SQLException {
        String sql = "SELECT SUM(subtotal) AS total FROM order_items WHERE order_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("total");
                    return rs.wasNull() ? 0.0 : val;
                }
                return 0.0;
            }
        }
    }

    public int getTotalQuantitySold(int productId) throws SQLException {
        String sql = "SELECT SUM(quantity) AS total_qty FROM order_items WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int val = rs.getInt("total_qty");
                    return rs.wasNull() ? 0 : val;
                }
                return 0;
            }
        }
    }

    public List<Map<String, Object>> findTopSellingProducts(int limit) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (limit <= 0) return result;

        String sql = "SELECT product_id, SUM(quantity) AS total_sold FROM order_items GROUP BY product_id ORDER BY total_sold DESC LIMIT ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("product_id", rs.getInt("product_id"));
                    row.put("total_sold", rs.getLong("total_sold"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    // --- Helper to map ResultSet to OrderItem ---
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem oi = new OrderItem();
        oi.setOrderItemId(rs.getInt("order_item_id"));
        oi.setOrderId(rs.getInt("order_id"));
        oi.setProductId(rs.getInt("product_id"));
        oi.setQuantity(rs.getInt("quantity"));
        oi.setUnitPrice(rs.getDouble("unit_price"));
        oi.setSubtotal(rs.getDouble("subtotal"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) oi.setCreatedAt(ts.toLocalDateTime());
        return oi;
    }
}
