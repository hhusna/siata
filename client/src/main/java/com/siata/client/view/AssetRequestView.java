package com.siata.client.view;

import com.siata.client.model.AssetRequest;
import com.siata.client.service.DataService;
import com.siata.client.session.LoginSession;
import com.siata.client.util.AnimationUtils;
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
        Button addPengajuanButton = new Button("+ Tambah Kebutuhan Aset");
        addPengajuanButton.getStyleClass().add("secondary-button");
        addPengajuanButton.setOnAction(e -> showAddPengajuanModal());

        Button addPermohonanButton = new Button("+ Tambah Permohonan");
        addPermohonanButton.getStyleClass().add("primary-button");
        addPermohonanButton.setOnAction(e -> showAddPermohonanModal());

        // Only show add buttons for TIM_MANAJEMEN_ASET role
        String currentRole = LoginSession.getRole();
        boolean canAdd = "TIM_MANAJEMEN_ASET".equals(currentRole);
        
        addPengajuanButton.setVisible(canAdd);
        addPengajuanButton.setManaged(canAdd);
        addPermohonanButton.setVisible(canAdd);
        addPermohonanButton.setManaged(canAdd);

        getChildren().add(buildPageHeader(addPengajuanButton, addPermohonanButton));

        // Permohonan Aset Section
        VBox permohonanSection = createTableSection("Permohonan Aset", permohonanTable, permohonanList, true);

        // Daftar Pengajuan Aset Section
        VBox pengajuanSection = createTableSection("Kebutuhan Aset", pengajuanTable, pengajuanList, false);

        getChildren().addAll(permohonanSection, pengajuanSection);
    }

    private Node buildPageHeader(Button addPengajuanButton, Button addPermohonanButton) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_RIGHT);

        HBox actions = new HBox(10, addPengajuanButton, addPermohonanButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().add(actions);
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

        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nomor, pemohon, atau jenis aset...");
        searchField.setPrefWidth(400);
        searchField.getStyleClass().add("filter-search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, list, isPermohonan));
        filterBar.getChildren().add(searchField);

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

        TableColumn<AssetRequest, Void> aksiCol = buildActionColumn();

        table.getColumns().setAll(List.of(nomorCol, tanggalCol, pemohonCol, unitCol, jenisAsetCol, jumlahCol, aksiCol));

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
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.setTitle(editableRequest == null
                ? ("Permohonan".equals(tipe) ? "Tambah Permohonan" : "Tambah Pengajuan")
                : "Edit " + tipe);

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(800);
        modalContent.setMaxWidth(800);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(editableRequest == null
                ? ("Permohonan".equals(tipe) ? "Tambah Permohonan" : "Tambah Kebutuhan Aset")
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

        // Get logged-in user data
        com.siata.client.dto.PegawaiDto loggedInUser = com.siata.client.session.LoginSession.getPegawaiDto();
        String loggedInNama = loggedInUser != null ? loggedInUser.getNama() : "";
        String loggedInUnit = loggedInUser != null ? loggedInUser.getNamaSubdir() : "";
        
        // For Permohonan: allow manual input (TIM_MANAJEMEN_ASET records for others)
        boolean isPengajuan = "Pengajuan".equals(tipe);
        
        // Searchable Dropdown Components
        VBox pemohonContainer = new VBox(8);
        TextField searchField = new TextField();
        searchField.setPromptText("Cari nama atau NIP pegawai...");
        ListView<com.siata.client.model.Employee> employeeListView = new ListView<>();
        employeeListView.setPrefHeight(120);
        Label selectedEmployeeLabel = new Label("Belum ada pegawai dipilih");
        selectedEmployeeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        
        // State for selected employee
        final com.siata.client.model.Employee[] selectedEmployee = {null};
        
        // Populate and Filter Logic
        List<com.siata.client.model.Employee> allEmployees = dataService.getEmployees();
        ObservableList<com.siata.client.model.Employee> filteredEmployees = FXCollections.observableArrayList(allEmployees);
        employeeListView.setItems(filteredEmployees);
        
        employeeListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(com.siata.client.model.Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                if (empty || emp == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(2);
                    Label nameLabel = new Label(emp.getNamaLengkap());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
                    Label nipLabel = new Label(emp.getNip());
                    nipLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
                    container.getChildren().addAll(nameLabel, nipLabel);
                    setGraphic(container);
                }
            }
        });
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredEmployees.clear();
            if (newVal == null || newVal.isEmpty()) {
                filteredEmployees.addAll(allEmployees);
            } else {
                String lower = newVal.toLowerCase();
                allEmployees.stream()
                    .filter(e -> e.getNamaLengkap().toLowerCase().contains(lower) || e.getNip().contains(newVal))
                    .forEach(filteredEmployees::add);
            }
        });
        
        employeeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedEmployee[0] = newVal;
                selectedEmployeeLabel.setText("✓ " + newVal.getNamaLengkap());
                selectedEmployeeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
            }
        });

        Label namaLabelObj = new Label("Permohonan".equals(tipe) ? "Nama Pemohon" : "Nama Pengaju");
        namaLabelObj.getStyleClass().add("form-label");
        
        pemohonContainer.getChildren().addAll(searchField, employeeListView, selectedEmployeeLabel);

        // Info display for Pengajuan (auto-filled from login)
        Label infoNamaLabel = new Label("Nama Pengaju");
        infoNamaLabel.getStyleClass().add("form-label");
        Label infoNamaValue = new Label(loggedInNama);
        infoNamaValue.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 8 12; -fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        
        Label infoUnitLabel = new Label("Subdirektorat");
        infoUnitLabel.getStyleClass().add("form-label");
        Label infoUnitValue = new Label(loggedInUnit);
        infoUnitValue.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 8 12; -fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        
        if (!isPengajuan) {
            // Permohonan - manual input via search
            if (editableRequest != null) {
                // If editing, try to pre-select
                String currentNama = editableRequest.getPemohon();
                allEmployees.stream()
                    .filter(e -> e.getNamaLengkap().equalsIgnoreCase(currentNama))
                    .findFirst()
                    .ifPresent(emp -> {
                        selectedEmployee[0] = emp;
                        employeeListView.getSelectionModel().select(emp);
                        selectedEmployeeLabel.setText("✓ " + emp.getNamaLengkap());
                        selectedEmployeeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    });
            }
        }

        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");
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

        DatePicker tanggalPicker = new DatePicker(editableRequest == null ? LocalDate.now() : editableRequest.getTanggal());
        Label tanggalLabel = new Label("Tanggal");
        tanggalLabel.getStyleClass().add("form-label");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 24, 24, 24));

        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modalStage.close());

        // For Pengajuan, use login session data; for Permohonan, use form input
        Button saveButton = new Button(editableRequest == null ? "Simpan" : "Simpan Perubahan");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            String nama = "";
            String unit = "";
            
            if (isPengajuan) {
                // Mode Edit: use existing data from editableRequest
                // Mode Tambah: use login session
                if (editableRequest != null) {
                    nama = editableRequest.getPemohon(); // Keep original pemohon NIP
                    unit = editableRequest.getUnit(); // Keep original unit
                } else {
                    nama = loggedInUser != null ? String.valueOf(loggedInUser.getNip()) : "";
                    unit = loggedInUnit;
                }
            } else {
                // Permohonan: always use selected employee
                if (selectedEmployee[0] != null) {
                    nama = selectedEmployee[0].getNip(); // Send NIP
                    unit = selectedEmployee[0].getUnit(); // Send Unit from employee
                }
            }
            
            // For Pengajuan: use empty deskripsi
            String deskripsiValue = isPengajuan ? "" : deskripsiArea.getText();
            
            if (saveAssetRequest(editableRequest, tipe, nama, unit, jenisCombo.getValue(),
                    jumlahField.getText(), deskripsiValue, tujuanArea.getText(),
                    tanggalPicker.getValue())) {
                modalStage.close();
            }
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // Two-column grid layout for form fields
        HBox formGrid = new HBox(32);
        formGrid.setPadding(new Insets(0, 24, 16, 24));
        
        // Left Column: Requester info and asset selection
        VBox leftColumn = new VBox(12);
        leftColumn.setPrefWidth(350);
        leftColumn.setMaxWidth(350);
        
        // Right Column: Description and details
        VBox rightColumn = new VBox(12);
        rightColumn.setPrefWidth(350);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        
        // Set max widths for form controls
        jenisCombo.setMaxWidth(Double.MAX_VALUE);
        tanggalPicker.setMaxWidth(Double.MAX_VALUE);
        deskripsiArea.setPrefHeight(80);
        tujuanArea.setPrefHeight(80);
        
        // Add different fields based on mode
        if (isPengajuan) {
            // Pengajuan: show info labels (non-editable) for NEW only, editable for EDIT
            if (editableRequest == null) {
                // Mode Tambah: show non-editable info
                VBox namaInfoBox = new VBox(8);
                namaInfoBox.getChildren().addAll(infoNamaLabel, infoNamaValue);
                
                VBox unitInfoBox = new VBox(8);
                unitInfoBox.getChildren().addAll(infoUnitLabel, infoUnitValue);
                
                VBox jenisInputBox = new VBox(8);
                jenisInputBox.getChildren().addAll(jenisLabel, jenisCombo);
                
                VBox jumlahInputBox = new VBox(8);
                jumlahInputBox.getChildren().addAll(jumlahLabel, jumlahField);
                
                leftColumn.getChildren().addAll(namaInfoBox, unitInfoBox, jenisInputBox, jumlahInputBox);
            } else {
                // Mode Edit: show editable fields only (jenis dan jumlah)
                VBox jenisInputBox = new VBox(8);
                jenisInputBox.getChildren().addAll(jenisLabel, jenisCombo);
                
                VBox jumlahInputBox = new VBox(8);
                jumlahInputBox.getChildren().addAll(jumlahLabel, jumlahField);
                
                leftColumn.getChildren().addAll(jenisInputBox, jumlahInputBox);
            }
        } else {
            // Permohonan: show searchable dropdown (no unit combo)
            VBox namaInputBox = new VBox(8);
            namaInputBox.getChildren().addAll(namaLabelObj, pemohonContainer);
            
            VBox jenisInputBox = new VBox(8);
            jenisInputBox.getChildren().addAll(jenisLabel, jenisCombo);
            
            VBox jumlahInputBox = new VBox(8);
            jumlahInputBox.getChildren().addAll(jumlahLabel, jumlahField);
            
            leftColumn.getChildren().addAll(namaInputBox, jenisInputBox, jumlahInputBox);
        }
        
        // Right column content - Deskripsi only for Permohonan
        VBox tujuanInputBox = new VBox(8);
        tujuanInputBox.getChildren().addAll(tujuanLabel, tujuanArea);
        
        VBox tanggalInputBox = new VBox(8);
        tanggalInputBox.getChildren().addAll(tanggalLabel, tanggalPicker);
        
        if (isPengajuan) {
            // Pengajuan: only show Tujuan and Tanggal
            rightColumn.getChildren().addAll(tujuanInputBox, tanggalInputBox);
        } else {
            // Permohonan: show all fields including Deskripsi
            VBox deskripsiInputBox = new VBox(8);
            deskripsiInputBox.getChildren().addAll(deskripsiLabel, deskripsiArea);
            
            rightColumn.getChildren().addAll(deskripsiInputBox, tujuanInputBox, tanggalInputBox);
        }
        
        formGrid.getChildren().addAll(leftColumn, rightColumn);

        modalContent.getChildren().addAll(headerBox, formGrid, buttonBox);

        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(modalScene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private boolean saveAssetRequest(AssetRequest editableRequest, String tipe, String nip, String subdir, String jenis, String jumlahStr,
                                     String deskripsi, String tujuan, LocalDate tanggal) {
        boolean isPengajuan = "Pengajuan".equals(tipe);
        
        // Validation Logic
        if (!isPengajuan) {
            if (nip == null || nip.trim().isEmpty()) {
                showAlert("Pilih pemohon dari daftar pegawai");
                return false;
            }
            if (subdir == null || subdir.trim().isEmpty()) {
                showAlert("Pilih subdirektorat (otomatis dari pegawai)");
                return false;
            }
            
            // Validasi: cek apakah NIP pegawai terdaftar di subdirektorat yang dipilih
            boolean nipDitemukan = dataService.getEmployees().stream()
                .anyMatch(emp -> emp.getNip().equals(nip.trim()) && 
                               emp.getUnit().equals(subdir));
            
            if (!nipDitemukan) {
                showAlert("NIP pemohon '" + nip.trim() + "' tidak terdaftar di subdirektorat " + subdir);
                return false;
            }
        }
        if (jenis == null || jenis.trim().isEmpty()) {
            showAlert("Pilih jenis aset");
            return false;
        }
        if (tanggal == null) {
            showAlert("Pilih tanggal permohonan/pengajuan");
            return false;
        }
        
        // Deskripsi validation
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
                    nip.trim(),
                    subdir,
                    jenis,
                    jumlah,
                    null, // Priority is removed
                    tipe,
                    deskripsi == null ? "" : deskripsi.trim(),
                    tujuan == null ? "" : tujuan.trim()
            );
            dataService.addAssetRequest(request);
        } else {
            editableRequest.setTanggal(tanggal);
            editableRequest.setPemohon(nip.trim()); // Now stores NIP
            editableRequest.setUnit(subdir);
            editableRequest.setJenisAset(jenis);
            editableRequest.setJumlah(jumlah);
            // Prioritas removed
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

    private void filterTable(String searchText, ObservableList<AssetRequest> list, boolean isPermohonan) {
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
            private final HBox actionBox = new HBox(6);

            {
                actionBox.setAlignment(Pos.CENTER);
                
                // Check role permissions
                String currentRole = LoginSession.getRole();
                boolean canEdit = "TIM_MANAJEMEN_ASET".equals(currentRole);
                
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
                
                // Add buttons based on role
                actionBox.getChildren().add(detailButton);
                if (canEdit) {
                    actionBox.getChildren().addAll(editButton, deleteButton);
                }
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
        modalStage.initStyle(StageStyle.TRANSPARENT);
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
        
        int nextRow = 6;
        // Prioritas removed
        // Deskripsi only for Permohonan (or check if Pengajuan needs it too? Pengajuan usually uses Deskripsi too per DTO)
        // Checks above show PengajuanDto has Deskripsi.
        // Logic below was: if Permohonan, show Deskripsi. 
        // Let's assume Deskripsi is valid for both or just Permohonan?
        // Logic was: if Permohonan { add Prioritas; add Deskripsi; }
        // If I strictly follow: simple remove Prioritas line.
        if ("Permohonan".equals(request.getTipe())) {
             addDetailRow(grid, nextRow++, "Deskripsi", request.getDeskripsi());
        }
        addDetailRow(grid, nextRow, "Tujuan Penggunaan", request.getTujuanPenggunaan());

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setMaxHeight(400);
        scrollPane.getStyleClass().add("modal-scroll-pane");

        modalContent.getChildren().addAll(headerBox, scrollPane);

        Scene scene = new Scene(modalContent);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
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
