package com.siata.client.view;

import com.siata.client.model.Asset;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AssetManagementView extends VBox {

    private TableView<Asset> table;
    private final ObservableList<Asset> assetList;
    private final DataService dataService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public AssetManagementView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        assetList = FXCollections.observableArrayList();
        
        buildView();
        
        // Refresh table when data changes
        refreshTable();
    }

    private void buildView() {
        Button addButton = new Button("+ Tambah Aset");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> showAssetForm(null));

        getChildren().add(buildPageHeader(addButton));

        // Search and filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Q Cari");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
        
        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Semua Jenis", "Laptop", "Printer", "Meja", "Kursi", "AC", "Monitor", "Scanner");
        jenisCombo.setValue("Semua Jenis");
        jenisCombo.setPrefWidth(150);
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Semua Status", "Tersedia", "Digunakan", "Rusak");
        statusCombo.setValue("Semua Status");
        statusCombo.setPrefWidth(150);
        
        filterBar.getChildren().addAll(searchField, jenisCombo, statusCombo);

        // Table
        table = new TableView<>();
        table.setItems(assetList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");

        TableColumn<Asset, String> kodeCol = new TableColumn<>("Kode Aset");
        kodeCol.setCellValueFactory(new PropertyValueFactory<>("kodeAset"));
        
        TableColumn<Asset, String> jenisCol = new TableColumn<>("Jenis");
        jenisCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));
        
        TableColumn<Asset, String> merkCol = new TableColumn<>("Merk Barang");
        merkCol.setCellValueFactory(new PropertyValueFactory<>("merkBarang"));
        
        TableColumn<Asset, String> keteranganCol = new TableColumn<>("Keterangan");
        keteranganCol.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        
        TableColumn<Asset, String> subditCol = new TableColumn<>("Subdit");
        subditCol.setCellValueFactory(new PropertyValueFactory<>("subdit"));
        
        TableColumn<Asset, String> tanggalCol = new TableColumn<>("Tanggal Perolehan");
        tanggalCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggalPerolehan();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        TableColumn<Asset, String> rupiahCol = new TableColumn<>("Rupiah Aset");
        rupiahCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            currencyFormat.format(cellData.getValue().getNilaiRupiah())
        ));
        
        TableColumn<Asset, String> kondisiCol = new TableColumn<>("Kondisi");
        kondisiCol.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        
        TableColumn<Asset, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Asset, Void> aksiCol = new TableColumn<>("Aksi");
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
        aksiCol.setPrefWidth(140);
        
        table.getColumns().setAll(List.of(kodeCol, jenisCol, merkCol, keteranganCol, subditCol, tanggalCol,
                rupiahCol, kondisiCol, statusCol, aksiCol));

        VBox tableContainer = new VBox(16);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getStyleClass().add("table-container");
        tableContainer.getChildren().addAll(filterBar, table);

        getChildren().addAll(tableContainer);
    }

    private Node buildPageHeader(Button actionButton) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(4);
        Label title = new Label("Manajemen Aset");
        title.getStyleClass().add("page-intro-title");
        Label description = new Label("Kelola data aset pegawai");
        description.getStyleClass().add("page-intro-description");
        textGroup.getChildren().addAll(title, description);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(textGroup, spacer, actionButton);
        return header;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        return button;
    }

    private void showAssetForm(Asset editableAsset) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UTILITY);
        modalStage.setTitle(editableAsset == null ? "Tambah Aset Baru" : "Edit Aset");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(480);
        modalContent.setMaxWidth(480);
        modalContent.setMaxHeight(650);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        VBox titleBox = new VBox(4);
        Label title = new Label(editableAsset == null ? "Tambah Aset Baru" : "Edit Aset");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label(editableAsset == null
            ? "Masukkan informasi aset yang akan ditambahkan"
            : "Perbarui informasi aset");
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());
        
        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        TextField kodeField = new TextField();
        kodeField.setPromptText("Contoh: 1234567890");
        Label kodeLabel = new Label("Kode Aset (10 digit)");
        kodeLabel.getStyleClass().add("form-label");
        Text kodeHint = new Text("Masukkan 10 digit angka sebagai kode unik aset");
        kodeHint.getStyleClass().add("form-hint");
        if (editableAsset != null) {
            kodeField.setText(editableAsset.getKodeAset());
            kodeField.setDisable(true);
        }

        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Laptop", "Printer", "Meja", "Kursi", "AC", "Monitor", "Scanner", "Proyektor");
        jenisCombo.setPromptText("Pilih jenis aset");
        Label jenisLabel = new Label("Jenis Aset");
        jenisLabel.getStyleClass().add("form-label");
        if (editableAsset != null) {
            jenisCombo.setValue(editableAsset.getJenisAset());
        }

        TextField merkField = new TextField();
        merkField.setPromptText("Contoh: Dell, HP, Canon");
        Label merkLabel = new Label("Merk Barang");
        merkLabel.getStyleClass().add("form-label");
        if (editableAsset != null) {
            merkField.setText(editableAsset.getMerkBarang());
        }

        TextField pemegangField = new TextField();
        pemegangField.setPromptText("Nama pegawai atau ruangan");
        Label pemegangLabel = new Label("Pemegang (Keterangan)");
        pemegangLabel.getStyleClass().add("form-label");
        Text pemegangHint = new Text("Kosongkan jika aset belum memiliki pemegang");
        pemegangHint.getStyleClass().add("form-hint");
        if (editableAsset != null) {
            pemegangField.setText(editableAsset.getKeterangan());
        }

        ComboBox<String> subditCombo = new ComboBox<>();
        subditCombo.getItems().addAll("Subdit Teknis", "Subdit Operasional", "Subdit Keamanan", "Subdit SDM");
        subditCombo.setPromptText("Pilih subdirektorat");
        Label subditLabel = new Label("Subdirektorat (Subdit)");
        subditLabel.getStyleClass().add("form-label");
        if (editableAsset != null) {
            subditCombo.setValue(editableAsset.getSubdit());
        }

        DatePicker tanggalPicker = new DatePicker(editableAsset == null ? LocalDate.now() : editableAsset.getTanggalPerolehan());
        Label tanggalLabel = new Label("Tanggal Perolehan");
        tanggalLabel.getStyleClass().add("form-label");

        TextField rupiahField = new TextField(editableAsset == null ? "0" : String.valueOf((long) editableAsset.getNilaiRupiah()));
        Label rupiahLabel = new Label("Nilai Aset (Rupiah)");
        rupiahLabel.getStyleClass().add("form-label");

        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll("Sangat Baik", "Baik", "Cukup", "Rusak");
        kondisiCombo.setPromptText("Pilih kondisi aset");
        kondisiCombo.setValue(editableAsset == null ? "Baik" : editableAsset.getKondisi());
        Label kondisiLabel = new Label("Kondisi");
        kondisiLabel.getStyleClass().add("form-label");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Tersedia", "Digunakan", "Rusak", "Perbaikan");
        statusCombo.setPromptText("Pilih status aset");
        statusCombo.setValue(editableAsset == null ? "Tersedia" : editableAsset.getStatus());
        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("form-label");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 24, 24, 24));
        
        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modalStage.close());
        
        Button saveButton = new Button(editableAsset == null ? "Simpan" : "Simpan Perubahan");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            if (saveAsset(editableAsset, kodeField.getText(), jenisCombo.getValue(),
                    merkField.getText(), pemegangField.getText(), subditCombo.getValue(),
                    tanggalPicker.getValue(), rupiahField.getText(), kondisiCombo.getValue(), statusCombo.getValue())) {
                modalStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(0, 24, 0, 24));
        formContent.getChildren().addAll(
            kodeLabel, kodeField, kodeHint,
            jenisLabel, jenisCombo,
            merkLabel, merkField,
            pemegangLabel, pemegangField, pemegangHint,
            subditLabel, subditCombo,
            tanggalLabel, tanggalPicker,
            rupiahLabel, rupiahField,
            kondisiLabel, kondisiCombo,
            statusLabel, statusCombo
        );

        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(450);
        scrollPane.setMaxHeight(450);
        scrollPane.getStyleClass().add("modal-scroll-pane");
        
        modalContent.getChildren().addAll(headerBox, scrollPane, buttonBox);

        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private boolean saveAsset(Asset editableAsset, String kode, String jenis, String merk, String pemegang,
                              String subdit, LocalDate tanggal, String rupiah, String kondisi, String status) {
        if (kode == null || kode.trim().isEmpty() || kode.length() != 10) {
            showAlert("Kode Aset harus 10 digit");
            return false;
        }
        if (jenis == null || jenis.trim().isEmpty()) {
            showAlert("Pilih jenis aset");
            return false;
        }
        if (merk == null || merk.trim().isEmpty()) {
            showAlert("Masukkan merk barang");
            return false;
        }
        if (subdit == null || subdit.trim().isEmpty()) {
            showAlert("Pilih subdirektorat");
            return false;
        }
        if (tanggal == null) {
            showAlert("Pilih tanggal perolehan");
            return false;
        }
        double nilai;
        try {
            nilai = Double.parseDouble(rupiah == null || rupiah.isBlank() ? "0" : rupiah.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            showAlert("Nilai aset harus berupa angka");
            return false;
        }
        if (kondisi == null || kondisi.trim().isEmpty()) {
            showAlert("Pilih kondisi aset");
            return false;
        }
        if (status == null || status.trim().isEmpty()) {
            showAlert("Pilih status aset");
            return false;
        }

        if (editableAsset == null) {
            Asset asset = new Asset(
                kode.trim(),
                jenis,
                merk.trim(),
                pemegang == null ? "" : pemegang.trim(),
                subdit,
                tanggal,
                nilai,
                kondisi,
                status
            );
            dataService.addAsset(asset);
        } else {
            editableAsset.setJenisAset(jenis);
            editableAsset.setMerkBarang(merk.trim());
            editableAsset.setKeterangan(pemegang == null ? "" : pemegang.trim());
            editableAsset.setSubdit(subdit);
            editableAsset.setTanggalPerolehan(tanggal);
            editableAsset.setNilaiRupiah(nilai);
            editableAsset.setKondisi(kondisi);
            editableAsset.setStatus(status);
            dataService.updateAsset(editableAsset);
        }
        refreshTable();
        return true;
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
            .filter(asset -> 
                asset.getKodeAset().toLowerCase().contains(searchText.toLowerCase()) ||
                asset.getJenisAset().toLowerCase().contains(searchText.toLowerCase()) ||
                asset.getMerkBarang().toLowerCase().contains(searchText.toLowerCase()) ||
                asset.getKeterangan().toLowerCase().contains(searchText.toLowerCase())
            )
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

