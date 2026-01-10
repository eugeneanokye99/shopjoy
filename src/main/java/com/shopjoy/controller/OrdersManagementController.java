package com.shopjoy.controller;

import com.shopjoy.model.Order;
import com.shopjoy.model.OrderStatus;
import com.shopjoy.model.PaymentStatus;
import com.shopjoy.model.User;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.UserService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrdersManagementController {

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private Button filterOrdersButton;

    @FXML
    private Button refreshOrdersButton;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label pendingOrdersLabel;

    @FXML
    private Label processingOrdersLabel;

    @FXML
    private Label shippedOrdersLabel;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private TableView<OrderViewModel> ordersTable;

    @FXML
    private TableColumn<OrderViewModel, Integer> orderIdColumn;

    @FXML
    private TableColumn<OrderViewModel, String> orderUserColumn;

    @FXML
    private TableColumn<OrderViewModel, String> orderEmailColumn;

    @FXML
    private TableColumn<OrderViewModel, String> orderDateColumn;

    @FXML
    private TableColumn<OrderViewModel, Integer> orderItemsColumn;

    @FXML
    private TableColumn<OrderViewModel, Double> orderTotalColumn;

    @FXML
    private TableColumn<OrderViewModel, String> orderStatusColumn;

    @FXML
    private TableColumn<OrderViewModel, String> orderPaymentColumn;

    @FXML
    private TableColumn<OrderViewModel, Void> orderActionsColumn;

    private final OrderService orderService = new OrderService();
    private final UserService userService = new UserService();
    private final ObservableList<OrderViewModel> orderList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterCombo();
        loadOrderData();
    }

    private void setupFilterCombo() {
        statusFilterCombo.getItems().add("All");
        for (OrderStatus status : OrderStatus.values()) {
            statusFilterCombo.getItems().add(status.name());
        }
        statusFilterCombo.getSelectionModel().select("All");
    }

    private void setupTableColumns() {
        orderIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getOrderId()).asObject());
        orderUserColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        orderEmailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerEmail()));

        orderDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getOrderDate() != null) {
                return new SimpleStringProperty(data.getValue().getOrderDate().format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });

        orderItemsColumn
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getItemCount()).asObject());

        orderTotalColumn
                .setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());
        orderTotalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.US, "$%.2f", item));
                }
            }
        });

        orderStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        orderPaymentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentStatus()));

        orderActionsColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<OrderViewModel, Void>, TableCell<OrderViewModel, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button nextStatusButton = new Button("Next Status");
            private final HBox actionBox = new HBox(10, viewButton, nextStatusButton);

            {
                viewButton.getStyleClass().add("secondary-button");
                viewButton.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                viewButton.setOnAction(event -> handleViewOrder(getTableView().getItems().get(getIndex())));

                nextStatusButton.getStyleClass().add("primary-button");
                nextStatusButton.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                nextStatusButton.setOnAction(event -> handleNextStatus(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderViewModel vm = getTableView().getItems().get(getIndex());
                    // Update button text/visibility based on status if needed
                    OrderStatus status = OrderStatus.valueOf(vm.getStatus());
                    if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
                        nextStatusButton.setDisable(true);
                        nextStatusButton.setText("Completed");
                    } else {
                        nextStatusButton.setDisable(false);
                        nextStatusButton.setText("Advance");
                    }
                    setGraphic(actionBox);
                }
            }
        };
    }

    private void loadOrderData() {
        String filter = statusFilterCombo.getValue();
        List<Order> orders;

        if (filter == null || filter.equals("All")) {
            orders = orderService.getAllOrders();
        } else {
            OrderStatus status = OrderStatus.valueOf(filter);
            orders = orderService.getOrdersByStatus(status);
        }

        orderList.clear();
        double revenue = 0;
        int pending = 0;
        int processing = 0;
        int shipped = 0;

        for (Order o : orders) {
            // Fetch User
            User u = userService.getUserById(o.getUserId());
            // Count items (inefficient but necessary without join)
            int itemsCount = 0;
            var items = orderService.getOrderItems(o.getOrderId());
            if (items != null) {
                itemsCount = items.stream().mapToInt(it -> it.getQuantity()).sum();
            }

            orderList.add(new OrderViewModel(o, u, itemsCount));

            // Stats
            revenue += o.getTotalAmount();
            if (o.getStatus() == OrderStatus.PENDING)
                pending++;
            else if (o.getStatus() == OrderStatus.PROCESSING)
                processing++;
            else if (o.getStatus() == OrderStatus.SHIPPED)
                shipped++;
        }

        ordersTable.setItems(orderList);

        // Update Stats Labels
        totalOrdersLabel.setText(String.valueOf(orders.size()));
        pendingOrdersLabel.setText(String.valueOf(pending));
        processingOrdersLabel.setText(String.valueOf(processing));
        shippedOrdersLabel.setText(String.valueOf(shipped));
        totalRevenueLabel.setText(String.format(Locale.US, "$%,.2f", revenue));
    }

    @FXML
    void handleFilterOrders(ActionEvent event) {
        loadOrderData();
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        // Reset filter
        statusFilterCombo.getSelectionModel().select("All");
        loadOrderData();
    }

    private void handleViewOrder(OrderViewModel vm) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + vm.getOrderId());
        alert.setContentText("Customer: " + vm.getCustomerName() + "\n" +
                "Total: $" + vm.getTotalAmount() + "\n" +
                "Status: " + vm.getStatus() + "\n" +
                "Address: " + vm.getOrder().getShippingAddress());
        alert.showAndWait();
    }

    private void handleNextStatus(OrderViewModel vm) {
        Order o = vm.getOrder();
        OrderStatus current = o.getStatus();
        OrderStatus next = null;

        if (current == OrderStatus.PENDING)
            next = OrderStatus.PROCESSING;
        else if (current == OrderStatus.PROCESSING)
            next = OrderStatus.SHIPPED;
        else if (current == OrderStatus.SHIPPED)
            next = OrderStatus.DELIVERED;

        if (next != null) {
            boolean success = orderService.updateOrderStatus(o.getOrderId(), next);
            if (success) {
                loadOrderData(); // refresh
                System.out.println("Order " + o.getOrderId() + " updated to " + next);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Failed to update order status.");
                alert.showAndWait();
            }
        }
    }

    // Helper Class for Table View
    public static class OrderViewModel {
        private final Order order;
        private final String customerName;
        private final String customerEmail;
        private final int itemCount;

        public OrderViewModel(Order order, User user, int itemCount) {
            this.order = order;
            if (user != null) {
                this.customerName = user.getFirstName() + " " + user.getLastName();
                this.customerEmail = user.getEmail();
            } else {
                this.customerName = "Unknown (ID: " + order.getUserId() + ")";
                this.customerEmail = "-";
            }
            this.itemCount = itemCount;
        }

        public int getOrderId() {
            return order.getOrderId();
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public java.time.LocalDateTime getOrderDate() {
            return order.getOrderDate();
        }

        public int getItemCount() {
            return itemCount;
        }

        public double getTotalAmount() {
            return order.getTotalAmount();
        }

        public String getStatus() {
            return order.getStatus() != null ? order.getStatus().toString() : "";
        }

        public String getPaymentStatus() {
            return order.getPaymentStatus() != null ? order.getPaymentStatus().toString() : "";
        }

        public Order getOrder() {
            return order;
        }
    }
}
