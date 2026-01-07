package com.shopjoy.dao;

import com.shopjoy.model.OrderItem;
import com.shopjoy.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items WHERE order_item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToOrderItem(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findById SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<OrderItem> findAll() throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToOrderItem(rs));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findAll SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public OrderItem save(OrderItem item) throws SQLException {
        if (item == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal, created_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING order_item_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public OrderItem update(OrderItem item) throws SQLException {
        if (item == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE order_items SET quantity = ?, unit_price = ?, subtotal = ? WHERE order_item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, item.getQuantity());
                ps.setDouble(2, item.getUnitPrice());
                ps.setDouble(3, item.getSubtotal());
                ps.setInt(4, item.getOrderItemId());
                ps.executeUpdate();
            }
            return item;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer orderItemId) throws SQLException {
        if (orderItemId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM order_items WHERE order_item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderItemId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("delete SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public long count() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM order_items";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("cnt");
                return 0L;
            }
        } catch (SQLException e) {
            System.out.println("count SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    // --- Custom methods ---

    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToOrderItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findByOrderId SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<OrderItem> findByProductId(int productId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at FROM order_items WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToOrderItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findByProductId SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean saveOrderItems(List<OrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);
            String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                // try to assign generated keys back to items if available
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    int idx = 0;
                    while (keys.next() && idx < items.size()) {
                        items.get(idx).setOrderItemId(keys.getInt(1));
                        idx++;
                    }
                }
                // verify all results are successful (>=0)
                for (int r : results) {
                    if (r == PreparedStatement.EXECUTE_FAILED) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            System.out.println("saveOrderItems SQLException: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public double calculateOrderTotal(int orderId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT SUM(subtotal) AS total FROM order_items WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double val = rs.getDouble("total");
                        if (rs.wasNull()) return 0.0;
                        return val;
                    }
                    return 0.0;
                }
            }
        } catch (SQLException e) {
            System.out.println("calculateOrderTotal SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public int getTotalQuantitySold(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT SUM(quantity) AS total_qty FROM order_items WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int val = rs.getInt("total_qty");
                        if (rs.wasNull()) return 0;
                        return val;
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("getTotalQuantitySold SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Map<String, Object>> findTopSellingProducts(int limit) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (limit <= 0) return result;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, SUM(quantity) AS total_sold FROM order_items GROUP BY product_id ORDER BY total_sold DESC LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            System.out.println("findTopSellingProducts SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Map ResultSet row to OrderItem.
     */
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
