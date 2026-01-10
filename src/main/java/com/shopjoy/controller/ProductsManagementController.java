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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ProductsManagementController - Manages product listing, search, filter, and
 * CRUD operations
 */
public class ProductsManagementController {

    // Header section
    @FXML
    private TextField productSearchField;

    @FXML
    private Button searchProductButton;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Button addProductButton;

    // Filter section
    @FXML
    private ComboBox<Category> categoryFilterCombo;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private Button applyFiltersButton;

    @FXML
    private Label productCountLabel;

    // Table section
    @FXML
    private TableView<Product> productsTable;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productImageColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> productCategoryColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, Double> productCostColumn;

    @FXML
    private TableColumn<Product, Integer> productStockColumn;

    @FXML
    private TableColumn<Product, String> productSkuColumn;

    @FXML
    private TableColumn<Product, String> productBrandColumn;

    @FXML
    private TableColumn<Product, String> productActiveColumn;

    @FXML
    private TableColumn<Product, Void> productActionsColumn;

    // Bottom section
    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    // Service instances
    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private InventoryService inventoryService = new InventoryService();

    // Data
    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private List<Product> allProducts = new ArrayList<>();

    /**
     * Initialize method - called after FXML injection
     */
    @FXML
    public void initialize() {
        // Set up table columns
        setupTableColumns();

        // Load category filter options
        loadCategoryFilter();

        // Load status filter options
        statusFilterCombo.getItems().addAll("All Status", "Active", "Inactive");
        statusFilterCombo.setValue("All Status");

        // Load all products
        loadAllProducts();

        // Set table items
        productsTable.setItems(productsList);

        // Add search field listener for real-time search
        productSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                handleClearSearch(null);
            }
        });
    }

    /**
     * Setup table columns with cell value factories and formatters
     */
    private void setupTableColumns() {
        // ID Column
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));

        // Image Column - show thumbnail or placeholder
        productImageColumn.setCellFactory(col -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Product product = getTableRow().getItem();
                    try {
                        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                            Image image = new Image(product.getImageUrl(), true);
                            imageView.setImage(image);
                        } else {
                            // Use placeholder - clear image
                            imageView.setImage(null);
                            setText("📦");
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                        setText("📦");
                    }
                    setGraphic(imageView);
                }
            }
        });

        // Name Column
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        // Category Column - get category name from categoryId
        productCategoryColumn.setCellValueFactory(cellData -> {
            int categoryId = cellData.getValue().getCategoryId();
            Optional<Category> category = Optional.ofNullable(categoryService.getCategoryById(categoryId));
            return new SimpleStringProperty(category.map(Category::getCategoryName).orElse("N/A"));
        });

        // Price Column - format as currency
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productPriceColumn.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        // Cost Column
        productCostColumn.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        productCostColumn.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double cost, boolean empty) {
                super.updateItem(cost, empty);
                if (empty || cost == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", cost));
                }
            }
        });

        // Stock Column - get from inventory
        productStockColumn.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getProductId();
            Optional<Inventory> inv = Optional.ofNullable(inventoryService.getInventoryByProductId(productId));
            int stock = inv.map(Inventory::getQuantityInStock).orElse(0);
            return new SimpleIntegerProperty(stock).asObject();
        });

        // Color code stock levels
        productStockColumn.setCellFactory(col -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(stock));
                    if (stock == 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (stock < 10) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        // SKU Column
        productSkuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));

        // Brand Column
        productBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));

        // Status Column
        productActiveColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            return new SimpleStringProperty(isActive ? "Active" : "Inactive");
        });

        productActiveColumn.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equals("Active")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });

        // Actions Column - Edit and Delete buttons
        productActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Edit");
            private final Button deleteBtn = new Button("🗑️ Delete");
            private final Button viewBtn = new Button("👁️ View");
            private final HBox pane = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");
                viewBtn.getStyleClass().add("view-button");

                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });

                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });

                viewBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleViewProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    /**
     * Load category filter options
     */
    private void loadCategoryFilter() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));

            // Set converter to display category name
            categoryFilterCombo.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category != null ? category.getCategoryName() : "";
                }

                @Override
                public Category fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all products from database
     */
    private void loadAllProducts() {
        try {
            allProducts = productService.getAllProducts();
            productsList.setAll(allProducts);
            updateProductCount();
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to load products: " + e.getMessage());
        }
    }

    /**
     * Update product count label
     */
    private void updateProductCount() {
        productCountLabel.setText("Showing " + productsList.size() + " products");
    }

    /**
     * Handle search button click
     */
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchTerm = productSearchField.getText().trim().toLowerCase();

        if (searchTerm.isEmpty()) {
            productsList.setAll(allProducts);
        } else {
            List<Product> results = allProducts.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(searchTerm) ||
                            (p.getSku() != null && p.getSku().toLowerCase().contains(searchTerm)) ||
                            (p.getBrand() != null && p.getBrand().toLowerCase().contains(searchTerm)))
                    .collect(Collectors.toList());
            productsList.setAll(results);
        }

        updateProductCount();
    }

    /**
     * Handle clear search button click
     */
    @FXML
    public void handleClearSearch(ActionEvent event) {
        productSearchField.clear();
        categoryFilterCombo.setValue(null);
        statusFilterCombo.setValue("All Status");
        productsList.setAll(allProducts);
        updateProductCount();
    }

    /**
     * Handle apply filters button click
     */
    @FXML
    public void handleApplyFilters(ActionEvent event) {
        List<Product> filtered = new ArrayList<>(allProducts);

        // Filter by category
        Category selectedCategory = categoryFilterCombo.getValue();
        if (selectedCategory != null) {
            filtered = filtered.stream()
                    .filter(p -> p.getCategoryId() == selectedCategory.getCategoryId())
                    .collect(Collectors.toList());
        }

        // Filter by status
        String status = statusFilterCombo.getValue();
        if (status != null && !status.equals("All Status")) {
            if (status.equals("Active")) {
                filtered = filtered.stream()
                        .filter(Product::isActive)
                        .collect(Collectors.toList());
            } else if (status.equals("Inactive")) {
                filtered = filtered.stream()
                        .filter(p -> !p.isActive())
                        .collect(Collectors.toList());
            }
        }

        productsList.setAll(filtered);
        updateProductCount();
    }

    /**
     * Handle add product button click
     */
    public void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_product_dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Product");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh list after dialog closes
            loadAllProducts();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open add product dialog: " + e.getMessage());
        }
    }

    /**
     * Handle view product
     */
    private void handleViewProduct(Product product) {
        System.out.println("View product: " + product.getProductName());

        // Show product details dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Details");
        alert.setHeaderText(product.getProductName());

        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(product.getProductId()).append("\n");
        details.append("SKU: ").append(product.getSku()).append("\n");
        details.append("Brand: ").append(product.getBrand()).append("\n");
        details.append("Price: $").append(String.format("%.2f", product.getPrice())).append("\n");
        details.append("Cost: $").append(String.format("%.2f", product.getCostPrice())).append("\n");
        details.append("Description: ").append(product.getDescription()).append("\n");

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    /**
     * Handle edit product
     */
    private void handleEditProduct(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_product_dialog.fxml"));
            Parent root = loader.load();

            EditProductDialogController controller = loader.getController();
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Product");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh list after dialog closes
            loadAllProducts();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open edit product dialog: " + e.getMessage());
        }
    }

    /**
     * Handle delete product
     */
    private void handleDeleteProduct(Product product) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Product");
        confirmation.setContentText(
                "Are you sure you want to delete: " + product.getProductName() + "?\nThis action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.deleteProduct(product.getProductId());
                    loadAllProducts();
                    showInfo("Success", "Product deleted successfully.");
                } catch (Exception e) {
                    System.err.println("Error deleting product: " + e.getMessage());
                    e.printStackTrace();
                    showError("Error", "Failed to delete product: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Handle refresh button click
     */
    @FXML
    public void handleRefresh(ActionEvent event) {
        loadAllProducts();
        showInfo("Refreshed", "Product list has been refreshed.");
    }

    /**
     * Handle export CSV button click
     */
    @FXML
    public void handleExport(ActionEvent event) {
        try {
            File file = new File("products_export.csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write CSV header
                writer.write("ID,Name,Category,SKU,Brand,Price,Cost,Stock,Status\n");

                // Write product data
                for (Product product : productsList) {
                    Optional<Category> category = Optional
                            .ofNullable(categoryService.getCategoryById(product.getCategoryId()));
                    String categoryName = category.map(Category::getCategoryName).orElse("N/A");

                    Optional<Inventory> inv = Optional
                            .ofNullable(inventoryService.getInventoryByProductId(product.getProductId()));
                    int stock = inv.map(Inventory::getQuantityInStock).orElse(0);

                    writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%d,\"%s\"\n",
                            product.getProductId(),
                            product.getProductName(),
                            categoryName,
                            product.getSku() != null ? product.getSku() : "",
                            product.getBrand() != null ? product.getBrand() : "",
                            product.getPrice(),
                            product.getCostPrice(),
                            stock,
                            product.isActive() ? "Active" : "Inactive"));
                }
            }

            showInfo("Export Successful", "Products exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error exporting products: " + e.getMessage());
            e.printStackTrace();
            showError("Export Failed", "Failed to export products: " + e.getMessage());
        }
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

    /**
     * Show error alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
