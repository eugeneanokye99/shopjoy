package com.shopjoy.controller;

import com.shopjoy.model.Category;
import com.shopjoy.service.CategoryService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.stage.Stage;

import java.util.List;

public class AddCategoryDialogController {

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

    @FXML
    public void initialize() {
        loadParentCategories();

        // Clear error message on typing
        categoryNameField.textProperty().addListener((obs, old, neu) -> messageLabel.setText(""));

        // Set focus on category name
        javafx.application.Platform.runLater(categoryNameField::requestFocus);
    }

    private void loadParentCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            parentCategoryComboBox.getItems().add(null); // Option for "No Parent"
            parentCategoryComboBox.getItems().addAll(categories);

            // Cell factory to show category names
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
            parentCategoryComboBox.getSelectionModel().selectFirst(); // Default to None
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading categories");
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInput())
            return;

        String name = categoryNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        Category parent = parentCategoryComboBox.getValue();
        Integer parentId = (parent != null) ? parent.getCategoryId() : null;

        Category saved = categoryService.addCategory(name, description, parentId);

        if (saved != null) {
            closeDialog(event);
        } else {
            messageLabel.setText("Error saving category. Name must be unique.");
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
