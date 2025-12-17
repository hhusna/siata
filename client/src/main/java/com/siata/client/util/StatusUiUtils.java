package com.siata.client.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.util.Callback;
import javafx.geometry.Pos;

public class StatusUiUtils {

    /**
     * Creates a cell factory for "Kondisi" column.
     * Full cell background color based on condition severity.
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createKondisiCellFactory() {
        return column -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStyleForKondisi(item));
                    // Ensure text is readable on colored background (white text for dark bg, black for light)
                    // But simplified: mostly white or black based on color intensity
                }
            }
        };
    }

    /**
     * Creates a cell factory for "Status" column.
     * Full cell background color (Aktif=Green, Nonaktif=Red).
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createStatusCellFactory() {
        return column -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStyleForStatus(item));
                }
            }
        };
    }

    /**
     * Creates a cell factory for "Tua" column.
     * Full cell background color (Tidak=Green, Ya=Red).
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createTuaCellFactory() {
        return column -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStyleForTua(item));
                }
            }
        };
    }

    private static String getStyleForKondisi(String kondisi) {
        String upper = kondisi.trim().toUpperCase();
        
        // Rusak Berat / R. BERAT = RED (Soft)
        if (upper.equals("RUSAK BERAT") || upper.equals("R. BERAT")) {
            return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        // Rusak Sedang / R. SEDANG = ORANGE (Soft)
        if (upper.equals("RUSAK SEDANG") || upper.equals("R. SEDANG")) {
            return "-fx-background-color: #ffedd5; -fx-text-fill: #9a3412; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        // Rusak Ringan / R. RINGAN = YELLOW (Soft)
        if (upper.equals("RUSAK RINGAN") || upper.equals("R. RINGAN")) {
            return "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        // Baik = GREEN (Soft)
        if (upper.equals("BAIK")) {
            return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        
        return "-fx-alignment: CENTER;";
    }

    private static String getStyleForStatus(String status) {
        String upper = status.trim().toUpperCase().replace(" ", "");
        
        // Nonaktif = RED (Soft)
        if (upper.contains("NON") || upper.equals("NONAKTIF") || upper.equals("INACTIVE")) {
            return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        // Aktif = GREEN (Soft)
        return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-alignment: CENTER;";
    }

    private static String getStyleForTua(String tua) {
        String upper = tua.trim().toUpperCase();
        
        // Ya = RED (Soft)
        if (upper.equals("YA") || upper.equals("TRUE")) {
             return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        // Tidak = GREEN (Soft)
        return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-alignment: CENTER;";
    }
}
