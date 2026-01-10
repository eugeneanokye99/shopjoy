package com.shopjoy.controller;

import com.shopjoy.ShopJoyApp;
import com.shopjoy.model.User;
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
 * Controller class for handling user registration
 */
public class RegisterController {

    @FXML
    private TextField regUsernameField;

    @FXML
    private TextField regEmailField;

    @FXML
    private TextField regFirstNameField;

    @FXML
    private TextField regLastNameField;

    @FXML
    private TextField regPhoneField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private PasswordField regConfirmPasswordField;

    @FXML
    private Label registerMessageLabel;

    @FXML
    private Button createAccountButton;

    @FXML
    private Button backToLoginButton;

    private UserService userService = new UserService();

    /**
     * Initialize the controller
     * Called automatically after FXML fields are injected
     */
    @FXML
    public void initialize() {
        // Clear message label
        registerMessageLabel.setText("");
    }

    /**
     * Handle create account button click
     * Validates input and creates new user account
     */
    @FXML
    public void handleCreateAccount(ActionEvent event) {
        try {
            // Get all field values
            String username = regUsernameField.getText().trim();
            String email = regEmailField.getText().trim();
            String password = regPasswordField.getText();
            String confirmPassword = regConfirmPasswordField.getText();
            String firstName = regFirstNameField.getText().trim();
            String lastName = regLastNameField.getText().trim();
            String phone = regPhoneField.getText().trim();

            // Validate all required fields are filled
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || 
                confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                showMessage("All fields are required", true);
                return;
            }

            // Validate username (3-50 chars)
            if (username.length() < 3 || username.length() > 50) {
                showMessage("Username must be 3-50 characters", true);
                return;
            }

            // Validate email format
            if (!isValidEmail(email)) {
                showMessage("Invalid email format", true);
                return;
            }

            // Validate password (min 8 chars)
            if (password.length() < 8) {
                showMessage("Password must be at least 8 characters", true);
                return;
            }

            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                showMessage("Passwords do not match", true);
                return;
            }

            // Attempt registration
            User user = userService.registerUser(username, email, password, firstName, lastName, phone);

            if (user == null) {
                // Registration failed
                showMessage("Registration failed. Username or email already exists.", true);
            } else {
                // Registration successful
                showMessage("Registration successful! Please login.", false);
                
                // Clear form
                clearForm();
                
                // Wait 2 seconds before navigating to login
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Stage stage = getStage(event);
                                ShopJoyApp.switchScene(stage, "login.fxml", "Login - ShopJoy");
                            } catch (Exception e) {
                                System.err.println("Error navigating to login: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }

        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            showMessage("An error occurred during registration. Please try again.", true);
        }
    }

    /**
     * Handle back to login button click
     * Navigates back to login screen
     */
    @FXML
    public void handleBackToLogin(ActionEvent event) {
        try {
            // Get stage
            Stage stage = getStage(event);
            
            // Switch to login.fxml
            ShopJoyApp.switchScene(stage, "login.fxml", "Login - ShopJoy");
            
        } catch (Exception e) {
            System.err.println("Error navigating to login: " + e.getMessage());
            e.printStackTrace();
            showMessage("Error loading login screen", true);
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
        registerMessageLabel.setText(message);
        
        if (isError) {
            // Set style to red text for errors
            registerMessageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            // Set style to green text for success
            registerMessageLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
        }
    }

    /**
     * Helper method to validate email format
     * @param email The email to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Helper method to clear the registration form
     */
    private void clearForm() {
        regUsernameField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        regFirstNameField.clear();
        regLastNameField.clear();
        regPhoneField.clear();
        registerMessageLabel.setText("");
        registerMessageLabel.setStyle("");
    }

    /**
     * Validate all fields
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFields() {
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String firstName = regFirstNameField.getText().trim();
        String lastName = regLastNameField.getText().trim();
        String phone = regPhoneField.getText().trim();

        // Check if all fields are filled
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || 
            confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            showMessage("All fields are required", true);
            return false;
        }

        // Validate username length
        if (username.length() < 3 || username.length() > 50) {
            showMessage("Username must be 3-50 characters", true);
            return false;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showMessage("Invalid email format", true);
            return false;
        }

        // Validate password length
        if (password.length() < 8) {
            showMessage("Password must be at least 8 characters", true);
            return false;
        }

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match", true);
            return false;
        }

        return true;
    }
}
