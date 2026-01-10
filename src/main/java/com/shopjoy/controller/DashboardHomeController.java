package com.shopjoy.controller;

import com.shopjoy.model.Order;
import com.shopjoy.model.OrderStatus;
import com.shopjoy.model.Product;
import com.shopjoy.model.User;
import com.shopjoy.service.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DashboardHomeController - Controls the dashboard home view
 * Displays comprehensive statistics, recent orders, and quick actions
 */
public class DashboardHomeController {

    // Statistics labels
    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private Label avgRatingLabel;

    // Recent orders table
    @FXML
    private TableView<Order> recentOrdersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdCol;

    @FXML
    private TableColumn<Order, String> customerCol;

    @FXML
    private TableColumn<Order, LocalDateTime> dateCol;

    @FXML
    private TableColumn<Order, Double> totalCol;

    @FXML
    private TableColumn<Order, OrderStatus> statusCol;

    // Service instances
    private ProductService productService = new ProductService();
    private OrderService orderService = new OrderService();
    private UserService userService = new UserService();
    private InventoryService inventoryService = new InventoryService();
    private ReviewService reviewService = new ReviewService();

    /**
     * Initialize method - called after FXML injection
     */
    @FXML
    public void initialize() {
        setupRecentOrdersTable();
        loadStatistics();
        loadRecentOrders();
        startAutoRefresh();
    }

    /**
     * Setup recent orders table columns
     */
    private void setupRecentOrdersTable() {
        // Order ID column
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        // Customer column - needs to fetch user name
        customerCol.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            User user = userService.getUserById(order.getUserId());
            String customerName = user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown";
            return new javafx.beans.property.SimpleStringProperty(customerName);
        });

        // Date column
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        dateCol.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                }
            }
        });

        // Total column
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", amount));
                }
            }
        });

        // Status column
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /**
     * Load all dashboard statistics
     */
    private void loadStatistics() {
        try {
            // Total Products
            List<Product> allProducts = productService.getAllProducts();
            totalProductsLabel.setText(String.valueOf(allProducts.size()));

            // Total Orders
            List<Order> allOrders = orderService.getAllOrders();
            totalOrdersLabel.setText(String.valueOf(allOrders.size()));

            // Total Revenue
            double totalRevenue = orderService.getTotalRevenue();
            totalRevenueLabel.setText(String.format("$%,.2f", totalRevenue));

            // Low Stock Items
            int lowStockCount = inventoryService.getLowStockProducts().size();
            lowStockLabel.setText(String.valueOf(lowStockCount));

            // Total Customers
            List<User> allCustomers = userService.getAllCustomers();
            totalCustomersLabel.setText(String.valueOf(allCustomers.size()));

            // Reviews statistics
            int totalReviews = 0;
            double totalRating = 0;
            int ratedProducts = 0;

            for (Product product : allProducts) {
                int reviewCount = reviewService.getReviewCount(product.getProductId());
                totalReviews += reviewCount;

                if (reviewCount > 0) {
                    double avgRating = reviewService.getAverageRating(product.getProductId());
                    totalRating += avgRating;
                    ratedProducts++;
                }
            }

            double overallAvgRating = ratedProducts > 0 ? totalRating / ratedProducts : 0;
            avgRatingLabel.setText(String.format("%.1f", overallAvgRating));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    /**
     * Load recent orders into table
     */
    private void loadRecentOrders() {
        try {
            // Get all orders and sort by date descending
            List<Order> allOrders = orderService.getAllOrders();
            List<Order> recentOrders = allOrders.stream()
                    .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                    .limit(10)
                    .toList();

            ObservableList<Order> ordersList = FXCollections.observableArrayList(recentOrders);
            recentOrdersTable.setItems(ordersList);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading recent orders: " + e.getMessage());
        }
    }

    /**
     * Handle check low stock button click
     */
    @FXML
    public void handleCheckLowStock(ActionEvent event) {
        // Navigate to inventory view with low stock filter
        System.out.println("Navigate to low stock inventory view");

        // Show info message for now
        showInfo("Navigation", "This will navigate to the Inventory view with low stock items filtered.");
        // TODO: Implement navigation to inventory view with low stock filter
    }

    /**
     * Handle quick add product button click
     */
    @FXML
    public void handleQuickAddProduct(ActionEvent event) {
        // Open add product dialog
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_product_dialog.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Product");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            // Refresh statistics after dialog closes
            loadStatistics();
            loadRecentOrders();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Add product dialog not yet implemented");
            showInfo("Feature", "Add product functionality will be available in the Products management section.");
        }
    }

    /**
     * Handle view pending orders button click
     */
    @FXML
    public void handleViewPendingOrders(ActionEvent event) {
        System.out.println("Navigate to pending orders view");
        showInfo("Navigation", "This will navigate to the Orders view with pending orders filtered.");
        // TODO: Navigate to orders view with pending filter
    }

    /**
     * Handle generate report button click
     */
    @FXML
    public void handleGenerateReport(ActionEvent event) {
        System.out.println("Navigate to reports view");
        showInfo("Navigation", "This will navigate to the Reports section.");
        // TODO: Navigate to reports view
    }

    /**
     * Start auto-refresh timer to update statistics every 30 seconds
     */
    private void startAutoRefresh() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            System.out.println("Auto-refreshing dashboard statistics...");
            loadStatistics();
            loadRecentOrders();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Show information alert
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
