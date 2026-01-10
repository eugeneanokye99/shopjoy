package com.shopjoy.controller;

import com.shopjoy.model.Category;
import com.shopjoy.service.CategoryService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CategoriesManagementController {

    @FXML
    private Button addCategoryButton;

    @FXML
    private Button refreshCategoriesButton;

    @FXML
    private Label totalCategoriesLabel;

    @FXML
    private Label topLevelCategoriesLabel;

    @FXML
    private TableView<Category> categoriesTable;

    @FXML
    private TableColumn<Category, Integer> categoryIdColumn;

    @FXML
    private TableColumn<Category, String> categoryNameColumn;

    @FXML
    private TableColumn<Category, String> categoryDescriptionColumn;

    @FXML
    private TableColumn<Category, String> categoryParentColumn;

    @FXML
    private TableColumn<Category, Integer> categoryProductCountColumn;

    @FXML
    private TableColumn<Category, Void> categoryActionsColumn;

    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategoryData();
    }

    private void setupTableColumns() {
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Parent Category Column - Display Parent Name instead of ID
        categoryParentColumn.setCellValueFactory(cellData -> {
            Integer parentId = cellData.getValue().getParentCategoryId();
            if (parentId == null || parentId == 0) {
                return new SimpleStringProperty("-");
            } else {
                Category parent = categoryService.getCategoryById(parentId);
                return new SimpleStringProperty(
                        parent != null ? parent.getCategoryName() : "Unknown (" + parentId + ")");
            }
        });

        // Product Count Column - Fetch dynamically
        categoryProductCountColumn.setCellValueFactory(cellData -> {
            int count = categoryService.getProductCount(cellData.getValue().getCategoryId());
            return new SimpleIntegerProperty(count).asObject();
        });

        // Actions Column - Edit and Delete Buttons
        categoryActionsColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<Category, Void>, TableCell<Category, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(10, editButton, deleteButton);

            {
                editButton.getStyleClass().add("primary-button");
                editButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
                editButton.setOnAction(event -> handleEditCategory(getTableView().getItems().get(getIndex())));

                deleteButton.getStyleClass().add("secondary-button"); // Or error-button if available
                deleteButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px; -fx-background-color: #ef4444;");
                deleteButton.setOnAction(event -> handleDeleteCategory(getTableView().getItems().get(getIndex())));
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

    private void loadCategoryData() {
        categoryList.clear();
        List<Category> categories = categoryService.getAllCategories();
        categoryList.addAll(categories);
        categoriesTable.setItems(categoryList);

        // Update Statistics
        totalCategoriesLabel.setText("Total Categories: " + categories.size());

        long topLevelCount = categories.stream()
                .filter(c -> c.getParentCategoryId() == null || c.getParentCategoryId() == 0)
                .count();
        topLevelCategoriesLabel.setText("Top Level: " + topLevelCount);
    }

    @FXML
    void handleAddCategory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_category_dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Category");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadCategoryData(); // Refresh
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadCategoryData();
        System.out.println("Categories refreshed");
    }

    private void handleEditCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_category_dialog.fxml"));
            Parent root = loader.load();

            EditCategoryDialogController controller = loader.getController();
            controller.setCategory(category);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Category");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadCategoryData(); // Refresh
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDeleteCategory(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete " + category.getCategoryName() + "?");
        alert.setContentText("Are you sure you want to delete this category?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = categoryService.deleteCategory(category.getCategoryId());
            if (deleted) {
                loadCategoryData();
                System.out.println("Category deleted: " + category.getCategoryName());
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Delete Failed");
                errorAlert.setContentText("Could not delete category. It may have products or subcategories.");
                errorAlert.showAndWait();
            }
        }
    }
}
