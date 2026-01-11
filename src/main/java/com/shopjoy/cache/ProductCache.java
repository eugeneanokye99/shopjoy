package com.shopjoy.cache;

import com.shopjoy.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductCache - An in-memory cache to reduce database queries for products.
 * Implements a synchronized singleton pattern for thread safety.
 */
public class ProductCache {

    private Map<Integer, Product> productCache = new HashMap<>();
    private Map<String, List<Product>> searchCache = new HashMap<>();
    private Map<Integer, List<Product>> categoryCache = new HashMap<>();
    private volatile long lastCacheUpdate = System.currentTimeMillis();
    private static final long CACHE_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static ProductCache instance;

    /**
     * Private constructor for singleton pattern.
     */
    private ProductCache() {
        // Initialization if needed
    }

    /**
     * Gets the singleton instance of ProductCache.
     * 
     * @return The ProductCache instance.
     */
    public static synchronized ProductCache getInstance() {
        if (instance == null) {
            instance = new ProductCache();
        }
        return instance;
    }

    /**
     * Caches a single product.
     * 
     * @param product The product to cache.
     */
    public synchronized void cacheProduct(Product product) {
        if (product != null) {
            productCache.put(product.getProductId(), product);
            lastCacheUpdate = System.currentTimeMillis();
            System.out.println("Product cached: " + product.getProductName() + " (ID: " + product.getProductId() + ")");
        }
    }

    /**
     * Retrieves a product from the cache.
     * 
     * @param productId The ID of the product.
     * @return The cached Product, or null if not found or expired.
     */
    public synchronized Product getProduct(int productId) {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return productCache.get(productId);
    }

    /**
     * Caches a list of products, clearing the existing product cache first.
     * 
     * @param products The list of products to cache.
     */
    public synchronized void cacheProductList(List<Product> products) {
        productCache.clear();
        if (products != null) {
            for (Product product : products) {
                productCache.put(product.getProductId(), product);
            }
        }
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("Cached " + productCache.size() + " products.");
    }

    /**
     * Caches search results for a specific term.
     * 
     * @param searchTerm The term searched.
     * @param results    The list of products returned.
     */
    public synchronized void cacheSearchResults(String searchTerm, List<Product> results) {
        if (searchTerm != null) {
            searchCache.put(searchTerm.toLowerCase(), results != null ? new ArrayList<>(results) : new ArrayList<>());
            lastCacheUpdate = System.currentTimeMillis();
            System.out.println("Search results cached for term: " + searchTerm);
        }
    }

    /**
     * Retrieves search results from the cache.
     * 
     * @param searchTerm The term searched.
     * @return The list of cached Products, or null if not found or expired.
     */
    public synchronized List<Product> getSearchResults(String searchTerm) {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return searchTerm != null ? searchCache.get(searchTerm.toLowerCase()) : null;
    }

    /**
     * Caches products for a specific category.
     * 
     * @param categoryId The category ID.
     * @param products   The list of products in the category.
     */
    public synchronized void cacheCategoryProducts(int categoryId, List<Product> products) {
        categoryCache.put(categoryId, products != null ? new ArrayList<>(products) : new ArrayList<>());
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("Category products cached for category ID: " + categoryId);
    }

    /**
     * Retrieves category products from the cache.
     * 
     * @param categoryId The category ID.
     * @return The list of cached Products, or null if not found or expired.
     */
    public synchronized List<Product> getCategoryProducts(int categoryId) {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return categoryCache.get(categoryId);
    }

    /**
     * Invalidates the entire cache.
     */
    public synchronized void invalidateCache() {
        productCache.clear();
        searchCache.clear();
        categoryCache.clear();
        lastCacheUpdate = System.currentTimeMillis();
        System.out.println("All product caches invalidated.");
    }

    /**
     * Invalidates a specific product and dependent caches.
     * 
     * @param productId The ID of the product to invalidate.
     */
    public synchronized void invalidateProduct(int productId) {
        productCache.remove(productId);
        // Search and category caches are cleared because they may contain the outdated
        // product
        searchCache.clear();
        categoryCache.clear();
        System.out.println("Product " + productId + " invalidated. Search and Category caches cleared.");
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
     * Gets the current size of the product cache.
     * 
     * @return Total number of cached products.
     */
    public int getCacheSize() {
        return productCache.size();
    }

    /**
     * Retrieves all products from the cache.
     * 
     * @return List of all cached Products, or null if expired or empty.
     */
    public synchronized List<Product> getAllCachedProducts() {
        if (!isCacheValid()) {
            invalidateCache();
            return null;
        }
        return productCache.isEmpty() ? null : new ArrayList<>(productCache.values());
    }

    /**
     * Prints current cache statistics to the console.
     */
    public synchronized void printCacheStats() {
        long ageSeconds = (System.currentTimeMillis() - lastCacheUpdate) / 1000;
        System.out.println("=== Product Cache Statistics ===");
        System.out.println("Total products cached: " + productCache.size());
        System.out.println("Search results cached: " + searchCache.size());
        System.out.println("Category caches: " + categoryCache.size());
        System.out.println("Cache age: " + ageSeconds + " seconds");
        System.out.println("Cache validity status: " + (isCacheValid() ? "VALID" : "EXPIRED"));
        System.out.println("================================");
    }
}
