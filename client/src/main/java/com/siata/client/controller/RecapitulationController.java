package com.siata.client.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class RecapitulationController implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private Button exportButton;
    @FXML private GridPane statsGrid;
    @FXML private TableView<Map<String, String>> rencanaPenghapusanTable;
    @FXML private TableView<Map<String, String>> rekapPemakaianTable;
    @FXML private TableView<Map<String, String>> keteranganKondisiTable;
    @FXML private TableView<Map<String, String>> rekapPemeganganTable;
    @FXML private TableView<Map<String, String>> jumlahPegawaiTable;
    @FXML private VBox usageTableSection;
    @FXML private TableView<Map<String, String>> usageTable;
    @FXML private HBox statusRow;
    @FXML private VBox legendBox;
    @FXML private TableView<Map<String, String>> employeeMatrixTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeStatsGrid();
        initializeTables();
        initializeUsageTableSection();
    }

    private void setupExportButton() {
        exportButton.setOnAction(event -> showExportPdfModal());
    }

    private void showExportPdfModal() {
        try {
            System.out.println("asdasd");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/siata/client/controller/ExportPdfModal.fxml"));
            VBox modalContent = loader.load();

            // Get the modal controller and pass reference to this controller
            ExportPdfModalController modalController = loader.getController();
            modalController.setParentController(this);

            Stage modalStage = new Stage();
            modalController.setModalStage(modalStage);

            Scene scene = new Scene(modalContent);
            modalStage.setScene(scene);
            modalStage.setTitle("Export PDF - Rekapitulasi");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(exportButton.getScene().getWindow());
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat dialog export PDF.");
        }
    }

    public void handleExportPdf(List<String> selectedTables, boolean includeCharts, boolean includeSummary, boolean landscape) {
        // Implementasi export PDF
        System.out.println("Memulai export PDF dengan konfigurasi:");
        System.out.println("Tabel yang dipilih: " + selectedTables);
        System.out.println("Sertakan grafik: " + includeCharts);
        System.out.println("Sertakan ringkasan: " + includeSummary);
        System.out.println("Orientasi landscape: " + landscape);

        // TODO: Implement actual PDF export logic here
        // Contoh: PdfExportService.exportRecapitulation(selectedTables, includeCharts, includeSummary, landscape);

        showAlert("Sukses", "Laporan PDF berhasil di-generate!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ... (methods lainnya tetap sama: initializeStatsGrid, initializeTables, dll.)
    private void initializeStatsGrid() {
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

    private void initializeTables() {
        setupTable(rencanaPenghapusanTable, "Rencana Penghapusan",
                new String[]{"Jenis Aset", "Harus Masa Pakai", "Bersih", "Akan Habis 1 Tahun", "Total Bersih"},
                new int[]{200, 150, 120, 150, 120});

        setupTable(rekapPemakaianTable, "Rekap Pemakaian",
                new String[]{"Jenis Aset", "Dipakai Belum Habis", "Dipakai Sudah Habis", "Total Dipakai"},
                new int[]{200, 150, 150, 150});

        setupTable(keteranganKondisiTable, "Keterangan Kondisi",
                new String[]{"Jenis Aset", "Rusak Berat", "Hilang", "Gudang"},
                new int[]{200, 150, 150, 150});

        setupTable(rekapPemeganganTable, "Rekap Pemegangan",
                new String[]{"Jenis Aset", "Tidak Ganda", "Ganda", "Total Pemegangan"},
                new int[]{200, 150, 150, 150});

        setupTable(jumlahPegawaiTable, "Jumlah Pegawai per Bagian",
                new String[]{"Bagian", "ASN", "PPNPN", "Total"},
                new int[]{250, 150, 150, 150});

        setupTable(employeeMatrixTable, "Matriks Distribusi Aset per Pegawai",
                new String[]{"Nama Pegawai", "Unit", "Laptop", "Printer", "Meja", "Kursi", "AC", "Proyektor", "Total"},
                new int[]{180, 150, 80, 80, 80, 80, 80, 100, 80});
    }

    private void initializeUsageTableSection() {
        setupUsageTable();
        setupStatusRow();
        setupLegend();
    }

    private void setupUsageTable() {
        String[] columns = {"Subdirektorat", "Laptop", "Printer", "Meja", "Kursi", "AC", "Proyektor", "Total"};
        int[] widths = {180, 100, 100, 100, 100, 100, 100, 100};

        setupTableColumns(usageTable, columns, widths);
    }

    private void setupStatusRow() {
        for (int i = 0; i < 6; i++) {
            Label badge = new Label("✔");
            badge.getStyleClass().add("status-badge-success");
            statusRow.getChildren().add(badge);
        }
    }

    private void setupLegend() {
        Label legendOk = new Label("✔ = Data subdirektorat sesuai dengan manajemen aset");
        Label legendWarn = new Label("⚠ = Ketidaksesuaian data (perlu investigasi)");
        legendBox.getChildren().addAll(legendOk, legendWarn);
    }

    private void setupTable(TableView<Map<String, String>> table, String title, String[] columns, int[] widths) {
        table.setUserData(title);
        setupTableColumns(table, columns, widths);
        table.setItems(FXCollections.observableArrayList());
    }

    private void setupTableColumns(TableView<Map<String, String>> table, String[] columns, int[] widths) {
        table.getColumns().clear();

        for (int idx = 0; idx < columns.length; idx++) {
            final int colIndex = idx;
            TableColumn<Map<String, String>, String> col = new TableColumn<>(columns[colIndex]);
            col.setPrefWidth(widths[colIndex]);
            col.setCellValueFactory(cellData -> {
                Map<String, String> row = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(
                        row.getOrDefault(columns[colIndex], ""));
            });
            col.setSortable(false);
            if (colIndex > 0) {
                col.setStyle("-fx-alignment: CENTER-RIGHT;");
            }
            table.getColumns().add(col);
        }
    }

    private VBox createStatCard(CardData data) {
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

    // Public methods untuk update data
    public void updateRencanaPenghapusanData(List<Map<String, String>> data) {
        rencanaPenghapusanTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateRekapPemakaianData(List<Map<String, String>> data) {
        rekapPemakaianTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateKeteranganKondisiData(List<Map<String, String>> data) {
        keteranganKondisiTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateRekapPemeganganData(List<Map<String, String>> data) {
        rekapPemeganganTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateJumlahPegawaiData(List<Map<String, String>> data) {
        jumlahPegawaiTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateUsageTableData(List<Map<String, String>> data) {
        usageTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateEmployeeMatrixData(List<Map<String, String>> data) {
        employeeMatrixTable.setItems(FXCollections.observableArrayList(data));
    }

    public void updateStatsData(List<CardData> newStats) {
        statsGrid.getChildren().clear();
        for (int i = 0; i < newStats.size(); i++) {
            statsGrid.add(createStatCard(newStats.get(i)), i, 0);
        }
    }

    // Helper method untuk mendapatkan data tabel yang tersedia
    public List<String> getAvailableTables() {
        return List.of(
                "Rencana Penghapusan",
                "Rekap Pemakaian",
                "Keterangan Kondisi",
                "Rekap Pemegangan",
                "Jumlah Pegawai per Bagian",
                "Penggunaan Aset per Subdirektorat",
                "Matriks Distribusi Aset per Pegawai"
        );
    }

    private record CardData(String title, String value, String description, String icon) {
    }
}