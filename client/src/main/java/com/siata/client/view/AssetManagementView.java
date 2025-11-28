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

import java.math.BigDecimal;
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

        Button cleanDuplicatesButton = new Button("🧹 Bersihkan Duplikat");
        cleanDuplicatesButton.getStyleClass().add("secondary-button");
        cleanDuplicatesButton.setOnAction(e -> cleanDuplicates());

        HBox actionButtons = new HBox(12, addButton, cleanDuplicatesButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        getChildren().add(buildPageHeader(actionButtons));

        // Search and filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Semua Jenis", "Mobil", "Motor", "Scanner", "PC","Laptop", "Tablet","Printer","Speaker", "Parabot");
        jenisCombo.setValue("Semua Jenis");
        jenisCombo.setPrefWidth(150);
        jenisCombo.getStyleClass().add("filter-combo-box");
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Semua Status", "Aktif", "Non Aktif");
        statusCombo.setValue("Semua Status");
        statusCombo.setPrefWidth(150);
        statusCombo.getStyleClass().add("filter-combo-box");
        
        // Filter Kesiapan Lelang - hanya untuk TIM_MANAJEMEN_ASET
        ComboBox<String> kesiapanLelangCombo = new ComboBox<>();
        kesiapanLelangCombo.getItems().addAll("Semua Kesiapan", "Siap", "Belum");
        kesiapanLelangCombo.setValue("Semua Kesiapan");
        kesiapanLelangCombo.setPrefWidth(150);
        kesiapanLelangCombo.getStyleClass().add("filter-combo-box");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan ID, nama, atau jenis aset...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, jenisCombo.getValue(), statusCombo.getValue(), kesiapanLelangCombo.getValue()));
        searchField.getStyleClass().add("filter-combo-box");
        
        jenisCombo.setOnAction(e -> filterTable(searchField.getText(), jenisCombo.getValue(), statusCombo.getValue(), kesiapanLelangCombo.getValue()));
        statusCombo.setOnAction(e -> filterTable(searchField.getText(), jenisCombo.getValue(), statusCombo.getValue(), kesiapanLelangCombo.getValue()));
        kesiapanLelangCombo.setOnAction(e -> filterTable(searchField.getText(), jenisCombo.getValue(), statusCombo.getValue(), kesiapanLelangCombo.getValue()));
        
        // Tambahkan filter ke filterBar berdasarkan role
        if ("TIM_MANAJEMEN_ASET".equals(com.siata.client.session.LoginSession.getRole())) {
            filterBar.getChildren().addAll(searchField, jenisCombo, statusCombo, kesiapanLelangCombo);
        } else {
            filterBar.getChildren().addAll(searchField, jenisCombo, statusCombo);
        }

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
        
        TableColumn<Asset, String> SubdirCol = new TableColumn<>("Subdir");
        SubdirCol.setCellValueFactory(new PropertyValueFactory<>("Subdir"));
        
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
        
        // Kolom Kesiapan Lelang - hanya untuk TIM_MANAJEMEN_ASET
        TableColumn<Asset, String> kesiapanLelangCol = new TableColumn<>("Kesiapan Lelang");
        kesiapanLelangCol.setCellValueFactory(new PropertyValueFactory<>("kesiapanLelang"));
        kesiapanLelangCol.setCellFactory(column -> new TableCell<Asset, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Styling berdasarkan status
                    if ("Siap".equals(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });
        
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
                    // Validasi: Aset Aktif tidak bisa dihapus
                    if ("Aktif".equals(asset.getStatus())) {
                        showAlert("Aset dengan status Aktif tidak dapat dihapus. Ubah status menjadi Non Aktif terlebih dahulu.");
                        return;
                    }
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
        
        // Tambahkan kolom ke tabel berdasarkan role
        if ("TIM_MANAJEMEN_ASET".equals(com.siata.client.session.LoginSession.getRole())) {
            table.getColumns().setAll(List.of(kodeCol, jenisCol, merkCol, keteranganCol, SubdirCol, tanggalCol,
                    rupiahCol, kondisiCol, statusCol, kesiapanLelangCol, aksiCol));
        } else {
            table.getColumns().setAll(List.of(kodeCol, jenisCol, merkCol, keteranganCol, SubdirCol, tanggalCol,
                    rupiahCol, kondisiCol, statusCol, aksiCol));
        }

        VBox tableContainer = new VBox(16);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getStyleClass().add("table-container");
        tableContainer.getChildren().addAll(filterBar, table);

        getChildren().addAll(tableContainer);
    }

    private Node buildPageHeader(HBox actionButtons) {
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

        header.getChildren().addAll(textGroup, spacer, actionButtons);
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
        modalStage.initStyle(StageStyle.UNDECORATED);
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
        }

        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Mobil", "Motor", "Scanner", "PC","Laptop", "Tablet","Printer","Speaker", "Parabot");
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
        pemegangField.setPromptText("NIP pegawai atau Kode Subdir");
        Label pemegangLabel = new Label("NIP Pemegang (Keterangan)");
        pemegangLabel.getStyleClass().add("form-label");
        Text pemegangHint = new Text("Kosongkan jika aset belum memiliki pemegang");
        pemegangHint.getStyleClass().add("form-hint");
        if (editableAsset != null) {
            pemegangField.setText(editableAsset.getKeterangan());
        }

        ComboBox<String> SubdirCombo = new ComboBox<>();
        SubdirCombo.getItems().addAll("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        SubdirCombo.setPromptText("Pilih subdirektorat");
        Label SubdirLabel = new Label("Subdirektorat");
        SubdirLabel.getStyleClass().add("form-label");
        if (editableAsset != null) {
            SubdirCombo.setValue(editableAsset.getSubdir());
        }

        DatePicker tanggalPicker = new DatePicker(editableAsset == null ? LocalDate.now() : editableAsset.getTanggalPerolehan());
        Label tanggalLabel = new Label("Tanggal Perolehan");
        tanggalLabel.getStyleClass().add("form-label");

        TextField rupiahField = new TextField(editableAsset == null ? "0" : editableAsset.getNilaiRupiah().toString());
        Label rupiahLabel = new Label("Nilai Aset (Rupiah)");
        rupiahLabel.getStyleClass().add("form-label");

        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll("Baik", "Rusak Ringan", "Rusak Berat", "Hilang", "Gudang");
        kondisiCombo.setPromptText("Pilih kondisi aset");
        kondisiCombo.setValue(editableAsset == null ? "Baik" : editableAsset.getKondisi());
        Label kondisiLabel = new Label("Kondisi");
        kondisiLabel.getStyleClass().add("form-label");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aktif", "Non Aktif");
        statusCombo.setPromptText("Pilih status aset");
        statusCombo.setValue(editableAsset == null ? "Aktif" : editableAsset.getStatus());
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
                    merkField.getText(), pemegangField.getText(), SubdirCombo.getValue(),
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
            SubdirLabel, SubdirCombo,
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
                              String Subdir, LocalDate tanggal, String rupiah, String kondisi, String status) {
        if (kode == null || kode.trim().isEmpty() || kode.length() != 10) {
            showAlert("Kode Aset harus 10 digit");
            return false;
        }
        if (!kode.matches("[0-9]+")) {
            showAlert("Kode Aset harus berupa angka");
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
        if (merk.trim().length() < 2) {
            showAlert("Merk barang minimal 2 karakter");
            return false;
        }
        if (Subdir == null || Subdir.trim().isEmpty()) {
            showAlert("Pilih subdirektorat");
            return false;
        }
        if (tanggal == null) {
            showAlert("Pilih tanggal perolehan");
            return false;
        }
        BigDecimal nilai;
        try {
            nilai = new BigDecimal(rupiah == null || rupiah.isBlank() ? "0" : rupiah.replaceAll("[^\\d.]", ""));
            if (nilai.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Harga aset tidak boleh negatif");
                return false;
            }
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

        // Validasi NIP jika diisi
        if (pemegang != null && !pemegang.trim().isEmpty()) {
            // Cek apakah input adalah angka (NIP)
            if (pemegang.matches("\\d+")) {
                // Validasi NIP ada di database dan sesuai dengan subdir yang dipilih
                if (!dataService.validateNipInSubdir(pemegang.trim(), Subdir)) {
                    showAlert("NIP tidak ditemukan atau tidak sesuai dengan subdirektorat yang dipilih");
                    return false;
                }
            }
            // Jika bukan angka (misalnya kode subdir), biarkan lolos tanpa validasi
        }

        if (editableAsset == null) {
            Asset asset = new Asset(
                kode.trim(),
                jenis,
                merk.trim(),
                pemegang == null ? "" : pemegang.trim(),
                Subdir,
                tanggal,
                nilai,
                kondisi,
                status
            );
            dataService.addAsset(asset);
        } else {
            editableAsset.setKodeAset(kode.trim()); // Fix: kode aset bisa diedit
            editableAsset.setJenisAset(jenis);
            editableAsset.setMerkBarang(merk.trim());
            editableAsset.setKeterangan(pemegang == null ? "" : pemegang.trim());
            editableAsset.setSubdir(Subdir);
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

    private void filterTable(String searchText, String jenisFilter, String statusFilter, String kesiapanLelangFilter) {
        List<Asset> allAssets = dataService.getAssets();
        
        assetList.setAll(allAssets.stream()
            .filter(asset -> {
                // Search filter
                if (searchText != null && !searchText.isEmpty()) {
                    String search = searchText.toLowerCase();
                    if (!asset.getKodeAset().toLowerCase().contains(search) &&
                        !asset.getJenisAset().toLowerCase().contains(search) &&
                        !asset.getMerkBarang().toLowerCase().contains(search) &&
                        !asset.getKeterangan().toLowerCase().contains(search)) {
                        return false;
                    }
                }
                
                // Jenis filter
                if (jenisFilter != null && !jenisFilter.equals("Semua Jenis")) {
                    if (!asset.getJenisAset().equals(jenisFilter)) {
                        return false;
                    }
                }
                
                // Status filter
                if (statusFilter != null && !statusFilter.equals("Semua Status")) {
                    if (!asset.getStatus().equals(statusFilter)) {
                        return false;
                    }
                }
                
                // Kesiapan Lelang filter - hanya untuk TIM_MANAJEMEN_ASET
                if ("TIM_MANAJEMEN_ASET".equals(com.siata.client.session.LoginSession.getRole())) {
                    if (kesiapanLelangFilter != null && !kesiapanLelangFilter.equals("Semua Kesiapan")) {
                        if (!asset.getKesiapanLelang().equals(kesiapanLelangFilter)) {
                            return false;
                        }
                    }
                }
                
                return true;
            })
            .toList()
        );
    }

    private boolean confirmDelete(Asset asset) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penghapusan");
        alert.setHeaderText("Hapus Aset");
        alert.setContentText("Apakah Anda yakin ingin menghapus aset " + asset.getNamaAset() + "?\n\n" +
                            "Kondisi: " + asset.getKondisi() + "\n" +
                            "Status: " + asset.getStatus());
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void cleanDuplicates() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Pembersihan Duplikat");
        confirmAlert.setHeaderText("Bersihkan Data Duplikat");
        confirmAlert.setContentText("Apakah Anda yakin ingin membersihkan semua data aset yang duplikat?\n\n" +
                                    "Sistem akan menghapus aset dengan semua field yang sama persis.\n" +
                                    "Aset pertama akan disimpan, duplikat akan dihapus.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Call API to clean duplicates
                com.siata.client.api.AssetApi assetApi = new com.siata.client.api.AssetApi();
                int deletedCount = assetApi.cleanDuplicates();
                
                refreshTable();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Berhasil");
                successAlert.setHeaderText("Pembersihan Selesai");
                successAlert.setContentText(deletedCount + " aset duplikat berhasil dihapus.");
                successAlert.showAndWait();
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Gagal Membersihkan Duplikat");
                errorAlert.setContentText("Terjadi kesalahan: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
}

