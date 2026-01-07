package com.shopjoy.dao;

import com.shopjoy.model.Category;
import com.shopjoy.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Category entity. Implements CRUD and category-specific queries.
 */
public class CategoryDAO implements GenericDAO<Category, Integer> {

    @Override
    public Category findById(Integer categoryId) throws SQLException {
        if (categoryId == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT category_id, category_name, description, parent_category_id, created_at FROM categories WHERE category_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToCategory(rs);
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
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT category_id, category_name, description, parent_category_id, created_at FROM categories ORDER BY category_name";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToCategory(rs));
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
    public Category save(Category category) throws SQLException {
        if (category == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO categories (category_name, description, parent_category_id, created_at) VALUES (?, ?, ?, ?) RETURNING category_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, category.getCategoryName());
                ps.setString(2, category.getDescription());
                if (category.getParentCategoryId() == null) ps.setNull(3, Types.INTEGER);
                else ps.setInt(3, category.getParentCategoryId());
                ps.setObject(4, category.getCreatedAt());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) category.setCategoryId(rs.getInt("category_id"));
                }
            }
            return category;
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Category update(Category category) throws SQLException {
        if (category == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE categories SET category_name = ?, description = ?, parent_category_id = ?, updated_at = CURRENT_TIMESTAMP WHERE category_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, category.getCategoryName());
                ps.setString(2, category.getDescription());
                if (category.getParentCategoryId() == null) ps.setNull(3, Types.INTEGER);
                else ps.setInt(3, category.getParentCategoryId());
                ps.setInt(4, category.getCategoryId());
                ps.executeUpdate();
            }
            return category;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer categoryId) throws SQLException {
        if (categoryId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM categories WHERE category_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoryId);
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
            String sql = "SELECT COUNT(*) AS cnt FROM categories";
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

    public List<Category> findTopLevelCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT category_id, category_name, description, parent_category_id, created_at FROM categories WHERE parent_category_id IS NULL ORDER BY category_name";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToCategory(rs));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findTopLevelCategories SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Category> findSubcategories(Integer parentCategoryId) throws SQLException {
        List<Category> list = new ArrayList<>();
        if (parentCategoryId == null) return list;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT category_id, category_name, description, parent_category_id, created_at FROM categories WHERE parent_category_id = ? ORDER BY category_name";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, parentCategoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToCategory(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findSubcategories SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public Category findByName(String categoryName) throws SQLException {
        if (categoryName == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT category_id, category_name, description, parent_category_id, created_at FROM categories WHERE category_name = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, categoryName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToCategory(rs);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("findByName SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean hasProducts(int categoryId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM products WHERE category_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong("cnt") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            System.out.println("hasProducts SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public int getProductCount(int categoryId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM products WHERE category_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("cnt");
                }
            }
            return 0;
        } catch (SQLException e) {
            System.out.println("getProductCount SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Category> findCategoriesWithProducts() throws SQLException {
        List<Category> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            // Join categories to products and return distinct categories that have products
            String sql = "SELECT DISTINCT c.category_id, c.category_name, c.description, c.parent_category_id, c.created_at FROM categories c INNER JOIN products p ON c.category_id = p.category_id ORDER BY c.category_name";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToCategory(rs));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findCategoriesWithProducts SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Map a ResultSet row to Category object.
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        c.setDescription(rs.getString("description"));
        int parentId = rs.getInt("parent_category_id");
        if (rs.wasNull()) c.setParentCategoryId(null);
        else c.setParentCategoryId(parentId);
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
