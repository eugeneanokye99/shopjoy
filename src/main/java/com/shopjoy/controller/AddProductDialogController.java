package com.shopjoy.controller;

import com.shopjoy.model.Category;
import com.shopjoy.model.Product;
import com.shopjoy.service.CategoryService;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Controller class for adding new products
 */
public class AddProductDialogController {

    @FXML
    private TextField productNameField;

    @FXML
    private TextField skuField;

    @FXML
    private TextField brandField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField costPriceField;

    @FXML
    private TextField initialStockField;

    @FXML
    private TextField reorderLevelField;

    @FXML
    private TextField warehouseField;

    @FXML
    private TextField imageUrlField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private CheckBox isActiveCheckBox;

    @FXML
    private Label productDialogMessageLabel;

    @FXML
    private Button saveProductButton;

    @FXML
    private Button cancelProductButton;

    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private InventoryService inventoryService = new InventoryService();

    /**
     * Initialize the controller
     * Called automatically after FXML fields are injected
     */
    @FXML
    public void initialize() {
        // Load categories into ComboBox
        loadCategories();

        // Set default values
        reorderLevelField.setText("10");
        isActiveCheckBox.setSelected(true);

        // Add input validation filters
        addNumericFilters();

        // Clear error messages when user starts typing
        addClearMessageListeners();

        // Set focus on initial text field
        javafx.application.Platform.runLater(productNameField::requestFocus);
    }

    /**
     * Load categories from database into ComboBox
     */
    private void loadCategories() {
        try {
            // Get all categories from service
            List<Category> categories = categoryService.getAllCategories();

            // Add to ComboBox
            categoryComboBox.getItems().addAll(categories);

            // Set custom cell factory to display category name
            categoryComboBox.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });

