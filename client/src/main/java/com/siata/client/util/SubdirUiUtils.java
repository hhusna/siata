package com.siata.client.util;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.geometry.Pos;

public class SubdirUiUtils {

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createSubdirCellFactory() {
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
                    label.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; " + getStyleForSubdir(item));
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

    private static String getStyleForSubdir(String subdir) {
        if (subdir == null) return "";
        
        String upper = subdir.trim().toUpperCase();
        String bgColor;
        String textColor;
        
        switch (upper) {
            case "PPTAU":
                bgColor = "#dbeafe"; // Blue 100
                textColor = "#1e40af"; // Blue 800
                break;
            case "AUNB":
                bgColor = "#dcfce7"; // Green 100
                textColor = "#166534"; // Green 800
                break;
            case "AUNTB":
                bgColor = "#f3e8ff"; // Purple 100
                textColor = "#6b21a8"; // Purple 800
                break;
            case "KAU":
                bgColor = "#ffedd5"; // Orange 100
                textColor = "#9a3412"; // Orange 800
                break;
            case "SILAU":
                bgColor = "#ccfbf1"; // Teal 100
                textColor = "#115e59"; // Teal 800
                break;
            case "TATA USAHA":
            case "TU":
                bgColor = "#f1f5f9"; // Slate 100
                textColor = "#475569"; // Slate 600
                break;
            case "DIREKTUR":
                bgColor = "#fee2e2"; // Red 100
                textColor = "#991b1b"; // Red 800
                break;
            case "PINDAH":
                bgColor = "#fecaca"; // Red 200 (Warning)
                textColor = "#b91c1c"; // Red 700
                break;
            default:
                bgColor = "#f3f4f6"; // Gray 100
                textColor = "#374151"; // Gray 700
        }
        
        return String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 12;", bgColor, textColor);
    }
}
