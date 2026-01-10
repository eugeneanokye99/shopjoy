package com.shopjoy.controller;

import com.shopjoy.model.Product;
import com.shopjoy.model.Review;
import com.shopjoy.model.User;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.ReviewService;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ReviewsManagementController {

    @FXML
    private ComboBox<String> ratingFilterCombo;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button refreshButton;

    @FXML
    private Label totalReviewsLabel;
    @FXML
    private Label avgRatingLabel;
    @FXML
    private Label verifiedCountLabel;

    @FXML
    private TableView<ReviewViewModel> reviewsTable;
    @FXML
    private TableColumn<ReviewViewModel, Integer> reviewIdCol;
    @FXML
    private TableColumn<ReviewViewModel, String> productCol;
    @FXML
    private TableColumn<ReviewViewModel, String> customerCol;
    @FXML
    private TableColumn<ReviewViewModel, Integer> ratingCol;
    @FXML
    private TableColumn<ReviewViewModel, String> titleCol;
    @FXML
    private TableColumn<ReviewViewModel, String> commentCol;
    @FXML
    private TableColumn<ReviewViewModel, String> verifiedCol;
    @FXML
    private TableColumn<ReviewViewModel, String> dateCol;
    @FXML
    private TableColumn<ReviewViewModel, Void> actionsCol;

    private final ReviewService reviewService = new ReviewService();
    private final ProductService productService = new ProductService();
    private final UserService userService = new UserService();
    private final ObservableList<ReviewViewModel> masterData = FXCollections.observableArrayList();
    private FilteredList<ReviewViewModel> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        setupCombo();
        loadData();
    }

    private void setupCombo() {
        ratingFilterCombo.setItems(FXCollections.observableArrayList(
                "All Ratings", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"));
        ratingFilterCombo.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        reviewIdCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getReviewId()).asObject());
        productCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        customerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        ratingCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRating()).asObject());
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        commentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getComment()));
        verifiedCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isVerified() ? "Yes" : "No"));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateFormatted()));

        actionsCol.setCellFactory(createActionCellFactory());

        filteredData = new FilteredList<>(masterData, p -> true);
        reviewsTable.setItems(filteredData);
    }

    private Callback<TableColumn<ReviewViewModel, Void>, TableCell<ReviewViewModel, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, deleteButton);

            {
                deleteButton.getStyleClass().add("danger-button"); // assuming danger-button exists or similar
                deleteButton.setStyle(
                        "-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #ef4444; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
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

    private void handleDelete(ReviewViewModel vm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Review");
        alert.setHeaderText("Delete review for " + vm.getProductName() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = reviewService.deleteReview(vm.getReviewId(), vm.getUserId(), true); // isAdmin=true
            if (success) {
                loadData();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("Failed to delete review.");
                error.showAndWait();
            }
        }
    }

    private void loadData() {
        masterData.clear();
        List<Review> allReviews = reviewService.getAllReviews(); // Added method

        double totalRatingSum = 0;
        int count = 0;
        int verified = 0;

        if (allReviews != null) {
            for (Review r : allReviews) {
                String pName = "Unknown Product";
                Product p = productService.getProductById(r.getProductId());
                if (p != null)
                    pName = p.getProductName();

                String uName = "Unknown User";
                User u = userService.getUserById(r.getUserId());
                if (u != null)
                    uName = u.getUsername();

                masterData.add(new ReviewViewModel(r, pName, uName));

                count++;
                totalRatingSum += r.getRating();
                if (r.isVerifiedPurchase())
                    verified++;
            }
        }

        totalReviewsLabel.setText(String.valueOf(count));
        double avg = count > 0 ? totalRatingSum / count : 0.0;
        avgRatingLabel.setText(String.format("%.1f", avg));
        verifiedCountLabel.setText(String.valueOf(verified));

        handleFilter(null); // Re-apply current filter
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        String selected = ratingFilterCombo.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("All Ratings")) {
            filteredData.setPredicate(p -> true);
        } else {
            // "5 Stars", "4 Stars" ...
            int stars = Integer.parseInt(selected.substring(0, 1));
            filteredData.setPredicate(r -> r.getRating() == stars);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    public static class ReviewViewModel {
        private final Review review;
        private final String productName;
        private final String customerName;
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public ReviewViewModel(Review review, String productName, String customerName) {
            this.review = review;
            this.productName = productName;
            this.customerName = customerName;
        }

        public int getReviewId() {
            return review.getReviewId();
        }

        public int getProductId() {
            return review.getProductId();
        }

        public int getUserId() {
            return review.getUserId();
        }

        public String getProductName() {
            return productName;
        }

        public String getCustomerName() {
            return customerName;
        }

        public int getRating() {
            return review.getRating();
        }

        public String getTitle() {
            return review.getTitle() != null ? review.getTitle() : "";
        }

        public String getComment() {
            return review.getComment() != null ? review.getComment() : "";
        }

        public boolean isVerified() {
            return review.isVerifiedPurchase();
        }

        public String getDateFormatted() {
            if (review.getCreatedAt() != null)
                return review.getCreatedAt().format(DATE_FMT);
            return "-";
        }
    }
}
