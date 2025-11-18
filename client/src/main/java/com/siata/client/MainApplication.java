package com.siata.client;

import com.siata.client.view.LoginView;
import com.siata.client.view.MainShellView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SIAD - Direktorat Angkutan Udara");
        showLogin(primaryStage);
        primaryStage.show();
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView();
        loginView.setOnLogin(() -> showDashboard(stage));
        Scene scene = createScene(loginView,
                stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                stage.getScene() != null ? stage.getScene().getHeight() : 800);
        stage.setScene(scene);
    }

    private void showDashboard(Stage stage) {
        MainShellView shellView = new MainShellView();
        shellView.setOnLogout(() -> showLogin(stage));
        Scene scene = createScene(shellView,
                stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                stage.getScene() != null ? stage.getScene().getHeight() : 800);
        stage.setScene(scene);
    }

    private Scene createScene(javafx.scene.Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        String stylesheet = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

