package com.siata.client;

import com.siata.client.controller.MainShellController;
import com.siata.client.controller.LoginController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
//        LoginView loginView = new LoginView();
//        loginView.setOnLogin(() -> showDashboard(stage));
//        Scene scene = createScene(loginView,
//                stage.getScene() != null ? stage.getScene().getWidth() : 1280,
//                stage.getScene() != null ? stage.getScene().getHeight() : 800);
//        stage.setScene(scene);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.siata.client/controller/LoginView.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setOnLogin(() -> showDashboard(stage));

            Scene scene = createScene(root,
                    stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                    stage.getScene() != null ? stage.getScene().getHeight() : 800);

            stage.setScene(scene);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showDashboard(Stage stage) {
//        MainShellView shellView = new MainShellView();
//        shellView.setOnLogout(() -> showLogin(stage));
//        Scene scene = createScene(shellView,
//                stage.getScene() != null ? stage.getScene().getWidth() : 1280,
//                stage.getScene() != null ? stage.getScene().getHeight() : 800);
//        stage.setScene(scene);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.siata.client/controller/MainShellView.fxml"));
            Parent root = loader.load();
            MainShellController controller = loader.getController();
            controller.setOnLogout(() -> showLogin(stage));
            Scene scene = createScene(root,
                    stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                    stage.getScene() != null ? stage.getScene().getHeight() : 800);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

