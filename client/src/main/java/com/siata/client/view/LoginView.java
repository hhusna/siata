package com.siata.client.view;

import com.siata.client.MainApplication;
import com.siata.client.api.UserApi;
import com.siata.client.session.LoginSession;
import com.siata.client.util.AnimationUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;

import java.util.Optional;
import java.util.Random;
import java.util.prefs.Preferences;

public class LoginView extends HBox {
    private Optional<Runnable> onLogin = Optional.empty();
    UserApi userApi = new UserApi();

    public LoginView() {
        buildView();
    }

    private void buildView() {
        // === LEFT PANEL - Dark blue with logo and decorations ===
        StackPane leftPanel = createLeftPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        // === RIGHT PANEL - Login form ===
        VBox rightPanel = createRightPanel();
        rightPanel.setMinWidth(420);
        rightPanel.setMaxWidth(480);
        
        getChildren().addAll(leftPanel, rightPanel);
        setStyle("-fx-background-color: white;");
    }
    
    private StackPane createLeftPanel() {
        StackPane panel = new StackPane();
        
        // Dark blue gradient background (matching SIADA logo colors)
        Region gradient = new Region();
        gradient.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #0a1628, #0f2847, #0a1628);"
        );
        gradient.prefWidthProperty().bind(panel.widthProperty());
        gradient.prefHeightProperty().bind(panel.heightProperty());
        
        // Decorative shapes container
        Pane decorations = new Pane();
        decorations.setMouseTransparent(true);
        
        // Add geometric decorations with blue tones
        Random rand = new Random(42);
        
        // Diagonal lines/rectangles (like building shapes)
        for (int i = 0; i < 8; i++) {
            Rectangle rect = new Rectangle();
            rect.setWidth(rand.nextInt(120) + 60);
            rect.setHeight(6 + rand.nextInt(6));
            rect.setRotate(-45 + rand.nextInt(30));
            rect.setFill(Color.rgb(59, 130, 246, 0.08 + rand.nextDouble() * 0.1)); // Blue tint
            rect.setArcWidth(15);
            rect.setArcHeight(15);
            
            rect.layoutXProperty().bind(panel.widthProperty().multiply(rand.nextDouble()));
            rect.layoutYProperty().bind(panel.heightProperty().multiply(rand.nextDouble()));
            
            decorations.getChildren().add(rect);
        }
        
        // Add some cyan/teal accent rectangles
        for (int i = 0; i < 5; i++) {
            Rectangle rect = new Rectangle();
            rect.setWidth(rand.nextInt(80) + 40);
            rect.setHeight(4 + rand.nextInt(4));
            rect.setRotate(-30 + rand.nextInt(60));
            rect.setFill(Color.rgb(34, 211, 238, 0.1 + rand.nextDouble() * 0.08)); // Cyan tint
            rect.setArcWidth(10);
            rect.setArcHeight(10);
            
            rect.layoutXProperty().bind(panel.widthProperty().multiply(rand.nextDouble()));
            rect.layoutYProperty().bind(panel.heightProperty().multiply(rand.nextDouble()));
            
            decorations.getChildren().add(rect);
        }
        
        // Add subtle circles
        for (int i = 0; i < 4; i++) {
            Circle circle = new Circle(rand.nextInt(40) + 20);
            circle.setFill(Color.rgb(59, 130, 246, 0.05 + rand.nextDouble() * 0.05));
            circle.centerXProperty().bind(panel.widthProperty().multiply(rand.nextDouble()));
            circle.centerYProperty().bind(panel.heightProperty().multiply(rand.nextDouble()));
            decorations.getChildren().add(circle);
        }
        
