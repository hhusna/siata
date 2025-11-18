package com.siata.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private Optional<Runnable> onLogin = Optional.empty();

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> onLogin.ifPresent(Runnable::run));
    }

    public void setOnLogin(Runnable runnable) {
        this.onLogin = Optional.ofNullable(runnable);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }
}
