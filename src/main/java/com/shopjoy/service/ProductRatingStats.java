package com.shopjoy.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates rating statistics for a product.
 */
public class ProductRatingStats {
    private final int productId;
    private final double averageRating;
    private final int totalReviews;
    // index 0 -> rating 1, index 4 -> rating 5
    private final int[] ratingDistribution;

    public ProductRatingStats(int productId, double averageRating, int totalReviews, int[] ratingDistribution) {
        this.productId = productId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        if (ratingDistribution == null || ratingDistribution.length != 5) {
            this.ratingDistribution = new int[5];
        } else {
            this.ratingDistribution = ratingDistribution.clone();
        }
    }

    public int getProductId() {
        return productId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public int[] getRatingDistribution() {
        return ratingDistribution.clone();
    }

    /**
     * Returns the percentage (0.0 - 100.0) of reviews that have the given rating.
     * Rating must be 1..5.
     */
    public double getPercentageForRating(int rating) {
        if (rating < 1 || rating > 5) return 0.0;
        if (totalReviews <= 0) return 0.0;
        int count = ratingDistribution[rating - 1];
        return ((double) count / (double) totalReviews) * 100.0;
    }

    /**
     * Returns a Map of star -> percentage for each rating 1..5.
     */
    public Map<Integer, Double> getStarPercentages() {
        Map<Integer, Double> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            map.put(i, getPercentageForRating(i));
        }
        return map;
    }
}
