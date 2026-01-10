package com.shopjoy.controller;

import com.shopjoy.model.Category;
import com.shopjoy.service.CategoryService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class EditCategoryDialogController {

    @FXML
    private TextField categoryIdField; // Hidden or read-only

    @FXML
    private TextField categoryNameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<Category> parentCategoryComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private final CategoryService categoryService = new CategoryService();
    private Category currentCategory;

    @FXML
    public void initialize() {
        // Clear error message on typing
        categoryNameField.textProperty().addListener((obs, old, neu) -> messageLabel.setText(""));

        // Set focus on category name
        javafx.application.Platform.runLater(categoryNameField::requestFocus);
    }

    public void setCategory(Category category) {
        this.currentCategory = category;
        if (category != null) {
            categoryIdField.setText(String.valueOf(category.getCategoryId()));
            categoryNameField.setText(category.getCategoryName());
            descriptionArea.setText(category.getDescription());

            loadParentCategories();
        }
    }

    private void loadParentCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();

            // Remove current category and its children to prevent cycles (simplified: just
            // remove self)
            // A more robust solution would detect cycles, but preventing self-parenting is
            // minimal req.
            if (currentCategory != null) {
                categories = categories.stream()
                        .filter(c -> c.getCategoryId() != currentCategory.getCategoryId())
                        .collect(Collectors.toList());
            }

            parentCategoryComboBox.getItems().add(null); // Option for "No Parent"
            parentCategoryComboBox.getItems().addAll(categories);

            parentCategoryComboBox.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : (item == null ? "None (Top Level)" : item.getCategoryName()));
                }
            });
            parentCategoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : (item == null ? "None (Top Level)" : item.getCategoryName()));
                }
            });

            // Select current parent
            if (currentCategory != null && currentCategory.getParentCategoryId() != null) {
                for (Category c : parentCategoryComboBox.getItems()) {
                    if (c != null && c.getCategoryId() == currentCategory.getParentCategoryId()) {
                        parentCategoryComboBox.getSelectionModel().select(c);
                        break;
                    }
                }
            } else {
                parentCategoryComboBox.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading categories");
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInput())
            return;

        Category parent = parentCategoryComboBox.getValue();
        Integer parentId = (parent != null) ? parent.getCategoryId() : null;

        // Cycle check: Ensure selected parent is not a child of currentCategory
        // (Recursive check omitted for brevity, but self-check done)
        if (parentId != null && parentId == currentCategory.getCategoryId()) {
            messageLabel.setText("Category cannot be its own parent.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        currentCategory.setCategoryName(categoryNameField.getText().trim());
        currentCategory.setDescription(descriptionArea.getText().trim());
        currentCategory.setParentCategoryId(parentId);
        // CreatedAt remains same

        Category updated = categoryService.updateCategory(currentCategory);

        if (updated != null) {
            closeDialog(event);
        } else {
            messageLabel.setText("Error updating category. Name may duplicate existing.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeDialog(event);
    }

    private boolean validateInput() {
        if (categoryNameField.getText() == null || categoryNameField.getText().trim().isEmpty()) {
            messageLabel.setText("Category name is required.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
        return true;
    }

    private void closeDialog(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
