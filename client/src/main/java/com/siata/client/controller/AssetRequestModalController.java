package com.siata.client.controller;

import com.siata.client.model.AssetRequest;
import com.siata.client.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AssetRequestModalController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Button closeButton, cancelButton, saveButton;
    @FXML private VBox formContent;

    @FXML private TextField namaField;
    @FXML private ComboBox<String> unitCombo;
    @FXML private ComboBox<String> jenisCombo;
    @FXML private TextField jumlahField;
    @FXML private TextArea deskripsiArea, tujuanArea;
    @FXML private ComboBox<String> prioritasCombo;
    @FXML private DatePicker tanggalPicker;

    private AssetRequest editableRequest;
    private String tipe;

    private final DataService dataService = DataService.getInstance();

    @FXML
    public void initialize() {
        closeButton.setOnAction(e -> close());
        cancelButton.setOnAction(e -> close());
        saveButton.setOnAction(e -> handleSave());

        unitCombo.getItems().addAll("Subdit Teknis", "Subdit Operasional", "Subdit Keamanan", "Subdit SDM");
        jenisCombo.getItems().addAll("Laptop", "Printer", "Meja", "Kursi", "AC", "Monitor", "Scanner", "Proyektor");
        prioritasCombo.getItems().addAll("Tinggi", "Sedang", "Rendah");
    }

    public void setEditableRequest(AssetRequest request, String tipe) {
        this.editableRequest = request;
        this.tipe = tipe;

        titleLabel.setText(request == null ? "Tambah " + tipe : "Edit " + tipe);
        subtitleLabel.setText("Permohonan".equals(tipe) ? "Catat permohonan aset dari pegawai" : "Catat pengajuan aset dari pegawai");

        if (request != null) {
            namaField.setText(request.getPemohon());
            unitCombo.setValue(request.getUnit());
            jenisCombo.setValue(request.getJenisAset());
            jumlahField.setText(String.valueOf(request.getJumlah()));
            deskripsiArea.setText(request.getDeskripsi());
            tujuanArea.setText(request.getTujuanPenggunaan());
            prioritasCombo.setValue(request.getPrioritas());
            tanggalPicker.setValue(request.getTanggal());
        } else {
            prioritasCombo.setValue("Sedang");
            tanggalPicker.setValue(LocalDate.now());
        }
    }

    private void handleSave() {
        if (namaField.getText() == null || namaField.getText().trim().isEmpty()) return;
        if (unitCombo.getValue() == null || jenisCombo.getValue() == null) return;

        int jumlah = 1;
        try { jumlah = Integer.parseInt(jumlahField.getText()); } catch (Exception ignored) {}

        if (editableRequest == null) {
            String noPermohonan = "REQ-2025-" + (dataService.getAssetRequests().size() + 1);
            AssetRequest request = new AssetRequest(
                    noPermohonan, tanggalPicker.getValue(), namaField.getText().trim(),
                    unitCombo.getValue(), jenisCombo.getValue(), jumlah,
                    prioritasCombo.getValue(), tipe,
                    deskripsiArea.getText(), tujuanArea.getText()
            );
            dataService.addAssetRequest(request);
        } else {
            editableRequest.setPemohon(namaField.getText().trim());
            editableRequest.setUnit(unitCombo.getValue());
            editableRequest.setJenisAset(jenisCombo.getValue());
            editableRequest.setJumlah(jumlah);
            editableRequest.setPrioritas(prioritasCombo.getValue());
            editableRequest.setDeskripsi(deskripsiArea.getText());
            editableRequest.setTujuanPenggunaan(tujuanArea.getText());
            editableRequest.setTanggal(tanggalPicker.getValue());
            dataService.updateAssetRequest(editableRequest);
        }
        close();
    }

    private void close() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
