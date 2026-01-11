package com.shopjoy.cache;

import com.shopjoy.model.Category;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CategoryCache - An in-memory cache to reduce database queries for categories.
 * Categories change less frequently than products, hence a longer expiry time.
 * Implements a synchronized singleton pattern for thread safety.
 */
public class CategoryCache {

    private Map<Integer, Category> categoryCache = new HashMap<>();
    private List<Category> topLevelCategories = new ArrayList<>();
    private Map<Integer, List<Category>> subcategoriesCache = new HashMap<>();
    private volatile long lastCacheUpdate = System.currentTimeMillis();
    private static final long CACHE_EXPIRY_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds
    private static CategoryCache instance;

    /**
     * Private constructor for singleton pattern.
     */
    private CategoryCache() {
        // Initialization if needed
    }

    /**
     * Gets the singleton instance of CategoryCache.
     * 
     * @return The CategoryCache instance.
     */
    public static synchronized CategoryCache getInstance() {
        if (instance == null) {
            instance = new CategoryCache();
        }
        return instance;
    }

    /**
     * Caches a single category.
     * 
     * @param category The category to cache.
     */
    public synchronized void cacheCategory(Category category) {
        if (category != null) {
            categoryCache.put(category.getCategoryId(), category);
            lastCacheUpdate = System.currentTimeMillis();
            System.out.println(
                    "Category cached: " + category.getCategoryName() + " (ID: " + category.getCategoryId() + ")");
        }
    }

    /**
     * Retrieves a category from the cache.
     * 
     * @param categoryId The ID of the category.
     * @return The cached Category, or null if not found or expired.
     */
    public synchronized Category getCategory(int categoryId) {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return categoryCache.get(categoryId);
    }

    /**
     * Caches a list of categories, clearing the existing internal ID map first.
     * 
     * @param categories The list of categories to cache.
     */
    public synchronized void cacheCategoryList(List<Category> categories) {
        categoryCache.clear();
        if (categories != null) {
            for (Category category : categories) {
                categoryCache.put(category.getCategoryId(), category);
            }
        }
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("Cached " + categoryCache.size() + " categories in master map.");
    }

    /**
     * Caches the list of top-level categories.
     * 
     * @param categories The list of top-level categories.
     */
    public synchronized void cacheTopLevelCategories(List<Category> categories) {
        topLevelCategories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("Top-level categories cached.");
    }

    /**
     * Retrieves top-level categories from the cache.
     * 
     * @return List of top-level categories, or null if expired.
     */
    public synchronized List<Category> getTopLevelCategories() {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return topLevelCategories.isEmpty() ? null : new ArrayList<>(topLevelCategories);
    }

    /**
     * Caches subcategories for a specific parent.
     * 
     * @param parentId      The parent category ID.
     * @param subcategories The list of subcategories.
     */
    public synchronized void cacheSubcategories(int parentId, List<Category> subcategories) {
        subcategoriesCache.put(parentId, subcategories != null ? new ArrayList<>(subcategories) : new ArrayList<>());
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("Subcategories cached for parent ID: " + parentId);
    }

    /**
     * Retrieves subcategories from the cache.
     * 
     * @param parentId The parent category ID.
     * @return List of cached subcategories, or null if not found or expired.
     */
    public synchronized List<Category> getSubcategories(int parentId) {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return subcategoriesCache.get(parentId);
    }

    /**
     * Invalidates the entire category cache.
     */
    public synchronized void invalidateCache() {
        categoryCache.clear();
        topLevelCategories.clear();
        subcategoriesCache.clear();
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("All category caches invalidated.");
    }

    /**
     * Invalidates a specific category and dependent structure caches.
     * 
     * @param categoryId The ID of the category to invalidate.
     */
    public synchronized void invalidateCategory(int categoryId) {
        categoryCache.remove(categoryId);
        // Clear structure caches because hierarchy may have changed
        subcategoriesCache.clear();
        topLevelCategories.clear();
        System.out.println("Category " + categoryId + " invalidated. Structural caches cleared.");
    }

    /**
     * Retrieves all categories from the cache.
     * 
     * @return List of all cached categories, or null if expired or empty.
     */
    public synchronized List<Category> getAllCachedCategories() {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return categoryCache.isEmpty() ? null : new ArrayList<>(categoryCache.values());
    }

    /**
     * Checks if the cache is still valid based on time.
     * 
     * @return true if valid, false if expired.
     */
    public boolean isCacheValid() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastCacheUpdate) < CACHE_EXPIRY_TIME;
    }

    /**
     * Gets the current size of the category cache.
     * 
     * @return Total number of cached categories.
     */
    public int getCacheSize() {
        return categoryCache.size();
    }

    /**
     * Prints current category cache statistics to the console.
     */
    public synchronized void printCacheStats() {
        long ageSeconds = (System.currentTimeMillis() - lastCacheUpdate) / 1000;
        System.out.println("=== Category Cache Statistics ===");
        System.out.println("Total categories cached: " + categoryCache.size());
        System.out.println("Top-level categories cached: " + topLevelCategories.size());
        System.out.println("Subcategory parent caches: " + subcategoriesCache.size());
        System.out.println("Cache age: " + ageSeconds + " seconds");
        System.out.println("Cache validity status: " + (isCacheValid() ? "VALID" : "EXPIRED"));
        System.out.println("=================================");
    }
}
