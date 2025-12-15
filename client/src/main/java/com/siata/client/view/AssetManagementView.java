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
import javafx.concurrent.Task;

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
    
    // Column references for visibility toggle
    private final java.util.Map<String, TableColumn<Asset, ?>> columnMap = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, CheckBox> columnCheckBoxes = new java.util.LinkedHashMap<>();
    // Map for NIP to Name lookup, refreshed in refreshTable
    private final java.util.Map<String, String> nipToNameMap = new java.util.HashMap<>();

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

        Button deleteSelectedBtn = new Button("🗑 Force Delete");
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
        jenisCombo.getItems().addAll("Semua Jenis", "Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");
        jenisCombo.setValue("Semua Jenis");
        jenisCombo.setPrefWidth(150);
        jenisCombo.getStyleClass().add("filter-combo-box");
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Semua Status", "Aktif", "Non Aktif");
        statusCombo.setValue("Semua Status");
        statusCombo.setPrefWidth(150);
        statusCombo.getStyleClass().add("filter-combo-box");
        

        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan ID, nama, atau jenis aset...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, jenisCombo.getValue(), statusCombo.getValue()));
        searchField.getStyleClass().add("filter-combo-box");
        
        jenisCombo.setOnAction(e -> filterTable(searchField.getText(), jenisCombo.getValue(), statusCombo.getValue()));
        statusCombo.setOnAction(e -> filterTable(searchField.getText(), jenisCombo.getValue(), statusCombo.getValue()));
        
        // Tambahkan filter ke filterBar
        filterBar.getChildren().addAll(searchField, jenisCombo, statusCombo);

        // Paginated Table with multi-selection
        paginatedTable = new PaginatedTableView<>();
        TableView<Asset> table = paginatedTable.getTable();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Asset, String> kodeCol = new TableColumn<>("Kode Aset");
        kodeCol.setCellValueFactory(new PropertyValueFactory<>("kodeAset"));
        kodeCol.setVisible(false);
        
        TableColumn<Asset, String> noAsetCol = new TableColumn<>("No Aset");
        noAsetCol.setCellValueFactory(cellData -> {
            Integer no = cellData.getValue().getNoAset();
            return new SimpleStringProperty(no != null ? String.valueOf(no) : "-");
        });
        noAsetCol.setVisible(false);
        
        TableColumn<Asset, String> jenisCol = new TableColumn<>("Jenis");
        jenisCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));
        
        TableColumn<Asset, String> merkCol = new TableColumn<>("Merk Barang");
        merkCol.setCellValueFactory(new PropertyValueFactory<>("merkBarang"));
        
        // Build employee NIP to Name map for display
        // nipToNameMap is now a class field refreshed in refreshTable()

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
                    
                    // Create VBox with name (bold) and NIP (small, low opacity)
                    VBox container = new VBox(2);
                    container.setStyle("-fx-cursor: hand; -fx-padding: 4 0;");
                    
                    Label nameLabel = new Label(employeeName);
                    nameLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
                    
                    String nipText = nip;
                    if (nip.length() != 18) {
                        nipText = "No NIP";
                    }
                    Label nipLabel = new Label(nipText);
                    nipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
                    
                    container.getChildren().addAll(nameLabel, nipLabel);
                    
                    // Hover effect - subtle background
                    container.setOnMouseEntered(e -> container.setStyle("-fx-cursor: hand; -fx-padding: 4 0; -fx-background-color: #f1f5f9; -fx-background-radius: 4;"));
                    container.setOnMouseExited(e -> container.setStyle("-fx-cursor: hand; -fx-padding: 4 0;"));
                    container.setOnMouseClicked(e -> navigateToEmployee(nip));
                    
                    setGraphic(container);
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
        tanggalCol.setVisible(false);
        
        TableColumn<Asset, String> rupiahCol = new TableColumn<>("Rupiah Aset");
        rupiahCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            currencyFormat.format(cellData.getValue().getNilaiRupiah())
        ));
        rupiahCol.setVisible(false);
        
        TableColumn<Asset, String> kondisiCol = new TableColumn<>("Kondisi");
        kondisiCol.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        
        TableColumn<Asset, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Kolom Dipakai (String: TRUE/FALSE -> YA/TIDAK)
        TableColumn<Asset, String> dipakaiCol = new TableColumn<>("Dipakai");
        dipakaiCol.setCellValueFactory(new PropertyValueFactory<>("dipakaiString"));
        dipakaiCol.setCellFactory(column -> new TableCell<Asset, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item); // "Ya" or "Tidak" from getDipakaiString()
                    if ("Ya".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;"); // Green
                    } else {
                        setStyle("-fx-text-fill: #ef4444;"); // Red
                    }
                }
            }
        });
        dipakaiCol.setVisible(false);
        
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
        kesiapanLelangCol.setVisible(false);
        
        // Kolom Tua (Computed - formerly Siap Lelang)
        TableColumn<Asset, String> tuaCol = new TableColumn<>("Tua");
        tuaCol.setCellValueFactory(new PropertyValueFactory<>("tuaString"));
        tuaCol.setCellFactory(column -> new TableCell<Asset, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Ya".equals(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;"); // Red for old
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });

        // Kolom Akan Tua (Computed - formerly Akan Siap Lelang)
        TableColumn<Asset, String> akanTuaCol = new TableColumn<>("Akan Tua");
        akanTuaCol.setCellValueFactory(new PropertyValueFactory<>("akanTuaString"));
        akanTuaCol.setCellFactory(column -> new TableCell<Asset, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Ya".equals(item)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;"); // Orange/Amber for upcoming
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });
        akanTuaCol.setVisible(false);

        // Kolom Siap Lelang (NEW: Tua=1 AND apakahDihapus=1)
        TableColumn<Asset, String> siapLelangCol = new TableColumn<>("Siap Lelang");
        siapLelangCol.setCellValueFactory(new PropertyValueFactory<>("siapLelangString"));
        siapLelangCol.setCellFactory(column -> new TableCell<Asset, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Ya".equals(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;"); // Green for ready
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });
        siapLelangCol.setVisible(false);
        
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
                        MainShellView.showWarning("Aset dengan status Aktif tidak dapat dihapus. Ubah status menjadi Non Aktif terlebih dahulu.");
                        return;
                    }
                    if (confirmDelete(asset)) {
                        javafx.concurrent.Task<Void> deleteTask = new javafx.concurrent.Task<>() {
                            @Override
                            protected Void call() throws Exception {
                                dataService.deleteAsset(asset);
                                return null;
                            }
                        };

                        deleteTask.setOnSucceeded(ev -> {
                            refreshTable(); // Now async
                            MainShellView.showSuccess("Aset berhasil dipindahkan ke daftar penghapusan.");
                        });

                        deleteTask.setOnFailed(ev -> {
                            ev.getSource().getException().printStackTrace();
                            MainShellView.showError("Gagal menghapus aset.");
                        });

                        new Thread(deleteTask).start();
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
            table.getColumns().setAll(List.of(kodeCol, noAsetCol, jenisCol, merkCol, keteranganCol, SubdirCol, tanggalCol,
                    rupiahCol, kondisiCol, statusCol, dipakaiCol, tuaCol, akanTuaCol, siapLelangCol, kesiapanLelangCol, aksiCol));
        } else {
            table.getColumns().setAll(List.of(kodeCol, noAsetCol, jenisCol, merkCol, keteranganCol, SubdirCol, tanggalCol,
                    rupiahCol, kondisiCol, statusCol, dipakaiCol, tuaCol, akanTuaCol, siapLelangCol, aksiCol));
        }
        
        // Register columns in columnMap for visibility toggle (exclude Aksi column)
        columnMap.put("Kode Aset", kodeCol);
        columnMap.put("No Aset", noAsetCol);
        columnMap.put("Jenis", jenisCol);
        columnMap.put("Merk Barang", merkCol);
        columnMap.put("Pemegang", keteranganCol);
        columnMap.put("Subdir", SubdirCol);
        columnMap.put("Tanggal Perolehan", tanggalCol);
        columnMap.put("Rupiah Aset", rupiahCol);
        columnMap.put("Kondisi", kondisiCol);
        columnMap.put("Status", statusCol);
        columnMap.put("Apakah Dipakai", dipakaiCol);
        columnMap.put("Tua", tuaCol);
        columnMap.put("Akan Tua", akanTuaCol);
        columnMap.put("Siap Lelang", siapLelangCol);
        if ("TIM_MANAJEMEN_ASET".equals(com.siata.client.session.LoginSession.getRole())) {
            columnMap.put("Kesiapan Lelang", kesiapanLelangCol);
        }
        
        // Add spacer and column configuration button to filter bar
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        
        Button columnConfigBtn = new Button("⚙ Kolom");
        columnConfigBtn.getStyleClass().add("secondary-button");
        columnConfigBtn.setOnAction(e -> showColumnConfigPopup(columnConfigBtn));
        
        filterBar.getChildren().addAll(filterSpacer, columnConfigBtn);

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
    
    private void showColumnConfigPopup(Button anchorButton) {
        Stage popupStage = new Stage();
        popupStage.initOwner(com.siata.client.MainApplication.getPrimaryStage());
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.initModality(Modality.NONE);
        
        VBox popup = new VBox(8);
        popup.setPadding(new Insets(16));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 3); -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        popup.setMinWidth(200);
        
        // Title
        Label title = new Label("Tampilkan Kolom");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");
        popup.getChildren().add(title);
        
        // Separator
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #e2e8f0;");
        separator.setMinHeight(1);
        separator.setMaxHeight(1);
        VBox.setMargin(separator, new Insets(4, 0, 4, 0));
        popup.getChildren().add(separator);
        
        // Checkboxes for each column
        columnCheckBoxes.clear();
        TableView<Asset> tableRef = paginatedTable.getTable();
        for (java.util.Map.Entry<String, TableColumn<Asset, ?>> entry : columnMap.entrySet()) {
            String colName = entry.getKey();
            TableColumn<Asset, ?> col = entry.getValue();
            
            CheckBox checkBox = new CheckBox(colName);
            checkBox.setSelected(col.isVisible());
            checkBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
            
            // Toggle column visibility when checkbox changes
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                col.setVisible(newVal);
                
                // Auto-resize column when it becomes visible
                if (newVal) {
                    Platform.runLater(() -> {
                        // Trigger column resize to fit content
                        col.setPrefWidth(col.getPrefWidth());
                        tableRef.refresh();
                        
                        // Use reflection to call private resizeColumnToFitContent method
                        try {
                            javafx.scene.control.skin.TableViewSkin<?> skin = 
                                (javafx.scene.control.skin.TableViewSkin<?>) tableRef.getSkin();
                            if (skin != null) {
                                java.lang.reflect.Method method = 
                                    javafx.scene.control.skin.TableViewSkin.class.getDeclaredMethod(
                                        "resizeColumnToFitContent", TableColumn.class, int.class);
                                method.setAccessible(true);
                                method.invoke(skin, col, -1);
                            }
                        } catch (Exception ex) {
                            // Fallback: just refresh
                            tableRef.refresh();
                        }
                    });
                }
            });
            
            columnCheckBoxes.put(colName, checkBox);
            popup.getChildren().add(checkBox);
        }
        
        // Show All / Hide All buttons
        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonRow, new Insets(8, 0, 0, 0));
        
        Button showAllBtn = new Button("Tampil Semua");
        showAllBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
        showAllBtn.setOnAction(e -> {
            for (java.util.Map.Entry<String, CheckBox> cbEntry : columnCheckBoxes.entrySet()) {
                cbEntry.getValue().setSelected(true);
            }
            // Trigger resize for all columns after a short delay
            Platform.runLater(() -> {
                Platform.runLater(() -> {
                    resizeAllColumns(tableRef);
                });
            });
        });
        
        Button hideAllBtn = new Button("Sembunyikan Semua");
        hideAllBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
        hideAllBtn.setOnAction(e -> {
            for (java.util.Map.Entry<String, CheckBox> cbEntry : columnCheckBoxes.entrySet()) {
                cbEntry.getValue().setSelected(false);
            }
        });
        
        buttonRow.getChildren().addAll(showAllBtn, hideAllBtn);
        popup.getChildren().add(buttonRow);
        
        // Position popup below the anchor button
        javafx.geometry.Bounds bounds = anchorButton.localToScreen(anchorButton.getBoundsInLocal());
        popupStage.setX(bounds.getMinX() - 100);
        popupStage.setY(bounds.getMaxY() + 8);
        
        Scene scene = new Scene(popup);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popupStage.setScene(scene);
        
        // Close popup when clicking outside
        popupStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                popupStage.close();
            }
        });
        
        popupStage.show();
    }
    
    @SuppressWarnings("unchecked")
    private void resizeAllColumns(TableView<Asset> table) {
        // Force table refresh first
        table.refresh();
        
        // Try to resize each column to fit content
        for (TableColumn<Asset, ?> column : table.getColumns()) {
            if (column.isVisible()) {
                try {
                    // Use reflection to call resizeColumnToFitContent
                    javafx.scene.control.skin.TableViewSkin<?> skin = 
                        (javafx.scene.control.skin.TableViewSkin<?>) table.getSkin();
                    if (skin != null) {
                        java.lang.reflect.Method method = 
                            javafx.scene.control.skin.TableViewSkin.class.getDeclaredMethod(
                                "resizeColumnToFitContent", TableColumn.class, int.class);
                        method.setAccessible(true);
                        method.invoke(skin, column, -1);
                    }
                } catch (Exception ex) {
                    // Fallback: set a reasonable width based on column header
                    String header = column.getText();
                    double minWidth = Math.max(80, header.length() * 10);
                    column.setMinWidth(minWidth);
                    column.setPrefWidth(minWidth + 20);
                }
            }
        }
        
        // Final refresh
        table.refresh();
    }

    private void showAssetForm(Asset editableAsset) {
        Stage modalStage = new Stage();
        modalStage.initOwner(com.siata.client.MainApplication.getPrimaryStage());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.setTitle(editableAsset == null ? "Tambah Aset Baru" : "Edit Aset");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(900);
        modalContent.setMaxWidth(900);
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

        TextField noAsetField = new TextField();
        noAsetField.setPromptText("Nomor urut aset");
        Label noAsetLabel = new Label("No Aset");
        noAsetLabel.getStyleClass().add("form-label");
        
        CheckBox autoGenerateCheck = new CheckBox("Otomatis (Auto Generate)");
        autoGenerateCheck.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        // Logic Auto Generate
        autoGenerateCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                noAsetField.setDisable(true);
                noAsetField.setText(""); // Clear field, DataService will handle sync with ID
            } else {
                noAsetField.setDisable(false);
            }
        });
        
        // Refresh number if code changes and auto is checked - REMOVED, we use ID now
        /* 
        kodeField.textProperty().addListener((obs, oldVal, newVal) -> { ... });
        */

        if (editableAsset != null) {
            if (editableAsset.getNoAset() != null) {
                noAsetField.setText(String.valueOf(editableAsset.getNoAset()));
            }
            // If editing, maybe default auto-check to false/unchecked to allow manual edit or keep existing
        }

        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");
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
        SubdirCombo.getItems().addAll("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur", "Hilang", "Gudang");
        SubdirCombo.setPromptText("Pilih subdirektorat");
        Label SubdirLabel = new Label("Subdirektorat / Keterangan");
        SubdirLabel.getStyleClass().add("form-label");
        
        // Container for Subdir field (will be shown/hidden)
        VBox subdirContainer = new VBox(8);
        
        // Horizontal box for Combo + Clear Button
        HBox subdirRow = new HBox(8);
        subdirRow.setAlignment(Pos.CENTER_LEFT);
        
        SubdirCombo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(SubdirCombo, Priority.ALWAYS);
        
        Button clearSubdirBtn = new Button("Hapus Pilihan");
        clearSubdirBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
        clearSubdirBtn.setOnAction(e -> SubdirCombo.setValue(null));
        
        subdirRow.getChildren().addAll(SubdirCombo, clearSubdirBtn);
        
        subdirContainer.getChildren().addAll(SubdirLabel, subdirRow);
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
        kondisiCombo.getItems().addAll("Baik", "Rusak Berat", "Rusak Sedang", "Rusak Ringan");
        kondisiCombo.setPromptText("Pilih kondisi aset");
        kondisiCombo.setValue(editableAsset == null ? "Baik" : editableAsset.getKondisi());
        Label kondisiLabel = new Label("Kondisi");
        kondisiLabel.getStyleClass().add("form-label");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("AKTIF", "NONAKTIF");
        statusCombo.setPromptText("Pilih status aset");
        // Normalize existing value to match new format
        String currentStatus = editableAsset == null ? "AKTIF" : normalizeStatus(editableAsset.getStatus());
        statusCombo.setValue(currentStatus);
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
            // Auto-calculate "Dipakai" status
            String subdirVal = SubdirCombo.getValue();
            boolean isUsed = (subdirVal != null && !subdirVal.isEmpty()) || (pemegangNip != null && !pemegangNip.isEmpty());
            String dipakaiVal = isUsed ? "TRUE" : "FALSE";
            
            // Collect all values
            final String kode = kodeField.getText();
            final String noAsetStr = noAsetField.getText();
            final String jenis = jenisCombo.getValue();
            final String merk = merkField.getText();
            final String nip = pemegangNip;
            final String subdir = SubdirCombo.getValue();
            final LocalDate tanggal = tanggalPicker.getValue();
            final String rupiah = rupiahField.getText();
            final String kond = kondisiCombo.getValue();
            final String stat = statusCombo.getValue();
            final String dipakai = dipakaiVal;
            
            // === VALIDATION ON UI THREAD ===
            if (kode == null || kode.trim().isEmpty() || kode.length() != 10 || !kode.matches("\\d+")) {
                MainShellView.showWarning("Kode Aset harus terdiri dari 10 digit angka!");
                return;
            }
            if (jenis == null) {
                MainShellView.showWarning("Pilih Jenis Aset!");
                return;
            }
            if (rupiah == null || rupiah.trim().isEmpty()) {
                MainShellView.showWarning("Masukkan nilai rupiah aset!");
                return;
            }
            
            // Parse noAset
            final Integer noAset;
            if (noAsetStr != null && !noAsetStr.trim().isEmpty()) {
                try {
                    noAset = Integer.parseInt(noAsetStr.trim());
                } catch (NumberFormatException ex) {
                    MainShellView.showWarning("No Aset harus berupa angka!");
                    return;
                }
            } else {
                noAset = null;
            }
            
            // Parse rupiah
            String cleanRupiah = rupiah;
            if (cleanRupiah.contains(".")) {
                cleanRupiah = cleanRupiah.substring(0, cleanRupiah.indexOf("."));
            }
            if (cleanRupiah.contains(",")) {
                cleanRupiah = cleanRupiah.substring(0, cleanRupiah.indexOf(","));
            }
            cleanRupiah = cleanRupiah.replaceAll("[^\\d]", "");
            if (cleanRupiah.isEmpty()) cleanRupiah = "0";
            final BigDecimal nilaiRupiah = new BigDecimal(cleanRupiah);
            
            // Prepare asset object
            final Asset assetToSave;
            final boolean isNew = (editableAsset == null);
            if (isNew) {
                assetToSave = new Asset(kode, jenis, merk, nip, subdir, tanggal, nilaiRupiah, kond, stat);
                if (noAset != null) assetToSave.setNoAset(noAset);
                assetToSave.setDipakai(dipakai);
            } else {
                editableAsset.setKodeAset(kode);
                editableAsset.setNoAset(noAset);
                editableAsset.setJenisAset(jenis);
                editableAsset.setMerkBarang(merk);
                editableAsset.setKeterangan(nip);
                editableAsset.setSubdir(subdir);
                editableAsset.setTanggalPerolehan(tanggal);
                editableAsset.setNilaiRupiah(nilaiRupiah);
                editableAsset.setKondisi(kond);
                editableAsset.setStatus(stat);
                editableAsset.setDipakai(dipakai);
                assetToSave = editableAsset;
            }
            
            // === API CALL ON BACKGROUND THREAD ===
            MainShellView.showLoading(isNew ? "Menambahkan aset..." : "Memperbarui aset...");
            
            javafx.concurrent.Task<Boolean> saveTask = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    if (isNew) {
                        return dataService.addAsset(assetToSave);
                    } else {
                        return dataService.updateAsset(assetToSave);
                    }
                }
            };
            
            saveTask.setOnSucceeded(ev -> {
                MainShellView.hideLoading();
                if (saveTask.getValue()) {
                    MainShellView.showSuccess(isNew ? "Aset berhasil ditambahkan!" : "Aset berhasil diperbarui!");
                    refreshTable();
                    MainShellView.invalidateDataViews();
                    modalStage.close();
                } else {
                    MainShellView.showError(isNew ? "Gagal menambah aset! Kemungkinan duplikat Kode+No Aset." : "Gagal memperbarui aset!");
                }
            });
            
            saveTask.setOnFailed(ev -> {
                MainShellView.hideLoading();
                MainShellView.showError("Gagal menyimpan aset: " + saveTask.getException().getMessage());
            });
            
            new Thread(saveTask).start();
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // Two-column grid layout for form fields
        HBox formGrid = new HBox(32);
        formGrid.setPadding(new Insets(0, 24, 16, 24));
        
        // Left column: Asset Info
        VBox leftColumn = new VBox(12);
        leftColumn.setPrefWidth(400);
        leftColumn.setMaxWidth(400);
        
        VBox kodeBox = new VBox(4);
        kodeBox.getChildren().addAll(kodeLabel, kodeField, kodeHint);

        VBox noAsetBox = new VBox(4);
        noAsetBox.getChildren().addAll(noAsetLabel, noAsetField, autoGenerateCheck);
        
        VBox jenisBox = new VBox(8);
        jenisCombo.setMaxWidth(Double.MAX_VALUE);
        jenisBox.getChildren().addAll(jenisLabel, jenisCombo);
        
        VBox merkBox = new VBox(8);
        merkBox.getChildren().addAll(merkLabel, merkField);
        
        VBox tanggalBox = new VBox(8);
        tanggalPicker.setMaxWidth(Double.MAX_VALUE);
        tanggalBox.getChildren().addAll(tanggalLabel, tanggalPicker);
        
        VBox rupiahBox = new VBox(8);
        rupiahBox.getChildren().addAll(rupiahLabel, rupiahField);
        
        leftColumn.getChildren().addAll(kodeBox, noAsetBox, jenisBox, merkBox, tanggalBox, rupiahBox);
        
        // Right column: Holder and Status Info
        VBox rightColumn = new VBox(12);
        rightColumn.setPrefWidth(400);
        rightColumn.setMaxWidth(400);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        
        VBox pemegangBox = new VBox(4);
        pemegangBox.getChildren().addAll(pemegangLabel, pemegangContainer, pemegangHint);
        
        VBox kondisiBox = new VBox(8);
        kondisiCombo.setMaxWidth(Double.MAX_VALUE);
        kondisiBox.getChildren().addAll(kondisiLabel, kondisiCombo);
        
        VBox statusBox = new VBox(8);
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusBox.getChildren().addAll(statusLabel, statusCombo);

        rightColumn.getChildren().addAll(pemegangBox, subdirContainer, kondisiBox, statusBox);
        
        formGrid.getChildren().addAll(leftColumn, rightColumn);

        modalContent.getChildren().addAll(headerBox, formGrid, buttonBox);

        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(modalScene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private boolean saveAsset(Asset editableAsset, String kodeAset, String noAsetStr, String jenisAset, String merkBarang, 
                              String nipPemegang, String subdir, LocalDate tanggalPerolehan, 
                              String rupiahStr, String kondisi, String status, String dipakai) {
        // Validation
        if (kodeAset == null || kodeAset.trim().isEmpty() || kodeAset.length() != 10 || !kodeAset.matches("\\d+")) {
            MainShellView.showWarning("Kode Aset harus terdiri dari 10 digit angka!");
            return false;
        }
        
        if (jenisAset == null) {
            MainShellView.showWarning("Pilih Jenis Aset!");
            return false;
        }

        if (rupiahStr == null || rupiahStr.trim().isEmpty()) {
            MainShellView.showWarning("Masukkan nilai rupiah aset!");
            return false;
        }

        Integer noAset = null;
        if (noAsetStr != null && !noAsetStr.trim().isEmpty()) {
            try {
                noAset = Integer.parseInt(noAsetStr.trim());
            } catch (NumberFormatException e) {
                MainShellView.showWarning("No Aset harus berupa angka!");
                return false;
            }
        }
        
        try {
            // Remove decimal part first (e.g., ".00" at the end), then remove non-numeric chars
            String cleanRupiah = rupiahStr;
            // If contains decimal point, take only the integer part
            if (cleanRupiah.contains(".")) {
                cleanRupiah = cleanRupiah.substring(0, cleanRupiah.indexOf("."));
            }
            if (cleanRupiah.contains(",")) {
                cleanRupiah = cleanRupiah.substring(0, cleanRupiah.indexOf(","));
            }
            // Now remove any remaining non-digit characters (e.g., currency symbols, spaces)
            cleanRupiah = cleanRupiah.replaceAll("[^\\d]", "");
            if (cleanRupiah.isEmpty()) {
                cleanRupiah = "0";
            }
            BigDecimal nilaiRupiah = new BigDecimal(cleanRupiah);
            
            if (editableAsset == null) {
                // New asset
                Asset asset = new Asset(kodeAset, jenisAset, merkBarang, nipPemegang, subdir, tanggalPerolehan, nilaiRupiah, kondisi, status);
                if (noAset != null) {
                    asset.setNoAset(noAset);
                }
                asset.setDipakai(dipakai); // Set Dipakai
                
                if (dataService.addAsset(asset)) {
                    MainShellView.showSuccess("Aset berhasil ditambahkan!");
                    refreshTable();
                    MainShellView.invalidateDataViews(); // Refresh Dashboard, Recapitulation, Employee
                    return true;
                } else {
                    MainShellView.showError("Gagal menambah aset! Kemungkinan duplikat Kode+No Aset.");
                    return false;
                }
            } else {
                // Update existing
                editableAsset.setKodeAset(kodeAset);
                editableAsset.setNoAset(noAset);
                editableAsset.setJenisAset(jenisAset);
                editableAsset.setMerkBarang(merkBarang);
                editableAsset.setKeterangan(nipPemegang); // Logic NIP/Subdir handled in DataService/API
                editableAsset.setSubdir(subdir);
                editableAsset.setTanggalPerolehan(tanggalPerolehan);
                editableAsset.setNilaiRupiah(nilaiRupiah);
                editableAsset.setKondisi(kondisi);
                editableAsset.setStatus(status);
                editableAsset.setDipakai(dipakai); // Set Dipakai
                
                if (dataService.updateAsset(editableAsset)) {
                    MainShellView.showSuccess("Aset berhasil diperbarui!");
                    refreshTable();
                    MainShellView.invalidateDataViews(); // Refresh Dashboard, Recapitulation, Employee
                    return true;
                } else {
                    MainShellView.showError("Gagal memperbarui aset!");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
            MainShellView.showError("Gagal menyimpan aset: " + e.getMessage());
        }
        
        return false;
    }


    public void refreshTable() {
        javafx.concurrent.Task<Object[]> task = new javafx.concurrent.Task<>() {
            @Override
            protected Object[] call() throws Exception {
                // Fetch data (will use cache if valid)
                List<Asset> assets = dataService.getAssets();
                List<Employee> employees = dataService.getEmployees();
                
                // Build map
                java.util.Map<String, String> map = new java.util.HashMap<>();
                for (Employee emp : employees) {
                    map.put(emp.getNip(), emp.getNamaLengkap());
                }
                
                return new Object[]{assets, map};
            }
        };
        
        task.setOnSucceeded(e -> {
            Object[] result = task.getValue();
            List<Asset> assets = (List<Asset>) result[0];
            java.util.Map<String, String> map = (java.util.Map<String, String>) result[1];
            
            // Update local map
            nipToNameMap.clear();
            nipToNameMap.putAll(map);
            
            paginatedTable.setItems(assets);
            // Force refresh columns to apply new map?
            paginatedTable.getTable().refresh();
        });
        
        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            MainShellView.showError("Gagal memuat data aset.");
        });
        
        new Thread(task).start();
    }

    private void filterTable(String searchText, String jenisFilter, String statusFilter) {
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
                
                // Jenis filter (case insensitive)
                if (jenisFilter != null && !jenisFilter.equals("Semua Jenis")) {
                    if (!asset.getJenisAset().equalsIgnoreCase(jenisFilter)) {
                        return false;
                    }
                }
                
                // Status filter (case insensitive, normalize spaces)
                if (statusFilter != null && !statusFilter.equals("Semua Status")) {
                    String normalizedFilter = statusFilter.replaceAll("\\s+", "").toLowerCase();
                    String normalizedStatus = asset.getStatus().replaceAll("\\s+", "").toLowerCase();
                    if (!normalizedStatus.equals(normalizedFilter)) {
                        return false;
                    }
                }
                
                return true;
            })
            .toList();
        
        paginatedTable.setItems(filtered);
    }

    private boolean confirmDelete(Asset asset) {
        // Validate: only non-active assets can be moved to deletion list
        String status = asset.getStatus();
        boolean isActive = status == null || 
                          "AKTIF".equalsIgnoreCase(status.replace(" ", "")) ||
                          "AKTIF".equalsIgnoreCase(status);
        
        if (isActive) {
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Tidak Dapat Menghapus");
            warningAlert.setHeaderText("Aset Masih Aktif");
            warningAlert.setContentText(
                "Aset \"" + asset.getNamaAset() + "\" tidak dapat dipindahkan ke penghapusan karena masih berstatus AKTIF.\n\n" +
                "Ubah status aset menjadi NONAKTIF terlebih dahulu sebelum memindahkan ke daftar penghapusan."
            );
            warningAlert.showAndWait();
            return false;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pindahkan ke Penghapusan Aset");
        alert.setHeaderText("Pindahkan Aset ke Daftar Penghapusan");
        alert.setContentText("Aset " + asset.getNamaAset() + " akan dipindahkan ke daftar penghapusan aset.\n\n" +
                            "Kondisi: " + asset.getKondisi() + "\n" +
                            "Status: " + asset.getStatus() + "\n\n" +
                            "Lanjutkan?");
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
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

    /**
     * Normalize status_pemakaian to AKTIF or NONAKTIF (case-insensitive)
     */
    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "AKTIF";
        }
        String upper = status.trim().toUpperCase().replace(" ", "");
        // Handle various formats: "Aktif", "AKTIF", "aktif", "Non Aktif", "NONAKTIF", "nonaktif", etc.
        if (upper.contains("NON") || upper.equals("NONAKTIF") || upper.equals("INACTIVE")) {
            return "NONAKTIF";
        }
        return "AKTIF";
    }

    /**
     * Find existing asset by kodeAset and noAset
     */
    private Asset findExistingAsset(String kodeAset, Integer noAset) {
        for (Asset asset : dataService.getAssets()) {
            boolean kodeMatch = (kodeAset == null && asset.getKodeAset() == null) || 
                               (kodeAset != null && kodeAset.equals(asset.getKodeAset()));
            boolean noMatch = (noAset == null && asset.getNoAset() == null) || 
                             (noAset != null && noAset.equals(asset.getNoAset()));
            if (kodeMatch && noMatch) {
                return asset;
            }
        }
        return null;
    }

    // ==================== IMPORT MODAL ====================
    private void showImportModal() {
        Stage modalStage = new Stage();
        modalStage.initOwner(com.siata.client.MainApplication.getPrimaryStage());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);
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
                    MainShellView.showError("Gagal membaca file Excel: " + ex.getMessage());
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
            // Keep only first occurrence of each Kode Aset + No Aset
            java.util.Set<String> seenKeys = new java.util.HashSet<>();
            List<ImportRow> toRemove = new ArrayList<>();
            for (ImportRow row : resultData) {
                String key = row.asset.getKodeAset() + "-" + (row.asset.getNoAset() != null ? row.asset.getNoAset() : "null");
                if (seenKeys.contains(key)) {
                    toRemove.add(row);
                } else {
                    seenKeys.add(key);
                }
            }
            resultData.removeAll(toRemove);
            updateAssetResultStatuses(resultData);
            MainShellView.showSuccess("Berhasil menghapus " + toRemove.size() + " data duplikat.");
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
        
        Button updateButton = new Button("Import & Update");
        updateButton.getStyleClass().add("primary-button");
        updateButton.setOnAction(e -> {
            // Separate new items and existing items
            List<ImportRow> newItems = resultData.stream()
                .filter(r -> "Baru".equals(r.status) || "".equals(r.status))
                .toList();
            List<ImportRow> existingItems = resultData.stream()
                .filter(r -> "Existing".equals(r.status))
                .toList();
            
            if (newItems.isEmpty() && existingItems.isEmpty()) {
                MainShellView.showWarning("Tidak ada data untuk diproses.");
                return;
            }
            
            // Check for duplicates (within import, not existing in DB)
            long dupCount = resultData.stream().filter(r -> "Duplikat".equals(r.status)).count();
            if (dupCount > 0) {
                MainShellView.showWarning("Masih ada " + dupCount + " data duplikat dalam import. Hapus duplikat terlebih dahulu.");
                return;
            }
            
            // Confirm update for existing items
            if (!existingItems.isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Konfirmasi Update");
                confirmAlert.setHeaderText("Update Data Existing");
                confirmAlert.setContentText(
                    "Akan diproses:\n" +
                    "• " + newItems.size() + " aset BARU (ditambahkan)\n" +
                    "• " + existingItems.size() + " aset EXISTING (di-update)\n\n" +
                    "Data existing akan di-UPDATE dengan data dari Excel.\n" +
                    "Lanjutkan?"
                );
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return;
                }
            }
            
            // Capture variables for background thread
            List<ImportRow> fNewItems = new ArrayList<>(newItems);
            List<ImportRow> fExistingItems = new ArrayList<>(existingItems);
            
            MainShellView.showLoading("Mengimport dan memproses data...");
            
            Task<String> importTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    int addSuccess = 0;
                    int addFail = 0;
                    int updateSuccess = 0;
                    int updateFail = 0;
                    
                    // 1. Fetch valid employees for validation
                    java.util.Map<String, String> validNips = dataService.getAllEmployeeNipNameMap();
                    
                    // 1b. Prepare for Auto-Generate No Aset
                    List<Asset> allDbAssets = dataService.getAllAssetsIncludingDeleted(true);
                    java.util.Map<String, Integer> maxNoAsetMap = new java.util.HashMap<>();
                    for (Asset a : allDbAssets) {
                        if (a.getKodeAset() != null && a.getNoAset() != null) {
                            maxNoAsetMap.merge(a.getKodeAset(), a.getNoAset(), Integer::max);
                        }
                    }

                    // 2. Validate NEW items & Generate No Aset if needed
                    List<Asset> assetsToInsert = new ArrayList<>();
                    StringBuilder validationErrors = new StringBuilder();
                    
                    for (ImportRow row : fNewItems) {
                        // Auto-Generate No Aset if empty/0
                        if (row.asset.getNoAset() == null || row.asset.getNoAset() == 0) {
                            String kode = row.asset.getKodeAset();
                            if (kode != null) {
                                int next = maxNoAsetMap.getOrDefault(kode, 0) + 1;
                                row.asset.setNoAset(next);
                                maxNoAsetMap.put(kode, next);
                            }
                        } else {
                            // If explicit No Aset provided, update map to prevent collisions
                            String kode = row.asset.getKodeAset();
                            if (kode != null) {
                                maxNoAsetMap.merge(kode, row.asset.getNoAset(), Integer::max);
                            }
                        }

                        String nip = row.asset.getKeterangan();
                        // If NIP is provided (not empty, not "0", not "-")
                        if (nip != null && !nip.trim().isEmpty() && !"0".equals(nip.trim()) && !"-".equals(nip.trim())) {
                            if (!validNips.containsKey(nip.trim())) {
                                validationErrors.append("• Baru: ").append(row.asset.getKodeAset())
                                    .append(" - NIP ").append(nip).append(" tidak ditemukan\n");
                                addFail++;
                                continue; // Skip this item
                            }
                        }
                        assetsToInsert.add(row.asset);
                    }
                    
                    // 3. Process Valid NEW items
                    if (!assetsToInsert.isEmpty()) {
                        try {
                            int count = dataService.batchAddAssets(assetsToInsert);
                            addSuccess = count;
                            addFail += (assetsToInsert.size() - count);
                        } catch (Exception ex) {
                            addFail += assetsToInsert.size();
                            System.err.println("Error batch adding assets: " + ex.getMessage());
                        }
                    }
                    
                    // 4. Process EXISTING items (Update)
                    for (ImportRow row : fExistingItems) {
                        try {
                            // Validate NIP first
                            String nip = row.asset.getKeterangan();
                            if (nip != null && !nip.trim().isEmpty() && !"0".equals(nip.trim()) && !"-".equals(nip.trim())) {
                                if (!validNips.containsKey(nip.trim())) {
                                    validationErrors.append("• Update: ").append(row.asset.getKodeAset())
                                        .append(" - NIP ").append(nip).append(" tidak ditemukan\n");
                                    updateFail++;
                                    continue; // Skip update
                                }
                            }
                            
                            // Proceed with update logic
                            Asset existingAsset = findExistingAsset(row.asset.getKodeAset(), row.asset.getNoAset());
                            if (existingAsset != null) {
                                row.asset.setIdAset(existingAsset.getIdAset());
                                boolean result = dataService.updateAsset(row.asset);
                                if (result) updateSuccess++; else updateFail++;
                            } else {
                                boolean result = dataService.addAsset(row.asset);
                                if (result) addSuccess++; else addFail++;
                            }
                        } catch (Exception ex) {
                            updateFail++;
                            System.err.println("Error updating asset: " + ex.getMessage());
                        }
                    }
                    
                    // 5. Build Result Summary
                    StringBuilder summary = new StringBuilder("Import selesai!\n\n");
                    if (addSuccess > 0 || addFail > 0) {
                        summary.append("TAMBAH BARU:\n");
                        summary.append("✓ Berhasil: ").append(addSuccess).append("\n");
                        if (addFail > 0) summary.append("✗ Gagal: ").append(addFail).append("\n");
                    }
                    if (updateSuccess > 0 || updateFail > 0) {
                        summary.append("\nUPDATE EXISTING:\n");
                        summary.append("✓ Berhasil: ").append(updateSuccess).append("\n");
                        if (updateFail > 0) summary.append("✗ Gagal: ").append(updateFail).append("\n");
                    }
                    
                    if (validationErrors.length() > 0) {
                        summary.append("\nDETAIL KEGAGALAN (Pemegang Belum Tercatat):\n");
                        // Limit logs if too many
                        if (validationErrors.length() > 500) {
                             summary.append(validationErrors.substring(0, 500)).append("\n...dan lainnya.");
                        } else {
                             summary.append(validationErrors);
                        }
                    }
                    
                    return summary.toString();
                }
            };
            
            importTask.setOnSucceeded(ev -> {
                MainShellView.hideLoading();
                String summary = importTask.getValue();
                
                // Determine if we show Success or Warning based on failures
                // Logic based on string content is brittle, so maybe just show Success for now as previous logic did
                // Or check the summary string? A bit hacky but works for now to match logic
                if (summary.contains("✗ Gagal")) { // If failure symbol exists
                     MainShellView.showWarning(summary);
                } else {
                     MainShellView.showSuccess(summary);
                }
                
                modalStage.close();
                refreshTable();
                MainShellView.invalidateDataViews(); // Refresh Dashboard, Recapitulation, Employee
            });
            
            importTask.setOnFailed(ev -> {
                MainShellView.hideLoading();
                MainShellView.showError("Gagal mengimport data: " + ev.getSource().getException().getMessage());
                ev.getSource().getException().printStackTrace();
            });
            
            new Thread(importTask).start();
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
        // Get existing items keys (Kode + No)
        java.util.Set<String> existingKeys = dataService.getAssets().stream()
            .map(a -> a.getKodeAset() + "-" + (a.getNoAset() != null ? a.getNoAset() : "null"))
            .collect(Collectors.toSet());
        
        // Track seen keys for duplicate detection within import
        java.util.Set<String> seenKeys = new java.util.HashSet<>();
        
        for (Object obj : resultData) {
            if (obj instanceof Object) {
                try {
                    java.lang.reflect.Field assetField = obj.getClass().getDeclaredField("asset");
                    assetField.setAccessible(true);
                    Asset asset = (Asset) assetField.get(obj);
                    
                    java.lang.reflect.Field statusField = obj.getClass().getDeclaredField("status");
                    statusField.setAccessible(true);
                    String currentStatus = (String) statusField.get(obj);
                    
                    String key = asset.getKodeAset() + "-" + (asset.getNoAset() != null ? asset.getNoAset() : "null");

                    // Skip existing items
                    if ("Existing".equals(currentStatus)) {
                        seenKeys.add(key);
                        continue;
                    }
                    
                    if (seenKeys.contains(key)) {
                        statusField.set(obj, "Duplikat");
                    } else if (existingKeys.contains(key)) {
                        statusField.set(obj, "Duplikat");
                    } else {
                        statusField.set(obj, "Baru");
                        seenKeys.add(key);
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
                MainShellView.showSuccess("Template berhasil disimpan di: " + file.getAbsolutePath());
            } catch (Exception e) {
                MainShellView.showError("Gagal menyimpan template: " + e.getMessage());
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
                MainShellView.showSuccess("Data berhasil diekspor ke: " + file.getAbsolutePath());
            } catch (Exception e) {
                MainShellView.showError("Gagal mengekspor data: " + e.getMessage());
            }
        }
    }

    private void handleBulkDelete() {
        ObservableList<Asset> selectedItems = paginatedTable.getTable().getSelectionModel().getSelectedItems();
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            MainShellView.showWarning("Pilih aset yang akan dihapus terlebih dahulu.\n\nTip: Gunakan Ctrl+Klik untuk memilih beberapa aset.");
            return;
        }

        // Force Delete allows deleting ALL selected assets regardless of status
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Force Delete");
        confirmAlert.setHeaderText("Hapus " + selectedItems.size() + " Aset");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus " + selectedItems.size() + " aset yang dipilih?\n\nAksi ini akan menghapus aset tanpa validasi status Non Aktif.\nAksi ini tidak dapat dibatalkan.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        List<Long> idList = selectedItems.stream()
            .map(Asset::getIdAset)
            .collect(Collectors.toList());
        
        // Show loading overlay
        MainShellView.showLoading("Menghapus " + idList.size() + " aset...");
        
        javafx.concurrent.Task<Integer> deleteBatchTask = new javafx.concurrent.Task<>() {
            @Override
            protected Integer call() throws Exception {
                // Use DataService to ensure cache is cleared
                return dataService.batchDeleteAssets(idList);
            }
        };

        deleteBatchTask.setOnSucceeded(ev -> {
            MainShellView.hideLoading();
            int result = deleteBatchTask.getValue();
            if (result >= 0) {
                MainShellView.showSuccess("Berhasil menghapus " + result + " aset.");
                refreshTable(); // Refresh UI with fresh data
            } else {
                MainShellView.showError("Gagal menghapus aset. Silakan coba lagi.");
            }
        });

        deleteBatchTask.setOnFailed(ev -> {
            MainShellView.hideLoading();
            ev.getSource().getException().printStackTrace();
            MainShellView.showError("Terjadi kesalahan saat menghapus aset.");
        });

        new Thread(deleteBatchTask).start();
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
            MainShellView.showInfo("Pegawai dengan NIP: " + nip + "\n\nNavigasi ke Manajemen Pegawai untuk melihat detail.");
        }
    }
}

