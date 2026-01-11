package com.shopjoy.util;

import com.shopjoy.cache.CategoryCache;
import com.shopjoy.cache.ProductCache;
import com.shopjoy.service.CategoryService;
import com.shopjoy.service.ProductService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CacheManager - Centralized controller for managing all application caches.
 * Provides utilities for invalidation, monitoring, and proactive data
 * pre-loading.
 */
public class CacheManager {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "CacheRefreshThread");
        t.setDaemon(true);
        return t;
    });

    static {
        // Schedule automatic cache refresh every 4 minutes to stay ahead of 5/10 minute
        // expiries
        scheduler.scheduleAtFixedRate(CacheManager::warmUpCaches, 4, 4, TimeUnit.MINUTES);
    }

    /**
     * Invalidates all application caches.
     */
    public static void invalidateAllCaches() {
        ProductCache.getInstance().invalidateCache();
        CategoryCache.getInstance().invalidateCache();
        System.out.println("Global Command: All caches invalidated.");
    }

    /**
     * Prints consolidated statistics for all active caches.
     */
    public static void printAllCacheStats() {
        System.out.println("\n=== GLOBAL CACHE STATISTICS ===");
        ProductCache.getInstance().printCacheStats();
        CategoryCache.getInstance().printCacheStats();
        System.out.println("===============================\n");
    }

    /**
     * Checks if all application caches are currently valid.
     * 
     * @return true if all caches are within their expiry window.
     */
    public static boolean areAllCachesValid() {
        return ProductCache.getInstance().isCacheValid() && CategoryCache.getInstance().isCacheValid();
    }

    /**
     * Proactively loads frequently accessed data into the caches from the database.
     * This helps prevent "cold starts" for users navigating the shop.
     */
    public static void warmUpCaches() {
        System.out.println("Starting cache warm-up sequence...");

        try {
            ProductService productService = new ProductService();
            CategoryService categoryService = new CategoryService();

            // Load all products (this populates productCache)
            productService.getAllProducts();

            // Load categories (this populates categoryCache master map)
            categoryService.getAllCategories();

            // Load top-level categories specifically
            categoryService.getTopLevelCategories();

            System.out.println("Caches warmed up successfully. System is primed.");
        } catch (Exception e) {
            System.err.println("Cache warm-up failed: " + e.getMessage());
        }
    }

    /**
     * Shuts down the background refresh scheduler.
     * Should be called during application shutdown.
     */
    public static void shutdown() {
        scheduler.shutdownNow();
    }
}
