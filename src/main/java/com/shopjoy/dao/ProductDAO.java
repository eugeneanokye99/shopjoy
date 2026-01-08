package com.shopjoy.dao;

import com.shopjoy.model.Product;
import com.shopjoy.util.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements GenericDAO<Product, Integer> {

    @Override
    public Product findById(Integer productId) throws SQLException {
        if (productId == null) return null;

        String sql = "SELECT * FROM products WHERE product_id=?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToProduct(rs) : null;
            }
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
        return queryList("SELECT * FROM products ORDER BY product_name", null);
    }

    @Override
    public Product save(Product product) throws SQLException {
        if (product == null) return null;

        String sql = """
                INSERT INTO products
                (product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING product_id
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setInt(3, product.getCategoryId());
            ps.setDouble(4, product.getPrice());
            ps.setDouble(5, product.getCostPrice());
            ps.setString(6, product.getSku());
            ps.setString(7, product.getBrand());
            ps.setString(8, product.getImageUrl());
            ps.setBoolean(9, product.isActive());
            ps.setObject(10, product.getCreatedAt());
            ps.setObject(11, product.getUpdatedAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) product.setProductId(rs.getInt("product_id"));
            }
        }
        return product;
    }

    @Override
    public Product update(Product product) throws SQLException {
        if (product == null) return null;

        String sql = """
                UPDATE products
                SET product_name=?, description=?, category_id=?, price=?, cost_price=?, sku=?, brand=?, image_url=?, is_active=?, updated_at=CURRENT_TIMESTAMP
                WHERE product_id=?
                """;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getDescription());
            ps.setInt(3, product.getCategoryId());
            ps.setDouble(4, product.getPrice());
            ps.setDouble(5, product.getCostPrice());
            ps.setString(6, product.getSku());
            ps.setString(7, product.getBrand());
            ps.setString(8, product.getImageUrl());
            ps.setBoolean(9, product.isActive());
            ps.setInt(10, product.getProductId());

            ps.executeUpdate();
        }
        return product;
    }

    @Override
    public boolean delete(Integer productId) throws SQLException {
        if (productId == null) return false;

        String sql = "DELETE FROM products WHERE product_id=?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM products";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // --- Custom queries ---

    public List<Product> findByCategory(int categoryId) throws SQLException {
        return queryList("SELECT * FROM products WHERE category_id=? ORDER BY product_name",
                ps -> ps.setInt(1, categoryId));
    }

    public List<Product> searchByName(String searchTerm) throws SQLException {
        if (searchTerm == null) return new ArrayList<>();
        return queryList("SELECT * FROM products WHERE product_name ILIKE ? ORDER BY product_name",
                ps -> ps.setString(1, "%" + searchTerm + "%"));
    }

    public List<Product> findActiveProducts() throws SQLException {
        return queryList("SELECT * FROM products WHERE is_active=true ORDER BY product_name", null);
    }

    public List<Product> findByPriceRange(double minPrice, double maxPrice) throws SQLException {
        if (minPrice > maxPrice) {
            double tmp = minPrice; minPrice = maxPrice; maxPrice = tmp;
        }
        double finalMinPrice = minPrice;
        double finalMaxPrice = maxPrice;
        return queryList("SELECT * FROM products WHERE price BETWEEN ? AND ? ORDER BY price",
                ps -> {
                    ps.setDouble(1, finalMinPrice);
                    ps.setDouble(2, finalMaxPrice);
                });
    }

    public Product findBySKU(String sku) throws SQLException {
        if (sku == null) return null;
        return queryList("SELECT * FROM products WHERE sku=?",
                ps -> ps.setString(1, sku)).stream().findFirst().orElse(null);
    }

    // --- Helper methods ---

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setPrice(rs.getDouble("price"));
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setSku(rs.getString("sku"));
        p.setBrand(rs.getString("brand"));
        p.setImageUrl(rs.getString("image_url"));
        p.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) p.setUpdatedAt(updated.toLocalDateTime());

        return p;
    }

    private List<Product> queryList(String sql, SQLConsumer<PreparedStatement> paramSetter) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (paramSetter != null) paramSetter.accept(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
