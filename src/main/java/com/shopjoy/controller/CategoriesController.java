package com.shopjoy.controller;

import com.shopjoy.model.Category;
import com.shopjoy.service.CategoryService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing categories.
 */
public class CategoriesController {

    @FXML
    private Button addCategoryButton;

    @FXML
    private TableView<Category> categoriesTable;

    @FXML
    private TableColumn<Category, Integer> categoryIdColumn;

    @FXML
    private TableColumn<Category, String> categoryNameColumn;

    @FXML
    private TableColumn<Category, String> categoryDescriptionColumn;

    @FXML
    private TableColumn<Category, Integer> categoryProductCountColumn;

    @FXML
    private TableColumn<Category, String> categoryParentColumn;

    @FXML
    private TableColumn<Category, Void> categoryActionsColumn;

    private CategoryService categoryService = new CategoryService();

    private ObservableList<Category> categoriesList = FXCollections.observableArrayList();

    /**
     * Initialize table and load data.
     */
    @FXML
    public void initialize() {
        // Basic columns
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Product count column
        categoryProductCountColumn.setCellValueFactory(cellData -> {
            try {
                int count = categoryService.getProductCount(cellData.getValue().getCategoryId());
                return new SimpleIntegerProperty(count).asObject();
            } catch (Exception e) {
                System.err.println("Error fetching product count: " + e.getMessage());
                return new SimpleIntegerProperty(0).asObject();
            }
        });

        // Parent category column
        categoryParentColumn.setCellValueFactory(cellData -> {
            try {
                Integer parentId = cellData.getValue().getParentCategoryId();
                if (parentId == null) {
                    return new SimpleStringProperty("Top Level");
                }
                Category parent = categoryService.getCategoryById(parentId);
                return new SimpleStringProperty(parent != null ? parent.getCategoryName() : "Top Level");
            } catch (Exception e) {
                System.err.println("Error fetching parent category: " + e.getMessage());
                return new SimpleStringProperty("Top Level");
            }
        });

        // Actions column
        categoryActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");

                editBtn.setOnAction(evt -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleEditCategory(category);
                });

                deleteBtn.setOnAction(evt -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Load and bind data
        loadCategories();
        categoriesTable.setItems(categoriesList);
    }

    /**
     * Load all categories from service.
     */
    private void loadCategories() {
        try {
            categoriesList.clear();
            List<Category> all = categoryService.getAllCategories();
            categoriesList.addAll(all);
            System.out.println("Loaded " + all.size() + " categories");
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    /**
     * Handle add category action.
     */
    @FXML
    public void handleAddCategory(ActionEvent event) {
        try {
            // Name dialog
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Add Category");
            nameDialog.setHeaderText("Create New Category");
            nameDialog.setContentText("Category Name:");
            Optional<String> nameResult = nameDialog.showAndWait();
            if (nameResult.isEmpty() || nameResult.get().trim().isEmpty()) {
                return;
            }
            String name = nameResult.get().trim();

            // Description dialog
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Category Description");
            descDialog.setHeaderText("Optional Description");
            descDialog.setContentText("Description:");
            Optional<String> descResult = descDialog.showAndWait();
            String description = descResult.orElse("").trim();

            // Parent selection dialog
            Dialog<Integer> parentDialog = buildParentDialog(null);
            Optional<Integer> parentResult = parentDialog.showAndWait();
            Integer parentId = parentResult.orElse(null);

            // Add category
            Category created = categoryService.addCategory(name, description.isEmpty() ? null : description, parentId);
            if (created != null) {
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category. Name may already exist.");
            }
        } catch (Exception e) {
            System.err.println("Error adding category: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category: " + e.getMessage());
        }
    }

    /**
     * Handle edit category action.
     */
    private void handleEditCategory(Category category) {
        try {
            // Name dialog pre-filled
            TextInputDialog nameDialog = new TextInputDialog(category.getCategoryName());
            nameDialog.setTitle("Edit Category");
            nameDialog.setHeaderText("Edit Category Name");
            nameDialog.setContentText("Category Name:");
            Optional<String> nameResult = nameDialog.showAndWait();
            if (nameResult.isEmpty() || nameResult.get().trim().isEmpty()) {
                return;
            }
            String name = nameResult.get().trim();

            // Description dialog pre-filled
            TextInputDialog descDialog = new TextInputDialog(category.getDescription() == null ? "" : category.getDescription());
            descDialog.setTitle("Edit Description");
            descDialog.setHeaderText("Edit Category Description");
            descDialog.setContentText("Description:");
            Optional<String> descResult = descDialog.showAndWait();
            String description = descResult.orElse("").trim();

            // Parent selection dialog pre-selecting current parent
            Dialog<Integer> parentDialog = buildParentDialog(category.getParentCategoryId());
            Optional<Integer> parentResult = parentDialog.showAndWait();
            Integer parentId = parentResult.orElse(null);

            category.setCategoryName(name);
            category.setDescription(description.isEmpty() ? null : description);
            category.setParentCategoryId(parentId);

            Category updated = categoryService.updateCategory(category);
            if (updated != null) {
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category. Name may already exist.");
            }
        } catch (Exception e) {
            System.err.println("Error editing category: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to edit category: " + e.getMessage());
        }
    }

    /**
     * Handle delete category action.
     */
    private void handleDeleteCategory(Category category) {
        try {
            int productCount = categoryService.getProductCount(category.getCategoryId());
            if (productCount > 0) {
                showAlert(Alert.AlertType.ERROR, "Cannot Delete", "Cannot delete category with products.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete Category");
            confirm.setContentText("Are you sure you want to delete " + category.getCategoryName() + "?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            boolean deleted = categoryService.deleteCategory(category.getCategoryId());
            if (deleted) {
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category: " + e.getMessage());
        }
    }

    /**
     * Build a dialog for selecting parent category.
     */
    private Dialog<Integer> buildParentDialog(Integer currentParentId) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Parent Category");
        dialog.setHeaderText("Select Parent Category (optional)");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Category> parentCombo = new ComboBox<>();
        parentCombo.getItems().add(null); // Top level option
        parentCombo.getItems().addAll(categoriesList);
        parentCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Top Level" : item.getCategoryName()));
            }
        });
        parentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Top Level" : item.getCategoryName()));
            }
        });

        // Pre-select current parent
        if (currentParentId == null) {
            parentCombo.setValue(null);
        } else {
            categoriesList.stream()
                    .filter(c -> currentParentId.equals(c.getParentCategoryId()) || currentParentId.equals(c.getCategoryId()))
                    .findFirst()
                    .ifPresent(parentCombo::setValue);
        }

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.add(new Label("Parent:"), 0, 0);
        pane.add(parentCombo, 1, 0);
        GridPane.setHgrow(parentCombo, Priority.ALWAYS);

        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(button -> button == ButtonType.OK ? (parentCombo.getValue() == null ? null : parentCombo.getValue().getCategoryId()) : null);
        return dialog;
    }

    /**
     * Show alert helper.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
