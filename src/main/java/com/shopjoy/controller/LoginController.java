package com.shopjoy.controller;

import com.shopjoy.ShopJoyApp;
import com.shopjoy.model.User;
import com.shopjoy.model.UserType;
import com.shopjoy.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for handling login screen functionality
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private UserService userService = new UserService();

    /**
     * Initialize the controller
     * Called automatically after FXML fields are injected
     */
    @FXML
    public void initialize() {
        // Clear any previous messages
        loginMessageLabel.setText("");
        
        // Set focus on username field
        if (usernameField != null) {
            usernameField.requestFocus();
        }
    }

    /**
     * Handle login button click
     * Authenticates user and navigates to appropriate dashboard
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            // Get username and password from fields
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            // Validate fields are not empty
            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please enter username and password", true);
                return;
            }

            // Call userService to authenticate user
            User user = userService.authenticateUser(username, password);

            // If authentication successful
            if (user != null) {
                // Store user in ShopJoyApp
                ShopJoyApp.setCurrentUser(user);
                System.out.println("Login successful: " + user.getUsername());

                // Get current stage
                Stage stage = getStage(event);

                // Check if user is admin and load appropriate dashboard
                boolean isAdmin = user.getUserType() == UserType.ADMIN || userService.isAdmin(user.getUserId());
                String target = isAdmin ? "admin_dashboard.fxml" : "customer_dashboard.fxml";
                String title = isAdmin ? "Admin Dashboard - ShopJoy" : "ShopJoy - Dashboard";

                try {
                    ShopJoyApp.switchScene(stage, target, title);
                } catch (Exception sceneEx) {
                    System.err.println("Failed to load " + target + ": " + sceneEx.getMessage());
                    sceneEx.printStackTrace();
                    showMessage("Unable to load dashboard. Please try again.", true);
                }
            } else {
                // Authentication failed
                showMessage("Invalid username or password", true);
                passwordField.clear();
                usernameField.requestFocus();
            }

        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            showMessage("An error occurred during login. Please try again.", true);
            passwordField.clear();
        }
    }

    /**
     * Handle register button click
     * Navigates to registration screen
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            // Get stage from event
            Stage stage = getStage(event);
            
            // Switch to registration scene
            ShopJoyApp.switchScene(stage, "register.fxml", "Register - ShopJoy");
            
        } catch (Exception e) {
            System.err.println("Error navigating to registration: " + e.getMessage());
            e.printStackTrace();
            showMessage("Error loading registration screen", true);
        }
    }

    /**
     * Helper method to get the stage from an action event
     */
    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /**
     * Helper method to show messages to the user
     * @param message The message to display
     * @param isError True if this is an error message, false for success
     */
    private void showMessage(String message, boolean isError) {
        loginMessageLabel.setText(message);
        
        if (isError) {
            // Set style to red text for errors
            loginMessageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            // Set style to green text for success
            loginMessageLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
        }
    }

    /**
     * Helper method to clear the form
     */
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        loginMessageLabel.setText("");
        loginMessageLabel.setStyle("");
    }
}