        // Content on left panel
        VBox content = new VBox(24);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60));
        
        // Logo image
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/siada_logo.jpg"));
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitWidth(280);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            content.getChildren().add(logoView);
        } catch (Exception e) {
            // Fallback if logo not found
            Label logoText = new Label("SIADA");
            logoText.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: 700;");
            content.getChildren().add(logoText);
        }
        
        // Subtitle
        Label subtitle = new Label("Sistem Informasi Administrasi\nDistribusi Aset Pegawai");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.85); -fx-font-weight: 500; -fx-text-alignment: center;");
        subtitle.setAlignment(Pos.CENTER);
        
        Label department = new Label("Direktorat Angkutan Udara\nKementerian Perhubungan RI");
        department.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.6); -fx-text-alignment: center;");
        department.setAlignment(Pos.CENTER);
        department.setPadding(new Insets(10, 0, 0, 0));
        
        content.getChildren().addAll(subtitle, department);
        StackPane.setAlignment(content, Pos.CENTER);
        
        panel.getChildren().addAll(gradient, decorations, content);
        return panel;
    }
    
    private VBox createRightPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60));
        panel.setStyle("-fx-background-color: white;");
        
        // Form container
        VBox formContainer = new VBox(24);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(320);
        
        // User icon circle (blue theme)
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(40);
        iconBg.setFill(Color.web("#eff6ff"));
        iconBg.setStroke(Color.web("#bfdbfe"));
        iconBg.setStrokeWidth(2);
        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 32px;");
        iconContainer.getChildren().addAll(iconBg, userIcon);
        
        // Title
        Label formTitle = new Label("USER LOGIN");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e3a5f; -fx-letter-spacing: 2px;");
        
        VBox headerSection = new VBox(16, iconContainer, formTitle);
        headerSection.setAlignment(Pos.CENTER);
        
        // Form fields
        VBox formFields = new VBox(16);
        formFields.setAlignment(Pos.CENTER);
        
        // Username field with icon
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        HBox usernameBox = createInputFieldWithNode("👤", usernameField);
        
        // Password field with icon
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        HBox passwordBox = createInputFieldWithNode("🔒", passwordField);
        
        // Status label
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        
        // Login button (dark blue theme)
        Button loginButton = new Button("LOGIN");
        loginButton.setStyle(getButtonStyle(false));
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(getButtonStyle(true)));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(getButtonStyle(false)));
        
        // Login action
        loginButton.setOnAction(event -> handleLogin(usernameField, passwordField, loginButton, statusLabel));
        
        // Enter key on password field
        passwordField.setOnAction(event -> handleLogin(usernameField, passwordField, loginButton, statusLabel));
        
        formFields.getChildren().addAll(usernameBox, passwordBox, statusLabel, loginButton);
        formFields.setPadding(new Insets(10, 0, 0, 0));
        
        formContainer.getChildren().addAll(headerSection, formFields);
        
        panel.getChildren().add(formContainer);
        
        // Animation
        Platform.runLater(() -> AnimationUtils.fadeIn(formContainer, AnimationUtils.NORMAL));
        
        return panel;
    }
    
    private String getButtonStyle(boolean hovered) {
        if (hovered) {
            return "-fx-background-color: linear-gradient(to right, #0f2847, #1e40af);" +
                   "-fx-text-fill: white;" +
                   "-fx-font-size: 14px;" +
                   "-fx-font-weight: 600;" +
                   "-fx-padding: 14 60;" +
                   "-fx-background-radius: 25;" +
                   "-fx-cursor: hand;" +
                   "-fx-letter-spacing: 1px;" +
                   "-fx-effect: dropshadow(gaussian, rgba(15, 40, 71, 0.5), 15, 0, 0, 5);";
        }
        return "-fx-background-color: linear-gradient(to right, #1e3a5f, #2563eb);" +
               "-fx-text-fill: white;" +
               "-fx-font-size: 14px;" +
               "-fx-font-weight: 600;" +
               "-fx-padding: 14 60;" +
               "-fx-background-radius: 25;" +
               "-fx-cursor: hand;" +
               "-fx-letter-spacing: 1px;";
    }
    
    private HBox createInputFieldWithNode(String icon, javafx.scene.control.Control field) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
            "-fx-background-color: #f8fafc;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;"
        );
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
        
        if (field instanceof TextField tf) {
            tf.setStyle(getInputStyle());
            HBox.setHgrow(tf, Priority.ALWAYS);
        } else if (field instanceof PasswordField pf) {
            pf.setStyle(getInputStyle());
            HBox.setHgrow(pf, Priority.ALWAYS);
        }
        
        box.getChildren().addAll(iconLabel, field);
        box.setPrefHeight(48);
        return box;
    }
    
    private String getInputStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-border-width: 0;" +
               "-fx-font-size: 14px;" +
               "-fx-text-fill: #1e293b;" +
               "-fx-prompt-text-fill: #94a3b8;";
    }
    
    private void handleLogin(TextField usernameField, PasswordField passwordField, Button loginButton, Label statusLabel) {
        // Validasi input tidak boleh kosong
        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            statusLabel.setText("Username tidak boleh kosong");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            return;
        }
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            statusLabel.setText("Password tidak boleh kosong");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            return;
        }
        
        // Disable button dan tampilkan loading
        loginButton.setDisable(true);
        loginButton.setText("LOADING...");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        
        // Jalankan login di background thread
        javafx.concurrent.Task<Boolean> loginTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    userApi.login(usernameField.getText(), passwordField.getText());
                    LoginSession.setPegawaiDto(userApi.getPegawaionSession());
                    return LoginSession.getJwt() != null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
        
        loginTask.setOnSucceeded(e -> {
            loginButton.setDisable(false);
            loginButton.setText("LOGIN");
            
            if (loginTask.getValue()) {
                System.out.println("Login berhasil!");
                onLogin.ifPresent(Runnable::run);
            } else {
                statusLabel.setText("Login gagal. Periksa username dan password.");
                statusLabel.setVisible(true);
                statusLabel.setManaged(true);
            }
        });
        
        loginTask.setOnFailed(e -> {
            loginButton.setDisable(false);
            loginButton.setText("LOGIN");
            statusLabel.setText("Terjadi kesalahan. Coba lagi.");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        });
        
        new Thread(loginTask).start();
    }

    public void setOnLogin(Runnable onLogin) {
        this.onLogin = Optional.ofNullable(onLogin);
    }
}
