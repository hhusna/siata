package com.siata.client.view;

import com.siata.client.api.AssetApi;
import com.siata.client.component.PaginatedTableView;
import com.siata.client.model.Asset;
import com.siata.client.model.Employee;
import com.siata.client.service.DataService;
import com.siata.client.util.AnimationUtils;
import com.siata.client.util.AssetExcelHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AssetManagementView extends VBox {

    private PaginatedTableView<Asset> paginatedTable;
    private final DataService dataService;
    private final AssetApi assetApi = new AssetApi();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public AssetManagementView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        
        buildView();
        
        // Refresh table when data changes
        refreshTable();
    }

    private void buildView() {
        Button addButton = new Button("+ Tambah Aset");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> showAssetForm(null));

        Button importButton = new Button("📥 Import Excel");
        importButton.getStyleClass().add("secondary-button");
        importButton.setOnAction(e -> showImportModal());

        Button exportButton = new Button("📤 Export Excel");
        exportButton.getStyleClass().add("secondary-button");
        exportButton.setOnAction(e -> handleExport());

        Button deleteSelectedBtn = new Button("🗑 Hapus Terpilih");
        deleteSelectedBtn.getStyleClass().add("secondary-button");
        deleteSelectedBtn.setStyle("-fx-text-fill: #dc2626;");
        deleteSelectedBtn.setOnAction(e -> handleBulkDelete());

        HBox actionButtons = new HBox(12, deleteSelectedBtn, exportButton, importButton, addButton);
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

        // Paginated Table with multi-selection
        paginatedTable = new PaginatedTableView<>();
        TableView<Asset> table = paginatedTable.getTable();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Asset, String> kodeCol = new TableColumn<>("Kode Aset");
        kodeCol.setCellValueFactory(new PropertyValueFactory<>("kodeAset"));
        
        TableColumn<Asset, String> jenisCol = new TableColumn<>("Jenis");
        jenisCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));
        
        TableColumn<Asset, String> merkCol = new TableColumn<>("Merk Barang");
        merkCol.setCellValueFactory(new PropertyValueFactory<>("merkBarang"));
        
        // Build employee NIP to Name map for display
        java.util.Map<String, String> nipToNameMap = new java.util.HashMap<>();
        for (Employee emp : dataService.getEmployees()) {
            nipToNameMap.put(emp.getNip(), emp.getNamaLengkap());
        }

        TableColumn<Asset, String> keteranganCol = new TableColumn<>("Pemegang");
        keteranganCol.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        keteranganCol.setCellFactory(column -> new TableCell<Asset, String>() {
            @Override
            protected void updateItem(String nip, boolean empty) {
                super.updateItem(nip, empty);
                if (empty || nip == null || nip.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Lookup employee name from NIP
                    String employeeName = nipToNameMap.getOrDefault(nip, nip);
                    
                    // Create clickable hyperlink-style label
                    Label nameLabel = new Label(employeeName);
                    nameLabel.setStyle("-fx-text-fill: #2563eb; -fx-cursor: hand; -fx-underline: true;");
                    nameLabel.setOnMouseEntered(e -> nameLabel.setStyle("-fx-text-fill: #1d4ed8; -fx-cursor: hand; -fx-underline: true;"));
                    nameLabel.setOnMouseExited(e -> nameLabel.setStyle("-fx-text-fill: #2563eb; -fx-cursor: hand; -fx-underline: true;"));
                    nameLabel.setOnMouseClicked(e -> {
                        // Navigate to EmployeeManagementView and search for this employee
                        navigateToEmployee(nip);
                    });
                    
                    setGraphic(nameLabel);
                    setText(null);
                }
            }
        });
        
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
                        
                        // Tunggu sebentar untuk memastikan server selesai update cache
                        new Thread(() -> {
                            try {
                                Thread.sleep(200); // 200ms delay
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            javafx.application.Platform.runLater(() -> refreshTable());
                        }).start();
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
        VBox.setVgrow(paginatedTable, Priority.ALWAYS);
        tableContainer.getChildren().addAll(filterBar, paginatedTable);

        getChildren().addAll(tableContainer);
    }

    private Node buildPageHeader(HBox actionButtons) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().add(actionButtons);
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

        // Searchable Employee picker with TextField + ListView
        Label pemegangLabel = new Label("NIP Pemegang (Keterangan)");
        pemegangLabel.getStyleClass().add("form-label");
        Text pemegangHint = new Text("Cari dan pilih pegawai, atau kosongkan jika belum ada pemegang");
        pemegangHint.getStyleClass().add("form-hint");
        
        // Load employees from DataService
        List<Employee> allEmployees = dataService.getEmployees();
        
        // Container for search field + list
        VBox pemegangContainer = new VBox(8);
        pemegangContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Search TextField
        TextField pemegangSearch = new TextField();
        pemegangSearch.setPromptText("Ketik nama atau NIP untuk mencari...");
        pemegangSearch.setMaxWidth(Double.MAX_VALUE);
        
        // Selected employee display
        final Employee[] selectedEmployee = {null};
        Label selectedLabel = new Label("Belum ada pegawai dipilih");
        selectedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-padding: 8 0 0 0;");
        
        // ListView for employee results
        ListView<Employee> employeeListView = new ListView<>();
        employeeListView.setPrefHeight(180); // ~5 items visible
        employeeListView.setMaxHeight(180);
        employeeListView.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        
        // Custom cell for ListView (Name large, NIP small gray)
        employeeListView.setCellFactory(lv -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                if (empty || emp == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox container = new VBox(2);
                    container.setPadding(new Insets(6, 10, 6, 10));
                    
                    Label nameLabel = new Label(emp.getNamaLengkap());
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
                    
                    String nipText = emp.getNip();
                    Label nipLabel;
                    if (nipText != null && nipText.length() != 18) {
                        nipLabel = new Label("No NIP");
                        nipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
                    } else {
                        nipLabel = new Label(nipText);
                        nipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
                    }
                    
                    container.getChildren().addAll(nameLabel, nipLabel);
                    setGraphic(container);
                    setText(null);
                }
            }
        });
        
        // Initial list - show all employees
        ObservableList<Employee> filteredEmployees = FXCollections.observableArrayList(allEmployees);
        employeeListView.setItems(filteredEmployees);
        
        // Search filter logic
        pemegangSearch.textProperty().addListener((obs, oldVal, newVal) -> {
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
        
        // Selection handler
        employeeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedEmployee[0] = newVal;
                String displayNip = (newVal.getNip() != null && newVal.getNip().length() == 18) ? newVal.getNip() : "No NIP";
                selectedLabel.setText("✓ " + newVal.getNamaLengkap() + " (" + displayNip + ")");
                selectedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a; -fx-font-weight: 600; -fx-padding: 8 0 0 0;");
            }
        });
        
        // Clear selection button
        Button clearBtn = new Button("Hapus Pilihan");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            selectedEmployee[0] = null;
            employeeListView.getSelectionModel().clearSelection();
            selectedLabel.setText("Belum ada pegawai dipilih");
            selectedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-padding: 8 0 0 0;");
        });
        
        HBox selectionRow = new HBox(12);
        selectionRow.setAlignment(Pos.CENTER_LEFT);
        selectionRow.getChildren().addAll(selectedLabel, clearBtn);
        
        pemegangContainer.getChildren().addAll(pemegangSearch, employeeListView, selectionRow);
        
        // Set initial value if editing
        if (editableAsset != null && editableAsset.getKeterangan() != null && !editableAsset.getKeterangan().isEmpty()) {
            String nipToFind = editableAsset.getKeterangan();
            allEmployees.stream()
                .filter(e -> e.getNip().equals(nipToFind))
                .findFirst()
                .ifPresent(emp -> {
                    selectedEmployee[0] = emp;
                    employeeListView.getSelectionModel().select(emp);
                    selectedLabel.setText("✓ " + emp.getNamaLengkap() + " (" + emp.getNip() + ")");
                    selectedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a; -fx-font-weight: 600; -fx-padding: 8 0 0 0;");
                });
        }

        // Subdirektorat - show/hide based on employee selection
        ComboBox<String> SubdirCombo = new ComboBox<>();
        SubdirCombo.getItems().addAll("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        SubdirCombo.setPromptText("Pilih subdirektorat");
        Label SubdirLabel = new Label("Subdirektorat");
        SubdirLabel.getStyleClass().add("form-label");
        
        // Container for Subdir field (will be shown/hidden)
        VBox subdirContainer = new VBox(8);
        subdirContainer.getChildren().addAll(SubdirLabel, SubdirCombo);
        subdirContainer.setManaged(true);
        subdirContainer.setVisible(true);
        
        if (editableAsset != null) {
            SubdirCombo.setValue(editableAsset.getSubdir());
        }
        
        // Update existing selection handler to hide subdir and auto-fill
        employeeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedEmployee[0] = newVal;
                String displayNip = (newVal.getNip() != null && newVal.getNip().length() == 18) ? newVal.getNip() : "No NIP";
                selectedLabel.setText("✓ " + newVal.getNamaLengkap() + " (" + displayNip + ")");
                selectedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a; -fx-font-weight: 600; -fx-padding: 8 0 0 0;");
                
                // Auto-fill subdirektorat from employee and hide the field
                SubdirCombo.setValue(newVal.getUnit());
                subdirContainer.setVisible(false);
                subdirContainer.setManaged(false);
            }
        });
        
        // Update clear button to show subdir field again
        clearBtn.setOnAction(e -> {
            selectedEmployee[0] = null;
            employeeListView.getSelectionModel().clearSelection();
            selectedLabel.setText("Belum ada pegawai dipilih");
            selectedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-padding: 8 0 0 0;");
            
            // Show subdirektorat field again
            subdirContainer.setVisible(true);
            subdirContainer.setManaged(true);
            SubdirCombo.setValue(null);
        });
        
        // If editing with existing employee, hide subdir initially
        if (editableAsset != null && editableAsset.getKeterangan() != null && !editableAsset.getKeterangan().isEmpty()) {
            String nipToFind = editableAsset.getKeterangan();
            allEmployees.stream()
                .filter(e -> e.getNip().equals(nipToFind))
                .findFirst()
                .ifPresent(emp -> {
                    selectedEmployee[0] = emp;
                    employeeListView.getSelectionModel().select(emp);
                    String displayNip = (emp.getNip() != null && emp.getNip().length() == 18) ? emp.getNip() : "No NIP";
                    selectedLabel.setText("✓ " + emp.getNamaLengkap() + " (" + displayNip + ")");
                    selectedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a; -fx-font-weight: 600; -fx-padding: 8 0 0 0;");
                    
                    // Hide subdir since employee is selected
                    subdirContainer.setVisible(false);
                    subdirContainer.setManaged(false);
                });
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
            // Get NIP from selected employee or empty
            String pemegangNip = selectedEmployee[0] != null ? selectedEmployee[0].getNip() : "";
            if (saveAsset(editableAsset, kodeField.getText(), jenisCombo.getValue(),
                    merkField.getText(), pemegangNip, SubdirCombo.getValue(),
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
            pemegangLabel, pemegangContainer, pemegangHint,
            subdirContainer,
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
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
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
        paginatedTable.setItems(dataService.getAssets());
    }

    private void filterTable(String searchText, String jenisFilter, String statusFilter, String kesiapanLelangFilter) {
        List<Asset> allAssets = dataService.getAssets();
        
        List<Asset> filtered = allAssets.stream()
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
            .toList();
        
        paginatedTable.setItems(filtered);
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

    // ==================== IMPORT MODAL ====================
    private void showImportModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setTitle("Import Data Aset");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(950);
        modalContent.setPrefHeight(750);
        modalContent.getStyleClass().add("modal-content");

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        
        VBox titleBox = new VBox(4);
        Label title = new Label("Import Data Aset");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Import data aset dari file Excel (.xlsx)");
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
        filePathField.setPrefWidth(450);
        HBox.setHgrow(filePathField, Priority.ALWAYS);
        
        Button browseButton = new Button("📁 Pilih File");
        browseButton.getStyleClass().add("secondary-button");
        
        Button templateButton = new Button("📄 Download Template");
        templateButton.getStyleClass().add("ghost-button");
        templateButton.setOnAction(e -> downloadTemplate());
        
        filePickerBox.getChildren().addAll(filePathField, browseButton, templateButton);

        // Data wrapper class for import
        class ImportRow {
            Asset asset;
            SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
            String status = "";
            
            ImportRow(Asset a) {
                this.asset = a;
            }
        }

        // Preview table (top) - Data dari Excel
        ObservableList<ImportRow> previewData = FXCollections.observableArrayList();
        TableView<ImportRow> previewTable = new TableView<>(previewData);
        previewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        previewTable.setEditable(true);
        previewTable.setPrefHeight(180);

        TableColumn<ImportRow, Boolean> selectCol = new TableColumn<>("Pilih");
        selectCol.setCellValueFactory(cellData -> cellData.getValue().selected);
        selectCol.setCellFactory(javafx.scene.control.cell.CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);
        selectCol.setPrefWidth(50);

        TableColumn<ImportRow, String> prevKodeCol = new TableColumn<>("Kode Aset");
        prevKodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getKodeAset()));

        TableColumn<ImportRow, String> prevJenisCol = new TableColumn<>("Jenis Aset");
        prevJenisCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getJenisAset()));

        TableColumn<ImportRow, String> prevPemegangCol = new TableColumn<>("Pemegang");
        prevPemegangCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getKeterangan()));

        TableColumn<ImportRow, String> prevSubdirCol = new TableColumn<>("Subdir");
        prevSubdirCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getSubdir()));

        previewTable.getColumns().addAll(List.of(selectCol, prevKodeCol, prevJenisCol, prevPemegangCol, prevSubdirCol));

        // Result table (bottom) - Data yang Akan Disimpan (with existing data)
        ObservableList<ImportRow> resultData = FXCollections.observableArrayList();
        TableView<ImportRow> resultTable = new TableView<>(resultData);
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        TableColumn<ImportRow, String> resKodeCol = new TableColumn<>("Kode Aset");
        resKodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getKodeAset()));

        TableColumn<ImportRow, String> resJenisCol = new TableColumn<>("Jenis Aset");
        resJenisCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getJenisAset()));

        TableColumn<ImportRow, String> resPemegangCol = new TableColumn<>("Pemegang");
        resPemegangCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getKeterangan()));

        TableColumn<ImportRow, String> resSubdirCol = new TableColumn<>("Subdir");
        resSubdirCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().asset.getSubdir()));

        TableColumn<ImportRow, String> resStatusCol = new TableColumn<>("Status");
        resStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status));
        resStatusCol.setCellFactory(column -> new TableCell<>() {
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
                    } else if ("Existing".equals(item)) {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
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
                    if (!"Existing".equals(row.status)) {
                        resultData.remove(row);
                        updateAssetResultStatuses(resultData);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ImportRow row = getTableView().getItems().get(getIndex());
                    setGraphic("Existing".equals(row.status) ? null : removeBtn);
                }
            }
        });
        removeCol.setPrefWidth(60);

        resultTable.getColumns().addAll(List.of(resKodeCol, resJenisCol, resPemegangCol, resSubdirCol, resStatusCol, removeCol));

        // Load existing assets into result table
        List<Asset> existingAssets = dataService.getAssets();
        for (Asset a : existingAssets) {
            ImportRow row = new ImportRow(a);
            row.status = "Existing";
            resultData.add(row);
        }

        // Browse button action
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih File Excel");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
            );
            File file = fileChooser.showOpenDialog(modalStage);
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
                try {
                    List<Asset> assets = AssetExcelHelper.parseExcel(file);
                    previewData.clear();
                    for (Asset a : assets) {
                        previewData.add(new ImportRow(a));
                    }
                } catch (Exception ex) {
                    showAlert("Gagal membaca file Excel: " + ex.getMessage());
                }
            }
        });

        // Preview section (top)
        VBox previewSection = new VBox(8);
        previewSection.setPadding(new Insets(0, 24, 8, 24));
        Label previewLabel = new Label("Data dari Excel");
        previewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox previewButtons = new HBox(8);
        previewButtons.setAlignment(Pos.CENTER_LEFT);
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
                .map(r -> new ImportRow(r.asset))
                .toList();
            resultData.addAll(selected);
            updateAssetResultStatuses(resultData);
        });
        
        previewButtons.getChildren().addAll(selectAllBtn, deselectAllBtn, addSelectedBtn);
        previewSection.getChildren().addAll(previewLabel, previewTable, previewButtons);

        // Result section (bottom)
        VBox resultSection = new VBox(8);
        resultSection.setPadding(new Insets(8, 24, 8, 24));
        VBox.setVgrow(resultSection, Priority.ALWAYS);
        Label resultLabel = new Label("Data yang Akan Disimpan");
        resultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox resultButtons = new HBox(8);
        resultButtons.setAlignment(Pos.CENTER_LEFT);
        Button removeDuplicatesBtn = new Button("🗑 Hapus Duplikat");
        removeDuplicatesBtn.getStyleClass().add("secondary-button");
        removeDuplicatesBtn.setStyle("-fx-text-fill: #e74c3c;");
        removeDuplicatesBtn.setOnAction(e -> {
            // Keep only first occurrence of each Kode Aset
            java.util.Set<String> seenCodes = new java.util.HashSet<>();
            List<ImportRow> toRemove = new ArrayList<>();
            for (ImportRow row : resultData) {
                if (seenCodes.contains(row.asset.getKodeAset())) {
                    toRemove.add(row);
                } else {
                    seenCodes.add(row.asset.getKodeAset());
                }
            }
            resultData.removeAll(toRemove);
            updateAssetResultStatuses(resultData);
            showSuccessAlert("Berhasil menghapus " + toRemove.size() + " data duplikat.");
        });
        
        resultButtons.getChildren().addAll(removeDuplicatesBtn);
        resultSection.getChildren().addAll(resultLabel, resultTable, resultButtons);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

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
            // Get only new items (not existing)
            List<ImportRow> newItems = resultData.stream()
                .filter(r -> !"Existing".equals(r.status))
                .toList();
            
            if (newItems.isEmpty()) {
                showAlert("Tidak ada data baru untuk disimpan.");
                return;
            }
            
            // Check for duplicates
            long dupCount = newItems.stream().filter(r -> "Duplikat".equals(r.status)).count();
            if (dupCount > 0) {
                showAlert("Masih ada " + dupCount + " data duplikat. Hapus duplikat terlebih dahulu.");
                return;
            }
            
            int successCount = 0;
            for (ImportRow row : newItems) {
                try {
                    dataService.addAsset(row.asset);
                    successCount++;
                } catch (Exception ex) {
                    System.err.println("Error importing asset: " + ex.getMessage());
                }
            }
            
            showSuccessAlert("Berhasil mengimport " + successCount + " aset baru.");
            modalStage.close();
            refreshTable();
        });
        
        footerBox.getChildren().addAll(cancelButton, updateButton);

        modalContent.getChildren().addAll(headerBox, filePickerBox, previewSection, resultSection, footerBox);

        Scene scene = new Scene(modalContent);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private void updateAssetResultStatuses(ObservableList<?> resultData) {
        // Get existing kode asets from database
        java.util.Set<String> existingCodes = dataService.getAssets().stream()
            .map(Asset::getKodeAset)
            .collect(Collectors.toSet());
        
        // Track seen codes for duplicate detection
        java.util.Set<String> seenCodes = new java.util.HashSet<>();
        
        for (Object obj : resultData) {
            if (obj instanceof Object) {
                try {
                    java.lang.reflect.Field assetField = obj.getClass().getDeclaredField("asset");
                    assetField.setAccessible(true);
                    Asset asset = (Asset) assetField.get(obj);
                    
                    java.lang.reflect.Field statusField = obj.getClass().getDeclaredField("status");
                    statusField.setAccessible(true);
                    String currentStatus = (String) statusField.get(obj);
                    
                    // Skip existing items
                    if ("Existing".equals(currentStatus)) {
                        seenCodes.add(asset.getKodeAset());
                        continue;
                    }
                    
                    String code = asset.getKodeAset();
                    if (seenCodes.contains(code)) {
                        statusField.set(obj, "Duplikat");
                    } else if (existingCodes.contains(code)) {
                        statusField.set(obj, "Duplikat");
                    } else {
                        statusField.set(obj, "Baru");
                        seenCodes.add(code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void downloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Template Excel");
        fileChooser.setInitialFileName("template_aset.xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                AssetExcelHelper.createTemplate(file);
                showSuccessAlert("Template berhasil disimpan di: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Gagal menyimpan template: " + e.getMessage());
            }
        }
    }

    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data Aset");
        fileChooser.setInitialFileName("data_aset_" + LocalDate.now() + ".xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                List<Asset> assets = dataService.getAssets();
                AssetExcelHelper.exportToExcel(assets, file);
                showSuccessAlert("Data berhasil diekspor ke: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Gagal mengekspor data: " + e.getMessage());
            }
        }
    }

    private void handleBulkDelete() {
        ObservableList<Asset> selectedItems = paginatedTable.getTable().getSelectionModel().getSelectedItems();
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            showAlert("Pilih aset yang akan dihapus terlebih dahulu.\n\nTip: Gunakan Ctrl+Klik untuk memilih beberapa aset.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus " + selectedItems.size() + " Aset");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus " + selectedItems.size() + " aset yang dipilih?\n\nAksi ini tidak dapat dibatalkan.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        List<Long> idList = selectedItems.stream()
            .map(Asset::getIdAset)
            .collect(Collectors.toList());
        
        int result = assetApi.batchDeleteAset(idList);
        
        if (result >= 0) {
            showSuccessAlert("Berhasil menghapus " + result + " aset.");
            refreshTable();
        } else {
            showAlert("Gagal menghapus aset. Silakan coba lagi.");
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Berhasil");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToEmployee(String nip) {
        // Find parent MainShellView and navigate to Employee Management
        javafx.scene.Parent parent = getParent();
        while (parent != null && !(parent instanceof MainShellView)) {
            parent = parent.getParent();
        }
        
        if (parent instanceof MainShellView mainShell) {
            // Store the NIP to search for after navigation
            mainShell.navigateToPageWithSearch(MainPage.EMPLOYEE_MANAGEMENT, nip);
        } else {
            // Fallback: just show info with employee NIP
            showAlert("Pegawai dengan NIP: " + nip + "\n\nNavigasi ke Manajemen Pegawai untuk melihat detail.");
        }
    }
}

