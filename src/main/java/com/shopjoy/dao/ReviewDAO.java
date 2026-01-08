package com.shopjoy.dao;

import com.shopjoy.model.Review;
import com.shopjoy.util.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for Review entity. Implements CRUD and review-specific queries.
 * Fully refactored to use DbConfig and try-with-resources.
 */
public class ReviewDAO implements GenericDAO<Review, Integer> {

    @Override
    public Review findById(Integer reviewId) throws SQLException {
        if (reviewId == null) return null;

        String sql = "SELECT review_id, product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count, created_at, updated_at " +
                "FROM reviews WHERE review_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToReview(rs) : null;
            }
        }
    }

    @Override
    public List<Review> findAll() throws SQLException {
        String sql = "SELECT review_id, product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count, created_at, updated_at " +
                "FROM reviews ORDER BY created_at DESC";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToReview(rs));
        }
        return list;
    }

    @Override
    public Review save(Review review) throws SQLException {
        if (review == null) return null;
        if (review.getRating() < 1 || review.getRating() > 5)
            throw new IllegalArgumentException("rating must be between 1 and 5");

        String sql = "INSERT INTO reviews (product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING review_id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getTitle());
            ps.setString(5, review.getComment());
            ps.setBoolean(6, review.isVerifiedPurchase());
            ps.setInt(7, review.getHelpfulCount());


            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) review.setReviewId(rs.getInt("review_id"));
            }
        }
        return review;
    }

    @Override
    public Review update(Review review) throws SQLException {
        if (review == null) return null;
        if (review.getRating() < 1 || review.getRating() > 5)
            throw new IllegalArgumentException("rating must be between 1 and 5");

        String sql = "UPDATE reviews SET rating = ?, title = ?, comment = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE review_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, review.getRating());
            ps.setString(2, review.getTitle());
            ps.setString(3, review.getComment());
            ps.setInt(4, review.getReviewId());
            ps.executeUpdate();
        }
        return review;
    }

    @Override
    public boolean delete(Integer reviewId) throws SQLException {
        if (reviewId == null) return false;

        String sql = "DELETE FROM reviews WHERE review_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM reviews";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong("cnt") : 0L;
        }
    }

    // ---------------- Custom Methods ----------------

    public List<Review> findByProductId(int productId) throws SQLException {
        String sql = "SELECT review_id, product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count, created_at, updated_at " +
                "FROM reviews WHERE product_id = ? ORDER BY created_at DESC";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReview(rs));
            }
        }
        return list;
    }

    public List<Review> findByUserId(int userId) throws SQLException {
        String sql = "SELECT review_id, product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count, created_at, updated_at " +
                "FROM reviews WHERE user_id = ? ORDER BY created_at DESC";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReview(rs));
            }
        }
        return list;
    }

    public double getAverageRating(int productId) throws SQLException {
        String sql = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : val;
                }
                return 0.0;
            }
        }
    }

    public int getReviewCount(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM reviews WHERE product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    public List<Review> findVerifiedPurchaseReviews(int productId) throws SQLException {
        String sql = "SELECT review_id, product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count, created_at, updated_at " +
                "FROM reviews WHERE product_id = ? AND is_verified_purchase = true " +
                "ORDER BY created_at DESC";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReview(rs));
            }
        }
        return list;
    }

    public List<Map<String, Object>> findTopRatedProducts(int limit) throws SQLException {
        String sql = "SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
                "FROM reviews GROUP BY product_id HAVING COUNT(*) >= 3 " +
                "ORDER BY avg_rating DESC LIMIT ?";

        List<Map<String, Object>> result = new ArrayList<>();
        if (limit <= 0) return result;

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    }

    public boolean incrementHelpfulCount(int reviewId) throws SQLException {
        String sql = "UPDATE reviews SET helpful_count = helpful_count + 1 WHERE review_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean userHasReviewed(int userId, int productId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM reviews WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong("cnt") > 0;
            }
        }
    }

    public List<Review> findRecentReviews(int limit) throws SQLException {
        if (limit <= 0) return new ArrayList<>();

        String sql = "SELECT review_id, product_id, user_id, rating, title, comment, " +
                "is_verified_purchase, helpful_count, created_at, updated_at " +
                "FROM reviews ORDER BY created_at DESC LIMIT ?";

        List<Review> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReview(rs));
            }
        }
        return list;
    }

    // ---------------- Helper Mapping ----------------

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
