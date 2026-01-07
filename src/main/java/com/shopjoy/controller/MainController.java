package com.shopjoy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        messageLabel.setText("Welcome to ShopJoy");
    }

    @FXML
    public void onLogin() {
        messageLabel.setText("Login clicked (stub)");
    }
}
