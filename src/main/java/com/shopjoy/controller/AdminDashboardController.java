package com.shopjoy.controller;

import com.shopjoy.ShopJoyApp;
import com.shopjoy.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AdminDashboardController - VIEW MANAGER
 * Responsible for navigation and dynamic view loading in admin dashboard
 * Each view has its own controller handling specific functionality
 */
public class AdminDashboardController {

    // FXML injected fields
    @FXML
    private Label adminWelcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button productsNavButton;

    @FXML
    private Button categoriesNavButton;

    @FXML
    private Button ordersNavButton;

    @FXML
    private Button inventoryNavButton;

    @FXML
    private Button customersNavButton;

    @FXML
    private Button reviewsNavButton;

    @FXML
    private Button reportsNavButton;

    @FXML
    private StackPane contentArea;

    // Instance variables
    private Button currentActiveButton = null;
    private Map<String, Parent> viewCache = new HashMap<>();

    /**
     * Initialize method - called automatically after FXML injection
     */
    @FXML
    public void initialize() {
        // Get current user from ShopJoyApp
        User currentUser = ShopJoyApp.getCurrentUser();

        if (currentUser != null) {
            adminWelcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName());
        } else {
            adminWelcomeLabel.setText("Welcome, Admin");
        }

        // Load products view by default
        loadProductsView(null);
    }

    /**
     * Dynamic view loading method with caching
     * 
     * @param fxmlFileName The FXML file to load from resources/fxml/admin/
     * @param navButton    The navigation button to highlight
     */
    private void loadView(String fxmlFileName, Button navButton) {
        try {
            // Check cache first
            Parent view = viewCache.get(fxmlFileName);

            if (view == null) {
                // Load FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/" + fxmlFileName));
                view = loader.load();

                // Cache the loaded view for better performance
                viewCache.put(fxmlFileName, view);

                System.out.println("Loaded view: " + fxmlFileName);
            } else {
                System.out.println("Using cached view: " + fxmlFileName);
            }

            // Clear content area and add new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Update active navigation button styling
            highlightActiveButton(navButton);

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFileName);
            e.printStackTrace();
            showError("Failed to load view: " + fxmlFileName);
        }
    }

    /**
     * Navigation handlers for each button
     */

    @FXML
    public void loadProductsView(ActionEvent event) {
        loadView("products_management.fxml", productsNavButton);
    }

    @FXML
    public void loadCategoriesView(ActionEvent event) {
        loadView("categories_management.fxml", categoriesNavButton);
    }

    @FXML
    public void loadOrdersView(ActionEvent event) {
        loadView("orders_management.fxml", ordersNavButton);
    }

    @FXML
    public void loadInventoryView(ActionEvent event) {
        loadView("inventory_management.fxml", inventoryNavButton);
    }

    @FXML
    public void loadCustomersView(ActionEvent event) {
        loadView("customers_management.fxml", customersNavButton);
    }

    @FXML
    public void loadReviewsView(ActionEvent event) {
        loadView("reviews_management.fxml", reviewsNavButton);
    }

    @FXML
    public void loadReportsView(ActionEvent event) {
        loadView("reports_view.fxml", reportsNavButton);
    }

    /**
     * Highlight the active navigation button
     * 
     * @param activeButton The button to highlight
     */
    private void highlightActiveButton(Button activeButton) {
        // Remove 'active' style class from all navigation buttons
        // Remove 'active' style class from all navigation buttons
        productsNavButton.getStyleClass().remove("active");
        categoriesNavButton.getStyleClass().remove("active");
        ordersNavButton.getStyleClass().remove("active");
        inventoryNavButton.getStyleClass().remove("active");
        customersNavButton.getStyleClass().remove("active");
        reviewsNavButton.getStyleClass().remove("active");
        reportsNavButton.getStyleClass().remove("active");

        // Add 'active' style class to the active button
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }

        currentActiveButton = activeButton;
    }

    /**
     * Handle logout button click
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Confirm logout
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Logout");
            confirmAlert.setHeaderText("Are you sure you want to logout?");
            confirmAlert.setContentText("You will be returned to the login screen.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Clear view cache
                viewCache.clear();

                // Logout
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                ShopJoyApp.logout(stage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error during logout");
        }
    }

    /**
     * Display error alert
     * 
     * @param message Error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Refresh the current view - useful for child controllers
     * Clears cache and reloads the current view
     */
    public void refreshCurrentView() {
        // Get the currently active button and reload its view
        // Clear cache for current view to force reload
        if (currentActiveButton != null) {
            String fxmlFile = null;

            if (currentActiveButton == productsNavButton) {
                fxmlFile = "products_management.fxml";
            } else if (currentActiveButton == categoriesNavButton) {
                fxmlFile = "categories_management.fxml";
            } else if (currentActiveButton == ordersNavButton) {
                fxmlFile = "orders_management.fxml";
            } else if (currentActiveButton == inventoryNavButton) {
                fxmlFile = "inventory_management.fxml";
            } else if (currentActiveButton == customersNavButton) {
                fxmlFile = "customers_management.fxml";
            } else if (currentActiveButton == reviewsNavButton) {
                fxmlFile = "reviews_management.fxml";
            } else if (currentActiveButton == reportsNavButton) {
                fxmlFile = "reports_view.fxml";
            }

            if (fxmlFile != null) {
                viewCache.remove(fxmlFile);
                loadView(fxmlFile, currentActiveButton);
            }
        }
    }
}