            // Set button cell to show selected category name
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });

        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
            showMessage("Failed to load categories", true);
        }
    }

    /**
     * Add numeric input filters to fields
     */
    private void addNumericFilters() {
        // Pattern for decimal numbers (price fields)
        Pattern decimalPattern = Pattern.compile("\\d*\\.?\\d*");
        UnaryOperator<TextFormatter.Change> decimalFilter = change -> {
            String newText = change.getControlNewText();
            if (decimalPattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        // Pattern for integers (stock fields)
        Pattern integerPattern = Pattern.compile("\\d*");
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (integerPattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        // Apply filters
        priceField.setTextFormatter(new TextFormatter<>(decimalFilter));
        costPriceField.setTextFormatter(new TextFormatter<>(decimalFilter));
        initialStockField.setTextFormatter(new TextFormatter<>(integerFilter));
        reorderLevelField.setTextFormatter(new TextFormatter<>(integerFilter));
    }

    /**
     * Add listeners to clear error messages when user starts typing
     */
    private void addClearMessageListeners() {
        productNameField.textProperty().addListener((obs, oldVal, newVal) -> productDialogMessageLabel.setText(""));
        skuField.textProperty().addListener((obs, oldVal, newVal) -> productDialogMessageLabel.setText(""));
        priceField.textProperty().addListener((obs, oldVal, newVal) -> productDialogMessageLabel.setText(""));
        initialStockField.textProperty().addListener((obs, oldVal, newVal) -> productDialogMessageLabel.setText(""));
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> productDialogMessageLabel.setText(""));
    }

    /**
     * Handle save product button click
     */
    @FXML
    public void handleSaveProduct(ActionEvent event) {
        try {
            // Validate all required fields
            if (!validateFields()) {
                return;
            }

            // Get field values
            String productName = productNameField.getText().trim();
            String description = descriptionArea.getText().trim();
            Category selectedCategory = categoryComboBox.getValue();
            String priceText = priceField.getText().trim();
            String costPriceText = costPriceField.getText().trim();
            String sku = skuField.getText().trim();
            String brand = brandField.getText().trim();
            String imageUrl = imageUrlField.getText().trim();
            boolean isActive = isActiveCheckBox.isSelected();
            String initialStockText = initialStockField.getText().trim();
            String reorderLevelText = reorderLevelField.getText().trim();
            String warehouse = warehouseField.getText().trim();

            // Parse numeric values
            double price = Double.parseDouble(priceText);
            double costPrice = costPriceText.isEmpty() ? 0.0 : Double.parseDouble(costPriceText);
            int initialStock = Integer.parseInt(initialStockText);
            int reorderLevel = reorderLevelText.isEmpty() ? 10 : Integer.parseInt(reorderLevelText);

            // Create new Product object
            Product product = new Product();
            product.setProductName(productName);
            product.setDescription(description.isEmpty() ? null : description);
            product.setCategoryId(selectedCategory.getCategoryId());
            product.setPrice(price);
            product.setSku(sku);
            product.setBrand(brand.isEmpty() ? null : brand);
            product.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
            product.setActive(isActive);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            // Try to save product
            Product savedProduct = productService.addProduct(product, initialStock);

            if (savedProduct == null) {
                showMessage("Failed to add product. SKU may already exist.", true);
                return;
            }

            // Update inventory reorder level if specified
            if (!reorderLevelText.isEmpty() && savedProduct.getProductId() > 0) {
                try {
                    inventoryService.updateReorderLevel(savedProduct.getProductId(), reorderLevel);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to update reorder level: " + e.getMessage());
                }
            }

            // Update inventory warehouse location if specified
            if (!warehouse.isEmpty() && savedProduct.getProductId() > 0) {
                try {
                    inventoryService.updateWarehouseLocation(savedProduct.getProductId(), warehouse);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to update warehouse location: " + e.getMessage());
                }
            }

            // Show success message
            showMessage("Product added successfully!", false);

            // Wait 1 second before closing
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        closeDialog(event);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values: " + e.getMessage());
            showMessage("Invalid numeric value. Please check price and stock fields.", true);
        } catch (Exception e) {
            System.err.println("Error saving product: " + e.getMessage());
            e.printStackTrace();
            showMessage("Error saving product: " + e.getMessage(), true);
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        closeDialog(event);
    }

    /**
     * Close the dialog window
     */
    private void closeDialog(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Validate all required fields
     * 
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFields() {
        // Check product name
        if (productNameField.getText().trim().isEmpty()) {
            showMessage("Product name is required", true);
            productNameField.requestFocus();
            return false;
        }

        // Check category
        if (categoryComboBox.getValue() == null) {
            showMessage("Please select a category", true);
            categoryComboBox.requestFocus();
            return false;
        }

        // Check SKU
        if (skuField.getText().trim().isEmpty()) {
            showMessage("SKU is required", true);
            skuField.requestFocus();
            return false;
        }

        // Check price
        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            showMessage("Price is required", true);
            priceField.requestFocus();
            return false;
        }

        if (!isValidPrice(priceText)) {
            showMessage("Please enter a valid price (must be >= 0)", true);
            priceField.requestFocus();
            return false;
        }

        // Check cost price if provided
        String costPriceText = costPriceField.getText().trim();
        if (!costPriceText.isEmpty() && !isValidPrice(costPriceText)) {
            showMessage("Please enter a valid cost price (must be >= 0)", true);
            costPriceField.requestFocus();
            return false;
        }

        // Check initial stock
        String stockText = initialStockField.getText().trim();
        if (stockText.isEmpty()) {
            showMessage("Initial stock is required", true);
            initialStockField.requestFocus();
            return false;
        }

        if (!isValidInteger(stockText)) {
            showMessage("Please enter a valid stock quantity (must be >= 0)", true);
            initialStockField.requestFocus();
            return false;
        }

        // Check reorder level if provided
        String reorderText = reorderLevelField.getText().trim();
        if (!reorderText.isEmpty() && !isValidInteger(reorderText)) {
            showMessage("Please enter a valid reorder level (must be >= 0)", true);
            reorderLevelField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validate price input
     * 
     * @param priceText The price text to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPrice(String priceText) {
        try {
            double price = Double.parseDouble(priceText);
            return price >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate integer input
     * 
     * @param text The text to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidInteger(String text) {
        try {
            int value = Integer.parseInt(text);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Show message to user
     * 
     * @param message The message to display
     * @param isError True if error message, false for success
     */
    private void showMessage(String message, boolean isError) {
        productDialogMessageLabel.setText(message);

        if (isError) {
            // Red text for errors
            productDialogMessageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            // Green text for success
            productDialogMessageLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
        }
    }
}
