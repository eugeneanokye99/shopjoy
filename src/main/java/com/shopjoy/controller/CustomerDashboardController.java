package com.shopjoy.controller;

import com.shopjoy.ShopJoyApp;
import com.shopjoy.model.*;
import com.shopjoy.service.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller for customer shopping dashboard.
 */
public class CustomerDashboardController {

    @FXML
    private TextField searchField;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private Button searchButton;
    @FXML
    private Button cartButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button myOrdersButton;
    @FXML
    private Button myReviewsButton;
    @FXML
    private Button myProfileButton;
    @FXML
    private Label customerWelcomeLabel;
    @FXML
    private ListView<Category> categoriesListView;
    @FXML
    private FlowPane productsFlowPane;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final OrderService orderService = new OrderService();
    private final ReviewService reviewService = new ReviewService();
    private final UserService userService = new UserService();
    private final CartService cartService = new CartService();

    private List<Product> currentProducts = new ArrayList<>();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        User user = ShopJoyApp.getCurrentUser();
        if (user != null) {
            customerWelcomeLabel.setText("Welcome, " + user.getFirstName() + "!");
            updateCartCount();
        } else {
            customerWelcomeLabel.setText("Welcome!");
        }

        loadCategories();

        categoriesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || "All Products".equals(newVal.getCategoryName())) {
                loadAndDisplayProducts(productService.getAllProducts());
            } else {
                loadAndDisplayProducts(productService.getProductsByCategory(newVal.getCategoryId()));
            }
        });

        // Load all products initially
        loadAndDisplayProducts(productService.getAllProducts());
    }

    private void updateCartCount() {
        User user = ShopJoyApp.getCurrentUser();
        if (user == null)
            return;
        List<CartItem> items = cartService.getCartItems(user.getUserId());
        int count = items.stream().mapToInt(CartItem::getQuantity).sum();
        cartButton.setText("🛒 Cart (" + count + ")");
    }

    private void loadCategories() {
        try {
            categoriesListView.getItems().clear();

            Category all = new Category();
            all.setCategoryName("All Products");
            categoriesListView.getItems().add(all);

            List<Category> top = categoryService.getTopLevelCategories();
            categoriesListView.getItems().addAll(top);

            categoriesListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCategoryName());
                }
            });

            categoriesListView.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
    }

    private void handleAddToCart(Product product) {
        User user = ShopJoyApp.getCurrentUser();
        if (user == null) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please login to add items to cart.");
            return;
        }
        try {
            if (!product.isActive()) {
                showAlert(Alert.AlertType.WARNING, "Unavailable", "This product is unavailable.");
                return;
            }
            boolean success = cartService.addToCart(user.getUserId(), product.getProductId(), 1);
            if (success) {
                updateCartCount();
                showAlert(Alert.AlertType.INFORMATION, "Added", "Added to cart!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not add to cart.");
            }
        } catch (Exception e) {
            System.err.println("Add to cart failed: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add to cart.");
        }
    }

    private void loadAndDisplayProducts(List<Product> products) {
        currentProducts = products != null ? products : new ArrayList<>();
        productsFlowPane.getChildren().clear();
        for (Product p : currentProducts) {
            productsFlowPane.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        try {
            String url = product.getImageUrl();
            if (url != null && !url.isBlank()) {
                imageView.setImage(new Image(url, true));
            } else {
                InputStream placeholder = getClass().getResourceAsStream("/images/placeholder.png");
                if (placeholder != null) {
                    imageView.setImage(new Image(placeholder));
                }
            }
        } catch (Exception ignored) {
        }

        Label nameLabel = new Label(product.getProductName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(CURRENCY.format(product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        double rating = 0.0;
        try {
            rating = reviewService.getAverageRating(product.getProductId());
        } catch (Exception e) {
            System.err.println("Rating lookup failed: " + e.getMessage());
        }
        Label ratingLabel = new Label("Rating: " + String.format("%.1f", rating) + "/5.0 ⭐");
        ratingLabel.getStyleClass().add("product-rating");

        Button detailsBtn = new Button("View Details");
        detailsBtn.setOnAction(evt -> handleViewProductDetails(product));

        Button addCartBtn = new Button("Add to Cart");
        addCartBtn.getStyleClass().add("add-to-cart-button");
        addCartBtn.setOnAction(evt -> handleAddToCart(product));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, ratingLabel, detailsBtn, addCartBtn);
        return card;
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String text = searchField.getText() != null ? searchField.getText().trim() : "";
        if (text.isEmpty()) {
            loadAndDisplayProducts(productService.getAllProducts());
            return;
        }
        try {
            loadAndDisplayProducts(productService.searchProducts(text));
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleApplyFilter(ActionEvent event) {
        try {
            String minTxt = minPriceField.getText().trim();
            String maxTxt = maxPriceField.getText().trim();
            Double min = minTxt.isEmpty() ? null : Double.parseDouble(minTxt);
            Double max = maxTxt.isEmpty() ? null : Double.parseDouble(maxTxt);
            List<Product> filtered = productService.getProductsByPriceRange(min, max);
            loadAndDisplayProducts(filtered);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid", "Enter valid numbers for price range.");
        } catch (Exception e) {
            System.err.println("Filter failed: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter products.");
        }
    }

    @FXML
    public void handleViewCart(ActionEvent event) {
        User user = ShopJoyApp.getCurrentUser();
        if (user == null)
            return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Your Cart");
        dialog.initOwner(cartButton.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        updateCartDialogContent(dialog, user);
        dialog.showAndWait();
    }

    private void updateCartDialogContent(Dialog<Void> dialog, User user) {
        updateCartDialogContent(dialog, user, false);
    }

    private void updateCartDialogContent(Dialog<Void> dialog, User user, boolean showCheckoutForm) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setPrefWidth(550);
        box.setPrefHeight(450);

        List<CartItem> items = cartService.getCartItems(user.getUserId());
        double total = 0.0;

        if (items.isEmpty()) {
            box.getChildren().add(new Label("Your cart is empty."));
        } else {
            // Cart items section
            VBox cartItemsBox = new VBox(10);
            cartItemsBox.setVisible(!showCheckoutForm);
            cartItemsBox.setManaged(!showCheckoutForm);

            for (CartItem item : items) {
                Product p = item.getProduct();
                if (p == null)
                    continue;
                double subtotal = p.getPrice() * item.getQuantity();
                total += subtotal;

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Label nameLbl = new Label(p.getProductName());
                nameLbl.setPrefWidth(150);
                nameLbl.setWrapText(true);

                // Quantity controls
                HBox qtyBox = new HBox(5);
                qtyBox.setAlignment(Pos.CENTER);
                Button minusBtn = new Button("-");
                minusBtn.setPrefWidth(30);
                Label qtyLbl = new Label(String.valueOf(item.getQuantity()));
                qtyLbl.setMinWidth(20);
                qtyLbl.setAlignment(Pos.CENTER);
                Button plusBtn = new Button("+");
                plusBtn.setPrefWidth(30);

                minusBtn.setOnAction(e -> {
                    if (cartService.updateQuantity(item.getCartItemId(), item.getQuantity() - 1)) {
                        updateCartDialogContent(dialog, user, showCheckoutForm);
                        updateCartCount();
                    }
                });

                plusBtn.setOnAction(e -> {
                    if (cartService.updateQuantity(item.getCartItemId(), item.getQuantity() + 1)) {
                        updateCartDialogContent(dialog, user, showCheckoutForm);
                        updateCartCount();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Stock Limit",
                                "Cannot increase quantity further. Out of stock.");
                    }
                });

                qtyBox.getChildren().addAll(minusBtn, qtyLbl, plusBtn);

                Label priceLbl = new Label(CURRENCY.format(subtotal));
                priceLbl.setPrefWidth(80);
                priceLbl.setAlignment(Pos.CENTER_RIGHT);

                Button removeBtn = new Button("❌");
                removeBtn.getStyleClass().add("secondary-button");
                removeBtn.setOnAction(e -> {
                    cartService.removeFromCart(item.getCartItemId());
                    updateCartDialogContent(dialog, user, showCheckoutForm);
                    updateCartCount();
                });

                row.getChildren().addAll(nameLbl, qtyBox, priceLbl, removeBtn);
                cartItemsBox.getChildren().add(row);
            }

            box.getChildren().add(cartItemsBox);

            // Total section (always visible)
            box.getChildren().add(new Separator());
            Label totalLbl = new Label("Total: " + CURRENCY.format(total));
            totalLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            totalLbl.setMaxWidth(Double.MAX_VALUE);
            totalLbl.setAlignment(Pos.CENTER_RIGHT);
            box.getChildren().add(totalLbl);

            // Checkout form section
            VBox checkoutFormBox = new VBox(15);
            checkoutFormBox.setVisible(showCheckoutForm);
            checkoutFormBox.setManaged(showCheckoutForm);
            checkoutFormBox.setPadding(new Insets(10, 0, 0, 0));

            Label checkoutTitle = new Label("Checkout Information");
            checkoutTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            // Shipping address
            Label addressLabel = new Label("Shipping Address:");
            TextField addressField = new TextField();
            addressField.setPromptText("Enter your full shipping address");
            addressField.setPrefWidth(480);

            // Payment method
            Label paymentLabel = new Label("Payment Method:");
            TextField paymentField = new TextField();
            paymentField.setPromptText("e.g., Credit Card, PayPal, Cash on Delivery");
            paymentField.setPrefWidth(480);

            checkoutFormBox.getChildren().addAll(
                    checkoutTitle,
                    addressLabel, addressField,
                    paymentLabel, paymentField);

            box.getChildren().add(checkoutFormBox);

            final double finalTotal = total;

            if (!showCheckoutForm) {
                // Show "Proceed to Checkout" button
                Button checkoutBtn = new Button("Proceed to Checkout");
                checkoutBtn.getStyleClass().add("primary-button");
                checkoutBtn.setMaxWidth(Double.MAX_VALUE);
                checkoutBtn.setOnAction(e -> {
                    dialog.setTitle("Checkout");
                    updateCartDialogContent(dialog, user, true);
                });
                box.getChildren().add(checkoutBtn);
            } else {
                // Show "Place Order" and "Back to Cart" buttons
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER);

                Button backBtn = new Button("← Back to Cart");
                backBtn.getStyleClass().add("secondary-button");
                backBtn.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(backBtn, Priority.ALWAYS);
                backBtn.setOnAction(e -> {
                    dialog.setTitle("Your Cart");
                    updateCartDialogContent(dialog, user, false);
                });

                Button placeOrderBtn = new Button("Place Order");
                placeOrderBtn.getStyleClass().add("primary-button");
                placeOrderBtn.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(placeOrderBtn, Priority.ALWAYS);
                placeOrderBtn.setOnAction(e -> {
                    String address = addressField.getText().trim();
                    String payment = paymentField.getText().trim();

                    if (address.isEmpty() || payment.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Missing Information",
                                "Please fill in both shipping address and payment method.");
                        return;
                    }

                    // Convert cart items to order items
                    List<OrderItem> orderItems = new ArrayList<>();
                    for (CartItem cartItem : items) {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(cartItem.getProduct().getProductId());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItems.add(orderItem);
                    }

                    Order createdOrder = orderService.createOrder(user.getUserId(), orderItems, address, payment);
                    if (createdOrder != null) {
                        // Clear cart
                        for (CartItem item : items) {
                            cartService.removeFromCart(item.getCartItemId());
                        }
                        updateCartCount();

                        showAlert(Alert.AlertType.INFORMATION, "Order Placed",
                                "Your order has been placed successfully!\nOrder Total: "
                                        + CURRENCY.format(finalTotal));
                        dialog.close();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Order Failed",
                                "Failed to place order. Please try again.");
                    }
                });

                buttonBox.getChildren().addAll(backBtn, placeOrderBtn);
                box.getChildren().add(buttonBox);

                // Set focus on address field
                Platform.runLater(() -> addressField.requestFocus());
            }
        }

        dialog.getDialogPane().setContent(box);
    }

    // Checkout is now handled within the cart dialog

    @FXML
    public void handleMyOrders(ActionEvent event) {
        try {
            User user = ShopJoyApp.getCurrentUser();
            if (user == null)
                return;
            var orders = orderService.getOrdersByUser(user.getUserId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Orders");
            dialog.initOwner(myOrdersButton.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox box = new VBox(10);
            box.setPadding(new Insets(15));
            box.setPrefWidth(500);

            if (orders.isEmpty()) {
                box.getChildren().add(new Label("No orders found."));
            } else {
                for (Order o : orders) {
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8));
                    row.setStyle(
                            "-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

                    VBox details = new VBox(2);
                    Label idLbl = new Label("Order #" + o.getOrderId());
                    idLbl.getStyleClass().add("info-label");
                    details.getChildren().addAll(idLbl, new Label(o.getOrderDate().toString().substring(0, 10)));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label totalVal = new Label(CURRENCY.format(o.getTotalAmount()));
                    totalVal.setStyle("-fx-font-weight: bold; -fx-text-fill: -success-color;");

                    Label statusLbl = new Label(o.getStatus().toString());
                    statusLbl.setStyle(
                            "-fx-padding: 3 10; -fx-background-radius: 12; -fx-background-color: #f1f5f9; -fx-font-size: 11px;");

                    row.getChildren().addAll(details, spacer, totalVal, statusLbl);

                    if (o.getStatus() == OrderStatus.DELIVERED) {
                        Button reviewBtn = new Button("Review Items");
                        reviewBtn.getStyleClass().add("secondary-button");
                        reviewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                        reviewBtn.setOnAction(e -> showOrderItemsForReview(o));
                        row.getChildren().add(reviewBtn);
                    }
                    box.getChildren().add(row);
                }
            }
            ScrollPane sp = new ScrollPane(box);
            sp.setFitToWidth(true);
            sp.setPrefHeight(400);
            sp.getStyleClass().add("content-scroll");
            dialog.getDialogPane().setContent(sp);
            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("My orders failed: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders.");
        }
    }

    private void showOrderItemsForReview(Order order) {
        List<OrderItem> items = orderService.getOrderItems(order.getOrderId());
        if (items == null || items.isEmpty())
            return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Review Order Items");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        for (OrderItem item : items) {
            Product p = productService.getProductById(item.getProductId());
            if (p == null)
                continue;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(new Label(p.getProductName()));

            Button writeReviewBtn = new Button("Write Review");
            writeReviewBtn.setOnAction(e -> handleWriteReview(p));
            row.getChildren().add(writeReviewBtn);

            box.getChildren().add(row);
        }
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void handleWriteReview(Product product) {
        User user = ShopJoyApp.getCurrentUser();
        if (user == null)
            return;

        if (reviewService.hasUserReviewedProduct(user.getUserId(), product.getProductId())) {
            showAlert(Alert.AlertType.INFORMATION, "Reviewed", "You have already reviewed this product.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Write Review: " + product.getProductName());
        dialog.initOwner(productsFlowPane.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        ButtonType submitType = new ButtonType("Submit Review", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        HBox ratingRow = new HBox(10);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(5, 4, 3, 2, 1);
        ratingBox.setValue(5);
        ratingBox.setPrefWidth(100);
        ratingRow.getChildren().addAll(new Label("Overall Rating:"), ratingBox);

        TextField titleField = new TextField();
        titleField.setPromptText("Summarize your experience...");
        titleField.setFocusTraversable(true);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Tell us more about the product...");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setFocusTraversable(true);

        Label titleLbl = new Label("Review Summary:");
        titleLbl.getStyleClass().add("info-label");

        Label commentLbl = new Label("Detailed Review:");
        commentLbl.getStyleClass().add("info-label");

        content.getChildren().addAll(ratingRow, titleLbl, titleField, commentLbl, commentArea);

        dialog.getDialogPane().setContent(content);

        /*
         * FIX: Keyboard input issue in JavaFX Dialogs.
         * Using setOnShown ensures the dialog is fully staged before requesting focus,
         * preventing the buttons from intercepting initial keyboard events.
         */
        dialog.setOnShown(event -> titleField.requestFocus());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitType) {
            int rating = ratingBox.getValue();
            String title = titleField.getText();
            String comment = commentArea.getText();
            Review r = reviewService.addReview(product.getProductId(), user.getUserId(), rating, title, comment);
            if (r != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Review submitted!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit review.");
            }
        }
    }

    @FXML
    public void handleMyReviews(ActionEvent event) {
        try {
            User user = ShopJoyApp.getCurrentUser();
            if (user == null)
                return;
            var reviews = reviewService.getUserReviews(user.getUserId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Reviews");
            dialog.initOwner(myReviewsButton.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox box = new VBox(15);
            box.setPadding(new Insets(15));
            box.setPrefWidth(450);

            if (reviews.isEmpty()) {
                box.getChildren().add(new Label("You haven't written any reviews yet."));
            } else {
                for (Review r : reviews) {
                    Product p = productService.getProductById(r.getProductId());
                    String pName = p != null ? p.getProductName() : "Product #" + r.getProductId();

                    VBox card = new VBox(8);
                    card.getStyleClass().add("product-card");
                    card.setPrefWidth(400);
                    card.setMaxWidth(400);

                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);
                    Label nameLbl = new Label(pName);
                    nameLbl.getStyleClass().add("info-label");
                    Label ratingLbl = new Label(r.getRating() + "⭐");
                    ratingLbl.getStyleClass().add("product-rating");
                    Region s = new Region();
                    HBox.setHgrow(s, Priority.ALWAYS);
                    header.getChildren().addAll(nameLbl, s, ratingLbl);

                    VBox body = new VBox(5);
                    if (r.getTitle() != null && !r.getTitle().isEmpty()) {
                        Label t = new Label(r.getTitle());
                        t.setStyle("-fx-font-weight: bold; -fx-text-fill: -primary-color;");
                        body.getChildren().add(t);
                    }
                    Label comment = new Label(r.getComment());
                    comment.setWrapText(true);
                    body.getChildren().add(comment);

                    card.getChildren().addAll(header, new Separator(), body);
                    box.getChildren().add(card);
                }
            }
            ScrollPane sp = new ScrollPane(box);
            sp.setFitToWidth(true);
            sp.setPrefHeight(400);
            sp.getStyleClass().add("content-scroll");
            dialog.getDialogPane().setContent(sp);
            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("My reviews failed: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load reviews.");
        }
    }

    @FXML
    public void handleMyProfile(ActionEvent event) {
        try {
            User user = ShopJoyApp.getCurrentUser();
            if (user == null)
                return;

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Update My Profile");
            dialog.initOwner(myProfileButton.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");

            ButtonType saveType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CLOSE);

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new Insets(25));
            grid.setPrefWidth(450);

            TextField firstNameField = new TextField(user.getFirstName());
            TextField lastNameField = new TextField(user.getLastName());
            TextField emailField = new TextField(user.getEmail());
            TextField phoneField = new TextField(user.getPhone());

            Label userLbl = new Label("Username:");
            userLbl.getStyleClass().add("info-label");
            grid.add(userLbl, 0, 0);
            Label nameVal = new Label(user.getUsername());
            nameVal.setStyle("-fx-font-weight: bold;");
            grid.add(nameVal, 1, 0);

            Label fnLbl = new Label("First Name:");
            fnLbl.getStyleClass().add("info-label");
            grid.add(fnLbl, 0, 1);
            grid.add(firstNameField, 1, 1);

            Label lnLbl = new Label("Last Name:");
            lnLbl.getStyleClass().add("info-label");
            grid.add(lnLbl, 0, 2);
            grid.add(lastNameField, 1, 2);

            Label emLbl = new Label("Email Address:");
            emLbl.getStyleClass().add("info-label");
            grid.add(emLbl, 0, 3);
            grid.add(emailField, 1, 3);

            Label phLbl = new Label("Phone Number:");
            phLbl.getStyleClass().add("info-label");
            grid.add(phLbl, 0, 4);
            grid.add(phoneField, 1, 4);

            dialog.getDialogPane().setContent(grid);

            // FIX: Ensure first form field gets focus when dialog becomes visible
            dialog.setOnShown(e -> firstNameField.requestFocus());

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveType) {
                user.setFirstName(firstNameField.getText());
                user.setLastName(lastNameField.getText());
                user.setEmail(emailField.getText());
                user.setPhone(phoneField.getText());

                if (userService.updateUserProfile(user)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated.");
                    customerWelcomeLabel.setText("Welcome, " + user.getFirstName() + "!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile.");
                }
            }
        } catch (Exception e) {
            System.err.println("Profile dialog failed: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open profile.");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        ShopJoyApp.logout(stage);
    }

    private void handleViewProductDetails(Product product) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(product.getProductName());
        dialog.initOwner(productsFlowPane.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(500);

        Label name = new Label(product.getProductName());
        name.getStyleClass().add("section-title");

        Label price = new Label(CURRENCY.format(product.getPrice()));
        price.getStyleClass().add("product-price");

        HBox meta = new HBox(20);
        meta.getChildren().addAll(new Label("SKU: " + product.getSku()), new Label("Brand: " + product.getBrand()));

        Label desc = new Label(
                product.getDescription() != null ? product.getDescription() : "No description available.");
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-secondary;");

        Button addBtn = new Button("🛒 Add to Store Cart");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(evt -> handleAddToCart(product));

        box.getChildren().addAll(name, price, meta, new Separator(), desc, new Separator(), addBtn);

        // Product Reviews
        VBox reviewsSection = new VBox(10);
        Label reviewsHeader = new Label("Product Reviews");
        reviewsHeader.getStyleClass().add("dialog-title");
        reviewsSection.getChildren().add(reviewsHeader);

        List<Review> reviews = reviewService.getProductReviews(product.getProductId());
        if (reviews.isEmpty()) {
            reviewsSection.getChildren().add(new Label("No reviews yet. Be the first to review!"));
        } else {
            VBox reviewsBox = new VBox(10);
            for (Review r : reviews) {
                VBox rCard = new VBox(5);
                rCard.setStyle(
                        "-fx-background-color: #fefefe; -fx-padding: 10; -fx-border-color: #eee; -fx-border-radius: 5;");
                Label rTitle = new Label(
                        (r.getTitle() == null || r.getTitle().isEmpty() ? "Detailed Rating" : r.getTitle())
                                + " " + r.getRating() + "⭐");
                rTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -primary-color;");
                Label rComment = new Label(r.getComment());
                rComment.setWrapText(true);
                rCard.getChildren().addAll(rTitle, rComment);
                reviewsBox.getChildren().add(rCard);
            }
            ScrollPane sp = new ScrollPane(reviewsBox);
            sp.setFitToWidth(true);
            sp.setPrefHeight(150);
            sp.setStyle("-fx-background-color: transparent;");
            reviewsSection.getChildren().add(sp);
        }

        // Check if user can review
        User user = ShopJoyApp.getCurrentUser();
        if (user != null && !reviewService.hasUserReviewedProduct(user.getUserId(), product.getProductId())) {
            Button writeReviewBtn = new Button("Write a Review");
            writeReviewBtn.getStyleClass().add("secondary-button");
            writeReviewBtn.setOnAction(e -> handleWriteReview(product));
            reviewsSection.getChildren().add(writeReviewBtn);
        }

        box.getChildren().add(reviewsSection);
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
