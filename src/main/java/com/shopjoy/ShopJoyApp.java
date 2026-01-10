package com.shopjoy;

import com.shopjoy.model.User;
import com.shopjoy.model.UserType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ShopJoyApp extends Application {
    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static boolean isAdmin() { return currentUser != null && currentUser.getUserType() == UserType.ADMIN; }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ShopJoy - E-Commerce Management System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        try {
            // Load login scene by default
            switchScene(primaryStage, "login.fxml", "ShopJoy - Login");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load initial scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchScene(Stage stage, String fxmlFile, String title) throws IOException {
        if (stage == null) throw new IllegalArgumentException("stage is null");
        FXMLLoader loader = new FXMLLoader(ShopJoyApp.class.getResource("/fxml/" + fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        if (title != null) stage.setTitle(title);
    }

    public static void logout(Stage stage) throws IOException {
        setCurrentUser(null);
        switchScene(stage, "login.fxml", "ShopJoy - Login");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Application closed");
        super.stop();
    }
}
