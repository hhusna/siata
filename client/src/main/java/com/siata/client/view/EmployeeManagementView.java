package com.siata.client.view;

import com.siata.client.api.PegawaiApi;
import com.siata.client.component.PaginatedTableView;
import com.siata.client.dto.PegawaiDto;
import com.siata.client.model.Asset;
import com.siata.client.model.Employee;
import com.siata.client.service.DataService;
import com.siata.client.util.AnimationUtils;
import com.siata.client.util.ExcelHelper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeManagementView extends VBox {

    private PaginatedTableView<Employee> paginatedTable;
    private final DataService dataService;
    private final PegawaiApi pegawaiApi = new PegawaiApi();

    public EmployeeManagementView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        buildView();
        refreshTable();
    }

    private void buildView() {
        Button addButton = new Button("+ Tambah Pegawai");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> showEmployeeForm(null));

        Button importButton = new Button("📥 Import Excel");
        importButton.getStyleClass().add("secondary-button");
        importButton.setOnAction(e -> showImportModal());

        Button exportButton = new Button("📤 Export Excel");
        exportButton.getStyleClass().add("secondary-button");
        exportButton.setOnAction(e -> handleExport());

        Button deleteSelectedBtn = new Button("🗑 Force Delete");
        deleteSelectedBtn.getStyleClass().add("secondary-button");
        deleteSelectedBtn.setStyle("-fx-text-fill: #dc2626;");
        deleteSelectedBtn.setOnAction(e -> handleBulkDelete());

        getChildren().add(buildPageHeader(deleteSelectedBtn, exportButton, importButton, addButton));

        // Search and filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("Semua Subdir", "PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur", "PINDAH");
        unitCombo.setValue("Semua Subdir");
        unitCombo.setPrefWidth(150);
        unitCombo.getStyleClass().add("filter-combo-box");
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Semua Status", "AKTIF", "NONAKTIF");
        statusCombo.setValue("Semua Status");
        statusCombo.setPrefWidth(130);
        statusCombo.getStyleClass().add("filter-combo-box");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nama atau NIP...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("filter-search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, unitCombo.getValue(), statusCombo.getValue()));
        
        unitCombo.setOnAction(e -> filterTable(searchField.getText(), unitCombo.getValue(), statusCombo.getValue()));
        statusCombo.setOnAction(e -> filterTable(searchField.getText(), unitCombo.getValue(), statusCombo.getValue()));
        
        filterBar.getChildren().addAll(searchField, unitCombo, statusCombo);

        // Paginated Table with multi-selection
        paginatedTable = new PaginatedTableView<>();
        TableView<Employee> table = paginatedTable.getTable();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Employee, String> nipCol = new TableColumn<>("NIP");
        nipCol.setCellValueFactory(new PropertyValueFactory<>("nip"));
        nipCol.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Jika panjang NIP tidak 18 digit (berarti PPNPN/Generated ID), tampilkan "No NIP"
                    if (item.length() != 18) {
                        setText("No NIP");
                        setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
                    } else {
                        setText(item);
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Employee, String> namaCol = new TableColumn<>("Nama");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        
        TableColumn<Employee, String> unitCol = new TableColumn<>("Subdir");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setCellFactory(com.siata.client.util.SubdirUiUtils.createSubdirCellFactory());
        
        // Pre-compute asset count per employee NIP for performance
        java.util.Map<String, Long> assetCountByNip = new java.util.HashMap<>();
        for (var asset : dataService.getAssets()) {
            String nip = asset.getKeterangan();
            if (nip != null && !nip.isBlank()) {
                assetCountByNip.merge(nip, 1L, Long::sum);
            }
        }
        
        TableColumn<Employee, String> asetCol = new TableColumn<>("Aset yang Dimiliki");
        asetCol.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue();
            // Use pre-computed count instead of streaming all assets
            long jumlahAset = assetCountByNip.getOrDefault(emp.getNip(), 0L);
            return new javafx.beans.property.SimpleStringProperty(jumlahAset + " aset");
        });
        asetCol.setCellFactory(column -> new TableCell<>() {
            private final Hyperlink detailLink = new Hyperlink();
            {
                // Keep consistent blue color without underline
                detailLink.setStyle("-fx-text-fill: #2563eb; -fx-underline: false;");
                detailLink.setOnAction(e -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    showEmployeeAssets(employee);
                    // Reset visited state to keep original color
                    detailLink.setVisited(false);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    detailLink.setText(item);
                    detailLink.setVisited(false); // Ensure consistent color
                    setGraphic(detailLink);
                }
            }
        });

        TableColumn<Employee, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton("✏");
            private final Button deleteButton = createIconButton("🗑");
            private final HBox actionBox = new HBox(6, editButton, deleteButton);

            {
                actionBox.setAlignment(Pos.CENTER);
                editButton.setOnAction(e -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    showEmployeeForm(employee);
                });
                deleteButton.setOnAction(e -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    if (confirmDelete(employee)) {
                        dataService.deleteEmployee(employee);
                        dataService.clearEmployeeCache(); // Invalidate employee cache
                        refreshTable();
                        MainShellView.invalidateDataViews(); // Refresh Dashboard, Recapitulation
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
        aksiCol.setPrefWidth(150);
        
        // Status column with color styling
        TableColumn<Employee, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(com.siata.client.util.StatusUiUtils.createStatusCellFactory());
        
        table.getColumns().setAll(List.of(nipCol, namaCol, unitCol, statusCol, asetCol, aksiCol));

        VBox tableContainer = new VBox(16);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getStyleClass().add("table-container");
        VBox.setVgrow(paginatedTable, Priority.ALWAYS);
        tableContainer.getChildren().addAll(filterBar, paginatedTable);

        VBox.setVgrow(tableContainer, Priority.ALWAYS); // Grow to fill window
        VBox.setVgrow(tableContainer, Priority.ALWAYS); // Grow to fill window
        tableContainer.setMaxHeight(Double.MAX_VALUE); // Explicitly allow unbounded growth
        
        // Hardcode min-height to Window Height - Offset to force expansion
        javafx.stage.Stage primaryStage = com.siata.client.MainApplication.getPrimaryStage();
        if (primaryStage != null) {
            tableContainer.minHeightProperty().bind(primaryStage.heightProperty().subtract(180));
        }
        
        getChildren().addAll(tableContainer);
    }

    private Node buildPageHeader(Button... actionButtons) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_RIGHT);

        HBox buttonGroup = new HBox(8);
        buttonGroup.setAlignment(Pos.CENTER_RIGHT);
        buttonGroup.getChildren().addAll(actionButtons);

        header.getChildren().add(buttonGroup);
        return header;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        return button;
    }

    // ==================== IMPORT MODAL ====================

    private void showImportModal() {
        Stage modalStage = new Stage();
        modalStage.initOwner(com.siata.client.MainApplication.getPrimaryStage());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.setTitle("Import Data Pegawai");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(900);
        modalContent.setMaxWidth(900);
        modalContent.setPrefHeight(650);
        modalContent.getStyleClass().add("modal-content");

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        
        VBox titleBox = new VBox(4);
        Label title = new Label("Import Data Pegawai");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Import data pegawai dari file Excel (.xlsx)");
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());
        
        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        // File picker
        HBox filePickerBox = new HBox(12);
        filePickerBox.setAlignment(Pos.CENTER_LEFT);
        filePickerBox.setPadding(new Insets(0, 24, 16, 24));
        
        TextField filePathField = new TextField();
        filePathField.setPromptText("Pilih file Excel...");
        filePathField.setEditable(false);
        filePathField.setPrefWidth(400);
        HBox.setHgrow(filePathField, Priority.ALWAYS);
        
        Button browseButton = new Button("📁 Pilih File");
        browseButton.getStyleClass().add("secondary-button");
        
        Button downloadTemplateButton = new Button("📄 Download Template");
        downloadTemplateButton.getStyleClass().add("ghost-button");
        downloadTemplateButton.setOnAction(e -> downloadTemplate());
        
        filePickerBox.getChildren().addAll(filePathField, browseButton, downloadTemplateButton);

        // Data wrapper class for import
        class ImportRow {
            Employee employee;
            SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
            String status = "";
            
            ImportRow(Employee emp) {
                this.employee = emp;
            }
        }

        // Preview table (left)
        ObservableList<ImportRow> previewData = FXCollections.observableArrayList();
        TableView<ImportRow> previewTable = new TableView<>(previewData);
        previewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        previewTable.setEditable(true);

        TableColumn<ImportRow, Boolean> selectCol = new TableColumn<>("Pilih");
        selectCol.setCellValueFactory(cellData -> cellData.getValue().selected);
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);
        selectCol.setPrefWidth(50);

        TableColumn<ImportRow, String> prevNipCol = new TableColumn<>("NIP");
        prevNipCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().employee.getNip()));

        TableColumn<ImportRow, String> prevNamaCol = new TableColumn<>("Nama");
        prevNamaCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().employee.getNamaLengkap()));

        TableColumn<ImportRow, String> prevSubdirCol = new TableColumn<>("Subdirektorat");
        prevSubdirCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().employee.getUnit()));

        previewTable.getColumns().addAll(selectCol, prevNipCol, prevNamaCol, prevSubdirCol);

        // Result table (right)
        ObservableList<ImportRow> resultData = FXCollections.observableArrayList();
        TableView<ImportRow> resultTable = new TableView<>(resultData);
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ImportRow, String> resNipCol = new TableColumn<>("NIP");
        resNipCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().employee.getNip()));

        TableColumn<ImportRow, String> resNamaCol = new TableColumn<>("Nama");
        resNamaCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().employee.getNamaLengkap()));

        TableColumn<ImportRow, String> resSubdirCol = new TableColumn<>("Subdirektorat");
        resSubdirCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().employee.getUnit()));

        TableColumn<ImportRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Duplikat".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if ("Baru".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if ("Update".equals(item)) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<ImportRow, Void> removeCol = new TableColumn<>("Hapus");
        removeCol.setCellFactory(column -> new TableCell<>() {
            private final Button removeBtn = new Button("✕");
            {
                removeBtn.getStyleClass().add("ghost-button");
                removeBtn.setStyle("-fx-text-fill: #e74c3c;");
                removeBtn.setOnAction(e -> {
                    ImportRow row = getTableView().getItems().get(getIndex());
                    resultData.remove(row);
                    updateResultStatuses(resultData);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
        removeCol.setPrefWidth(60);

        resultTable.getColumns().addAll(resNipCol, resNamaCol, resSubdirCol, statusCol, removeCol);

        // Browse button action
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih File Excel");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            File file = fileChooser.showOpenDialog(modalStage);
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
                try {
                    List<Employee> employees = ExcelHelper.parseExcel(file);
                    previewData.clear();
                    for (Employee emp : employees) {
                        previewData.add(new ImportRow(emp));
                    }
                } catch (Exception ex) {
                    MainShellView.showError("Gagal membaca file Excel: " + ex.getMessage());
                }
            }
        });

        // Preview section
        VBox previewSection = new VBox(8);
        Label previewLabel = new Label("Data dari Excel");
        previewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox previewButtons = new HBox(8);
        Button selectAllBtn = new Button("Select All");
        selectAllBtn.getStyleClass().add("ghost-button");
        selectAllBtn.setOnAction(e -> previewData.forEach(r -> r.selected.set(true)));
        
        Button deselectAllBtn = new Button("Deselect All");
        deselectAllBtn.getStyleClass().add("ghost-button");
        deselectAllBtn.setOnAction(e -> previewData.forEach(r -> r.selected.set(false)));
        
        Button addSelectedBtn = new Button("Tambahkan →");
        addSelectedBtn.getStyleClass().add("primary-button");
        addSelectedBtn.setOnAction(e -> {
            List<ImportRow> selected = previewData.stream()
                .filter(r -> r.selected.get())
                .map(r -> new ImportRow(r.employee))
                .toList();
            resultData.addAll(selected);
            updateResultStatuses(resultData);
        });
        
        previewButtons.getChildren().addAll(selectAllBtn, deselectAllBtn, addSelectedBtn);
        previewSection.getChildren().addAll(previewLabel, previewTable, previewButtons);
        VBox.setVgrow(previewTable, Priority.ALWAYS);

        // Result section
        VBox resultSection = new VBox(8);
        Label resultLabel = new Label("Data yang Akan Disimpan");
        resultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox resultButtons = new HBox(8);
        Button removeDuplicatesBtn = new Button("🗑 Hapus Duplikat");
        removeDuplicatesBtn.getStyleClass().add("secondary-button");
        removeDuplicatesBtn.setStyle("-fx-text-fill: #e74c3c;");
        removeDuplicatesBtn.setOnAction(e -> {
            // Keep only first occurrence of each NIP
            Set<String> seenNips = new HashSet<>();
            List<ImportRow> toRemove = new ArrayList<>();
            for (ImportRow row : resultData) {
                if (seenNips.contains(row.employee.getNip())) {
                    toRemove.add(row);
                } else {
                    seenNips.add(row.employee.getNip());
                }
            }
            resultData.removeAll(toRemove);
            updateResultStatuses(resultData);
        });
        
        resultButtons.getChildren().addAll(removeDuplicatesBtn);
        resultSection.getChildren().addAll(resultLabel, resultTable, resultButtons);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        // Tables container
        HBox tablesBox = new HBox(16);
        tablesBox.setPadding(new Insets(0, 24, 16, 24));
        HBox.setHgrow(previewSection, Priority.ALWAYS);
        HBox.setHgrow(resultSection, Priority.ALWAYS);
        previewSection.setPrefWidth(400);
        resultSection.setPrefWidth(450);
        tablesBox.getChildren().addAll(previewSection, resultSection);
        VBox.setVgrow(tablesBox, Priority.ALWAYS);

        // Footer buttons
        HBox footerBox = new HBox(12);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new Insets(16, 24, 24, 24));
        
        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modalStage.close());
        
        Button updateButton = new Button("Update Database");
        updateButton.getStyleClass().add("primary-button");
        updateButton.setOnAction(e -> {
            if (resultData.isEmpty()) {
                MainShellView.showWarning("Tidak ada data untuk disimpan.");
                return;
            }
            
            // Check for duplicates (only for non-PPNPN with same NIP)
            long dupCount = resultData.stream().filter(r -> "Duplikat".equals(r.status)).count();
            if (dupCount > 0) {
                MainShellView.showWarning("Masih ada " + dupCount + " data duplikat. Hapus duplikat terlebih dahulu.");
                return;
            }
            
            // Convert to PegawaiDto list (use AtomicLong to ensure unique IDs for PPNPN in batch)
            java.util.concurrent.atomic.AtomicLong ppnpnIdCounter = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());
            
            List<PegawaiDto> dtoList = resultData.stream()
                .map(r -> {
                    PegawaiDto dto = new PegawaiDto();
                    String nipStr = r.employee.getNip();
                    
                    // Check if PPNPN (empty NIP)
                    if (nipStr == null || nipStr.trim().isEmpty()) {
                        // Generate unique ID for PPNPN (unique timestamp + sequence)
                        long generatedNip = ppnpnIdCounter.getAndIncrement();
                        dto.setNip(generatedNip);
                        dto.setIsPpnpn(true);
                    } else {
                        dto.setNip(Long.parseLong(nipStr));
                        dto.setIsPpnpn(false);
                    }
                    
                    dto.setNama(r.employee.getNamaLengkap());
                    dto.setNamaSubdir(r.employee.getUnit());
                    dto.setStatus(r.employee.getStatus() != null ? r.employee.getStatus() : "AKTIF");
                    return dto;
                })
                .collect(Collectors.toList());
            
            // Show loading overlay and run in background
            MainShellView.showLoading("Mengimpor " + dtoList.size() + " pegawai...");
            
            javafx.concurrent.Task<Integer> importTask = new javafx.concurrent.Task<>() {
                @Override
                protected Integer call() {
                    return pegawaiApi.batchAddPegawai(dtoList);
                }
            };
            
            importTask.setOnSucceeded(ev -> {
                MainShellView.hideLoading();
                int result = importTask.getValue();
                if (result >= 0) {
                    dataService.clearEmployeeCache();
                    MainShellView.showSuccess("Berhasil menyimpan " + result + " data pegawai.");
                    modalStage.close();
                    refreshTable();
                    MainShellView.invalidateDataViews();
                } else {
                    MainShellView.showError("Gagal menyimpan data ke database.");
                }
            });
            
            importTask.setOnFailed(ev -> {
                MainShellView.hideLoading();
                MainShellView.showError("Error saat mengimpor data: " + importTask.getException().getMessage());
            });
            
            new Thread(importTask).start();
        });
        
        footerBox.getChildren().addAll(cancelButton, updateButton);

        modalContent.getChildren().addAll(headerBox, filePickerBox, tablesBox, footerBox);

        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(modalScene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private void updateResultStatuses(ObservableList<? extends Object> resultData) {
        // Get existing NIPs from database
        Set<String> existingNips = dataService.getEmployees().stream()
            .map(Employee::getNip)
            .filter(nip -> nip != null && !nip.trim().isEmpty()) // Exclude PPNPN
            .collect(Collectors.toSet());
        
        // Count occurrences in result list (excluding empty NIPs / PPNPN)
        Map<String, Integer> nipCounts = new HashMap<>();
        for (Object obj : resultData) {
            if (obj instanceof EmployeeManagementView) continue; // Skip wrong types
            try {
                java.lang.reflect.Field empField = obj.getClass().getDeclaredField("employee");
                empField.setAccessible(true);
                Employee emp = (Employee) empField.get(obj);
                String nip = emp.getNip();
                // Only count non-empty NIPs (skip PPNPN)
                if (nip != null && !nip.trim().isEmpty()) {
                    nipCounts.merge(nip, 1, Integer::sum);
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
        
        // Update statuses
        Set<String> seenNips = new HashSet<>();
        for (Object obj : resultData) {
            try {
                java.lang.reflect.Field empField = obj.getClass().getDeclaredField("employee");
                java.lang.reflect.Field statusField = obj.getClass().getDeclaredField("status");
                empField.setAccessible(true);
                statusField.setAccessible(true);
                
                Employee emp = (Employee) empField.get(obj);
                String nip = emp.getNip();
                
                // PPNPN (empty NIP) - always mark as "Baru (PPNPN)"
                if (nip == null || nip.trim().isEmpty()) {
                    statusField.set(obj, "Baru (PPNPN)");
                    continue;
                }
                
                if (seenNips.contains(nip)) {
                    statusField.set(obj, "Duplikat");
                } else if (nipCounts.getOrDefault(nip, 0) > 1) {
                    seenNips.add(nip);
                    // First occurrence, others will be marked as duplicate
                    if (existingNips.contains(nip)) {
                        statusField.set(obj, "Update");
                    } else {
                        statusField.set(obj, "Baru");
                    }
                } else if (existingNips.contains(nip)) {
                    statusField.set(obj, "Update");
                    seenNips.add(nip);
                } else {
                    statusField.set(obj, "Baru");
                    seenNips.add(nip);
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    // ==================== EXPORT ====================

    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan File Excel");
        fileChooser.setInitialFileName("data_pegawai.xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                List<Employee> allEmployees = dataService.getEmployees();
                ExcelHelper.exportToExcel(allEmployees, file);
                MainShellView.showSuccess("Data berhasil diekspor ke:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                MainShellView.showError("Gagal mengekspor data: " + e.getMessage());
            }
        }
    }

    private void downloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Template Excel");
        fileChooser.setInitialFileName("template_pegawai.xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                ExcelHelper.createTemplate(file);
                MainShellView.showSuccess("Template berhasil disimpan ke:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                MainShellView.showError("Gagal membuat template: " + e.getMessage());
            }
        }
    }

    // ==================== EXISTING METHODS ====================

    private void showEmployeeForm(Employee editableEmployee) {
        Stage modalStage = new Stage();
        modalStage.initOwner(com.siata.client.MainApplication.getPrimaryStage());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.setTitle(editableEmployee == null ? "Tambah Pegawai Baru" : "Edit Pegawai");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(700);
        modalContent.setMaxWidth(700);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        VBox titleBox = new VBox(4);
        Label title = new Label(editableEmployee == null ? "Tambah Pegawai Baru" : "Edit Pegawai");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label(editableEmployee == null
            ? "Masukkan informasi pegawai yang akan ditambahkan"
            : "Perbarui informasi pegawai");
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());
        
        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        TextField nipField = new TextField();
        nipField.setPromptText("Contoh: 199001152015011001 (Kosongkan jika PPNPN)");
        Label nipLabel = new Label("NIP (Opsional)");
        nipLabel.getStyleClass().add("form-label");
        
        CheckBox notAsnCheckbox = new CheckBox("Bukan ASN");
        notAsnCheckbox.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Store original NIP for edit case
        final String originalNip = editableEmployee != null ? editableEmployee.getNip() : null;
        final boolean wasOriginallyPpnpn = editableEmployee != null && editableEmployee.isPpnpn();
        
        if (editableEmployee != null) {
            // For PPNPN employees, show empty field; for others, show NIP
            if (!wasOriginallyPpnpn) {
                nipField.setText(editableEmployee.getNip());
                notAsnCheckbox.setSelected(false);
            } else {
                notAsnCheckbox.setSelected(true);
            }
            // NIP field is now editable (removed setDisable(true))
        }

        TextField namaField = new TextField();
        namaField.setPromptText("Contoh: Budi Santoso");
        Label namaLabel = new Label("Nama Lengkap");
        namaLabel.getStyleClass().add("form-label");
        if (editableEmployee != null) {
            namaField.setText(editableEmployee.getNamaLengkap());
        }

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur", "PINDAH");
        unitCombo.setPromptText("Pilih unit");
        unitCombo.setMaxWidth(Double.MAX_VALUE);
        Label unitLabel = new Label("Unit / Subdirektorat");
        unitLabel.getStyleClass().add("form-label");
        if (editableEmployee != null) {
            unitCombo.setValue(editableEmployee.getUnit());
        }

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("AKTIF", "NONAKTIF");
        statusCombo.setPromptText("Pilih status");
        statusCombo.setValue("AKTIF");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        Label statusLabel = new Label("Status Pegawai");
        statusLabel.getStyleClass().add("form-label");
        if (editableEmployee != null && editableEmployee.getStatus() != null) {
            statusCombo.setValue(editableEmployee.getStatus());
        }

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 24, 24, 24));
        
        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modalStage.close());
        
        Button saveButton = new Button(editableEmployee == null ? "Simpan" : "Simpan Perubahan");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            String nipInput = nipField.getText();
            String nama = namaField.getText();
            String unit = unitCombo.getValue();
            String status = statusCombo.getValue();

            // Determine if this is PPNPN based on Checkbox
            boolean isNotAsn = notAsnCheckbox.isSelected();
            boolean isNipEmpty = (nipInput == null || nipInput.trim().isEmpty());
            
            // For edit mode: if NIP is empty, use original NIP (no change)
            String finalNip;
            boolean isPpnpn;
            
            if (isNotAsn) {
                // Relaxed Validation for Non-ASN
                if (!isNipEmpty && !nipInput.matches("\\d+")) {
                    MainShellView.showWarning("NIP/ID harus berupa angka");
                    return;
                }
                // No length check for Non-ASN
                
                if (editableEmployee != null && isNipEmpty) {
                    finalNip = originalNip; // Keep existing if empty
                } else if (isNipEmpty) {
                    finalNip = null; // Auto-generate if new and empty
                } else {
                    finalNip = nipInput;
                }
                isPpnpn = true;
            } else {
                // Strict Validation for ASN
                if (editableEmployee != null && isNipEmpty) {
                     // Keep original NIP when editing and field is left empty
                     finalNip = originalNip;
                     isPpnpn = wasOriginallyPpnpn; // Should mostly be false here if checkbox unchecked
                } else if (isNipEmpty) {
                    // Cannot be empty if ASN
                    MainShellView.showWarning("NIP wajib diisi untuk ASN");
                    return;
                } else {
                     // Validate NIP format
                    if (!nipInput.matches("\\d+")) {
                        MainShellView.showWarning("NIP harus berupa angka");
                        return;
                    }
                    if (nipInput.length() != 18) {
                        MainShellView.showWarning("NIP harus 18 digit");
                        return;
                    }
                    finalNip = nipInput;
                    isPpnpn = false;
                }
            }

            if (nama == null || nama.trim().isEmpty()) {
                MainShellView.showWarning("Nama tidak boleh kosong");
                return;
            }
            if (unit == null || unit.trim().isEmpty()) {
                MainShellView.showWarning("Pilih subdirektorat");
                return;
            }
            if (status == null || status.trim().isEmpty()) {
                MainShellView.showWarning("Pilih status pegawai");
                return;
            }

            // Validasi nama minimal 3 karakter
            if (nama.length() < 3) {
                MainShellView.showWarning("Nama minimal 3 karakter");
                return;
            }

            // Show loading overlay
            MainShellView.showLoading(editableEmployee == null ? "Menambahkan pegawai..." : "Memperbarui pegawai...");
            
            // Prepare DTO
            final PegawaiDto dto = new PegawaiDto();
            if (editableEmployee == null) {
                // Add new
                if (isPpnpn) {
                    dto.setNip(System.currentTimeMillis());
                    dto.setIsPpnpn(true);
                } else {
                    dto.setNip(Long.parseLong(finalNip));
                    dto.setIsPpnpn(false);
                }
            } else {
                // Edit existing
                try {
                    dto.setNip(Long.parseLong(finalNip));
                } catch (NumberFormatException ex) {
                    dto.setNip(0L);
                }
                dto.setIsPpnpn(isPpnpn);
            }
            dto.setNama(nama);
            dto.setNamaSubdir(unit);
            dto.setStatus(status);
            
            final boolean isNewEmployee = (editableEmployee == null);
            final String origNip = originalNip;
            
            javafx.concurrent.Task<Boolean> saveTask = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    if (isNewEmployee) {
                        return pegawaiApi.addPegawai(dto);
                    } else {
                        return pegawaiApi.updatePegawai(origNip, dto);
                    }
                }
            };
            
            saveTask.setOnSucceeded(ev -> {
                MainShellView.hideLoading();
                if (saveTask.getValue()) {
                    dataService.clearEmployeeCache();
                    MainShellView.showSuccess(isNewEmployee ? "Pegawai berhasil ditambahkan" : "Data pegawai berhasil diperbarui");
                    modalStage.close();
                    refreshTable();
                    MainShellView.invalidateDataViews();
                } else {
                    MainShellView.showError("Gagal menyimpan data pegawai");
                }
            });
            
            saveTask.setOnFailed(ev -> {
                MainShellView.hideLoading();
                MainShellView.showError("Error: " + saveTask.getException().getMessage());
            });
            
            new Thread(saveTask).start();
        });
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        // Two-column grid layout for form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(24);
        formGrid.setVgap(16);
        formGrid.setPadding(new Insets(0, 24, 0, 24));
        
        // Column constraints for equal width columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col1, col2);
        
        // Left column: NIP and Unit
        VBox nipBox = new VBox(8);
        nipBox.getChildren().addAll(nipLabel, nipField, notAsnCheckbox);
        
        VBox unitBox = new VBox(8);
        unitBox.getChildren().addAll(unitLabel, unitCombo);
        
        // Right column: Nama and Status
        VBox namaBox = new VBox(8);
        namaBox.getChildren().addAll(namaLabel, namaField);
        
        VBox statusBox = new VBox(8);
        statusBox.getChildren().addAll(statusLabel, statusCombo);
        
        // Row 0: NIP and Nama
        formGrid.add(nipBox, 0, 0);
        formGrid.add(namaBox, 1, 0);
        
        // Row 1: Unit and Status
        formGrid.add(unitBox, 0, 1);
        formGrid.add(statusBox, 1, 1);
        
        modalContent.getChildren().addAll(headerBox, formGrid, buttonBox);

        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(modalScene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }


    private void showEmployeeAssets(Employee employee) {
        Stage modalStage = new Stage();
        modalStage.initOwner(com.siata.client.MainApplication.getPrimaryStage());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.setTitle("Aset yang Dimiliki");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(480);
        modalContent.setMaxWidth(480);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        VBox titleBox = new VBox(4);
        Label title = new Label("Aset yang Dimiliki");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label(employee.getNamaLengkap() + " - " + employee.getUnit());
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());
        
        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        // Ambil list aset dari manajemen aset berdasarkan NIP
        List<String> asetList = dataService.getAssets().stream()
            .filter(asset -> employee.getNip().equals(asset.getKeterangan()))
            .map(asset -> asset.getJenisAset() + " " + asset.getMerkBarang() + " (" + asset.getKodeAset() + ")")
            .toList();

        ListView<String> listView = new ListView<>();
        if (asetList.isEmpty()) {
            listView.getItems().add("Belum ada aset yang tercatat");
        } else {
            listView.getItems().addAll(asetList);
        }
        listView.setPrefHeight(300);
        listView.setPadding(new Insets(0, 24, 0, 24));

        modalContent.getChildren().addAll(headerBox, listView);

        Scene scene = new Scene(modalContent);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private boolean confirmDelete(Employee employee) {
    // Check if employee has any active assets (not marked for deletion)
    List<Asset> employeeAssets = dataService.getAssets().stream()
        .filter(asset -> employee.getNip().equals(asset.getKeterangan()))
        .toList();
    
    if (!employeeAssets.isEmpty()) {
        // Show warning with force delete option
        Alert warningAlert = new Alert(Alert.AlertType.CONFIRMATION);
        warningAlert.setTitle("Pegawai Memiliki Aset");
        warningAlert.setHeaderText("Pegawai Masih Memiliki " + employeeAssets.size() + " Aset");
        
        StringBuilder message = new StringBuilder();
        message.append("Pegawai \"").append(employee.getNamaLengkap()).append("\" masih memiliki ").append(employeeAssets.size()).append(" aset:\n\n");
        for (int i = 0; i < Math.min(5, employeeAssets.size()); i++) {
            Asset a = employeeAssets.get(i);
            message.append("• ").append(a.getJenisAset()).append(" ").append(a.getMerkBarang()).append(" (").append(a.getKodeAset()).append(")\n");
        }
        if (employeeAssets.size() > 5) {
            message.append("• ... dan ").append(employeeAssets.size() - 5).append(" lainnya\n");
        }
        message.append("\n⚠️ Jika tetap ingin menghapus pegawai ini, aset-aset tersebut akan:\n");
        message.append("• Pemegang dihapus\n");
        message.append("• Subdirektorat dihapus\n");
        message.append("• Status dipegang menjadi TIDAK\n\n");
        message.append("Lanjutkan hapus paksa?");
        
        warningAlert.setContentText(message.toString());
        
        ButtonType forceDeleteBtn = new ButtonType("Hapus Paksa", ButtonBar.ButtonData.YES);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        warningAlert.getButtonTypes().setAll(forceDeleteBtn, cancelBtn);
        
        if (warningAlert.showAndWait().orElse(cancelBtn) == forceDeleteBtn) {
            // Force delete - cleanup assets first
            for (Asset asset : employeeAssets) {
                asset.setKeterangan(""); // Clear pemegang
                asset.setSubdir("");     // Clear subdir
                asset.setDipakai("FALSE"); // Set dipakai to false
                dataService.updateAsset(asset);
            }
            dataService.clearAssetCache(); // Ensure cache is refreshed
            return true; // Proceed with employee deletion
        }
        return false;
    }
    
    // No active assets, proceed with normal confirmation
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Konfirmasi Penghapusan");
    alert.setHeaderText("Hapus Pegawai");
    alert.setContentText("Apakah Anda yakin ingin menghapus pegawai " + employee.getNamaLengkap() + "?");
    return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
}

    private void refreshTable() {
        paginatedTable.setItems(dataService.getEmployees());
    }

    private void filterTable(String searchText, String unitFilter, String statusFilter) {
        List<Employee> allEmployees = dataService.getEmployees();
        
        List<Employee> filtered = allEmployees.stream()
            .filter(employee -> {
                // Search filter
                if (searchText != null && !searchText.isEmpty()) {
                    String search = searchText.toLowerCase();
                    if (!employee.getNip().toLowerCase().contains(search) &&
                        !employee.getNamaLengkap().toLowerCase().contains(search)) {
                        return false;
                    }
                }
                
                // Unit filter (case insensitive)
                if (unitFilter != null && !unitFilter.equals("Semua Subdir")) {
                    if (!employee.getUnit().equalsIgnoreCase(unitFilter)) {
                        return false;
                    }
                }
                
                // Status filter
                if (statusFilter != null && !statusFilter.equals("Semua Status")) {
                    if (!statusFilter.equalsIgnoreCase(employee.getStatus())) {
                        return false;
                    }
                }
                
                return true;
            })
            .toList();
        
        paginatedTable.setItems(filtered);
    }

    private void handleBulkDelete() {
    ObservableList<Employee> selectedItems = paginatedTable.getTable().getSelectionModel().getSelectedItems();
    
    if (selectedItems == null || selectedItems.isEmpty()) {
        MainShellView.showWarning("Pilih pegawai yang akan dihapus terlebih dahulu.\n\nTip: Gunakan Ctrl+Klik untuk memilih beberapa pegawai.");
        return;
    }
    
    // Check which employees have assets
    List<Asset> allAssets = dataService.getAssets();
    Map<String, Long> assetCountByNip = new HashMap<>();
    for (Asset asset : allAssets) {
        String nip = asset.getKeterangan();
        if (nip != null && !nip.isBlank()) {
            assetCountByNip.merge(nip, 1L, Long::sum);
        }
    }
    
    List<Employee> employeesWithAssets = selectedItems.stream()
        .filter(emp -> assetCountByNip.getOrDefault(emp.getNip(), 0L) > 0)
        .toList();
    
    List<Employee> employeesWithoutAssets = selectedItems.stream()
        .filter(emp -> assetCountByNip.getOrDefault(emp.getNip(), 0L) == 0)
        .toList();
    
    // Show warning if there are employees with assets
    if (!employeesWithAssets.isEmpty()) {
        StringBuilder warning = new StringBuilder();
        warning.append("⚠️ ").append(employeesWithAssets.size()).append(" pegawai yang dipilih masih memiliki aset aktif.\n");
        // for loop removed as requested to simplify message
        warning.append("\n⚠️ Jika tetap ingin menghapus, aset-aset tersebut akan:\n");
        warning.append("• Pemegang dihapus\n");
        warning.append("• Subdirektorat dihapus\n");
        warning.append("• Status dipegang menjadi TIDAK\n\n");
        
        if (!employeesWithoutAssets.isEmpty()) {
            warning.append(employeesWithoutAssets.size()).append(" pegawai lainnya tanpa aset juga akan dihapus.\n\n");
        }
        warning.append("Lanjutkan hapus paksa?");
        
        Alert warningAlert = new Alert(Alert.AlertType.CONFIRMATION);
        warningAlert.setTitle("Pegawai Memiliki Aset");
        warningAlert.setHeaderText("Beberapa Pegawai Akan Dihapus Memiliki Aset");
        warningAlert.setContentText(warning.toString());
        
        ButtonType forceDeleteBtn = new ButtonType("Hapus Paksa", ButtonBar.ButtonData.YES);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        warningAlert.getButtonTypes().setAll(forceDeleteBtn, cancelBtn);
        
        if (warningAlert.showAndWait().orElse(cancelBtn) != forceDeleteBtn) {
            return;
        }
        // Proceed to background task (logic moved below)
    } else {
        // No employees with assets - simple confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus " + selectedItems.size() + " Pegawai");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus " + selectedItems.size() + " pegawai yang dipilih?\n\nAksi ini tidak dapat dibatalkan.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
    }
    
    // Collect NIPs to delete (all selected employees)
    List<Long> nipList = selectedItems.stream()
        .map(emp -> {
            try {
                return Long.parseLong(emp.getNip());
            } catch (NumberFormatException e) {
                return null;
            }
        })
        .filter(nip -> nip != null)
        .collect(Collectors.toList());
    
    if (nipList.isEmpty()) {
        MainShellView.showWarning("Tidak ada NIP valid untuk dihapus.");
        return;
    }
    
    // Show loading and run in background
    MainShellView.showLoading("Memproses penghapusan...");
    
    // Capture data for background thread
    List<Employee> fEmployeesWithAssets = new ArrayList<>(employeesWithAssets);
    // Be careful with allAssets if it's an ObservableList or modified concurrently. 
    // DataService.getAssets() returns a copy ArrayList, so it is safe.
    List<Asset> fAllAssets = new ArrayList<>(allAssets);

    javafx.concurrent.Task<Integer> deleteTask = new javafx.concurrent.Task<>() {
        @Override
        protected Integer call() throws Exception {
            // 1. Force delete - cleanup assets for all employees with assets
            if (!fEmployeesWithAssets.isEmpty()) {
                updateMessage("Melepas aset dari pegawai...");
                for (Employee emp : fEmployeesWithAssets) {
                    List<Asset> empAssets = fAllAssets.stream()
                        .filter(a -> emp.getNip().equals(a.getKeterangan()))
                        .toList();
                    for (Asset asset : empAssets) {
                        asset.setKeterangan(""); // Clear pemegang
                        asset.setSubdir("");     // Clear subdir
                        asset.setDipakai("FALSE"); // Set dipakai to false
                        dataService.updateAsset(asset);
                    }
                }
                dataService.clearAssetCache();
            }
            
            // 2. Delete Employees
            updateMessage("Menghapus data pegawai...");
            return pegawaiApi.batchDeletePegawai(nipList);
        }
    };
    
    deleteTask.setOnSucceeded(ev -> {
        MainShellView.hideLoading();
        int result = deleteTask.getValue();
        if (result >= 0) {
            dataService.clearEmployeeCache();
            MainShellView.showSuccess("Berhasil menghapus " + result + " pegawai.");
            refreshTable();
            MainShellView.invalidateDataViews();
        } else {
            MainShellView.showError("Gagal menghapus pegawai. Silakan coba lagi.");
        }
    });
    
    deleteTask.setOnFailed(ev -> {
        MainShellView.hideLoading();
        MainShellView.showError("Error saat menghapus: " + deleteTask.getException().getMessage());
        deleteTask.getException().printStackTrace();
    });
    
    new Thread(deleteTask).start();
}


    /**
     * Search and highlight an employee by NIP.
     * Called from MainShellView when navigating from Asset Management.
     */
    public void searchAndHighlight(String nipOrName) {
        List<Employee> allEmployees = dataService.getEmployees();
        
        // Find matching employee by exact NIP without filtering
        Employee match = allEmployees.stream()
            .filter(emp -> emp.getNip().equals(nipOrName))
            .findFirst()
            .orElse(null);
            
        if (match != null) {
            // Ensure table shows all employees (no filter)
            paginatedTable.setItems(allEmployees);
            
            // Navigate to the page containing the employee and select it
            paginatedTable.goToItem(match);
        }
    }
}
