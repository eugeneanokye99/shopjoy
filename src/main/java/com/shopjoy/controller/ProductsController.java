package com.shopjoy.controller;

import com.shopjoy.model.Category;
import com.shopjoy.model.Inventory;
import com.shopjoy.model.Product;
import com.shopjoy.service.CategoryService;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller class for handling product management
 */
public class ProductsController {

    @FXML
    private TextField productSearchField;

    @FXML
    private Button searchProductButton;

    @FXML
    private Button addProductButton;

    @FXML
    private TableView<Product> productsTable;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> productCategoryColumn;

    @FXML
    private TableColumn<Product, String> productSkuColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, Integer> productStockColumn;

    @FXML
    private TableColumn<Product, Boolean> productActiveColumn;

    @FXML
    private TableColumn<Product, Void> productActionsColumn;

    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private InventoryService inventoryService = new InventoryService();

    private ObservableList<Product> productsList = FXCollections.observableArrayList();

    /**
     * Initialize the controller
     * Called automatically after FXML fields are injected
     */
    @FXML
    public void initialize() {
        // Set up basic table columns with PropertyValueFactory
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productSkuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        productActiveColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));

        // Set up price column with PropertyValueFactory
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Format price column as currency
        productPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                    setText(currencyFormat.format(price));
                }
            }
        });

        // Set up category column with custom cell value factory
        productCategoryColumn.setCellValueFactory(cellData -> {
            try {
                int categoryId = cellData.getValue().getCategoryId();
                Category category = categoryService.getCategoryById(categoryId);
                return new SimpleStringProperty(category != null ? category.getCategoryName() : "N/A");
            } catch (Exception e) {
                System.err.println("Error getting category: " + e.getMessage());
                return new SimpleStringProperty("N/A");
            }
        });

        // Set up stock column with custom cell value factory
        productStockColumn.setCellValueFactory(cellData -> {
            try {
                int productId = cellData.getValue().getProductId();
                Inventory inv = inventoryService.getInventoryByProductId(productId);
                return new SimpleIntegerProperty(inv != null ? inv.getQuantityInStock() : 0).asObject();
            } catch (Exception e) {
                System.err.println("Error getting inventory: " + e.getMessage());
                return new SimpleIntegerProperty(0).asObject();
            }
        });

        // Set up actions column with Edit and Delete buttons
        productActionsColumn.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                // Style buttons
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");

                // Set button actions
                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });

                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Load all products
        loadProducts();

        // Set items on table
        productsTable.setItems(productsList);
    }

    /**
     * Load all products from the database
     */
    private void loadProducts() {
        try {
            // Clear existing products
            productsList.clear();

            // Get all products from service
            List<Product> products = productService.getAllProducts();

            // Add to observable list
            productsList.addAll(products);

            System.out.println("Loaded " + products.size() + " products");

        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }

    /**
     * Handle search button click
     */
    @FXML
    public void handleSearch(ActionEvent event) {
        try {
            // Get search text
            String searchText = productSearchField.getText().trim();

            if (searchText.isEmpty()) {
                // Load all products if search is empty
                loadProducts();
            } else {
                // Search for products
                List<Product> searchResults = productService.searchProducts(searchText);

                // Clear and update list
                productsList.clear();
                productsList.addAll(searchResults);

                System.out.println("Found " + searchResults.size() + " products matching '" + searchText + "'");
            }

        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to search products: " + e.getMessage());
        }
    }

    /**
     * Handle add product button click
     */
    @FXML
    public void handleAddProduct(ActionEvent event) {
        try {
            // Create new stage for add product dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Product");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Load add product FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_product.fxml"));
            Parent root = loader.load();

            // Set scene
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Show and wait
            dialogStage.showAndWait();

            // Reload products after dialog closes
            loadProducts();

        } catch (Exception e) {
            System.err.println("Error opening add product dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add product dialog: " + e.getMessage());
        }
    }

    /**
     * Handle edit product
     */
    private void handleEditProduct(Product product) {
        try {
            // Create new stage for edit product dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Product");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Load edit product FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_product.fxml"));
            Parent root = loader.load();

            // Get controller and pass product data
            // EditProductController controller = loader.getController();
            // controller.setProduct(product);

            // Set scene
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Show and wait
            dialogStage.showAndWait();

            // Reload products after dialog closes
            loadProducts();

        } catch (Exception e) {
            System.err.println("Error opening edit product dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit product dialog: " + e.getMessage());
        }
    }

    /**
     * Handle delete product
     */
    private void handleDeleteProduct(Product product) {
        try {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Product");
            confirmAlert.setContentText("Are you sure you want to delete " + product.getProductName() + "?");

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Attempt to delete product
                boolean deleted = productService.deleteProduct(product.getProductId());

                if (deleted) {
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully");

                    // Reload products
                    loadProducts();
                } else {
                    // Show error message
                    showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete product. It may have existing orders.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + e.getMessage());
        }
    }

    /**
     * Helper method to show alert dialogs
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
