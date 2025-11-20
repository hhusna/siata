package com.siata.client.view;

import com.siata.client.api.AssetApi;
import com.siata.client.api.ExportPdfApi;
import com.siata.client.dto.AssetDto;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class RecapitulationView extends VBox {

    ExportPdfApi exportApi = new ExportPdfApi();
    AssetApi assetApi = new AssetApi();

    public RecapitulationView() {
        setSpacing(24);
        buildView();
    }

    private void buildView() {
        getChildren().add(buildHeader());
        getChildren().add(buildStatsGrid());
        getChildren().add(createRencanaPenghapusanTable());
        getChildren().add(createRekapPemakaianTable());
        getChildren().add(createKeteranganKondisiTable());
        getChildren().add(createRekapPemeganganTable());
        getChildren().add(createJumlahPegawaiTable());
        getChildren().add(createUsageTable());
        getChildren().add(createEmployeeMatrixTable());
    }

    private Node buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(4);
        Label title = new Label("Rekapitulasi & Matriks");
        title.getStyleClass().add("section-heading");
        Label description = new Label("Ringkasan matriks distribusi aset dan pegawai");
        description.getStyleClass().add("section-description");
        textGroup.getChildren().addAll(title, description);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportButton = new Button("Export PDF");
        exportButton.getStyleClass().add("primary-button");
        exportButton.setOnAction(event -> {
            Stage stage = (Stage) exportButton.getScene().getWindow(); // ambil stage saat klik
            exportApi.handle(stage);
        });
        
        header.getChildren().addAll(textGroup, spacer, exportButton);
        return header;
    }

    public void exportPdfWithChooser(byte[] pdfBytes) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan PDF");
        chooser.setSelectedFile(new java.io.File("laporan.pdf")); // default name

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                Files.write(path, pdfBytes);
                System.out.println("PDF berhasil disimpan: " + path.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Gagal menyimpan file: " + e.getMessage());
            }
        } else {
            System.out.println("User membatalkan simpan file");
        }
    }

    private Node buildStatsGrid() {
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            statsGrid.getColumnConstraints().add(column);
        }

        List<CardData> cards = List.of(
                new CardData("Total Aset", Integer.toString(getTotalAsset()), "unit di seluruh sistem", "🧾"),
                new CardData("Sedang Digunakan", Integer.toString(getSedangDigunakan()), Float.toString(getSedangDigunakanPersen()) + "% dari total", "✅"),
                new CardData("Tersedia", "21", "8.0% dari total", "📦"),
                new CardData("Rusak", "9", "3.4% dari total", "⚠")
        );

        for (int i = 0; i < cards.size(); i++) {
            statsGrid.add(createStatCard(cards.get(i)), i, 0);
        }

        return statsGrid;
    }

    private float getSedangDigunakanPersen() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        float persen = 0f;
        for (AssetDto dto : assetDtos) {
            if (dto.getStatusPemakaian().equals("Digunakan")) {
                count += 1;
            }
        }

        int jumlahSemuaSet = getTotalAsset();

        if (count > 0) {
            persen = ((float) count/jumlahSemuaSet)*100;
            float persenPretty = Math.round(persen * 10f) / 10f;;
            return persenPretty;
        }

        return persen;
    }

    private int getSedangDigunakan() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 5;
        for (AssetDto dto : assetDtos) {
            if (dto.getStatusPemakaian().equals("Digunakan")) {
                count += 1;
            }
        }

        return count;
    }

    private int getTotalAsset() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        for (AssetDto dto : assetDtos) {
            count += 1;
        }
        return count;
    }

    private int getSiapDilelang() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        LocalDate timeNow = LocalDate.now();
        for (AssetDto dto : assetDtos) {
            Long selisihHari = ChronoUnit.DAYS.between(dto.getTanggalPerolehan(), timeNow);

            if (selisihHari >= 1460) {
                count += 1;
            }
        }

        return count;
    }

    private int getRusakBerat() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        for (AssetDto dto : assetDtos) {
            if (dto.getKondisi().equals("Rusak Berat")) {
                count += 1;
            }
        }

        return count;
    }

    private Node createRencanaPenghapusanTable() {
        return createTableSection("Rencana Penghapusan", 
            new String[]{"Jenis Aset", "Harus Masa Pakai", "Bersih", "Akan Habis 1 Tahun", "Total Bersih"},
            new int[]{200, 150, 120, 150, 120}
        );
    }

    private Node createRekapPemakaianTable() {
        return createTableSection("Rekap Pemakaian",
            new String[]{"Jenis Aset", "Dipakai Belum Habis", "Dipakai Sudah Habis", "Total Dipakai"},
            new int[]{200, 150, 150, 150}
        );
    }

    private Node createKeteranganKondisiTable() {
        return createTableSection("Keterangan Kondisi",
            new String[]{"Jenis Aset", "Rusak Berat", "Hilang", "Gudang"},
            new int[]{200, 150, 150, 150}
        );
    }

    private Node createRekapPemeganganTable() {
        return createTableSection("Rekap Pemegangan",
            new String[]{"Jenis Aset", "Tidak Ganda", "Ganda", "Total Pemegangan"},
            new int[]{200, 150, 150, 150}
        );
    }

    private Node createJumlahPegawaiTable() {
        return createTableSection("Jumlah Pegawai per Bagian",
            new String[]{"Bagian", "ASN", "PPNPN", "Total"},
            new int[]{250, 150, 150, 150}
        );
    }

    private Node createUsageTable() {
        VBox section = new VBox(12);
        section.getStyleClass().add("table-container");
        section.setPadding(new Insets(20));

        Label title = new Label("Penggunaan Aset per Subdirektorat dan Jenis");
        title.getStyleClass().add("table-title");

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        table.setItems(FXCollections.observableArrayList());

        String[] columns = {"Subdirektorat", "Laptop", "Printer", "Meja", "Kursi", "AC", "Proyektor", "Total"};
        int[] widths = {180, 100, 100, 100, 100, 100, 100, 100};
        
        for (int idx = 0; idx < columns.length; idx++) {
            final int colIndex = idx;
            TableColumn<Map<String, String>, String> col = new TableColumn<>(columns[colIndex]);
            col.setPrefWidth(widths[colIndex]);
            col.setCellValueFactory(cellData -> {
                Map<String, String> row = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(row.getOrDefault(columns[colIndex], ""));
            });
            col.setSortable(false);
            if (colIndex > 0) {
                col.setStyle("-fx-alignment: CENTER-RIGHT;");
            }
            table.getColumns().add(col);
        }

        HBox statusRow = new HBox(16);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setSpacing(10);
        statusRow.getStyleClass().add("status-row");
        Label statusLabel = new Label("Status Validasi");
        statusLabel.getStyleClass().add("chart-title");

        for (int i = 0; i < 6; i++) {
            Label badge = new Label("✔");
            badge.getStyleClass().add("status-badge-success");
            statusRow.getChildren().add(badge);
        }

        VBox legend = new VBox(4);
        legend.getStyleClass().add("status-legend");
        Label legendOk = new Label("✔ = Data subdirektorat sesuai dengan manajemen aset");
        Label legendWarn = new Label("⚠ = Ketidaksesuaian data (perlu investigasi)");
        legend.getChildren().addAll(legendOk, legendWarn);

        section.getChildren().addAll(title, table, statusLabel, statusRow, legend);
        return section;
    }

    private Node createEmployeeMatrixTable() {
        return createTableSection("Matriks Distribusi Aset per Pegawai",
            new String[]{"Nama Pegawai", "Unit", "Laptop", "Printer", "Meja", "Kursi", "AC", "Proyektor", "Total"},
            new int[]{180, 150, 80, 80, 80, 80, 80, 100, 80}
        );
    }

    private Node createTableSection(String title, String[] columns, int[] widths) {
        VBox section = new VBox(12);
        section.getStyleClass().add("table-container");
        section.setPadding(new Insets(20));

        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("table-title");

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        table.setItems(FXCollections.observableArrayList());

        for (int idx = 0; idx < columns.length; idx++) {
            final int colIndex = idx;
            TableColumn<Map<String, String>, String> col = new TableColumn<>(columns[colIndex]);
            col.setPrefWidth(widths[colIndex]);
            col.setCellValueFactory(cellData -> {
                Map<String, String> row = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(row.getOrDefault(columns[colIndex], ""));
            });
            col.setSortable(false);
            if (colIndex > 0) {
                col.setStyle("-fx-alignment: CENTER-RIGHT;");
            }
            table.getColumns().add(col);
        }

        section.getChildren().addAll(sectionTitle, table);
        return section;
    }

    private Node createStatCard(CardData data) {
        VBox card = new VBox(12);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(24));
        
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(data.title());
        titleLabel.getStyleClass().add("stat-card-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label iconLabel = new Label(data.icon());
        iconLabel.getStyleClass().add("stat-card-icon");
        
        headerBox.getChildren().addAll(titleLabel, spacer, iconLabel);
        
        Label valueLabel = new Label(data.value());
        valueLabel.getStyleClass().add("stat-card-value");
        
        Label descLabel = new Label(data.description());
        descLabel.getStyleClass().add("stat-card-description");
        
        card.getChildren().addAll(headerBox, valueLabel, descLabel);
        return card;
    }

    private record CardData(String title, String value, String description, String icon) {
    }
}










