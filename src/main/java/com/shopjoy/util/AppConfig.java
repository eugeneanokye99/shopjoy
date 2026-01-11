package com.shopjoy.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * AppConfig - Centralized application configuration and constants.
 * Manages all application-wide settings, UI constants, and environment
 * variables.
 */
public class AppConfig {

    // Application Info
    public static final String APP_NAME = "ShopJoy";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_AUTHOR = "Your Name";

    // Database Configuration (loaded from properties)
    public static String DB_URL;
    public static String DB_USERNAME;
    public static String DB_PASSWORD;

    // Cache Configuration
    public static final long PRODUCT_CACHE_EXPIRY = 5 * 60 * 1000; // 5 minutes
    public static final long CATEGORY_CACHE_EXPIRY = 10 * 60 * 1000; // 10 minutes

    // UI Configuration
    public static final int DEFAULT_WINDOW_WIDTH = 1200;
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    public static final int MIN_WINDOW_WIDTH = 1024;
    public static final int MIN_WINDOW_HEIGHT = 768;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_SEARCH_RESULTS = 100;

    // File Paths
    public static final String FXML_PATH = "/fxml/";
    public static final String CSS_PATH = "/css/";
    public static final String IMAGES_PATH = "/images/";

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";

    static {
        loadConfiguration();
    }

    /**
     * Loads database configuration from the application.properties file.
     */
    private static void loadConfiguration() {
        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
                return;
            }
            props.load(input);

            DB_URL = props.getProperty("db.url");
            DB_USERNAME = props.getProperty("db.username");
            DB_PASSWORD = props.getProperty("db.password");

            System.out.println("Configuration loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints the current application configuration to the console.
     */
    public static void printConfiguration() {
        System.out.println("=== " + APP_NAME + " Configuration ===");
        System.out.println("Version: " + APP_VERSION);
        System.out.println("Database URL: " + DB_URL);
        System.out.println("Cache Settings:");
        System.out.println("  Product Cache Expiry: " + (PRODUCT_CACHE_EXPIRY / 60000) + " minutes");
        System.out.println("  Category Cache Expiry: " + (CATEGORY_CACHE_EXPIRY / 60000) + " minutes");
        System.out.println("====================================");
    }
}
