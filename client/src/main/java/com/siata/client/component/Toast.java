package com.siata.client.component;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Toast extends HBox {
    
    public enum Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    public Toast(String title, String message, Type type) {
        getStyleClass().add("toast-notification");
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(12, 16, 12, 16));
        setMinWidth(300);
        setMaxWidth(350);
        
        // Icon
        Label icon = new Label();
        icon.setStyle("-fx-font-size: 20px;");
        
        String styleClass = "";
        String colorStyle = "";
        switch (type) {
            case SUCCESS:
                icon.setText("✓");
                styleClass = "toast-success";
                colorStyle = "-fx-text-fill: #16a34a;";
                break;
            case ERROR:
                icon.setText("✕");
                styleClass = "toast-error";
                colorStyle = "-fx-text-fill: #dc2626;";
                break;
            case WARNING:
                icon.setText("⚠");
                styleClass = "toast-warning";
                colorStyle = "-fx-text-fill: #d97706;";
                break;
            case INFO:
            default:
                icon.setText("ℹ");
                styleClass = "toast-info";
                colorStyle = "-fx-text-fill: #2563eb;";
                break;
        }
        
        getStyleClass().add(styleClass);
        icon.setStyle(icon.getStyle() + colorStyle);
        
        // Text Content
        VBox content = new VBox(2);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        content.getChildren().addAll(titleLabel, messageLabel);
        
        getChildren().addAll(icon, content);
        
        // Add drop shadow effect inline since we might not have CSS for it yet
        setStyle(getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-background-radius: 8; -fx-background-color: white;");
    }
}
