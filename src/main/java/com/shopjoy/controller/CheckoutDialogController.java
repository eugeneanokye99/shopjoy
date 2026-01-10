package com.shopjoy.controller;

import com.shopjoy.service.CartService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutDialogController {

    @FXML
    private Label headerLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField paymentField;

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

    public void setOrderData(int userId, double total) {
        headerLabel.setText("Order Summary: Total " + CURRENCY.format(total));
    }

    public String getAddress() {
        return addressField.getText() != null ? addressField.getText().trim() : "";
    }

    public String getPaymentMethod() {
        return paymentField.getText() != null ? paymentField.getText().trim() : "";
    }

    public void requestInitialFocus() {
        // Use Platform.runLater to ensure focus is set after dialog is fully rendered
        Platform.runLater(() -> {
            if (addressField != null) {
                addressField.requestFocus();
            }
        });
    }
}
