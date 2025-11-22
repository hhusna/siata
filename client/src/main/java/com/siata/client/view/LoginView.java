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


        loginButton.setOnAction(event -> {
            // Validasi input tidak boleh kosong
            if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
                System.err.println("Username tidak boleh kosong");
                return;
            }
            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                System.err.println("Password tidak boleh kosong");
                return;
            }
            
            try {
                userApi.login(usernameField.getText(), passwordField.getText());
                LoginSession.setPegawaiDto(userApi.getPegawaionSession());
                System.out.println("JWT DI LOGIN VIEW" + LoginSession.getJwt());

                if (LoginSession.getJwt() != null) {
                    System.out.println("SUKSES LOGIN HARUSNYA??");
                    onLogin.ifPresent(Runnable::run);
                } else {
                    System.out.println("GAGAL LOGIN HARUSNYA??");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        VBox form = new VBox(12,
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
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

