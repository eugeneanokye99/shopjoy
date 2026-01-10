package com.shopjoy.controller;

import com.shopjoy.model.Order;
import com.shopjoy.model.OrderItem;
import com.shopjoy.model.OrderStatus;
import com.shopjoy.model.User;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.UserService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller for managing orders in the admin interface.
 */
public class OrdersController {

    @FXML
    private ComboBox<String> orderStatusFilterCombo;

    @FXML
    private Button filterOrdersButton;

    @FXML
    private Button refreshOrdersButton;

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, String> orderUserColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, String> orderTotalColumn;

    @FXML
    private TableColumn<Order, String> orderStatusColumn;

    @FXML
    private TableColumn<Order, String> orderPaymentStatusColumn;

    @FXML
    private TableColumn<Order, Void> orderActionsColumn;

    private OrderService orderService = new OrderService();
    private UserService userService = new UserService();

    private ObservableList<Order> ordersList = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        // Basic property bindings
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        orderUserColumn.setCellValueFactory(cellData -> {
            try {
                User user = userService.getUserById(cellData.getValue().getUserId());
                return new SimpleStringProperty(user != null ? user.getUsername() : "Unknown");
            } catch (Exception e) {
                System.err.println("Error fetching user: " + e.getMessage());
                return new SimpleStringProperty("Unknown");
            }
        });

        orderDateColumn.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue().getOrderDate() == null) return new SimpleStringProperty("N/A");
                return new SimpleStringProperty(cellData.getValue().getOrderDate().format(DATE_FORMAT));
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });

        orderTotalColumn.setCellValueFactory(cellData -> {
            try {
                double total = cellData.getValue().getTotalAmount();
                return new SimpleStringProperty(CURRENCY_FORMAT.format(total));
            } catch (Exception e) {
                return new SimpleStringProperty("$0.00");
            }
        });

        orderStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : "N/A"));

        orderPaymentStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPaymentStatus() != null ? cellData.getValue().getPaymentStatus().name() : "N/A"));

        orderActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            private final Button updateBtn = new Button("Update Status");
            private final Button cancelBtn = new Button("Cancel");
            private final VBox box = new VBox(5, viewBtn, updateBtn, cancelBtn);

            {
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");
                updateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");
                cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");

                viewBtn.setOnAction(evt -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewOrderDetails(order);
                });

                updateBtn.setOnAction(evt -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(order);
                });

                cancelBtn.setOnAction(evt -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleCancelOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Populate status filter
        orderStatusFilterCombo.getItems().add("All Orders");
        for (OrderStatus status : OrderStatus.values()) {
            orderStatusFilterCombo.getItems().add(status.name());
        }
        orderStatusFilterCombo.setValue("All Orders");

        // Load data
        loadOrders();
        ordersTable.setItems(ordersList);
    }

    private void loadOrders() {
        try {
            ordersList.clear();
            List<Order> all = orderService.getAllOrders();
            ordersList.addAll(all);
            System.out.println("Loaded " + all.size() + " orders");
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
    }

    @FXML
    public void handleFilterOrders(ActionEvent event) {
        try {
            String selected = orderStatusFilterCombo.getValue();
            if (selected == null || selected.equals("All Orders")) {
                loadOrders();
                return;
            }

            OrderStatus status = OrderStatus.valueOf(selected);
            List<Order> filtered = orderService.getOrdersByStatus(status);
            ordersList.clear();
            ordersList.addAll(filtered);
            System.out.println("Filtered to " + filtered.size() + " orders for status " + selected);
        } catch (Exception e) {
            System.err.println("Error filtering orders: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter orders: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshOrders(ActionEvent event) {
        try {
            orderStatusFilterCombo.setValue("All Orders");
            loadOrders();
        } catch (Exception e) {
            System.err.println("Error refreshing orders: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh orders: " + e.getMessage());
        }
    }

    private void handleViewOrderDetails(Order order) {
        try {
            List<OrderItem> items = orderService.getOrderItems(order.getOrderId());
            User user = userService.getUserById(order.getUserId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Details");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            grid.add(new Label("Order ID:"), 0, 0);
            grid.add(new Label(String.valueOf(order.getOrderId())), 1, 0);

            grid.add(new Label("Customer:"), 0, 1);
            grid.add(new Label(user != null ? user.getUsername() : "Unknown"), 1, 1);

            grid.add(new Label("Status:"), 0, 2);
            grid.add(new Label(order.getStatus() != null ? order.getStatus().name() : "N/A"), 1, 2);

            grid.add(new Label("Payment:"), 0, 3);
            grid.add(new Label(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "N/A"), 1, 3);

            grid.add(new Label("Total:"), 0, 4);
            grid.add(new Label(CURRENCY_FORMAT.format(order.getTotalAmount())), 1, 4);

            grid.add(new Label("Items:"), 0, 5);
            VBox itemsBox = new VBox(5);
            for (OrderItem item : items) {
                String line = String.format("Product x%d - %s", item.getQuantity(), CURRENCY_FORMAT.format(item.getUnitPrice()));
                itemsBox.getChildren().add(new Label(line));
            }
            grid.add(itemsBox, 1, 5);
            GridPane.setHgrow(itemsBox, Priority.ALWAYS);

            dialog.getDialogPane().setContent(grid);
            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("Error viewing order details: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order details: " + e.getMessage());
        }
    }

    private void handleUpdateStatus(Order order) {
        try {
            Dialog<OrderStatus> dialog = new Dialog<>();
            dialog.setTitle("Update Order Status");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            ComboBox<OrderStatus> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll(OrderStatus.values());
            statusCombo.setValue(order.getStatus());
            statusCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(OrderStatus status) {
                    return status == null ? "" : status.name();
                }

                @Override
                public OrderStatus fromString(String string) {
                    return OrderStatus.valueOf(string);
                }
            });

            dialog.getDialogPane().setContent(statusCombo);
            dialog.setResultConverter(btn -> btn == ButtonType.OK ? statusCombo.getValue() : null);

            Optional<OrderStatus> result = dialog.showAndWait();
            if (result.isEmpty()) return;

            OrderStatus newStatus = result.get();
            OrderStatus current = order.getStatus();

            // Validate transition (basic check: prevent going backwards from completed/cancelled)
            if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
                showAlert(Alert.AlertType.ERROR, "Invalid", "Cannot change status from " + current.name());
                return;
            }

            boolean updated = orderService.updateOrderStatus(order.getOrderId(), newStatus);
            if (updated) {
                loadOrders();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order status updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
            }
        } catch (Exception e) {
            System.err.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status: " + e.getMessage());
        }
    }

    private void handleCancelOrder(Order order) {
        try {
            OrderStatus status = order.getStatus();
            if (!(status == OrderStatus.PENDING || status == OrderStatus.PROCESSING)) {
                showAlert(Alert.AlertType.ERROR, "Invalid", "Only PENDING or PROCESSING orders can be cancelled.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Order");
            confirm.setHeaderText("Cancel Order " + order.getOrderId());
            confirm.setContentText("Are you sure you want to cancel this order?");
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) return;

            boolean cancelled = orderService.cancelOrder(order.getOrderId());
            if (cancelled) {
                loadOrders();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order cancelled.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order.");
            }
        } catch (Exception e) {
            System.err.println("Error cancelling order: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
