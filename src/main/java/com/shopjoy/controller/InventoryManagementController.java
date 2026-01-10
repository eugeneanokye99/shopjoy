package com.shopjoy.controller;

import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

public class InventoryManagementController {

    @FXML
    private Button showLowStockButton;
    @FXML
    private Button showOutOfStockButton;
    @FXML
    private Button showAllButton;
    @FXML
    private Button refreshButton;

    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label lowStockCountLabel;
    @FXML
    private Label outOfStockCountLabel;
    @FXML
    private Label inventoryValueLabel;

    @FXML
    private TableView<InventoryViewModel> inventoryTable;
    @FXML
    private TableColumn<InventoryViewModel, Integer> productIdCol;
    @FXML
    private TableColumn<InventoryViewModel, String> productNameCol;
    @FXML
    private TableColumn<InventoryViewModel, String> skuCol;
    @FXML
    private TableColumn<InventoryViewModel, Integer> quantityCol;
    @FXML
    private TableColumn<InventoryViewModel, Integer> reorderLevelCol;
    @FXML
    private TableColumn<InventoryViewModel, String> warehouseCol;
    @FXML
    private TableColumn<InventoryViewModel, String> statusCol;
    @FXML
    private TableColumn<InventoryViewModel, Void> actionsCol;

    private final InventoryService inventoryService = new InventoryService();
    private final ProductService productService = new ProductService();
    private final ObservableList<InventoryViewModel> inventoryList = FXCollections.observableArrayList();

    private enum FilterType {
        ALL, LOW_STOCK, OUT_OF_STOCK
    }

    private FilterType currentFilter = FilterType.ALL;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadInventoryData();
    }

    private void setupTableColumns() {
        productIdCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getProductId()).asObject());
        productNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        skuCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSku()));
        quantityCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        reorderLevelCol
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getReorderLevel()).asObject());
        warehouseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWarehouseLocation()));

        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Out")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item.contains("Low")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        actionsCol.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<InventoryViewModel, Void>, TableCell<InventoryViewModel, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button restockButton = new Button("Restock");
            private final HBox actionBox = new HBox(10, restockButton);

            {
                restockButton.getStyleClass().add("primary-button");
                restockButton.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                restockButton.setOnAction(event -> handleRestock(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        };
    }

    private void loadInventoryData() {
        inventoryList.clear();

        // Manual loading to ensure we have all data
        List<Inventory> allInventory = inventoryService.getAllInventory();

        // Stats calculation
        int totalItems = allInventory.size();
        long lowStock = 0;
        long outOfStock = 0;
        double totalValue = inventoryService.getTotalStockValue(); // Service has this method

        for (Inventory inv : allInventory) {
            if (inv.getQuantityInStock() == 0)
                outOfStock++;
            else if (inv.getQuantityInStock() <= inv.getReorderLevel())
                lowStock++;
        }

        totalItemsLabel.setText(String.valueOf(totalItems));
        lowStockCountLabel.setText(String.valueOf(lowStock));
        outOfStockCountLabel.setText(String.valueOf(outOfStock));
        inventoryValueLabel.setText(String.format("$%,.2f", totalValue));

        // Filter and Populate List
        for (Inventory inv : allInventory) {
            boolean add = false;
            if (currentFilter == FilterType.ALL)
                add = true;
            else if (currentFilter == FilterType.LOW_STOCK && inv.getQuantityInStock() <= inv.getReorderLevel())
                add = true;
            else if (currentFilter == FilterType.OUT_OF_STOCK && inv.getQuantityInStock() == 0)
                add = true;

            if (add) {
                Product p = productService.getProductById(inv.getProductId());
                if (p != null) {
                    inventoryList.add(new InventoryViewModel(inv, p));
                }
            }
        }
        inventoryTable.setItems(inventoryList);
    }

    @FXML
    void handleShowLowStock(ActionEvent event) {
        currentFilter = FilterType.LOW_STOCK;
        loadInventoryData();
    }

    @FXML
    void handleShowOutOfStock(ActionEvent event) {
        currentFilter = FilterType.OUT_OF_STOCK;
        loadInventoryData();
    }

    @FXML
    void handleShowAll(ActionEvent event) {
        currentFilter = FilterType.ALL;
        loadInventoryData();
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadInventoryData();
    }

    private void handleRestock(InventoryViewModel vm) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Restock Product");
        dialog.setHeaderText("Restock " + vm.getProductName());
        dialog.setContentText("Enter quantity to add:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    boolean success = inventoryService.addStock(vm.getProductId(), qty);
                    if (success) {
                        loadInventoryData();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Stock updated successfully.");
                        alert.showAndWait();
                    } else {
                        showError("Failed to update stock.");
                    }
                } else {
                    showError("Quantity must be positive.");
                }
            } catch (NumberFormatException e) {
                showError("Invalid number format.");
            }
        });
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class InventoryViewModel {
        private final Inventory inventory;
        private final Product product;

        public InventoryViewModel(Inventory inventory, Product product) {
            this.inventory = inventory;
            this.product = product;
        }

        public int getProductId() {
            return inventory.getProductId();
        }

        public String getProductName() {
            return product.getProductName();
        }

        public String getSku() {
            return product.getSku();
        }

        public int getQuantity() {
            return inventory.getQuantityInStock();
        }

        public int getReorderLevel() {
            return inventory.getReorderLevel();
        }

        public String getWarehouseLocation() {
            return inventory.getWarehouseLocation() != null ? inventory.getWarehouseLocation() : "-";
        }

        public String getStatus() {
            if (getQuantity() == 0)
                return "Out of Stock";
            if (getQuantity() <= getReorderLevel())
                return "Low Stock";
            return "In Stock";
        }
    }
}
