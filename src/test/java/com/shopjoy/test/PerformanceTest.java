package com.shopjoy.test;

import com.shopjoy.model.Product;
import com.shopjoy.service.CategoryService;
import com.shopjoy.service.ProductService;
import com.shopjoy.util.CacheManager;
import java.util.List;

/**
 * PerformanceTest - Verifies the effectiveness of the caching implementation
 * by comparing database access times vs memory cache access times.
 */
public class PerformanceTest {

    private static final ProductService productService = new ProductService();
    private static final CategoryService categoryService = new CategoryService();

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      PERFORMANCE TEST RESULTS          ");
        System.out.println("========================================");

        try {
            testProductLoadPerformance();
            testProductSearchPerformance();
            testCategoryLoadPerformance();
            testRepeatedAccess();
        } catch (Exception e) {
            System.err.println("Performance test interrupted: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================");
        System.out.println("Testing sequence completed.");
        CacheManager.shutdown(); // Stop the refresh thread to allow JVM to exit
    }

    private static void testProductLoadPerformance() {
        CacheManager.invalidateAllCaches();

        long dbTime = measureExecutionTime(() -> productService.getAllProducts());
        long cacheTime = measureExecutionTime(() -> productService.getAllProducts());

        printStats("Product Load Performance", "All Products", dbTime, cacheTime);
    }

    private static void testProductSearchPerformance() {
        CacheManager.invalidateAllCaches();
        String keyword = "laptop";

        long dbTime = measureExecutionTime(() -> productService.searchProducts(keyword));
        long cacheTime = measureExecutionTime(() -> productService.searchProducts(keyword));

        printStats("Product Search Performance", "Keyword: '" + keyword + "'", dbTime, cacheTime);
    }

    private static void testCategoryLoadPerformance() {
        CacheManager.invalidateAllCaches();

        long dbTime = measureExecutionTime(() -> categoryService.getAllCategories());
        long cacheTime = measureExecutionTime(() -> categoryService.getAllCategories());

        printStats("Category Load Performance", "All Categories", dbTime, cacheTime);
    }

    private static void testRepeatedAccess() {
        CacheManager.invalidateAllCaches();

        // Warm up and get a real ID to ensure a fair test
        List<Product> products = productService.getAllProducts();
        if (products == null || products.isEmpty()) {
            System.out.println("\nTest: Repeated Access");
            System.out.println("Status: SKIPPED (No products found in database)");
            return;
        }

        int testId = products.get(0).getProductId();
        int iterations = 100;

        long totalTime = measureExecutionTime(() -> {
            for (int i = 0; i < iterations; i++) {
                productService.getProductById(testId);
            }
        });

        System.out.println("\nTest: Repeated Access Performance (ID: " + testId + ")");
        System.out.println("Total iterations: " + iterations);
        System.out.println("Total execution time: " + totalTime + "ms");
        System.out.printf("Average time per access: %.4fms\n", (double) totalTime / iterations);
        System.out.println("Estimated Cache Hit Ratio: " + ((iterations - 1) * 100 / iterations) + "%");
    }

    /**
     * Helper to measure the execution time of a specific task.
     */
    private static long measureExecutionTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - start;
    }

    /**
     * Standardized output for test results.
     */
    private static void printStats(String testName, String detail, long dbTime, long cacheTime) {
        // Prevent division by zero if dbTime is 0 (though unlikely)
        double improvement = dbTime > 0 ? ((double) (dbTime - cacheTime) / dbTime) * 100.0 : 0.0;

        System.out.println("\nTest: " + testName + " (" + detail + ")");
        System.out.println("First Access (Database): " + dbTime + "ms");
        System.out.println("Second Access (Cache):    " + cacheTime + "ms");
        if (dbTime > 0) {
            System.out.printf("Improvement: %.1f%%\n", improvement);
        } else {
            System.out.println("Improvement: N/A (Database call was near 0ms)");
        }
    }
}
