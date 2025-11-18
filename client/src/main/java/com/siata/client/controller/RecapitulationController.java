package com.siata.client.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class RecapitulationController {

    @FXML private VBox tablesContainer;
    @FXML private GridPane statsGrid;

    @FXML
    public void initialize() {
        buildStats();
        buildTables();
    }

    private void buildStats() {
        List<CardData> cards = List.of(
                new CardData("Total Aset", "263", "unit di seluruh sistem", "🧾"),
                new CardData("Sedang Digunakan", "233", "88.6% dari total", "✅"),
                new CardData("Tersedia", "21", "8.0% dari total", "📦"),
                new CardData("Rusak", "9", "3.4% dari total", "⚠")
        );

        for (int i = 0; i < cards.size(); i++) {
            statsGrid.add(createStatCard(cards.get(i)), i, 0);
        }
    }

    private void buildTables() {
        tablesContainer.getChildren().addAll(
                createTableSection("Rencana Penghapusan",
                        new String[]{"Jenis Aset","Harus Masa Pakai","Bersih","Akan Habis 1 Tahun","Total Bersih"},
                        new int[]{200,150,120,150,120}),
                createTableSection("Rekap Pemakaian",
                        new String[]{"Jenis Aset","Dipakai Belum Habis","Dipakai Sudah Habis","Total Dipakai"},
                        new int[]{200,150,150,150}),
                createTableSection("Keterangan Kondisi",
                        new String[]{"Jenis Aset","Rusak Berat","Hilang","Gudang"},
                        new int[]{200,150,150,150}),
                createTableSection("Rekap Pemegangan",
                        new String[]{"Jenis Aset","Tidak Ganda","Ganda","Total Pemegangan"},
                        new int[]{200,150,150,150}),
                createTableSection("Jumlah Pegawai per Bagian",
                        new String[]{"Bagian","ASN","PPNPN","Total"},
                        new int[]{250,150,150,150}),
                createTableSection("Matriks Distribusi Aset per Pegawai",
                        new String[]{"Nama Pegawai","Unit","Laptop","Printer","Meja","Kursi","AC","Proyektor","Total"},
                        new int[]{180,150,80,80,80,80,80,100,80})
        );
    }

    private Node createTableSection(String title, String[] columns, int[] widths) {
        VBox section = new VBox(12);
        section.getStyleClass().add("table-container");
        section.setPadding(new Insets(20));

        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("table-title");

        TableView<Map<String, String>> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");

        for (int idx = 0; idx < columns.length; idx++) {
            final int colIndex = idx;
            TableColumn<Map<String, String>, String> col = new TableColumn<>(columns[idx]);
            col.setPrefWidth(widths[idx]);
            col.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrDefault(columns[colIndex], ""))
            );
            col.setSortable(false);
            if (idx > 0) col.setStyle("-fx-alignment: CENTER-RIGHT;");
            table.getColumns().add(col);
        }

        section.getChildren().addAll(sectionTitle, table);
        return section;
    }

    private Node createStatCard(CardData data) {
        VBox card = new VBox(12);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(24));

        Label title = new Label(data.title());
        title.getStyleClass().add("stat-card-title");

        Label value = new Label(data.value());
        value.getStyleClass().add("stat-card-value");

        Label desc = new Label(data.description());
        desc.getStyleClass().add("stat-card-description");

        Label icon = new Label(data.icon());
        icon.getStyleClass().add("stat-card-icon");

        card.getChildren().addAll(title, value, desc, icon);
        return card;
    }

    private record CardData(String title, String value, String description, String icon) {}
}
