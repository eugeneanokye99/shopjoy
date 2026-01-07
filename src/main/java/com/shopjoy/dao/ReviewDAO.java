package com.shopjoy.dao;

import com.shopjoy.model.Review;
import com.shopjoy.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for Review entity. Implements CRUD and review-specific queries.
 */
public class ReviewDAO implements GenericDAO<Review, Integer> {

    @Override
    public Review findById(Integer reviewId) throws SQLException {
        if (reviewId == null) return null;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at FROM reviews WHERE review_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, reviewId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToReview(rs);
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
    public List<Review> findAll() throws SQLException {
        List<Review> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at FROM reviews ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReview(rs));
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
    public Review save(Review review) throws SQLException {
        if (review == null) return null;
        // validate rating
        if (review.getRating() < 1 || review.getRating() > 5) throw new IllegalArgumentException("rating must be between 1 and 5");

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING review_id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, review.getProductId());
                ps.setInt(2, review.getUserId());
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getTitle());
                ps.setString(5, review.getComment());
                ps.setBoolean(6, review.isVerifiedPurchase());
                ps.setInt(7, review.getHelpfulCount());
                ps.setObject(8, review.getCreatedAt());
                ps.setObject(9, review.getUpdatedAt());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) review.setReviewId(rs.getInt("review_id"));
                }
            }
            return review;
        } catch (SQLException e) {
            System.out.println("save SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Review update(Review review) throws SQLException {
        if (review == null) return null;
        if (review.getRating() < 1 || review.getRating() > 5) throw new IllegalArgumentException("rating must be between 1 and 5");

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE reviews SET rating = ?, title = ?, comment = ?, updated_at = CURRENT_TIMESTAMP WHERE review_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, review.getRating());
                ps.setString(2, review.getTitle());
                ps.setString(3, review.getComment());
                ps.setInt(4, review.getReviewId());
                ps.executeUpdate();
            }
            return review;
        } catch (SQLException e) {
            System.out.println("update SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer reviewId) throws SQLException {
        if (reviewId == null) return false;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM reviews WHERE review_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, reviewId);
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
            String sql = "SELECT COUNT(*) AS cnt FROM reviews";
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

    public List<Review> findByProductId(int productId) throws SQLException {
        List<Review> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at FROM reviews WHERE product_id = ? ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToReview(rs));
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

    public List<Review> findByUserId(int userId) throws SQLException {
        List<Review> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at FROM reviews WHERE user_id = ? ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToReview(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findByUserId SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public double getAverageRating(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double val = rs.getDouble("avg_rating");
                        if (rs.wasNull()) return 0.0;
                        return val;
                    }
                    return 0.0;
                }
            }
        } catch (SQLException e) {
            System.out.println("getAverageRating SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public int getReviewCount(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM reviews WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("cnt");
                    return 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("getReviewCount SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Review> findVerifiedPurchaseReviews(int productId) throws SQLException {
        List<Review> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at FROM reviews WHERE product_id = ? AND is_verified_purchase = true ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToReview(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findVerifiedPurchaseReviews SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Map<String, Object>> findTopRatedProducts(int limit) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (limit <= 0) return result;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count FROM reviews GROUP BY product_id HAVING COUNT(*) >= 3 ORDER BY avg_rating DESC LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("product_id", rs.getInt("product_id"));
                        row.put("avg_rating", rs.getDouble("avg_rating"));
                        row.put("review_count", rs.getInt("review_count"));
                        result.add(row);
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            System.out.println("findTopRatedProducts SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean incrementHelpfulCount(int reviewId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE reviews SET helpful_count = helpful_count + 1 WHERE review_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, reviewId);
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("incrementHelpfulCount SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public boolean userHasReviewed(int userId, int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) AS cnt FROM reviews WHERE user_id = ? AND product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong("cnt") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            System.out.println("userHasReviewed SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    public List<Review> findRecentReviews(int limit) throws SQLException {
        List<Review> list = new ArrayList<>();
        if (limit <= 0) return list;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at FROM reviews ORDER BY created_at DESC LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToReview(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            System.out.println("findRecentReviews SQLException: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConfig.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Map ResultSet row to Review object.
     */
    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setRating(rs.getInt("rating"));
        r.setTitle(rs.getString("title"));
        r.setComment(rs.getString("comment"));
        r.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
        r.setHelpfulCount(rs.getInt("helpful_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());
        return r;
    }
}
