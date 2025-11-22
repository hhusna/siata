package com.siata.client.view;

import com.siata.client.model.AssetRequest;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssetRequestView extends VBox {

    private final TableView<AssetRequest> permohonanTable;
    private final TableView<AssetRequest> pengajuanTable;
    private final ObservableList<AssetRequest> permohonanList;
    private final ObservableList<AssetRequest> pengajuanList;
    private final DataService dataService;

    public AssetRequestView() {
        setSpacing(24);
        dataService = DataService.getInstance();
        permohonanList = FXCollections.observableArrayList();
        pengajuanList = FXCollections.observableArrayList();
        permohonanTable = new TableView<>();
        pengajuanTable = new TableView<>();

        buildView();
        refreshTables();
    }

    private void buildView() {
        Button addPengajuanButton = new Button("+ Tambah Pengajuan Aset");
        addPengajuanButton.getStyleClass().add("secondary-button");
        addPengajuanButton.setOnAction(e -> showAddPengajuanModal());

        Button addPermohonanButton = new Button("+ Tambah Permohonan");
        addPermohonanButton.getStyleClass().add("primary-button");
        addPermohonanButton.setOnAction(e -> showAddPermohonanModal());

        getChildren().add(buildPageHeader(addPengajuanButton, addPermohonanButton));

        // Permohonan Aset Section
        VBox permohonanSection = createTableSection("Permohonan Aset", permohonanTable, permohonanList, true);

        // Daftar Pengajuan Aset Section
        VBox pengajuanSection = createTableSection("Pengajuan Aset", pengajuanTable, pengajuanList, false);

        getChildren().addAll(permohonanSection, pengajuanSection);
    }

    private Node buildPageHeader(Button addPengajuanButton, Button addPermohonanButton) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(4);
        Label title = new Label("Pengajuan Aset");
        title.getStyleClass().add("page-intro-title");
        Label description = new Label("Catat dan kelola pengajuan & permohonan aset pegawai");
        description.getStyleClass().add("page-intro-description");
        textGroup.getChildren().addAll(title, description);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, addPengajuanButton, addPermohonanButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(textGroup, spacer, actions);
        return header;
    }

    private VBox createTableSection(String title, TableView<AssetRequest> table, ObservableList<AssetRequest> list, boolean isPermohonan) {
        VBox section = new VBox(16);
        section.setSpacing(16);

        // Section Header
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        // Search and filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> prioritasCombo = new ComboBox<>();
        prioritasCombo.getItems().addAll("Semua Prioritas", "Tinggi", "Sedang", "Rendah");
        prioritasCombo.setValue("Semua Prioritas");
        prioritasCombo.setPrefWidth(150);

        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nomor, pemohon, atau jenis aset...");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, list, isPermohonan, prioritasCombo.getValue()));

        prioritasCombo.setOnAction(e -> filterTable(searchField.getText(), list, isPermohonan, prioritasCombo.getValue()));

        filterBar.getChildren().addAll(searchField, prioritasCombo);

        // Table
        table.setItems(list);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");

        TableColumn<AssetRequest, String> nomorCol = new TableColumn<>(isPermohonan ? "No. Permohonan" : "No. Pengajuan");
        nomorCol.setCellValueFactory(new PropertyValueFactory<>("noPermohonan"));

        TableColumn<AssetRequest, String> pemohonCol = new TableColumn<>(isPermohonan ? "Pemohon" : "Pengaju");
        pemohonCol.setCellValueFactory(new PropertyValueFactory<>("pemohon"));

        TableColumn<AssetRequest, String> tanggalCol = new TableColumn<>("Tanggal");
        tanggalCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggal();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        date.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        TableColumn<AssetRequest, String> unitCol = new TableColumn<>("Subdir");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<AssetRequest, String> jenisAsetCol = new TableColumn<>("Jenis Aset");
        jenisAsetCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));

        TableColumn<AssetRequest, String> jumlahCol = new TableColumn<>("Jumlah");
        jumlahCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getJumlah())
                )
        );

        TableColumn<AssetRequest, String> prioritasCol = new TableColumn<>("Prioritas");
        prioritasCol.setCellValueFactory(new PropertyValueFactory<>("prioritas"));
        prioritasCol.setCellFactory(column -> new TableCell<AssetRequest, String>() {
            @Override
            protected void updateItem(String prioritas, boolean empty) {
                super.updateItem(prioritas, empty);
                if (empty || prioritas == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(prioritas);
                    badge.getStyleClass().add("priority-badge");
                    if (prioritas.equals("Tinggi")) {
                        badge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else if (prioritas.equals("Sedang")) {
                        badge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    } else if (prioritas.equals("Rendah")) {
                        badge.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    }
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(badge.getStyle() + " -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: 600;");
                    setGraphic(badge);
                }
            }
        });

        TableColumn<AssetRequest, Void> aksiCol = buildActionColumn();

        table.getColumns().setAll(List.of(nomorCol, tanggalCol, pemohonCol, unitCol, jenisAsetCol, jumlahCol, prioritasCol, aksiCol));

        VBox tableContainer = new VBox(16);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getStyleClass().add("table-container");
        tableContainer.getChildren().addAll(filterBar, table);

        section.getChildren().addAll(sectionTitle, tableContainer);
        return section;
    }

    private void showAddPengajuanModal() {
        showAssetRequestModal("Pengajuan", null);
    }

    private void showAddPermohonanModal() {
        showAssetRequestModal("Permohonan", null);
    }

    private void showAssetRequestModal(String tipe, AssetRequest editableRequest) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setTitle(editableRequest == null
                ? ("Permohonan".equals(tipe) ? "Tambah Permohonan" : "Tambah Pengajuan")
                : "Edit " + tipe);

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
        Label titleLabel = new Label(editableRequest == null
                ? ("Permohonan".equals(tipe) ? "Tambah Permohonan" : "Tambah Pengajuan Aset")
                : "Edit " + tipe);
        titleLabel.getStyleClass().add("modal-title");
        Label subtitle = new Label("Permohonan".equals(tipe)
                ? "Catat permohonan aset dari pegawai"
                : "Catat pengajuan aset dari pegawai");
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());

        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        TextField namaField = new TextField();
        namaField.setPromptText("Permohonan".equals(tipe) ? "Nama lengkap pemohon" : "Nama lengkap pengaju");
        Label namaLabelObj = new Label("Permohonan".equals(tipe) ? "Nama Pemohon" : "Nama Pengaju");
        namaLabelObj.getStyleClass().add("form-label");
        if (editableRequest != null) {
            namaField.setText(editableRequest.getPemohon());
        }

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        unitCombo.setPromptText("Pilih subdirektorat");
        Label unitLabel = new Label("Subdirektorat");
        unitLabel.getStyleClass().add("form-label");
        if (editableRequest != null) {
            unitCombo.setValue(editableRequest.getUnit());
        }

        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Mobil", "Motor", "Scanner", "PC","Laptop", "Tablet","Printer","Speaker", "Parabot");
        jenisCombo.setPromptText("Pilih jenis aset");
        Label jenisLabel = new Label("Jenis Aset");
        jenisLabel.getStyleClass().add("form-label");
        if (editableRequest != null) {
            jenisCombo.setValue(editableRequest.getJenisAset());
        }

        TextField jumlahField = new TextField("1");
        Label jumlahLabel = new Label("Jumlah");
        jumlahLabel.getStyleClass().add("form-label");
        if (editableRequest != null) {
            jumlahField.setText(String.valueOf(editableRequest.getJumlah()));
        }

        TextArea deskripsiArea = new TextArea();
        deskripsiArea.setPromptText("Deskripsi aset yang diajukan (opsional)");
        deskripsiArea.setPrefRowCount(3);
        Label deskripsiLabel = new Label("Deskripsi");
        deskripsiLabel.getStyleClass().add("form-label");
        if (editableRequest != null) {
            deskripsiArea.setText(editableRequest.getDeskripsi());
        }

        TextArea tujuanArea = new TextArea();
        tujuanArea.setPromptText("Jelaskan tujuan penggunaan aset...");
        tujuanArea.setPrefRowCount(3);
        Label tujuanLabel = new Label("Tujuan Penggunaan");
        tujuanLabel.getStyleClass().add("form-label");
        if (editableRequest != null) {
            tujuanArea.setText(editableRequest.getTujuanPenggunaan());
        }

        ComboBox<String> prioritasCombo = new ComboBox<>();
        prioritasCombo.getItems().addAll("Tinggi", "Sedang", "Rendah");
        prioritasCombo.setValue(editableRequest == null ? "Sedang" : editableRequest.getPrioritas());
        Label prioritasLabel = new Label("Prioritas");
        prioritasLabel.getStyleClass().add("form-label");

        DatePicker tanggalPicker = new DatePicker(editableRequest == null ? LocalDate.now() : editableRequest.getTanggal());
        Label tanggalLabel = new Label("Tanggal");
        tanggalLabel.getStyleClass().add("form-label");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 24, 24, 24));

        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modalStage.close());

        Button saveButton = new Button(editableRequest == null ? "Simpan" : "Simpan Perubahan");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            if (saveAssetRequest(editableRequest, tipe, namaField.getText(), unitCombo.getValue(), jenisCombo.getValue(),
                    jumlahField.getText(), deskripsiArea.getText(), tujuanArea.getText(),
                    prioritasCombo.getValue(), tanggalPicker.getValue())) {
                modalStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(0, 24, 0, 24));
        formContent.getChildren().addAll(
                namaLabelObj, namaField,
                unitLabel, unitCombo,
                jenisLabel, jenisCombo,
                jumlahLabel, jumlahField,
                deskripsiLabel, deskripsiArea,
                tujuanLabel, tujuanArea,
                prioritasLabel, prioritasCombo,
                tanggalLabel, tanggalPicker
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

    private boolean saveAssetRequest(AssetRequest editableRequest, String tipe, String nama, String unit, String jenis, String jumlahStr,
                                     String deskripsi, String tujuan, String prioritas, LocalDate tanggal) {
        if (nama == null || nama.trim().isEmpty()) {
            showAlert("Masukkan nama " + ("Permohonan".equals(tipe) ? "pemohon" : "pengaju"));
            return false;
        }
        if (nama.trim().length() < 3) {
            showAlert("Nama " + ("Permohonan".equals(tipe) ? "pemohon" : "pengaju") + " minimal 3 karakter");
            return false;
        }
        if (!nama.trim().matches("[a-zA-Z\\s]+")) {
            showAlert("Nama " + ("Permohonan".equals(tipe) ? "pemohon" : "pengaju") + " hanya boleh berisi huruf dan spasi");
            return false;
        }
        if (unit == null || unit.trim().isEmpty()) {
            showAlert("Pilih subdirektorat");
            return false;
        }
        if (jenis == null || jenis.trim().isEmpty()) {
            showAlert("Pilih jenis aset");
            return false;
        }
        if (tanggal == null) {
            showAlert("Pilih tanggal permohonan/pengajuan");
            return false;
        }
        if (prioritas == null || prioritas.trim().isEmpty()) {
            showAlert("Pilih prioritas");
            return false;
        }
        if (deskripsi != null && deskripsi.length() > 500) {
            showAlert("Deskripsi maksimal 500 karakter");
            return false;
        }
        if (tujuan != null && tujuan.length() > 500) {
            showAlert("Tujuan maksimal 500 karakter");
            return false;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
            if (jumlah < 1) {
                showAlert("Jumlah harus lebih dari 0");
                return false;
            }
            if (jumlah > 1000) {
                showAlert("Jumlah tidak boleh lebih dari 1000");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Jumlah harus berupa angka");
            return false;
        }

        if (editableRequest == null) {
            String noPermohonan = tipe.equals("Permohonan") 
                ? dataService.generatePermohonanNumber() 
                : dataService.generatePengajuanNumber();
            AssetRequest request = new AssetRequest(
                    noPermohonan,
                    tanggal,
                    nama.trim(),
                    unit,
                    jenis,
                    jumlah,
                    prioritas,
                    tipe,
                    deskripsi == null ? "" : deskripsi.trim(),
                    tujuan == null ? "" : tujuan.trim()
            );
            dataService.addAssetRequest(request);
        } else {
            editableRequest.setTanggal(tanggal);
            editableRequest.setPemohon(nama.trim());
            editableRequest.setUnit(unit);
            editableRequest.setJenisAset(jenis);
            editableRequest.setJumlah(jumlah);
            editableRequest.setPrioritas(prioritas);
            editableRequest.setDeskripsi(deskripsi == null ? "" : deskripsi.trim());
            editableRequest.setTujuanPenggunaan(tujuan == null ? "" : tujuan.trim());
            dataService.updateAssetRequest(editableRequest);
        }

        refreshTables();
        return true;
    }

    private void refreshTables() {
        permohonanList.setAll(dataService.getPermohonanAset());
        pengajuanList.setAll(dataService.getPengajuanAset());
    }

    private void filterTable(String searchText, ObservableList<AssetRequest> list, boolean isPermohonan, String prioritasFilter) {
        List<AssetRequest> source = isPermohonan ? dataService.getPermohonanAset() : dataService.getPengajuanAset();

        list.setAll(source.stream()
                .filter(request -> {
                    // Search filter
                    if (searchText != null && !searchText.isEmpty()) {
                        String search = searchText.toLowerCase();
                        if (!request.getNoPermohonan().toLowerCase().contains(search) &&
                            !request.getPemohon().toLowerCase().contains(search) &&
                            !request.getJenisAset().toLowerCase().contains(search)) {
                            return false;
                        }
                    }
                    
                    // Prioritas filter
                    if (prioritasFilter != null && !prioritasFilter.equals("Semua Prioritas")) {
                        if (!request.getPrioritas().equals(prioritasFilter)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .toList()
        );
    }

    private TableColumn<AssetRequest, Void> buildActionColumn() {
        TableColumn<AssetRequest, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(column -> new TableCell<>() {
            private final Button detailButton = createIconButton("👁");
            private final Button editButton = createIconButton("✏");
            private final Button deleteButton = createIconButton("🗑");
            private final HBox actionBox = new HBox(6, detailButton, editButton, deleteButton);

            {
                actionBox.setAlignment(Pos.CENTER);
                detailButton.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    showRequestDetail(request);
                });
                editButton.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    showAssetRequestModal(request.getTipe(), request);
                });
                deleteButton.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    if (confirmDelete(request)) {
                        dataService.deleteAssetRequest(request);
                        refreshTables();
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
        aksiCol.setPrefWidth(200);
        return aksiCol;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 8;");
        return button;
    }

    private void showRequestDetail(AssetRequest request) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setTitle("Detail " + request.getTipe());

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(480);
        modalContent.setMaxWidth(480);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);

        Label title = new Label("Detail " + request.getTipe());
        title.getStyleClass().add("modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());

        headerBox.getChildren().addAll(title, spacer, closeButton);

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(16);
        grid.setPadding(new Insets(0, 24, 0, 24));

        addDetailRow(grid, 0, "No. " + ("Permohonan".equals(request.getTipe()) ? "Permohonan" : "Pengajuan"), request.getNoPermohonan());
        addDetailRow(grid, 1, "Pemohon", request.getPemohon());
        addDetailRow(grid, 2, "Tanggal", request.getTanggal() != null ? request.getTanggal().format(DateTimeFormatter.ofPattern("d/M/yyyy")) : "-");
        addDetailRow(grid, 3, "Subdir", request.getUnit());
        addDetailRow(grid, 4, "Jenis Aset", request.getJenisAset());
        addDetailRow(grid, 5, "Jumlah", String.valueOf(request.getJumlah()) + " unit");
        addDetailRow(grid, 6, "Prioritas", request.getPrioritas());
        addDetailRow(grid, 7, "Deskripsi", request.getDeskripsi());
        addDetailRow(grid, 8, "Tujuan Penggunaan", request.getTujuanPenggunaan());

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setMaxHeight(400);
        scrollPane.getStyleClass().add("modal-scroll-pane");

        modalContent.getChildren().addAll(headerBox, scrollPane);

        Scene scene = new Scene(modalContent);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");
        Label valueNode = new Label(value == null || value.isEmpty() ? "-" : value);
        valueNode.setStyle("-fx-font-weight: 600;");
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private boolean confirmDelete(AssetRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penghapusan");
        alert.setHeaderText("Hapus " + request.getTipe());
        alert.setContentText("Apakah Anda yakin ingin menghapus data " + request.getNoPermohonan() + "?");
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
