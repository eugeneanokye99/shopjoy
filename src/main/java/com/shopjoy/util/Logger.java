package com.shopjoy.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger - A simple logging system for debugging and monitoring the ShopJoy
 * application.
 * Logs messages to both the console and a persistent local text file.
 */
public class Logger {

    private static final String LOG_FILE = "shopjoy.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Logging levels to categorize application events.
     */
    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG
    }

    /**
     * Logs an informational message.
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Logs a warning message.
     */
    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    /**
     * Logs an error message.
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    /**
     * Logs an error message along with the exception details.
     */
    public static void error(String message, Exception e) {
        log(LogLevel.ERROR, message + " - " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Logs a debug message for development purposes.
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    /**
     * Core logging logic: formats the message with a timestamp and level,
     * then outputs to both console and file.
     */
    private static void log(LogLevel level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Print to console
        System.out.println(logMessage);

        // Write to persistent log file
        writeToFile(logMessage);
    }

    /**
     * Appends the given message to the local shopjoy.log file.
     */
    private static void writeToFile(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Wipes the existing log file content and starts fresh.
     */
    public static void clearLog() {
        try {
            new FileWriter(LOG_FILE, false).close();
            info("Log file cleared");
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }
    }
}
