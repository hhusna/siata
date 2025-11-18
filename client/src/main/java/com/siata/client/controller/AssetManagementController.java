package com.siata.client.controller;

import com.siata.client.model.Asset;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AssetManagementController implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private Button addButton;
    @FXML private HBox filterBar;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> jenisFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private VBox tableContainer;
    @FXML private TableView<Asset> assetTable;
    @FXML private TableColumn<Asset, String> kodeCol;
    @FXML private TableColumn<Asset, String> jenisCol;
    @FXML private TableColumn<Asset, String> merkCol;
    @FXML private TableColumn<Asset, String> keteranganCol;
    @FXML private TableColumn<Asset, String> subditCol;
    @FXML private TableColumn<Asset, String> tanggalCol;
    @FXML private TableColumn<Asset, String> rupiahCol;
    @FXML private TableColumn<Asset, String> kondisiCol;
    @FXML private TableColumn<Asset, String> statusCol;
    @FXML private TableColumn<Asset, Void> aksiCol;

    private final ObservableList<Asset> assetList = FXCollections.observableArrayList();
    private final DataService dataService = DataService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("AssetManagementController initialized!");

        setupEventHandlers();
        initializeFilterComboBoxes();
        initializeTableColumns();
        refreshTable();
    }

    private void setupEventHandlers() {
        // Setup add button
        addButton.setOnAction(event -> showAssetForm(null));

        // Setup search field
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        // Setup filter comboboxes
        jenisFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initializeFilterComboBoxes() {
        // Initialize jenis filter
        jenisFilterCombo.getItems().addAll("Semua Jenis", "Laptop", "Printer", "Meja", "Kursi", "AC", "Monitor", "Scanner");
        jenisFilterCombo.setValue("Semua Jenis");

        // Initialize status filter
        statusFilterCombo.getItems().addAll("Semua Status", "Tersedia", "Digunakan", "Rusak");
        statusFilterCombo.setValue("Semua Status");
    }

    private void initializeTableColumns() {
        // Configure table columns
        kodeCol.setCellValueFactory(new PropertyValueFactory<>("kodeAset"));
        jenisCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));
        merkCol.setCellValueFactory(new PropertyValueFactory<>("merkBarang"));
        keteranganCol.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        subditCol.setCellValueFactory(new PropertyValueFactory<>("subdit"));

        // Tanggal column with formatter
        tanggalCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggalPerolehan();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        // Rupiah column with currency format
        rupiahCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                currencyFormat.format(cellData.getValue().getNilaiRupiah())
        ));

        kondisiCol.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Action column
        aksiCol.setCellFactory(column -> new TableCell<Asset, Void>() {
            private final Button editButton = createIconButton("✏");
            private final Button deleteButton = createIconButton("🗑");
            private final HBox actionBox = new HBox(6, editButton, deleteButton);

            {
                actionBox.setAlignment(Pos.CENTER);
                editButton.setOnAction(e -> {
                    Asset asset = getTableView().getItems().get(getIndex());
                    showAssetForm(asset);
                });
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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });

        // Set table data
        assetTable.setItems(assetList);
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        return button;
    }

    @FXML
    private void showAssetForm(Asset editableAsset) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/siata/client/view/AssetFormModal.fxml"));
            VBox modalContent = loader.load();

            AssetFormModalController modalController = loader.getController();
            modalController.setParentController(this);
            modalController.setEditableAsset(editableAsset);

            Stage modalStage = new Stage();
            modalController.setModalStage(modalStage);

            Scene scene = new Scene(modalContent);
            modalStage.setScene(scene);
            modalStage.setTitle(editableAsset == null ? "Tambah Aset Baru" : "Edit Aset");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(addButton.getScene().getWindow());
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat form aset.");
        }
    }

    private void filterTable(String searchText) {
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText();
        String selectedJenis = jenisFilterCombo.getValue();
        String selectedStatus = statusFilterCombo.getValue();

        List<Asset> filteredAssets = dataService.getAssets().stream()
                .filter(asset ->
                        (searchText == null || searchText.isEmpty() ||
                                asset.getKodeAset().toLowerCase().contains(searchText.toLowerCase()) ||
                                asset.getJenisAset().toLowerCase().contains(searchText.toLowerCase()) ||
                                asset.getMerkBarang().toLowerCase().contains(searchText.toLowerCase()) ||
                                asset.getKeterangan().toLowerCase().contains(searchText.toLowerCase()))
                )
                .filter(asset ->
                        "Semua Jenis".equals(selectedJenis) ||
                                asset.getJenisAset().equals(selectedJenis)
                )
                .filter(asset ->
                        "Semua Status".equals(selectedStatus) ||
                                asset.getStatus().equals(selectedStatus)
                )
                .toList();

        assetList.setAll(filteredAssets);
    }

    public void refreshTable() {
        assetList.setAll(dataService.getAssets());
        applyFilters(); // Apply current filters after refresh
    }

    private boolean confirmDelete(Asset asset) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penghapusan");
        alert.setHeaderText("Hapus Aset");
        alert.setContentText("Apakah Anda yakin ingin menghapus aset " + asset.getKodeAset() + "?");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}