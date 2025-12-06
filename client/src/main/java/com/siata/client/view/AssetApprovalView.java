package com.siata.client.view;

import com.siata.client.model.AssetRequest;
import com.siata.client.service.DataService;
import com.siata.client.session.LoginSession;
import com.siata.client.api.LogRiwayatApi;
import com.siata.client.dto.ApprovalLogDto;
import com.siata.client.util.AnimationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssetApprovalView extends VBox {

    private final TableView<AssetRequest> permohonanTable;
    private final TableView<AssetRequest> pengajuanTable;
    private final ObservableList<AssetRequest> permohonanList;
    private final ObservableList<AssetRequest> pengajuanList;
    private final DataService dataService;
    private final LogRiwayatApi logRiwayatApi;
    
    // Cache untuk approval logs
    private final Map<String, List<ApprovalLogDto>> approvalLogsCache = new ConcurrentHashMap<>();
    private final Label loadingLabel;

    public AssetApprovalView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        logRiwayatApi = new LogRiwayatApi();
        permohonanList = FXCollections.observableArrayList();
        pengajuanList = FXCollections.observableArrayList();
        permohonanTable = new TableView<>();
        pengajuanTable = new TableView<>();
        
        loadingLabel = new Label("Loading persetujuan aset...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
        
        getChildren().add(loadingLabel);
        
        // Load data di background thread
        loadDataInBackground();
    }
    
    private void loadDataInBackground() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load data dari API
                List<AssetRequest> permohonan = dataService.getPermohonanAset();
                List<AssetRequest> pengajuan = dataService.getPengajuanAset();
                
                javafx.application.Platform.runLater(() -> {
                    permohonanList.setAll(permohonan);
                    pengajuanList.setAll(pengajuan);
                    
                    getChildren().clear();
                    buildView();
                });
                
                return null;
            }
        };
        
        loadTask.setOnFailed(e -> {
            loadingLabel.setText("Gagal memuat data. Silakan refresh.");
            loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-padding: 20;");
        });
        
        new Thread(loadTask).start();
    }

    private void buildView() {
        VBox permohonanSection = createApprovalSection("Daftar Permohonan", permohonanTable, permohonanList, true);
        VBox pengajuanSection = createApprovalSection("Daftar Pengajuan", pengajuanTable, pengajuanList, false);
        getChildren().addAll(buildPageHeader(), permohonanSection, pengajuanSection);
    }

    private Node buildPageHeader() {
        // Title and description now shown in main header
        return new HBox();
    }

    private VBox createApprovalSection(String title, TableView<AssetRequest> tableView,
                                       ObservableList<AssetRequest> data, boolean isPermohonan) {
        VBox section = new VBox(16);
        section.setSpacing(16);

        // Section Header
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("section-title");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        VBox container = new VBox(16);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("table-container");

        tableView.setItems(data);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("data-table");
        tableView.getColumns().clear();

        TableColumn<AssetRequest, String> nomorCol = new TableColumn<>(isPermohonan ? "No. Permohonan" : "No. Pengajuan");
        nomorCol.setCellValueFactory(new PropertyValueFactory<>("noPermohonan"));

        TableColumn<AssetRequest, String> pemohonCol = new TableColumn<>(isPermohonan ? "Pemohon" : "Diajukan Oleh");
        pemohonCol.setCellValueFactory(new PropertyValueFactory<>("pemohon"));

        TableColumn<AssetRequest, String> jenisCol = new TableColumn<>("Jenis Aset");
        jenisCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));

        TableColumn<AssetRequest, String> jumlahCol = new TableColumn<>("Jumlah");
        jumlahCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getJumlah()))
        );

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

        TableColumn<AssetRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> createStatusCell());

        TableColumn<AssetRequest, Void> aksiCol = buildActionColumn();
        TableColumn<AssetRequest, Void> keputusanCol = buildDecisionColumn(isPermohonan);

        tableView.getColumns().setAll(List.of(nomorCol, pemohonCol, jenisCol, jumlahCol, tanggalCol, statusCol, aksiCol, keputusanCol));

        container.getChildren().addAll(tableView);
        
        section.getChildren().addAll(sectionTitle, container);
        return section;
    }

    private TableCell<AssetRequest, String> createStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);

                } else {
                    Label badge = new Label();
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: 600;");

                    if (status.contains("Disetujui Direktur")) {
                        badge.setText("✔ Disetujui Direktur");
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #3498db; -fx-text-fill: white;");
                    } else if (status.contains("Disetujui PPK")) {
                        badge.setText("✔ Disetujui PPK");
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #3498db; -fx-text-fill: white;");
                    } else if (status.equalsIgnoreCase("Pending")) {
                        badge.setText("⏰ Pending");
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #95a5a6; -fx-text-fill: white;");
                    } else if (status.equalsIgnoreCase("Ditolak")) {
                        badge.setText("✗ Ditolak");
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else {
                        badge.setText(status);
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #95a5a6; -fx-text-fill: white;");
                    }

                    setGraphic(badge);
                }
            }
        };
    }

    private TableColumn<AssetRequest, Void> buildActionColumn() {
        TableColumn<AssetRequest, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(column -> new TableCell<>() {
            private final Button detailButton = createIconButton("👁");

            {
                detailButton.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    showRequestDetail(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
        });
        aksiCol.setPrefWidth(80);
        return aksiCol;
    }

    private TableColumn<AssetRequest, Void> buildDecisionColumn(boolean isPermohonan) {
        TableColumn<AssetRequest, Void> keputusanCol = new TableColumn<>("Persetujuan");
        keputusanCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    String currentUserRole = LoginSession.getRole();
                    
                    // Cek apakah user sudah approve/reject
                    String currentStatus = getCurrentUserApprovalStatus(request, currentUserRole);
                    
                    if (currentStatus != null) {
                        // Sudah approve/reject -> tampilkan tombol Edit
                        Button editButton = new Button();
                        editButton.setOnAction(e -> showEditApprovalModal(request));
                        
                        if ("Disetujui".equals(currentStatus)) {
                            editButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 16;");
                            editButton.setText("✓ Edit Persetujuan");
                        } else {
                            editButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 16;");
                            editButton.setText("✗ Edit Persetujuan");
                        }
                        
                        HBox editBox = new HBox(8, editButton);
                        editBox.setAlignment(Pos.CENTER);
                        setGraphic(editBox);
                    } else {
                        // Belum approve/reject -> tampilkan tombol Setujui & Tolak
                        Button approveButton = new Button("Setujui");
                        approveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 12;");
                        approveButton.setOnAction(e -> {
                            if (confirmDecision("Setujui", request)) {
                                dataService.updateAssetRequestStatus(request, "Disetujui "+LoginSession.getRole(), LoginSession.getPegawaiDto().getNama());
                                refreshTables();
                            }
                        });
                        
                        Button rejectButton = new Button("Tolak");
                        rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 12;");
                        rejectButton.setOnAction(e -> {
                            if (confirmDecision("Tolak", request)) {
                                dataService.updateAssetRequestStatus(request, "Ditolak "+LoginSession.getRole(), LoginSession.getPegawaiDto().getNama());
                                refreshTables();
                            }
                        });
                        
                        HBox defaultBox = new HBox(8, approveButton, rejectButton);
                        defaultBox.setAlignment(Pos.CENTER);
                        setGraphic(defaultBox);
                    }
                }
            }
        });
        keputusanCol.setPrefWidth(220);
        return keputusanCol;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 16px; -fx-padding: 4 8;");
        return button;
    }

    private void showRequestDetail(AssetRequest request) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UTILITY);
        modalStage.setTitle("Detail " + request.getTipe());

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(700);
        modalContent.setMaxWidth(700);
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

        // Main Content Grid
        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(16);
        grid.setPadding(new Insets(0, 24, 16, 24));

        addDetailRow(grid, 0, "No. " + ("Permohonan".equals(request.getTipe()) ? "Permohonan" : "Pengajuan"), request.getNoPermohonan());
        addDetailRow(grid, 1, "Diajukan Oleh", request.getPemohon());
        addDetailRow(grid, 2, "Tanggal", request.getTanggal() != null ? request.getTanggal().format(DateTimeFormatter.ofPattern("d/M/yyyy")) : "-");
        addDetailRow(grid, 3, "Jenis Aset", request.getJenisAset());
        addDetailRow(grid, 4, "Jumlah", String.valueOf(request.getJumlah()) + " unit");
        
        // Status Badge
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Status Saat Ini");
        statusLabel.getStyleClass().add("form-label");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        
        Label statusBadge = new Label(request.getStatus());
        statusBadge.setPadding(new Insets(6, 16, 6, 16));
        statusBadge.setStyle("-fx-background-radius: 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-color: #3498db; -fx-text-fill: white;");
        
        grid.add(statusLabel, 0, 5);
        grid.add(statusBadge, 1, 5);

        addDetailRow(grid, 6, "Deskripsi", request.getDeskripsi());

        // Alur Persetujuan Section
        VBox approvalSection = buildApprovalSection(request);

        ScrollPane scrollPane = new ScrollPane();
        VBox scrollContent = new VBox(16, grid, approvalSection);
        scrollContent.setPadding(new Insets(0, 0, 16, 0));
        scrollPane.setContent(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(500);
        scrollPane.setMaxHeight(500);
        scrollPane.getStyleClass().add("modal-scroll-pane");

        modalContent.getChildren().addAll(headerBox, scrollPane);

        Scene scene = new Scene(modalContent);
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

    private VBox buildApprovalSection(AssetRequest request) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(16, 24, 0, 24));
        
        Label sectionTitle = new Label("Alur Persetujuan");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #2c3e50;");
        
        // Generate cache key
        String cacheKey = request.getId() + "-" + request.getTipe();
        
        // Cek cache dulu
        List<ApprovalLogDto> logs = approvalLogsCache.get(cacheKey);
        
        // Jika tidak ada di cache, fetch dari API
        if (logs == null) {
            ApprovalLogDto[] logsArray = "Permohonan".equals(request.getTipe()) 
                ? logRiwayatApi.getApprovalLogs(request.getId(), null)
                : logRiwayatApi.getApprovalLogs(null, request.getId());
            
            logs = Arrays.asList(logsArray);
            approvalLogsCache.put(cacheKey, logs); // Cache hasil
        }
        
        // Alur: TMA -> PPK -> PPBJ -> Direktur
        String[] roleOrder = {"Tim Manajemen Aset", "PPK", "PPBJ", "Direktur"};
        
        VBox approvalCards = new VBox(10);
        
        for (String role : roleOrder) {
            ApprovalLogDto log = logs.stream()
                .filter(l -> role.equals(l.getRole()))
                .findFirst()
                .orElse(null);
            
            HBox card = createApprovalCard(role, log);
            approvalCards.getChildren().add(card);
        }
        
        section.getChildren().addAll(sectionTitle, approvalCards);
        return section;
    }

    private String getCurrentUserApprovalStatus(AssetRequest request, String currentUserRole) {
        // Generate cache key
        String cacheKey = request.getId() + "-" + request.getTipe();
        
        // Cek cache dulu
        List<ApprovalLogDto> logs = approvalLogsCache.get(cacheKey);
        
        // Jika tidak ada di cache, fetch dari API
        if (logs == null) {
            try {
                ApprovalLogDto[] logsArray = "Permohonan".equals(request.getTipe())
                    ? logRiwayatApi.getApprovalLogs(request.getId(), null)
                    : logRiwayatApi.getApprovalLogs(null, request.getId());
                
                logs = Arrays.asList(logsArray);
                approvalLogsCache.put(cacheKey, logs); // Cache hasil
            } catch (Exception e) {
                System.err.println("Error checking approval status: " + e.getMessage());
                return null;
            }
        }
        
        // Map role ke format role di log
        String roleToCheck;
        if ("TIM_MANAJEMEN_ASET".equals(currentUserRole)) {
            roleToCheck = "Tim Manajemen Aset";
        } else {
            roleToCheck = currentUserRole;
        }
        
        // Sort by timestamp DESCENDING di client side untuk memastikan yang terbaru pertama
        // Lalu ambil yang PERTAMA (yang paling baru)
        String status = logs.stream()
            .filter(log -> roleToCheck.equals(log.getRole()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Sort descending
            .findFirst() // Ambil yang pertama (terbaru)
            .map(ApprovalLogDto::getStatus)
            .orElse(null);
        
        return status;
    }

    private void showEditApprovalModal(AssetRequest request) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UTILITY);
        modalStage.setTitle("Edit Persetujuan");

        VBox modalContent = new VBox(16);
        modalContent.setPadding(new Insets(24));
        modalContent.setPrefWidth(400);
        modalContent.getStyleClass().add("modal-content");

        Label title = new Label("Edit Keputusan Persetujuan");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #2c3e50;");

        Label requestLabel = new Label(request.getTipe() + " untuk " + request.getPemohon());
        requestLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        VBox radioBox = new VBox(12);
        radioBox.setPadding(new Insets(12, 0, 12, 0));
        
        ToggleGroup decisionGroup = new ToggleGroup();
        
        RadioButton approveRadio = new RadioButton("Setujui");
        approveRadio.setToggleGroup(decisionGroup);
        approveRadio.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        RadioButton rejectRadio = new RadioButton("Tolak");
        rejectRadio.setToggleGroup(decisionGroup);
        rejectRadio.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        // Set default selection based on current status
        String currentStatus = getCurrentUserApprovalStatus(request, LoginSession.getRole());
        if ("Disetujui".equals(currentStatus)) {
            approveRadio.setSelected(true);
        } else if ("Ditolak".equals(currentStatus)) {
            rejectRadio.setSelected(true);
        }
        
        radioBox.getChildren().addAll(approveRadio, rejectRadio);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = new Button("Batal");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 20;");
        cancelButton.setOnAction(e -> modalStage.close());
        
        Button saveButton = new Button("Simpan");
        saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 20;");
        saveButton.setOnAction(e -> {
            RadioButton selected = (RadioButton) decisionGroup.getSelectedToggle();
            if (selected != null) {
                String newDecision = selected == approveRadio ? "Setujui" : "Tolak";
                String newStatus = (selected == approveRadio ? "Disetujui " : "Ditolak ") + LoginSession.getRole();
                
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Konfirmasi Perubahan");
                confirmAlert.setHeaderText("Ubah Keputusan Persetujuan");
                confirmAlert.setContentText("Apakah Anda yakin ingin mengubah keputusan menjadi '" + newDecision + "'?");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    dataService.updateAssetRequestStatus(request, newStatus, LoginSession.getPegawaiDto().getNama());
                    modalStage.close();
                    
                    // Tunggu sebentar agar backend selesai update log
                    new Thread(() -> {
                        try {
                            Thread.sleep(300); // 300ms delay
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        
                        javafx.application.Platform.runLater(() -> {
                            refreshTables();
                            rebuildTableColumns();
                            
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Berhasil");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Keputusan persetujuan berhasil diubah.");
                            successAlert.showAndWait();
                        });
                    }).start();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Peringatan");
                alert.setHeaderText(null);
                alert.setContentText("Silakan pilih keputusan terlebih dahulu.");
                alert.showAndWait();
            }
        });
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        modalContent.getChildren().addAll(title, requestLabel, radioBox, buttonBox);

        Scene scene = new Scene(modalContent);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private HBox createApprovalCard(String role, ApprovalLogDto log) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-border-width: 1;");
        
        // Status Icon
        Label icon = new Label();
        icon.setStyle("-fx-font-size: 24px;");
        
        VBox infoBox = new VBox(4);
        
        Label roleLabel = new Label(role);
        roleLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 13px;");
        
        if (log == null) {
            // Pending
            icon.setText("⏰");
            icon.setStyle(icon.getStyle() + " -fx-text-fill: #95a5a6;");
            statusLabel.setText("Belum Diproses");
            statusLabel.setStyle(statusLabel.getStyle() + " -fx-text-fill: #7f8c8d;");
            infoBox.getChildren().addAll(roleLabel, statusLabel);
        } else {
            if ("Disetujui".equals(log.getStatus())) {
                icon.setText("✓");
                icon.setStyle(icon.getStyle() + " -fx-text-fill: #2ecc71;");
                card.setStyle(card.getStyle() + " -fx-border-color: #2ecc71;");
                
                Label approvedBy = new Label("Disetujui");
                approvedBy.setStyle("-fx-font-weight: 600; -fx-text-fill: #2ecc71; -fx-font-size: 13px;");
                
                infoBox.getChildren().addAll(roleLabel, approvedBy);
            } else if ("Ditolak".equals(log.getStatus())) {
                icon.setText("✗");
                icon.setStyle(icon.getStyle() + " -fx-text-fill: #e74c3c;");
                card.setStyle(card.getStyle() + " -fx-border-color: #e74c3c;");
                
                Label rejectedBy = new Label("Ditolak");
                rejectedBy.setStyle("-fx-font-weight: 600; -fx-text-fill: #e74c3c; -fx-font-size: 13px;");
                
                infoBox.getChildren().addAll(roleLabel, rejectedBy);
            }
        }
        
        card.getChildren().addAll(icon, infoBox);
        
        return card;
    }

    private boolean confirmDecision(String action, AssetRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(action + " Permohonan");
        alert.setHeaderText(action + " " + request.getTipe());
        alert.setContentText("Apakah Anda yakin ingin " + action.toLowerCase() + " " + request.getTipe().toLowerCase() +
            " untuk " + request.getPemohon() + "?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void refreshTables() {
        // Clear cache saat refresh
        approvalLogsCache.clear();
        permohonanList.setAll(dataService.getPermohonanAset());
        pengajuanList.setAll(dataService.getPengajuanAset());
    }
    
    private void rebuildTableColumns() {
        // Rebuild columns untuk force re-render buttons dengan status terbaru
        TableColumn<AssetRequest, Void> keputusanColPermohonan = buildDecisionColumn(true);
        TableColumn<AssetRequest, Void> keputusanColPengajuan = buildDecisionColumn(false);
        
        // Replace keputusan column di kedua table
        permohonanTable.getColumns().set(7, keputusanColPermohonan);
        pengajuanTable.getColumns().set(7, keputusanColPengajuan);
    }
}

