package com.siata.client.util;

import com.siata.client.MainApplication;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Utility class to manage modal overlay effects.
 * Shows a semi-transparent dark overlay behind modals to focus user attention.
 */
public class ModalOverlayManager {

    // Reference to the main application stage
    private static Stage mainStage;
    private static StackPane overlayPane;
    private static Region overlayBackground;
    private static int overlayCount = 0; // Track nested modals

    /**
     * Initialize the overlay manager with the main application stage.
     * Call this once when the application starts.
     */
    public static void initialize(Stage stage) {
        mainStage = stage;
    }

    /**
     * Show the overlay on the main application window.
     * The overlay will animate in with a fade effect.
     * Call this before showing a modal.
     */
    public static void showOverlay() {
        if (mainStage == null || mainStage.getScene() == null) {
            return;
        }

        overlayCount++;
        if (overlayCount > 1) {
            // Overlay already showing (nested modal)
            return;
        }

        Scene scene = mainStage.getScene();
        
        // Get the current root and wrap it in a StackPane if not already wrapped
        if (!(scene.getRoot() instanceof StackPane) || overlayPane == null) {
            javafx.scene.Parent originalRoot = scene.getRoot();
            
            // Create overlay background
            overlayBackground = new Region();
            overlayBackground.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
            overlayBackground.setOpacity(0);
            overlayBackground.setMouseTransparent(true); // Don't block clicks initially
            
            // Wrap original content in StackPane with overlay on top
            overlayPane = new StackPane();
            overlayPane.getChildren().addAll(originalRoot, overlayBackground);
            
            // Apply rounded corners to the new root (overlayPane)
            MainApplication.applyWindowRoundedCorners(overlayPane, mainStage);
            
            // Listen for window resize/maximize to update clipping
            mainStage.maximizedProperty().addListener((obs, wasMaximized, isMaximized) -> {
                if (overlayPane != null) {
                    MainApplication.applyWindowRoundedCorners(overlayPane, mainStage);
                }
            });
            
            scene.setRoot(overlayPane);
        }

        // Show overlay with fade animation
        overlayBackground.setMouseTransparent(false);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlayBackground);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Hide the overlay on the main application window.
     * The overlay will animate out with a fade effect.
     * Call this after the modal is closed.
     */
    public static void hideOverlay() {
        if (overlayBackground == null) {
            overlayCount = 0;
            return;
        }

        overlayCount--;
        if (overlayCount > 0) {
            // Still have nested modals open
            return;
        }
        overlayCount = 0;

        // Hide overlay with fade animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlayBackground);
        fadeOut.setFromValue(overlayBackground.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> overlayBackground.setMouseTransparent(true));
        fadeOut.play();
    }

    /**
     * Setup a modal stage to automatically show/hide overlay.
     * Call this after creating the modal stage but before showing it.
     */
    public static void setupModalOverlay(Stage modalStage) {
        modalStage.setOnShowing(e -> showOverlay());
        modalStage.setOnHidden(e -> hideOverlay());
    }
}
