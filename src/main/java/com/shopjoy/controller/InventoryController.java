package com.shopjoy.controller;

import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;
import com.shopjoy.service.ProductWithStock;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing inventory.
 */
public class InventoryController {

    @FXML
    private Button showLowStockButton;

    @FXML
    private Button showAllInventoryButton;

    @FXML
    private TableView<Inventory> inventoryTable;

    @FXML
    private TableColumn<Inventory, Integer> invProductIdColumn;

    @FXML
    private TableColumn<Inventory, String> invProductNameColumn;

    @FXML
    private TableColumn<Inventory, String> invSkuColumn;

    @FXML
    private TableColumn<Inventory, Integer> invQuantityColumn;

    @FXML
    private TableColumn<Inventory, Integer> invReorderLevelColumn;

    @FXML
    private TableColumn<Inventory, String> invWarehouseColumn;

    @FXML
    private TableColumn<Inventory, String> invStatusColumn;

    @FXML
    private TableColumn<Inventory, Void> invActionsColumn;

    private InventoryService inventoryService = new InventoryService();
    private ProductService productService = new ProductService();

    private ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Basic mappings
        invProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        invQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantityInStock"));
        invReorderLevelColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        invWarehouseColumn.setCellValueFactory(new PropertyValueFactory<>("warehouseLocation"));

        // Product name lookup
        invProductNameColumn.setCellValueFactory(cellData -> {
            try {
                Product p = productService.getProductById(cellData.getValue().getProductId());
                return new SimpleStringProperty(p != null ? p.getProductName() : "Unknown");
            } catch (Exception e) {
                System.err.println("Error fetching product name: " + e.getMessage());
                return new SimpleStringProperty("Unknown");
            }
        });

        // SKU lookup
        invSkuColumn.setCellValueFactory(cellData -> {
            try {
                Product p = productService.getProductById(cellData.getValue().getProductId());
                return new SimpleStringProperty(p != null ? p.getSku() : "N/A");
            } catch (Exception e) {
                System.err.println("Error fetching SKU: " + e.getMessage());
                return new SimpleStringProperty("N/A");
            }
        });

        // Status column with color coding
        invStatusColumn.setCellValueFactory(cellData -> {
            Inventory inv = cellData.getValue();
            String status;
            if (inv.getQuantityInStock() <= 0) {
                status = "Out of Stock";
            } else if (inv.getQuantityInStock() <= inv.getReorderLevel()) {
                status = "Low Stock";
            } else {
                status = "In Stock";
            }
            return new SimpleStringProperty(status);
        });
        invStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Out of Stock")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item.equals("Low Stock")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        // Actions column
        invActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("Add Stock");
            private final Button updateBtn = new Button("Update");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, addBtn, updateBtn);

            {
                addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");

                addBtn.setOnAction(evt -> {
                    Inventory inv = getTableView().getItems().get(getIndex());
                    handleAddStock(inv);
                });

                updateBtn.setOnAction(evt -> {
                    Inventory inv = getTableView().getItems().get(getIndex());
                    handleUpdateInventory(inv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Load data
        loadInventory();
        inventoryTable.setItems(inventoryList);
    }

    private void loadInventory() {
        try {
            inventoryList.clear();
            List<Inventory> list = inventoryService.getAllInventory();
            inventoryList.addAll(list);
            System.out.println("Loaded " + list.size() + " inventory records");
        } catch (Exception e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load inventory: " + e.getMessage());
        }
    }

    @FXML
    public void handleShowLowStock(ActionEvent event) {
        try {
            inventoryList.clear();
            var lowStockProducts = inventoryService.getLowStockProducts();
            // Extract Inventory objects from ProductWithStock
            lowStockProducts.stream()
                    .map(ProductWithStock::getInventory)
                    .forEach(inventoryList::add);
            System.out.println("Loaded low stock items: " + lowStockProducts.size());
        } catch (Exception e) {
            System.err.println("Error loading low stock items: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load low stock items: " + e.getMessage());
        }
    }

    @FXML
    public void handleShowAll(ActionEvent event) {
        loadInventory();
    }

    private void handleAddStock(Inventory inventory) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock for product " + inventory.getProductId());
        dialog.setContentText("Quantity to add:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try {
            int qty = Integer.parseInt(result.get());
            if (qty <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid", "Quantity must be a positive integer.");
                return;
            }
            boolean updated = inventoryService.addStock(inventory.getProductId(), qty);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Added " + qty + " units to stock.");
                loadInventory();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add stock.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid", "Please enter a valid integer quantity.");
        } catch (Exception e) {
            System.err.println("Error adding stock: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add stock: " + e.getMessage());
        }
    }

    private void handleUpdateInventory(Inventory inventory) {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Update Inventory");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            TextField reorderField = new TextField(String.valueOf(inventory.getReorderLevel()));
            TextField warehouseField = new TextField(inventory.getWarehouseLocation() != null ? inventory.getWarehouseLocation() : "");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Reorder Level:"), 0, 0);
            grid.add(reorderField, 1, 0);
            grid.add(new Label("Warehouse:"), 0, 1);
            grid.add(warehouseField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        int rl = Integer.parseInt(reorderField.getText().trim());
                        if (rl < 0) throw new NumberFormatException();
                        inventoryService.updateReorderLevel(inventory.getProductId(), rl);
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Invalid", "Reorder level must be a non-negative integer.");
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update inventory: " + ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
            loadInventory();
        } catch (Exception e) {
            System.err.println("Error updating inventory: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update inventory: " + e.getMessage());
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
