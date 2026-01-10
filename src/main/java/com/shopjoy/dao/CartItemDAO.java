package com.shopjoy.dao;

import com.shopjoy.model.CartItem;
import com.shopjoy.util.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartItemDAO implements GenericDAO<CartItem, Integer> {

    @Override
    public CartItem findById(Integer id) throws SQLException {
        if (id == null)
            return null;
        String sql = "SELECT * FROM cart_items WHERE cart_item_id = ?";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToCartItem(rs) : null;
            }
        }
    }

    @Override
    public List<CartItem> findAll() throws SQLException {
        return new ArrayList<>(); // Not typically used for carts
    }

    @Override
    public CartItem save(CartItem item) throws SQLException {
        if (item == null)
            return null;
        // Check if exists first for this user and product
        CartItem existing = findByUserAndProduct(item.getUserId(), item.getProductId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            return update(existing);
        }

        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) RETURNING cart_item_id";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getUserId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item.setCartItemId(rs.getInt("cart_item_id"));
                }
            }
        } catch (SQLException e) {
            // Fallback if table doesn't exist? No, we assume it does.
            // If table missing, we should probably create it?
            // But usually DAOs assume schema.
            throw e;
        }
        return item;
    }

    @Override
    public CartItem update(CartItem item) throws SQLException {
        if (item == null)
            return null;
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantity());
            ps.setInt(2, item.getCartItemId());
            ps.executeUpdate();
        }
        return item;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        if (id == null)
            return false;
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        return 0;
    }

    public List<CartItem> findByUserId(int userId) throws SQLException {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT * FROM cart_items WHERE user_id = ? ORDER BY cart_item_id";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCartItem(rs));
                }
            }
        }
        return list;
    }

    public void clearCart(int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public CartItem findByUserAndProduct(int userId, int productId) throws SQLException {
        String sql = "SELECT * FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToCartItem(rs) : null;
            }
        }
    }

    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setCartItemId(rs.getInt("cart_item_id"));
        item.setUserId(rs.getInt("user_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        // item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); //
        // Optional if column exists
        return item;
    }

    // Helper to ensure table exists (HACK since I cannot run DDL easily)
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS cart_items (" +
                "cart_item_id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "product_id INT NOT NULL, " +
                "quantity INT NOT NULL DEFAULT 1" +
                ")";
        try (Connection conn = DbConfig.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Ignore or log
            System.err.println("Could not create cart_items table: " + e.getMessage());
        }
    }
}
