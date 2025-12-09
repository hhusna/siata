package com.siata.client.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Custom Title Bar component for modern window appearance with resize support
 */
public class CustomTitleBar extends HBox {
    
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean maximized = false;
    private double prevX, prevY, prevWidth, prevHeight;
    
    private static final int RESIZE_MARGIN = 6;
    
    public CustomTitleBar(Stage stage, String title) {
        super(8);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 12, 8, 12));
        getStyleClass().add("custom-title-bar");
        setMinHeight(40);
        setPrefHeight(40);
        setMaxHeight(40);
        
        // App Icon
        ImageView appIcon = new ImageView();
        try {
            Image icon = new Image(getClass().getResourceAsStream("/app_icon.png"));
            appIcon.setImage(icon);
            appIcon.setFitWidth(20);
            appIcon.setFitHeight(20);
            appIcon.setPreserveRatio(true);
        } catch (Exception e) {
            // Icon not found, skip
        }
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-bar-title");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Window control buttons
        Button minimizeBtn = createControlButton("—", "minimize-btn");
        minimizeBtn.setOnAction(e -> stage.setIconified(true));
        
        Button maximizeBtn = createControlButton("□", "maximize-btn");
        maximizeBtn.setOnAction(e -> toggleMaximize(stage, maximizeBtn));
        
        Button closeBtn = createControlButton("✕", "close-btn");
        closeBtn.setOnAction(e -> stage.close());
        
        HBox controlBox = new HBox(0);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
        
        getChildren().addAll(appIcon, titleLabel, spacer, controlBox);
        
        // Make title bar draggable
        setOnMousePressed(event -> {
            if (!maximized) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        
        setOnMouseDragged(event -> {
            if (!maximized) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        // Double-click to maximize/restore
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize(stage, maximizeBtn);
            }
        });
    }
    
    // Static fields for window state tracking when controls are in header
    private static boolean headerMaximized = false;
    private static double headerPrevX, headerPrevY, headerPrevWidth, headerPrevHeight;
    
    /**
     * Create window control buttons (minimize, maximize, close) that can be added to any container.
     * Use this when you want window controls in a custom header instead of the title bar.
     */
    public static HBox createWindowControls(Stage stage) {
        Button minimizeBtn = new Button("—");
        minimizeBtn.getStyleClass().addAll("title-bar-btn", "minimize-btn");
        minimizeBtn.setMinSize(40, 28);
        minimizeBtn.setPrefSize(40, 28);
        minimizeBtn.setMaxSize(40, 28);
        minimizeBtn.setOnAction(e -> stage.setIconified(true));
        
        Button maximizeBtn = new Button("□");
        maximizeBtn.getStyleClass().addAll("title-bar-btn", "maximize-btn");
        maximizeBtn.setMinSize(40, 28);
        maximizeBtn.setPrefSize(40, 28);
        maximizeBtn.setMaxSize(40, 28);
        maximizeBtn.setOnAction(e -> {
            if (headerMaximized) {
                stage.setX(headerPrevX);
                stage.setY(headerPrevY);
                stage.setWidth(headerPrevWidth);
                stage.setHeight(headerPrevHeight);
                maximizeBtn.setText("□");
                headerMaximized = false;
            } else {
                headerPrevX = stage.getX();
                headerPrevY = stage.getY();
                headerPrevWidth = stage.getWidth();
                headerPrevHeight = stage.getHeight();
                
                javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                maximizeBtn.setText("❐");
                headerMaximized = true;
            }
        });
        
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("title-bar-btn", "close-btn");
        closeBtn.setMinSize(40, 28);
        closeBtn.setPrefSize(40, 28);
        closeBtn.setMaxSize(40, 28);
        closeBtn.setOnAction(e -> stage.close());
        
        HBox controlBox = new HBox(0);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
        
        return controlBox;
    }
    
    /**
     * Setup resize handlers on the scene root. Call this after scene is created.
     */
    public static void setupResizeHandlers(Stage stage, javafx.scene.layout.Region root) {
        final double[] resizeStartX = new double[1];
        final double[] resizeStartY = new double[1];
        final double[] resizeStartWidth = new double[1];
        final double[] resizeStartHeight = new double[1];
        final double[] resizeStartStageX = new double[1];
        final double[] resizeStartStageY = new double[1];
        final String[] resizeDirection = new String[1];
        
        root.setOnMouseMoved(event -> {
            if (stage.isMaximized()) {
                root.setCursor(Cursor.DEFAULT);
                return;
            }
            
            double x = event.getX();
            double y = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();
            
            boolean left = x < RESIZE_MARGIN;
            boolean right = x > width - RESIZE_MARGIN;
            boolean top = y < RESIZE_MARGIN;
            boolean bottom = y > height - RESIZE_MARGIN;
            
            if (left && top) {
                root.setCursor(Cursor.NW_RESIZE);
            } else if (right && top) {
                root.setCursor(Cursor.NE_RESIZE);
            } else if (left && bottom) {
                root.setCursor(Cursor.SW_RESIZE);
            } else if (right && bottom) {
                root.setCursor(Cursor.SE_RESIZE);
            } else if (left) {
                root.setCursor(Cursor.W_RESIZE);
            } else if (right) {
                root.setCursor(Cursor.E_RESIZE);
            } else if (top) {
                root.setCursor(Cursor.N_RESIZE);
            } else if (bottom) {
                root.setCursor(Cursor.S_RESIZE);
            } else {
                root.setCursor(Cursor.DEFAULT);
            }
        });
        
        root.setOnMousePressed(event -> {
            if (stage.isMaximized()) return;
            
            resizeStartX[0] = event.getScreenX();
            resizeStartY[0] = event.getScreenY();
            resizeStartWidth[0] = stage.getWidth();
            resizeStartHeight[0] = stage.getHeight();
            resizeStartStageX[0] = stage.getX();
            resizeStartStageY[0] = stage.getY();
            
            double x = event.getX();
            double y = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();
            
            boolean left = x < RESIZE_MARGIN;
            boolean right = x > width - RESIZE_MARGIN;
            boolean top = y < RESIZE_MARGIN;
            boolean bottom = y > height - RESIZE_MARGIN;
            
            if (left && top) resizeDirection[0] = "NW";
            else if (right && top) resizeDirection[0] = "NE";
            else if (left && bottom) resizeDirection[0] = "SW";
            else if (right && bottom) resizeDirection[0] = "SE";
            else if (left) resizeDirection[0] = "W";
            else if (right) resizeDirection[0] = "E";
            else if (top) resizeDirection[0] = "N";
            else if (bottom) resizeDirection[0] = "S";
            else resizeDirection[0] = null;
        });
        
        root.setOnMouseDragged(event -> {
            if (stage.isMaximized() || resizeDirection[0] == null) return;
            
            double dx = event.getScreenX() - resizeStartX[0];
            double dy = event.getScreenY() - resizeStartY[0];
            
            double minWidth = 800;
            double minHeight = 600;
            
            switch (resizeDirection[0]) {
                case "E":
                    stage.setWidth(Math.max(minWidth, resizeStartWidth[0] + dx));
                    break;
                case "S":
                    stage.setHeight(Math.max(minHeight, resizeStartHeight[0] + dy));
                    break;
                case "SE":
                    stage.setWidth(Math.max(minWidth, resizeStartWidth[0] + dx));
                    stage.setHeight(Math.max(minHeight, resizeStartHeight[0] + dy));
                    break;
                case "W":
                    double newWidthW = resizeStartWidth[0] - dx;
                    if (newWidthW >= minWidth) {
                        stage.setWidth(newWidthW);
                        stage.setX(resizeStartStageX[0] + dx);
                    }
                    break;
                case "N":
                    double newHeightN = resizeStartHeight[0] - dy;
                    if (newHeightN >= minHeight) {
                        stage.setHeight(newHeightN);
                        stage.setY(resizeStartStageY[0] + dy);
                    }
                    break;
                case "NW":
                    double newWidthNW = resizeStartWidth[0] - dx;
                    double newHeightNW = resizeStartHeight[0] - dy;
                    if (newWidthNW >= minWidth) {
                        stage.setWidth(newWidthNW);
                        stage.setX(resizeStartStageX[0] + dx);
                    }
                    if (newHeightNW >= minHeight) {
                        stage.setHeight(newHeightNW);
                        stage.setY(resizeStartStageY[0] + dy);
                    }
                    break;
                case "NE":
                    stage.setWidth(Math.max(minWidth, resizeStartWidth[0] + dx));
                    double newHeightNE = resizeStartHeight[0] - dy;
                    if (newHeightNE >= minHeight) {
                        stage.setHeight(newHeightNE);
                        stage.setY(resizeStartStageY[0] + dy);
                    }
                    break;
                case "SW":
                    double newWidthSW = resizeStartWidth[0] - dx;
                    if (newWidthSW >= minWidth) {
                        stage.setWidth(newWidthSW);
                        stage.setX(resizeStartStageX[0] + dx);
                    }
                    stage.setHeight(Math.max(minHeight, resizeStartHeight[0] + dy));
                    break;
            }
        });
        
        root.setOnMouseReleased(event -> {
            resizeDirection[0] = null;
        });
    }
    
    private Button createControlButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("title-bar-btn", styleClass);
        btn.setMinSize(46, 32);
        btn.setPrefSize(46, 32);
        btn.setMaxSize(46, 32);
        return btn;
    }
    
    private void toggleMaximize(Stage stage, Button maximizeBtn) {
        if (maximized) {
            // Restore
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            maximizeBtn.setText("□");
            maximized = false;
        } else {
            // Maximize
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();
            
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            maximizeBtn.setText("❐");
            maximized = true;
        }
    }
    
    public void updateTitle(String title) {
        for (javafx.scene.Node node : getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setText(title);
                break;
            }
        }
    }
}

