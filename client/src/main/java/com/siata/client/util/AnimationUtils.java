package com.siata.client.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

/**
 * Utility class for modern JavaFX animations.
 * Provides reusable animation effects for UI components.
 */
public class AnimationUtils {

    // Default durations
    public static final Duration FAST = Duration.millis(150);
    public static final Duration NORMAL = Duration.millis(250);
    public static final Duration SLOW = Duration.millis(400);

    /**
     * Fade in animation
     */
    public static void fadeIn(Node node, Duration duration, Duration delay) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(delay);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    public static void fadeIn(Node node, Duration duration) {
        fadeIn(node, duration, Duration.ZERO);
    }

    public static void fadeIn(Node node) {
        fadeIn(node, NORMAL, Duration.ZERO);
    }

    /**
     * Fade out animation
     */
    public static void fadeOut(Node node, Duration duration, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(node.getOpacity());
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_IN);
        if (onFinished != null) {
            ft.setOnFinished(e -> onFinished.run());
        }
        ft.play();
    }

    /**
     * Slide in from left
     */
    public static void slideInFromLeft(Node node, Duration duration) {
        node.setTranslateX(-50);
        node.setOpacity(0);
        
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setFromX(-50);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }

    /**
     * Slide in from right
     */
    public static void slideInFromRight(Node node, Duration duration) {
        node.setTranslateX(50);
        node.setOpacity(0);
        
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setFromX(50);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }

    /**
     * Slide in from bottom with fade
     */
    public static void slideInFromBottom(Node node, Duration duration, Duration delay) {
        node.setTranslateY(30);
        node.setOpacity(0);
        
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setFromY(30);
        tt.setToY(0);
        tt.setDelay(delay);
        tt.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(delay);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }

    /**
     * Scale animation (zoom in)
     */
    public static void scaleIn(Node node, Duration duration) {
        node.setScaleX(0.9);
        node.setScaleY(0.9);
        node.setOpacity(0);
        
        ScaleTransition st = new ScaleTransition(duration, node);
        st.setFromX(0.9);
        st.setFromY(0.9);
        st.setToX(1);
        st.setToY(1);
        st.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.play();
    }

    /**
     * Add hover scale effect to a node
     */
    public static void addHoverScaleEffect(Node node, double scaleFactor) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(FAST, node);
            st.setToX(scaleFactor);
            st.setToY(scaleFactor);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(FAST, node);
            st.setToX(1);
            st.setToY(1);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
    }

    /**
     * Add hover lift effect (scale + shadow)
     */
    public static void addHoverLiftEffect(Node node) {
        DropShadow normalShadow = new DropShadow();
        normalShadow.setRadius(8);
        normalShadow.setOffsetY(2);
        normalShadow.setColor(Color.rgb(0, 0, 0, 0.08));
        
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setRadius(16);
        hoverShadow.setOffsetY(6);
        hoverShadow.setColor(Color.rgb(0, 0, 0, 0.12));
        
        node.setEffect(normalShadow);
        
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(FAST, node);
            st.setToX(1.02);
            st.setToY(1.02);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
            node.setEffect(hoverShadow);
        });
        
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(FAST, node);
            st.setToX(1);
            st.setToY(1);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
            node.setEffect(normalShadow);
        });
    }

    /**
     * Stagger fade in for multiple nodes
     */
    public static void staggerFadeIn(List<Node> nodes, Duration staggerDelay) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Duration delay = staggerDelay.multiply(i);
            fadeIn(node, NORMAL, delay);
        }
    }

    /**
     * Stagger slide in from bottom for multiple nodes
     */
    public static void staggerSlideInFromBottom(List<Node> nodes, Duration staggerDelay) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Duration delay = staggerDelay.multiply(i);
            slideInFromBottom(node, NORMAL, delay);
        }
    }

    /**
     * Pulse animation for attention
     */
    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    /**
     * Shake animation for error feedback
     */
    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    /**
     * Button click feedback animation
     */
    public static void buttonClickFeedback(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(80), node);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    /**
     * Slide sidebar animation
     */
    public static void slideSidebar(Node sidebar, boolean show, double width) {
        TranslateTransition tt = new TranslateTransition(NORMAL, sidebar);
        if (show) {
            sidebar.setVisible(true);
            tt.setFromX(-width);
            tt.setToX(0);
        } else {
            tt.setFromX(0);
            tt.setToX(-width);
            tt.setOnFinished(e -> sidebar.setVisible(false));
        }
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    /**
     * Page transition - fade and slide
     */
    public static void pageTransitionIn(Node page) {
        page.setOpacity(0);
        page.setTranslateY(20);
        
        FadeTransition ft = new FadeTransition(NORMAL, page);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        TranslateTransition tt = new TranslateTransition(NORMAL, page);
        tt.setFromY(20);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.play();
    }

    /**
     * Modal open animation
     */
    public static void modalOpen(Node modal) {
        modal.setOpacity(0);
        // Scale transition removed for performance
        // modal.setScaleX(0.95);
        // modal.setScaleY(0.95);
        
        FadeTransition ft = new FadeTransition(Duration.millis(120), modal);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Prepare modal for animation - call this BEFORE stage.show() or stage.showAndWait()
     * This sets up the initial invisible/scaled state so animation happens smoothly.
     */
    public static void prepareModalForAnimation(Node modal) {
        modal.setOpacity(0);
        // Scale removed
    }

    /**
     * Show modal with smooth animation using the Stage's onShown event.
     * Call this instead of modalOpen() for proper timing.
     * Also automatically manages the background overlay and transparent styling.
     * @param stage The modal Stage
     * @param modalContent The modal content node to animate
     */
    public static void setupModalAnimation(javafx.stage.Stage stage, Node modalContent) {
        // Set initial state - invisible and slightly scaled down
        // Set initial state - invisible
        modalContent.setOpacity(0);
        // Scale removed
        
        // Setup overlay management
        com.siata.client.util.ModalOverlayManager.setupModalOverlay(stage);
        
        // When stage is shown, configure scene transparency and play animation
        stage.setOnShown(event -> {
            // Set scene fill to transparent for rounded corners to show properly
            if (stage.getScene() != null) {
                stage.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            }
            
            FadeTransition ft = new FadeTransition(Duration.millis(120), modalContent);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
    }

    /**
     * Create a slow floating animation for decorative elements.
     * The element will gently drift up and down.
     * @param node The node to animate
     * @param amplitude Maximum vertical movement in pixels
     * @param durationSeconds Duration of one full cycle
     */
    public static Timeline createFloatingAnimation(Node node, double amplitude, double durationSeconds) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.translateYProperty(), -amplitude / 2, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds / 2),
                new KeyValue(node.translateYProperty(), amplitude / 2, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds),
                new KeyValue(node.translateYProperty(), -amplitude / 2, Interpolator.EASE_BOTH))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    /**
     * Create a slow horizontal drifting animation.
     * @param node The node to animate
     * @param amplitude Maximum horizontal movement in pixels
     * @param durationSeconds Duration of one full cycle
     */
    public static Timeline createDriftingAnimation(Node node, double amplitude, double durationSeconds) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.translateXProperty(), -amplitude / 2, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds / 2),
                new KeyValue(node.translateXProperty(), amplitude / 2, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds),
                new KeyValue(node.translateXProperty(), -amplitude / 2, Interpolator.EASE_BOTH))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    /**
     * Create a slow rotation animation for decorative elements.
     * @param node The node to animate
     * @param degrees Rotation amount in degrees (can be negative for reverse)
     * @param durationSeconds Duration of one full cycle
     */
    public static Timeline createRotatingAnimation(Node node, double degrees, double durationSeconds) {
        double startRotation = node.getRotate();
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.rotateProperty(), startRotation, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds / 2),
                new KeyValue(node.rotateProperty(), startRotation + degrees, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds),
                new KeyValue(node.rotateProperty(), startRotation, Interpolator.EASE_BOTH))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    /**
     * Create a gentle pulsing opacity animation.
     * @param node The node to animate
     * @param minOpacity Minimum opacity
     * @param maxOpacity Maximum opacity
     * @param durationSeconds Duration of one full cycle
     */
    public static Timeline createPulsingOpacityAnimation(Node node, double minOpacity, double maxOpacity, double durationSeconds) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.opacityProperty(), minOpacity, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds / 2),
                new KeyValue(node.opacityProperty(), maxOpacity, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(durationSeconds),
                new KeyValue(node.opacityProperty(), minOpacity, Interpolator.EASE_BOTH))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }
}
