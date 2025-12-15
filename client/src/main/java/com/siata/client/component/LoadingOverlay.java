package com.siata.client.component;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Reusable loading overlay component.
 * Shows a semi-transparent background with a spinner in the center.
 * Use this to prevent UI freeze and keep Windows cursor normal during heavy operations.
 * 
 * Usage:
 * 1. Add LoadingOverlay to your StackPane-based view
 * 2. Call show() before heavy operation
 * 3. Run operation in background Task<>
 * 4. Call hide() after operation completes
 */
public class LoadingOverlay extends StackPane {
    
    private final ProgressIndicator spinner;
    private final Label statusLabel;
    private final VBox content;
    
    public LoadingOverlay() {
        this("Memuat...");
    }
    
    public LoadingOverlay(String defaultMessage) {
        // Semi-transparent dark background
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        setAlignment(Pos.CENTER);
        
        // Content container
        content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4);");
        content.setMaxWidth(200);
        content.setMaxHeight(150);
        
        // Spinner
        spinner = new ProgressIndicator();
        spinner.setMaxSize(50, 50);
        spinner.setStyle("-fx-accent: #6366f1;");
        
        // Status label
        statusLabel = new Label(defaultMessage);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 500;");
        
        content.getChildren().addAll(spinner, statusLabel);
        getChildren().add(content);
        
        // Initially hidden
        setVisible(false);
        setMouseTransparent(true);
    }
    
    /**
     * Show the loading overlay with fade-in animation.
     */
    public void show() {
        show("Memuat...");
    }
    
    /**
     * Show the loading overlay with custom message.
     */
    public void show(String message) {
        statusLabel.setText(message);
        setVisible(true);
        setMouseTransparent(false);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    /**
     * Hide the loading overlay with fade-out animation.
     */
    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            setVisible(false);
            setMouseTransparent(true);
        });
        fadeOut.play();
    }
    
    /**
     * Update the status message while loading.
     */
    public void setMessage(String message) {
        statusLabel.setText(message);
    }
}
