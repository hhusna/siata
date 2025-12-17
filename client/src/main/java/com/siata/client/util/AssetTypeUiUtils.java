package com.siata.client.util;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.geometry.Pos;

public class AssetTypeUiUtils {

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createAssetTypeCellFactory() {
        return column -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; " + getStyleForAssetType(item));
                    label.setPadding(new javafx.geometry.Insets(4, 10, 4, 10));
                    
                    // Center the badge
                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }

    private static String getStyleForAssetType(String type) {
        if (type == null) return "";
        
        String upper = type.trim().toUpperCase();
        String bgColor;
        String textColor;
        
        // "Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot"
        
        switch (upper) {
            case "MOBIL":
                bgColor = "#dbeafe"; // Blue 100
                textColor = "#1e40af"; // Blue 800
                break;
            case "MOTOR":
                bgColor = "#dcfce7"; // Green 100
                textColor = "#166534"; // Green 800
                break;
            case "PC":
            case "KOMPUTER":
                bgColor = "#ffedd5"; // Orange 100
                textColor = "#9a3412"; // Orange 800
                break;
            case "LAPTOP":
                bgColor = "#e0e7ff"; // Indigo 100
                textColor = "#3730a3"; // Indigo 800
                break;
            case "NOTEBOOK":
                bgColor = "#cffafe"; // Cyan 100
                textColor = "#0e7490"; // Cyan 800
                break;
            case "TABLET":
                bgColor = "#f3e8ff"; // Purple 100
                textColor = "#6b21a8"; // Purple 800
                break;
            case "PRINTER":
            case "SCANNER":
                bgColor = "#f1f5f9"; // Slate 100
                textColor = "#475569"; // Slate 600
                break;
            case "SPEAKER":
                bgColor = "#fce7f3"; // Pink 100
                textColor = "#9d174d"; // Pink 800
                break;
            case "PARABOT":
                bgColor = "#f3f4f6"; // Gray 100
                textColor = "#374151"; // Gray 700
                break;
            default:
                bgColor = "#f3f4f6"; // Gray 100
                textColor = "#374151"; // Gray 700
        }
        
        return String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 12;", bgColor, textColor);
    }
}
