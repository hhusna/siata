package com.siata.client.view;

import com.siata.client.MainApplication;
import com.siata.client.api.UserApi;
import com.siata.client.session.LoginSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.prefs.Preferences;

public class LoginView extends StackPane {
    private Optional<Runnable> onLogin = Optional.empty();
    UserApi userApi = new UserApi();

    public LoginView() {
        buildView();
    }

    private void buildView() {
        Region gradientBackground = new Region();
        gradientBackground.getStyleClass().add("login-background");
        gradientBackground.prefWidthProperty().bind(widthProperty());
        gradientBackground.prefHeightProperty().bind(heightProperty());

        VBox card = new VBox(20);
        card.setPadding(new Insets(32, 36, 36, 36));
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(420);
        card.setMinWidth(360);

        Label iconCircle = new Label("✈");
        iconCircle.getStyleClass().add("login-icon");

        Label title = new Label("Sistem Informasi Administrasi");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Distribusi Aset Pegawai");
        subtitle.getStyleClass().add("login-subtitle");

        Label department = new Label("Direktorat Angkutan Udara - Kementerian Perhubungan");
        department.getStyleClass().add("login-department");

        VBox textSection = new VBox(4, subtitle, department);
        textSection.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("form-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Masukkan username Anda");

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("form-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password Anda");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("form-label");
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
        statusLabel.setVisible(false);

        loginButton.setOnAction(event -> {
            // Validasi input tidak boleh kosong
            if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
                statusLabel.setText("Username tidak boleh kosong");
                statusLabel.setVisible(true);
                return;
            }
            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                statusLabel.setText("Password tidak boleh kosong");
                statusLabel.setVisible(true);
                return;
            }
            
            // Disable button dan tampilkan loading
            loginButton.setDisable(true);
            loginButton.setText("Loading...");
            statusLabel.setVisible(false);
            
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
                loginButton.setText("Login");
                
                if (loginTask.getValue()) {
                    System.out.println("Login berhasil!");
                    onLogin.ifPresent(Runnable::run);
                } else {
                    statusLabel.setText("Login gagal. Periksa username dan password.");
                    statusLabel.setVisible(true);
                }
            });
            
            loginTask.setOnFailed(e -> {
                loginButton.setDisable(false);
                loginButton.setText("Login");
                statusLabel.setText("Terjadi kesalahan. Coba lagi.");
                statusLabel.setVisible(true);
            });
            
            new Thread(loginTask).start();
        });


        VBox form = new VBox(12,
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                statusLabel,
                loginButton
        );
        form.setPrefWidth(360);
        form.setFillWidth(true);

        card.getChildren().addAll(iconCircle, title, textSection, form);

        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().addAll(gradientBackground, card);
    }

    public void setOnLogin(Runnable onLogin) {
        this.onLogin = Optional.ofNullable(onLogin);
    }
}

