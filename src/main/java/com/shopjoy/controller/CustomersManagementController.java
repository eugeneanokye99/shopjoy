package com.shopjoy.controller;

import com.shopjoy.model.User;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.UserService;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomersManagementController {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;

    @FXML
    private Label totalCustomersLabel;
    @FXML
    private Label newCustomersLabel;
    @FXML
    private Label activeOrdersLabel;

    @FXML
    private TableView<CustomerViewModel> customersTable;
    @FXML
    private TableColumn<CustomerViewModel, Integer> userIdCol;
    @FXML
    private TableColumn<CustomerViewModel, String> usernameCol;
    @FXML
    private TableColumn<CustomerViewModel, String> nameCol;
    @FXML
    private TableColumn<CustomerViewModel, String> emailCol;
    @FXML
    private TableColumn<CustomerViewModel, String> phoneCol;
    @FXML
    private TableColumn<CustomerViewModel, Integer> ordersCountCol;
    @FXML
    private TableColumn<CustomerViewModel, String> joinDateCol;
    @FXML
    private TableColumn<CustomerViewModel, Void> actionsCol;

    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final ObservableList<CustomerViewModel> masterData = FXCollections.observableArrayList();
    private FilteredList<CustomerViewModel> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        userIdCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getUserId()).asObject());
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        ordersCountCol
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getOrderCount()).asObject());
        joinDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJoinDateFormatted()));

        actionsCol.setCellFactory(createActionCellFactory());

        filteredData = new FilteredList<>(masterData, p -> true);
        customersTable.setItems(filteredData);
    }

    private Callback<TableColumn<CustomerViewModel, Void>, TableCell<CustomerViewModel, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button detailsButton = new Button("Details");
            private final HBox container = new HBox(5, detailsButton);

            {
                detailsButton.getStyleClass().add("secondary-button");
                detailsButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
                detailsButton.setOnAction(e -> handleDetails(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        };
    }

    private void handleDetails(CustomerViewModel customer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Details");
        alert.setHeaderText(customer.getUsername());
        alert.setContentText("Name: " + customer.getFullName() + "\n" +
                "Email: " + customer.getEmail() + "\n" +
                "Phone: " + customer.getPhone() + "\n" +
                "Joined: " + customer.getJoinDateFormatted() + "\n" +
                "Total Orders: " + customer.getOrderCount());
        alert.showAndWait();
    }

    private void loadData() {
        masterData.clear();
        List<User> customers = userService.getAllCustomers();
        if (customers != null) {
            int newCustomers = 0;
            LocalDate now = LocalDate.now();

            for (User u : customers) {
                int orderCount = orderService.getOrderCountByUser(u.getUserId());
                masterData.add(new CustomerViewModel(u, orderCount));

                if (u.getCreatedAt() != null &&
                        u.getCreatedAt().getMonth() == now.getMonth() &&
                        u.getCreatedAt().getYear() == now.getYear()) {
                    newCustomers++;
                }
            }

            totalCustomersLabel.setText(String.valueOf(customers.size()));
            newCustomersLabel.setText(String.valueOf(newCustomers));

            // For active orders, we'd need a way to sum active orders across all users,
            // or just query all active orders from OrderService directly.
            // Simplified: Query all pending/processing orders from OrderService
            int activeOrders = orderService.getPendingOrders() != null ? orderService.getPendingOrders().size() : 0;
            activeOrdersLabel.setText(String.valueOf(activeOrders));
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String filter = searchField.getText();
        if (filter == null || filter.isBlank()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = filter.toLowerCase();
            filteredData.setPredicate(cust -> cust.getUsername().toLowerCase().contains(lower) ||
                    cust.getEmail().toLowerCase().contains(lower) ||
                    cust.getFullName().toLowerCase().contains(lower));
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        searchField.clear();
        filteredData.setPredicate(p -> true);
        loadData();
    }

    public static class CustomerViewModel {
        private final User user;
        private final int orderCount;
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public CustomerViewModel(User user, int orderCount) {
            this.user = user;
            this.orderCount = orderCount;
        }

        public int getUserId() {
            return user.getUserId();
        }

        public String getUsername() {
            return user.getUsername();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getFullName() {
            String f = user.getFirstName() != null ? user.getFirstName() : "";
            String l = user.getLastName() != null ? user.getLastName() : "";
            return (f + " " + l).trim();
        }

        public String getPhone() {
            return user.getPhone() != null ? user.getPhone() : "";
        }

        public int getOrderCount() {
            return orderCount;
        }

        public String getJoinDateFormatted() {
            if (user.getCreatedAt() != null) {
                return user.getCreatedAt().format(DATE_FMT);
            }
            return "-";
        }
    }
}
