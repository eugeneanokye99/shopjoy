package com.shopjoy.controller;

import com.shopjoy.model.Inventory;
import com.shopjoy.model.Order;
import com.shopjoy.model.Product;
import com.shopjoy.model.User;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsViewController {

    @FXML
    private RadioButton salesReportRadio;
    @FXML
    private RadioButton inventoryReportRadio;
    @FXML
    private RadioButton customerReportRadio;
    @FXML
    private RadioButton productReportRadio;
    @FXML
    private ToggleGroup reportType;

    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TextArea reportResultsArea;

    private final OrderService orderService = new OrderService();
    private final InventoryService inventoryService = new InventoryService();
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        reportResultsArea.clear();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from == null || to == null) {
            reportResultsArea.setText("Please select a date range.");
            return;
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        StringBuilder report = new StringBuilder();
        report.append("ShopJoy Report\n");
        report.append("Generated on: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        report.append("Period: ").append(from).append(" to ").append(to).append("\n");
        report.append("--------------------------------------------------\n\n");

        if (salesReportRadio.isSelected()) {
            generateSalesReport(report, start, end);
        } else if (inventoryReportRadio.isSelected()) {
            generateInventoryReport(report); // Inventory is usually point-in-time
        } else if (customerReportRadio.isSelected()) {
            generateCustomerReport(report, start, end);
        } else if (productReportRadio.isSelected()) {
            generateProductReport(report, start, end);
        }

        reportResultsArea.setText(report.toString());
    }

    private void generateSalesReport(StringBuilder sb, LocalDateTime start, LocalDateTime end) {
        sb.append("SALES REPORT\n\n");

        List<Order> allOrders = orderService.getAllOrders();
        // Filter by date
        List<Order> rangeOrders = allOrders.stream()
                .filter(o -> !o.getOrderDate().isBefore(start) && !o.getOrderDate().isAfter(end))
                .collect(Collectors.toList());

        double totalRevenue = rangeOrders.stream().mapToDouble(Order::getTotalAmount).sum();
        int totalOrders = rangeOrders.size();

        sb.append(String.format("Total Revenue: $%,.2f\n", totalRevenue));
        sb.append("Total Orders: ").append(totalOrders).append("\n");
        sb.append("Average Order Value: $")
                .append(totalOrders > 0 ? String.format("%,.2f", totalRevenue / totalOrders) : "0.00").append("\n\n");

        sb.append("Details:\n");
        for (Order o : rangeOrders) {
            sb.append(String.format("Order #%d - %s - $%,.2f - %s\n",
                    o.getOrderId(),
                    o.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    o.getTotalAmount(),
                    o.getStatus()));
        }
    }

    private void generateInventoryReport(StringBuilder sb) {
        sb.append("INVENTORY REPORT (Current State)\n\n");
        List<Inventory> inventoryList = inventoryService.getAllInventory();

        double totalStockValue = inventoryService.getTotalStockValue();
        long lowStockCount = inventoryList.stream().filter(i -> i.getQuantityInStock() <= i.getReorderLevel()).count();
        long outOfStockCount = inventoryList.stream().filter(i -> i.getQuantityInStock() == 0).count();

        sb.append(String.format("Total Stock Value: $%,.2f\n", totalStockValue));
        sb.append("Total Items Tracked: ").append(inventoryList.size()).append("\n");
        sb.append("Low Stock Items: ").append(lowStockCount).append("\n");
        sb.append("Out of Stock Items: ").append(outOfStockCount).append("\n\n"); // Fixed typo

        sb.append("Low Stock / Out of Stock List:\n");
        for (Inventory i : inventoryList) {
            if (i.getQuantityInStock() <= i.getReorderLevel()) {
                Product p = productService.getProductById(i.getProductId());
                String pName = p != null ? p.getProductName() : "ID:" + i.getProductId();
                sb.append(String.format("- %s : In Stock: %d (Reorder: %d)\n", pName, i.getQuantityInStock(),
                        i.getReorderLevel()));
            }
        }
    }

    private void generateCustomerReport(StringBuilder sb, LocalDateTime start, LocalDateTime end) {
        sb.append("CUSTOMER REPORT\n\n");
        List<User> customers = userService.getAllCustomers();

        // New customers in range
        long newCustomers = customers.stream()
                .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isBefore(start)
                        && !u.getCreatedAt().isAfter(end))
                .count();

        sb.append("Total Customers: ").append(customers.size()).append("\n");
        sb.append("New Customers (this period): ").append(newCustomers).append("\n\n");

        sb.append("Top Customers by Order Count (All Time):\n");
        // Simple top 5 logic
        Map<User, Integer> orderCounts = customers.stream()
                .collect(Collectors.toMap(u -> u, u -> orderService.getOrderCountByUser(u.getUserId())));

        orderCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(e -> {
                    sb.append(String.format("%s (%s) - %d orders\n", e.getKey().getUsername(), e.getKey().getEmail(),
                            e.getValue()));
                });
    }

    private void generateProductReport(StringBuilder sb, LocalDateTime start, LocalDateTime end) {
        sb.append("PRODUCT REPORT\n\n");
        List<Product> products = productService.getAllProducts();
        sb.append("Total Products: ").append(products.size()).append("\n\n");

        // This would ideally require OrderItem analysis joined with Orders filtered by
        // date
        // For now, listing products by price or simplified view
        sb.append("Product List:\n");
        for (Product p : products) {
            sb.append(String.format("ID %d: %s - Price: $%,.2f\n",
                    p.getProductId(), p.getProductName(), p.getPrice()));
        }
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        // Placeholder
        showAlert("Export PDF", "This feature is not yet implemented (requires PDF library).");
    }

    @FXML
    private void handleExportCsv(ActionEvent event) {
        String content = reportResultsArea.getText();
        if (content.isEmpty()) {
            showAlert("Export CSV", "No report to export. Generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(content);
                showAlert("Success", "Report saved to " + file.getPath());
            } catch (Exception e) {
                showAlert("Error", "Could not save file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrint(ActionEvent event) {
        showAlert("Print", "Sending to printer... (Simulation)");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
