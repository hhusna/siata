package com.siata.client.controller;

import com.siata.client.model.Asset;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AssetManagementController {

    @FXML private Button addButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> jenisCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TableView<Asset> assetTable;

    private final ObservableList<Asset> assetList = FXCollections.observableArrayList();
    private final DataService dataService = DataService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    @FXML
    public void initialize() {
        // Setup filter combobox
        jenisCombo.getItems().addAll("Semua Jenis", "Laptop", "Printer", "Meja", "Kursi", "AC", "Monitor", "Scanner");
        jenisCombo.setValue("Semua Jenis");

        statusCombo.getItems().addAll("Semua Status", "Tersedia", "Digunakan", "Rusak");
        statusCombo.setValue("Semua Status");

        // Setup search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        // Setup table columns
        assetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();

        // Bind data
        assetTable.setItems(assetList);

        // Add button action
        addButton.setOnAction(e -> showAssetForm(null));

        refreshTable();
    }

    private void setupTableColumns() {
        TableColumn<Asset, String> kodeCol = new TableColumn<>("Kode Aset");
        kodeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKodeAset()));

        TableColumn<Asset, String> jenisCol = new TableColumn<>("Jenis");
        jenisCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJenisAset()));

        TableColumn<Asset, String> merkCol = new TableColumn<>("Merk Barang");
        merkCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMerkBarang()));

        TableColumn<Asset, String> tanggalCol = new TableColumn<>("Tanggal Perolehan");
        tanggalCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggalPerolehan();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DateTimeFormatter.ofPattern("d MMM yyyy")) : "-"
            );
        });

        TableColumn<Asset, String> rupiahCol = new TableColumn<>("Rupiah Aset");
        rupiahCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                currencyFormat.format(cellData.getValue().getNilaiRupiah())
        ));

        TableColumn<Asset, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = createIconButton("✏");
            private final Button deleteButton = createIconButton("🗑");
            private final HBox actionBox = new HBox(6, editButton, deleteButton);
            {
                actionBox.setAlignment(javafx.geometry.Pos.CENTER);
                editButton.setOnAction(e -> showAssetForm(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> {
                    Asset asset = getTableView().getItems().get(getIndex());
                    if (confirmDelete(asset)) {
                        dataService.deleteAsset(asset);
                        refreshTable();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        assetTable.getColumns().setAll(List.of(kodeCol, jenisCol, merkCol, tanggalCol, rupiahCol, aksiCol));
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        return button;
    }

    private void refreshTable() {
        assetList.setAll(dataService.getAssets());
    }

    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refreshTable();
            return;
        }
        assetList.setAll(dataService.getAssets().stream()
                .filter(asset -> asset.getKodeAset().toLowerCase().contains(searchText.toLowerCase())
                        || asset.getJenisAset().toLowerCase().contains(searchText.toLowerCase())
                        || asset.getMerkBarang().toLowerCase().contains(searchText.toLowerCase()))
                .toList()
        );
    }

    private boolean confirmDelete(Asset asset) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penghapusan");
        alert.setHeaderText("Hapus Aset");
        alert.setContentText("Apakah Anda yakin ingin menghapus aset " + asset.getNamaAset() + "?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAssetForm(Asset editableAsset) {
        // Pindahkan semua modal dialog ke method terpisah atau FXML + controller sendiri
    }
}
