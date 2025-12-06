package com.siata.client;

import com.siata.client.view.LoginView;
import com.siata.client.view.MainShellView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        Font.loadFont(getClass().getResourceAsStream("/Poppins-Regular.ttf"),12);
        primaryStage.setTitle("SIADA - Direktorat Angkutan Udara");
        
        // Set application icon
        try {
            Image appIcon = new Image(getClass().getResourceAsStream("/app_icon.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Could not load app icon: " + e.getMessage());
        }
        
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

