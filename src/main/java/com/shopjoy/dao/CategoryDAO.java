package com.shopjoy.dao;

import com.shopjoy.model.Category;
import com.shopjoy.util.DbConfig;

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

        String sql = "SELECT category_id, category_name, description, parent_category_id, created_at " +
                "FROM categories WHERE category_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToCategory(rs) : null;
            }
        }
    }

    @Override
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();

        String sql = "SELECT category_id, category_name, description, parent_category_id, created_at " +
                "FROM categories ORDER BY category_name";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToCategory(rs));
        }
        return list;
    }

    @Override
    public Category save(Category category) throws SQLException {
        if (category == null) return null;

        String sql = "INSERT INTO categories (category_name, description, parent_category_id) " +
                "VALUES (?, ?, ?) RETURNING category_id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());

            if (category.getParentCategoryId() == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, category.getParentCategoryId());


            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) category.setCategoryId(rs.getInt("category_id"));
            }
        }
        return category;
    }

    @Override
    public Category update(Category category) throws SQLException {
        if (category == null) return null;

        String sql = "UPDATE categories SET category_name = ?, description = ?, parent_category_id = ?, " +
                "WHERE category_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());

            if (category.getParentCategoryId() == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, category.getParentCategoryId());

            ps.setInt(4, category.getCategoryId());
            ps.executeUpdate();
        }
        return category;
    }

    @Override
    public boolean delete(Integer categoryId) throws SQLException {
        if (categoryId == null) return false;

        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM categories";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // --- Custom methods ---

    public List<Category> findTopLevelCategories() throws SQLException {
        List<Category> list = new ArrayList<>();

        String sql = "SELECT category_id, category_name, description, parent_category_id, created_at " +
                "FROM categories WHERE parent_category_id IS NULL ORDER BY category_name";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToCategory(rs));
        }
        return list;
    }

    public List<Category> findSubcategories(Integer parentCategoryId) throws SQLException {
        List<Category> list = new ArrayList<>();
        if (parentCategoryId == null) return list;

        String sql = "SELECT category_id, category_name, description, parent_category_id, created_at " +
                "FROM categories WHERE parent_category_id = ? ORDER BY category_name";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, parentCategoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToCategory(rs));
            }
        }
        return list;
    }

    public Category findByName(String categoryName) throws SQLException {
        if (categoryName == null) return null;

        String sql = "SELECT category_id, category_name, description, parent_category_id, created_at " +
                "FROM categories WHERE category_name = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToCategory(rs) : null;
            }
        }
    }

    public boolean hasProducts(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM products WHERE category_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong("cnt") > 0;
            }
        }
    }

    public int getProductCount(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM products WHERE category_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    public List<Category> findCategoriesWithProducts() throws SQLException {
        List<Category> list = new ArrayList<>();

        String sql = "SELECT DISTINCT c.category_id, c.category_name, c.description, " +
                "c.parent_category_id, c.created_at " +
                "FROM categories c INNER JOIN products p ON c.category_id = p.category_id " +
                "ORDER BY c.category_name";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToCategory(rs));
        }
        return list;
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
        c.setParentCategoryId(rs.wasNull() ? null : parentId);

        var ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());

        return c;
    }
}
