package com.siata.client.controller;

import com.siata.client.model.Asset;
import com.siata.client.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AssetFormModalController implements Initializable {

    @FXML private VBox modalContainer;
    @FXML private Label modalTitle;
    @FXML private Label modalSubtitle;
    @FXML private Button closeButton;
    @FXML private ScrollPane formScrollPane;
    @FXML private VBox formContent;
    @FXML private TextField kodeField;
    @FXML private ComboBox<String> jenisCombo;
    @FXML private TextField merkField;
    @FXML private TextField pemegangField;
    @FXML private ComboBox<String> subditCombo;
    @FXML private DatePicker tanggalPicker;
    @FXML private TextField rupiahField;
    @FXML private ComboBox<String> kondisiCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Stage modalStage;
    private AssetManagementController parentController;
    private Asset editableAsset;
    private final DataService dataService = DataService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        setupEventHandlers();
        setDefaultValues();
    }

    private void initializeComboBoxes() {
        // Initialize jenis combo
        jenisCombo.getItems().addAll("Laptop", "Printer", "Meja", "Kursi", "AC", "Monitor", "Scanner", "Proyektor");

        // Initialize subdit combo
        subditCombo.getItems().addAll("Subdit Teknis", "Subdit Operasional", "Subdit Keamanan", "Subdit SDM");

        // Initialize kondisi combo
        kondisiCombo.getItems().addAll("Sangat Baik", "Baik", "Cukup", "Rusak");

        // Initialize status combo
        statusCombo.getItems().addAll("Tersedia", "Digunakan", "Rusak", "Perbaikan");
    }

    private void setupEventHandlers() {
        closeButton.setOnAction(event -> closeModal());
        cancelButton.setOnAction(event -> closeModal());
        saveButton.setOnAction(event -> handleSave());
    }

    private void setDefaultValues() {
        tanggalPicker.setValue(LocalDate.now());
        kondisiCombo.setValue("Baik");
        statusCombo.setValue("Tersedia");
    }

    public void setEditableAsset(Asset asset) {
        this.editableAsset = asset;
        if (asset != null) {
            // Edit mode
            modalTitle.setText("Edit Aset");
            modalSubtitle.setText("Perbarui informasi aset");
            saveButton.setText("Simpan Perubahan");

            // Populate fields
            kodeField.setText(asset.getKodeAset());
            kodeField.setDisable(true); // Kode tidak bisa diubah
            jenisCombo.setValue(asset.getJenisAset());
            merkField.setText(asset.getMerkBarang());
            pemegangField.setText(asset.getKeterangan());
            subditCombo.setValue(asset.getSubdit());
            tanggalPicker.setValue(asset.getTanggalPerolehan());
            rupiahField.setText(String.valueOf((long) asset.getNilaiRupiah()));
            kondisiCombo.setValue(asset.getKondisi());
            statusCombo.setValue(asset.getStatus());
        } else {
            // Add mode
            modalTitle.setText("Tambah Aset Baru");
            modalSubtitle.setText("Masukkan informasi aset yang akan ditambahkan");
            saveButton.setText("Simpan");
            kodeField.setDisable(false);
        }
    }

    private void handleSave() {
        if (validateForm()) {
            saveAsset();
            closeModal();
        }
    }

    private boolean validateForm() {
        // Kode validation
        String kode = kodeField.getText();
        if (kode == null || kode.trim().isEmpty()) {
            showAlert("Kode Aset harus diisi");
            return false;
        }
        if (kode.length() != 10) {
            showAlert("Kode Aset harus 10 digit");
            return false;
        }

        // Jenis validation
        if (jenisCombo.getValue() == null) {
            showAlert("Pilih jenis aset");
            return false;
        }

        // Merk validation
        if (merkField.getText() == null || merkField.getText().trim().isEmpty()) {
            showAlert("Masukkan merk barang");
            return false;
        }

        // Subdit validation
        if (subditCombo.getValue() == null) {
            showAlert("Pilih subdirektorat");
            return false;
        }

        // Tanggal validation
        if (tanggalPicker.getValue() == null) {
            showAlert("Pilih tanggal perolehan");
            return false;
        }

        // Rupiah validation
        try {
            String rupiahText = rupiahField.getText();
            if (rupiahText == null || rupiahText.isBlank()) {
                showAlert("Nilai aset harus diisi");
                return false;
            }
            Double.parseDouble(rupiahText.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            showAlert("Nilai aset harus berupa angka");
            return false;
        }

        // Kondisi validation
        if (kondisiCombo.getValue() == null) {
            showAlert("Pilih kondisi aset");
            return false;
        }

        // Status validation
        if (statusCombo.getValue() == null) {
            showAlert("Pilih status aset");
            return false;
        }

        return true;
    }

    private void saveAsset() {
        String kode = kodeField.getText().trim();
        String jenis = jenisCombo.getValue();
        String merk = merkField.getText().trim();
        String pemegang = pemegangField.getText() != null ? pemegangField.getText().trim() : "";
        String subdit = subditCombo.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        double nilai = Double.parseDouble(rupiahField.getText().replaceAll("[^\\d.]", ""));
        String kondisi = kondisiCombo.getValue();
        String status = statusCombo.getValue();

        if (editableAsset == null) {
            // Create new asset
            Asset asset = new Asset(kode, jenis, merk, pemegang, subdit, tanggal, nilai, kondisi, status);
            dataService.addAsset(asset);
        } else {
            // Update existing asset
            editableAsset.setJenisAset(jenis);
            editableAsset.setMerkBarang(merk);
            editableAsset.setKeterangan(pemegang);
            editableAsset.setSubdit(subdit);
            editableAsset.setTanggalPerolehan(tanggal);
            editableAsset.setNilaiRupiah(nilai);
            editableAsset.setKondisi(kondisi);
            editableAsset.setStatus(status);
            dataService.updateAsset(editableAsset);
        }

        // Refresh parent table
        if (parentController != null) {
            parentController.refreshTable();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeModal() {
        if (modalStage != null) {
            modalStage.close();
        }
    }

    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    public void setParentController(AssetManagementController controller) {
        this.parentController = controller;
    }
}