package com.shopjoy.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.SQLException;
import java.util.Optional;

/**
 * ExceptionHandler - Centralized utility for consistent error handling and user
 * feedback.
 * Provides standard methods for logging exceptions and displaying JavaFX
 * alerts.
 */
public class ExceptionHandler {

    /**
     * Standard handler for generic exceptions.
     * Logs the error to console and displays a user-friendly alert.
     * 
     * @param context The location or action where the exception occurred.
     * @param e       The exception object.
     */
    public static void handleException(String context, Exception e) {
        // Log the error
        System.err.println("ERROR in " + context + ": " + e.getMessage());
        e.printStackTrace();

        // Show user-friendly alert
        showErrorAlert("Error", "An error occurred in " + context, getUserFriendlyMessage(e));
    }

    /**
     * Specialized handler for SQL/Database exceptions.
     * Logs technical details (SQL State, Error Code) and shows a connection-focused
     * alert.
     * 
     * @param operation The database operation being performed (e.g., "saving
     *                  product").
     * @param e         The SQLException object.
     */
    public static void handleDatabaseException(String operation, SQLException e) {
        System.err.println("DATABASE ERROR during " + operation);
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        e.printStackTrace();

        showErrorAlert("Database Error",
                "Database error during " + operation,
                "Please check your database connection and try again.");
    }

    /**
     * Displays an error alert on the JavaFX application thread.
     */
    public static void showErrorAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Displays a success/information alert on the JavaFX application thread.
     */
    public static void showSuccessAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    /**
     * Displays a warning alert on the JavaFX application thread.
     */
    public static void showWarningAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Maps complex exceptions to simplified, user-friendly strings.
     */
    private static String getUserFriendlyMessage(Exception e) {
        if (e instanceof SQLException) {
            return "A database error occurred. Please try again.";
        } else if (e instanceof NullPointerException) {
            return "Required data is missing. Please check your input.";
        } else if (e instanceof NumberFormatException) {
            return "Invalid number format. Please enter valid numbers.";
        } else {
            return "An unexpected error occurred: " + e.getMessage();
        }
    }

    /**
     * Shows a confirmation dialog and returns the user's choice.
     * Note: This must be called from the JavaFX UI thread as it returns a value.
     */
    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
