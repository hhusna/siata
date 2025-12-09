package com.siata.client;

import com.siata.client.component.CustomTitleBar;
import com.siata.client.util.ModalOverlayManager;
import com.siata.client.view.LoginView;
import com.siata.client.view.MainShellView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApplication extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Load Outfit font family (all weights for proper rendering)
        Font.loadFont(getClass().getResourceAsStream("/outfitfonts/static/Outfit-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/outfitfonts/static/Outfit-Medium.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/outfitfonts/static/Outfit-SemiBold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/outfitfonts/static/Outfit-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/outfitfonts/static/Outfit-Light.ttf"), 14);
        
        // Use TRANSPARENT stage for rounded corners
        stage.initStyle(StageStyle.TRANSPARENT);
        
        // Set application icon (still works for taskbar)
        try {
            Image appIcon = new Image(getClass().getResourceAsStream("/app_icon.png"));
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Could not load app icon: " + e.getMessage());
        }
        
        // Initialize modal overlay manager with primary stage
        ModalOverlayManager.initialize(stage);
        
        showLogin(stage);
        stage.show();
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView();
        loginView.setOnLogin(() -> showDashboard(stage));
        
        Scene scene = createScene(loginView,
                stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                stage.getScene() != null ? stage.getScene().getHeight() : 800,
                stage);
        stage.setScene(scene);
    }

    private void showDashboard(Stage stage) {
        MainShellView shellView = new MainShellView();
        shellView.setOnLogout(() -> showLogin(stage));
        
        Scene scene = createScene(shellView,
                stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                stage.getScene() != null ? stage.getScene().getHeight() : 800,
                stage);
        stage.setScene(scene);
    }

    private Scene createScene(javafx.scene.Parent root, double width, double height, Stage stage) {
        Scene scene = new Scene(root, width, height);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        String stylesheet = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        
        // Apply rounded corners to root when not maximized
        if (root instanceof javafx.scene.layout.Region region) {
            // Apply rounded corners via CSS
            applyWindowRoundedCorners(region, stage);
            
            // Listen for maximize state changes
            stage.maximizedProperty().addListener((obs, wasMaximized, isMaximized) -> {
                applyWindowRoundedCorners(region, stage);
            });
            
            CustomTitleBar.setupResizeHandlers(stage, region);
        }
        
        return scene;
    }
    
    public static void applyWindowRoundedCorners(javafx.scene.layout.Region region, Stage stage) {
        if (stage.isMaximized()) {
            region.setClip(null);
            region.setStyle(region.getStyle().replace("-fx-background-radius: 16;", ""));
        } else {
            // Create rounded rectangle clip
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
            clip.widthProperty().bind(region.widthProperty());
            clip.heightProperty().bind(region.heightProperty());
            clip.setArcWidth(32);
            clip.setArcHeight(32);
            region.setClip(clip);
        }
    }

    public static void main(String[] args) {
        // Enable better font rendering/anti-aliasing for Windows
        System.setProperty("prism.lcdtext", "false");  // Use grayscale instead of LCD
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.subpixeltext", "false");
        System.setProperty("prism.order", "d3d,sw");  // Prefer Direct3D rendering
        System.setProperty("prism.targetvram", "512m");
        System.setProperty("prism.vsync", "false");
        System.setProperty("glass.win.uiScale", "100%");  // Prevent DPI scaling issues
        launch(args);
    }
}


