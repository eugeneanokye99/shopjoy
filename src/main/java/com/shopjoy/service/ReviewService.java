package com.shopjoy.service;

import com.shopjoy.dao.OrderDAO;
import com.shopjoy.dao.OrderItemDAO;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.dao.ReviewDAO;
import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.Order;
import com.shopjoy.model.OrderItem;
import com.shopjoy.model.Product;
import com.shopjoy.model.Review;
import com.shopjoy.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that handles business logic for product reviews and ratings.
 */
public class ReviewService {
    private final ReviewDAO reviewDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final OrderItemDAO orderItemDAO; // optional, may be null

    /**
     * Initialize DAOs used by the service.
     */
    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
        this.orderItemDAO = new OrderItemDAO();
    }

    /**
     * Add a new review for a product by a user.
     * Returns the saved Review, or null on validation failure or error.
     */
    public Review addReview(int productId, int userId, int rating, String title, String comment) {
        // validate inputs
        if (productId <= 0 || userId <= 0) {
            System.err.println("addReview: invalid productId or userId");
            return null;
        }
        if (!isValidRating(rating)) {
            System.err.println("addReview: rating out of range");
            return null;
        }
        if (!isValidTitle(title) || !isValidComment(comment)) {
            System.err.println("addReview: title/comment too long");
            return null;
        }

        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                System.err.println("addReview: product not found");
                return null;
            }
            User user = userDAO.findById(userId);
            if (user == null) {
                System.err.println("addReview: user not found");
                return null;
            }

            // Prevent duplicate review
            if (reviewDAO.userHasReviewed(userId, productId)) {
                return null;
            }

            // Determine verified purchase
            boolean isVerified = false;
            if (orderItemDAO != null) {
                try {
                    List<OrderItem> items = orderItemDAO.findByProductId(productId);
                    if (items != null && !items.isEmpty()) {
                        OrderDAO orderDAO = new OrderDAO();
                        for (OrderItem oi : items) {
                            Order o = orderDAO.findById(oi.getOrderId());
                            if (o != null && o.getUserId() == userId) {
                                isVerified = true;
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("addReview: error checking purchases: " + e.getMessage());
                }
            }

            Review r = new Review();
            r.setProductId(productId);
            r.setUserId(userId);
            r.setRating(rating);
            r.setTitle(title != null ? title.trim() : null);
            r.setComment(comment != null ? comment.trim() : null);
            r.setVerifiedPurchase(isVerified);
            r.setHelpfulCount(0);
            LocalDateTime now = LocalDateTime.now();
            r.setCreatedAt(now);
            r.setUpdatedAt(now);

            try {
                return reviewDAO.save(r);
            } catch (SQLException e) {
                System.err.println("addReview: SQLException saving review: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("addReview: SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update an existing review. Only the review owner may update.
     * Returns the updated Review or null on failure.
     */
    public Review updateReview(int reviewId, int userId, int rating, String title, String comment) {
        if (reviewId <= 0 || userId <= 0) return null;
        if (!isValidRating(rating)) {
            System.err.println("updateReview: rating out of range");
            return null;
        }
        if (!isValidTitle(title) || !isValidComment(comment)) {
            System.err.println("updateReview: title/comment too long");
            return null;
        }

        try {
            Review existing = reviewDAO.findById(reviewId);
            if (existing == null) return null;
            if (existing.getUserId() != userId) return null; // not owner

            existing.setRating(rating);
            existing.setTitle(title != null ? title.trim() : null);
            existing.setComment(comment != null ? comment.trim() : null);
            existing.setUpdatedAt(LocalDateTime.now());

            try {
                return reviewDAO.update(existing);
            } catch (SQLException e) {
                System.err.println("updateReview: SQLException updating review: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("updateReview: SQLException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a review. Only owner or admin may delete.
     */
    public boolean deleteReview(int reviewId, int userId, boolean isAdmin) {
        if (reviewId <= 0) return false;
        try {
            Review r = reviewDAO.findById(reviewId);
            if (r == null) return false;
            if (r.getUserId() != userId && !isAdmin) return false;
            try {
                return reviewDAO.delete(reviewId);
            } catch (SQLException e) {
                System.err.println("deleteReview: SQLException deleting review: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("deleteReview: SQLException: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get reviews for a product, sorted by date descending.
     */
    public List<Review> getProductReviews(int productId) {
        if (productId <= 0) return new ArrayList<>();
        try {
            List<Review> list = reviewDAO.findByProductId(productId);
            if (list == null) return new ArrayList<>();
            list.sort(Comparator.comparing((Review rv) -> rv.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())));
            return list;
        } catch (SQLException e) {
            System.err.println("getProductReviews: SQLException: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get reviews written by a user.
     */
    public List<Review> getUserReviews(int userId) {
        if (userId <= 0) return new ArrayList<>();
        try {
            List<Review> list = reviewDAO.findByUserId(userId);
            return list != null ? list : new ArrayList<>();
        } catch (SQLException e) {
            System.err.println("getUserReviews: SQLException: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Return average rating for a product (0.0 when none).
     */
    public double getAverageRating(int productId) {
        if (productId <= 0) return 0.0;
        try {
            return reviewDAO.getAverageRating(productId);
        } catch (SQLException e) {
            System.err.println("getAverageRating: SQLException: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Return total review count for a product.
     */
    public int getReviewCount(int productId) {
        if (productId <= 0) return 0;
        try {
            return reviewDAO.getReviewCount(productId);
        } catch (SQLException e) {
            System.err.println("getReviewCount: SQLException: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Return only verified-purchase reviews for a product.
     */
    public List<Review> getVerifiedReviews(int productId) {
        if (productId <= 0) return new ArrayList<>();
        try {
            List<Review> list = reviewDAO.findVerifiedPurchaseReviews(productId);
            return list != null ? list : new ArrayList<>();
        } catch (SQLException e) {
            System.err.println("getVerifiedReviews: SQLException: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Increment helpful count for a review.
     */
    public boolean markReviewHelpful(int reviewId) {
        if (reviewId <= 0) return false;
        try {
            return reviewDAO.incrementHelpfulCount(reviewId);
        } catch (SQLException e) {
            System.err.println("markReviewHelpful: SQLException: " + e.getMessage());
            return false;
        }
    }

    /**
     * Calculate and return rating stats for a product.
     */
    public ProductRatingStats getProductRatingStats(int productId) {
        if (productId <= 0) return new ProductRatingStats(productId, 0.0, 0, new int[5]);
        try {
            List<Review> reviews = reviewDAO.findByProductId(productId);
            int total = reviews == null ? 0 : reviews.size();
            int[] dist = new int[5];
            for (Review r : reviews) {
                int rt = r.getRating();
                if (rt >= 1 && rt <= 5) dist[rt - 1]++;
            }
            double avg = reviewDAO.getAverageRating(productId);
            return new ProductRatingStats(productId, avg, total, dist);
        } catch (SQLException e) {
            System.err.println("getProductRatingStats: SQLException: " + e.getMessage());
            return new ProductRatingStats(productId, 0.0, 0, new int[5]);
        }
    }

    /**
     * Check if the user has already reviewed the product.
     */
    public boolean hasUserReviewedProduct(int userId, int productId) {
        if (userId <= 0 || productId <= 0) return false;
        try {
            return reviewDAO.userHasReviewed(userId, productId);
        } catch (SQLException e) {
            System.err.println("hasUserReviewedProduct: SQLException: " + e.getMessage());
            return false;
        }
    }

    /**
     * Return recent reviews across all products up to the limit.
     */
    public List<Review> getRecentReviews(int limit) {
        if (limit <= 0) return new ArrayList<>();
        try {
            List<Review> list = reviewDAO.findRecentReviews(limit);
            return list != null ? list : new ArrayList<>();
        } catch (SQLException e) {
            System.err.println("getRecentReviews: SQLException: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Return top rated products (productId, avgRating, reviewCount) limited by `limit`.
     */
    public List<Map<String, Object>> getTopRatedProducts(int limit) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (limit <= 0) return out;
        try {
            List<Map<String, Object>> rows = reviewDAO.findTopRatedProducts(limit);
            if (rows == null) return out;
            for (Map<String, Object> row : rows) {
                Map<String, Object> m = new HashMap<>();
                Object pid = row.getOrDefault("product_id", row.get("productId"));
                Object avg = row.getOrDefault("avg_rating", row.get("avgRating"));
                Object cnt = row.getOrDefault("review_count", row.get("reviewCount"));
                m.put("productId", pid instanceof Number ? ((Number) pid).intValue() : pid);
                m.put("avgRating", avg instanceof Number ? ((Number) avg).doubleValue() : avg);
                m.put("reviewCount", cnt instanceof Number ? ((Number) cnt).intValue() : cnt);
                out.add(m);
            }
            return out;
        } catch (SQLException e) {
            System.err.println("getTopRatedProducts: SQLException: " + e.getMessage());
            return out;
        }
    }

    /**
     * Check whether a user can review a product.
     */
    public boolean canUserReview(int userId, int productId) {
        if (userId <= 0 || productId <= 0) return false;
        try {
            if (reviewDAO.userHasReviewed(userId, productId)) return false;
            if (userDAO.findById(userId) == null) return false;
            if (productDAO.findById(productId) == null) return false;

            // optional: require a purchase to review
            if (orderItemDAO != null) {
                try {
                    List<OrderItem> items = orderItemDAO.findByProductId(productId);
                    if (items != null) {
                        OrderDAO orderDAO = new OrderDAO();
                        for (OrderItem oi : items) {
                            Order o = orderDAO.findById(oi.getOrderId());
                            if (o != null && o.getUserId() == userId) return true;
                        }
                    }
                    // no purchase found
                    return true; // allow reviews even if not purchased (flexible policy)
                } catch (SQLException e) {
                    System.err.println("canUserReview: SQLException checking purchases: " + e.getMessage());
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("canUserReview: SQLException: " + e.getMessage());
            return false;
        }
    }

    // --- Validation helpers ---
    private boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    private boolean isValidTitle(String title) {
        if (title == null) return true; // title optional
        return title.length() <= 200;
    }

    private boolean isValidComment(String comment) {
        if (comment == null) return true;
        return comment.length() <= 2000;
    }
}
