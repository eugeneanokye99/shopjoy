package com.shopjoy.dao;

import com.shopjoy.model.Product;
import com.shopjoy.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Product entity. Implements CRUD and additional queries.
 */
public class ProductDAO implements GenericDAO<Product, Integer> {

    @Override
    public Product findById(Integer productId) throws SQLException {
        if (productId == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToProduct(rs);
                    }
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
    public List<Product> findAll() throws SQLException {
        List<Product> results = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products ORDER BY product_name";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapResultSetToProduct(rs));
            }
            return results;
        } catch (SQLException e) {
            System.out.println("findAll SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Product save(Product product) throws SQLException {
        if (product == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO products (product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING product_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
                    if (rs.next()) {
                        int generatedId = rs.getInt("product_id");
                        product.setProductId(generatedId);
                    }
                }
            }
            return product;
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Product update(Product product) throws SQLException {
        if (product == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE products SET product_name=?, description=?, category_id=?, price=?, cost_price=?, sku=?, brand=?, image_url=?, is_active=?, updated_at=CURRENT_TIMESTAMP WHERE product_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            // Return the product; caller may choose to re-query for freshest data
            return product;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer productId) throws SQLException {
        if (productId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM products WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
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
            String sql = "SELECT COUNT(*) AS cnt FROM products";
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

    // --- Additional custom methods ---

    public List<Product> findByCategory(int categoryId) throws SQLException {
        List<Product> results = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products WHERE category_id = ? ORDER BY product_name";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) results.add(mapResultSetToProduct(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            System.out.println("findByCategory SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Product> searchByName(String searchTerm) throws SQLException {
        List<Product> results = new ArrayList<>();
        if (searchTerm == null) return results;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products WHERE product_name ILIKE ? ORDER BY product_name";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + searchTerm + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) results.add(mapResultSetToProduct(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            System.out.println("searchByName SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Product> findActiveProducts() throws SQLException {
        List<Product> results = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products WHERE is_active = true ORDER BY product_name";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapResultSetToProduct(rs));
            }
            return results;
        } catch (SQLException e) {
            System.out.println("findActiveProducts SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Product> findByPriceRange(double minPrice, double maxPrice) throws SQLException {
        List<Product> results = new ArrayList<>();
        if (minPrice > maxPrice) {
            double tmp = minPrice; minPrice = maxPrice; maxPrice = tmp;
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products WHERE price BETWEEN ? AND ? ORDER BY price";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, minPrice);
                ps.setDouble(2, maxPrice);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) results.add(mapResultSetToProduct(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            System.out.println("findByPriceRange SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public Product findBySKU(String sku) throws SQLException {
        if (sku == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at FROM products WHERE sku = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sku);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToProduct(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findBySKU SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Map a ResultSet row to Product object.
     */
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
        p.setCreatedAt((java.time.LocalDateTime) rs.getObject("created_at"));
        p.setUpdatedAt((java.time.LocalDateTime) rs.getObject("updated_at"));
        return p;
    }
}
