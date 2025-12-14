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
import javafx.application.Platform;

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
        VBox pengajuanSection = createApprovalSection("Daftar Kebutuhan", pengajuanTable, pengajuanList, false);
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
        TableColumn<AssetRequest, String> keputusanCol = buildDecisionColumn(); // Removed boolean arg

        tableView.getColumns().setAll(List.of(nomorCol, pemohonCol, jenisCol, jumlahCol, tanggalCol, statusCol, aksiCol, keputusanCol));

        container.getChildren().addAll(tableView);
        
        section.getChildren().addAll(sectionTitle, container);
        return section;
    }

    private void showNotification(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
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
                        badge.setText("✔ " + status);
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #27ae60; -fx-text-fill: white;"); // Green
                    } else if (status.contains("Disetujui")) {
                        // PPBJ or PPK or other
                        badge.setText("✔ " + status);
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #3498db; -fx-text-fill: white;"); // Blue
                    } else if (status.contains("Ditolak")) {
                        badge.setText("✗ " + status);
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #e74c3c; -fx-text-fill: white;"); // Red
                    } else if (status.equalsIgnoreCase("Pending")) {
                        badge.setText("⏰ Pending");
                        badge.setStyle(badge.getStyle() + " -fx-background-color: #95a5a6; -fx-text-fill: white;"); // Gray
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

    private TableColumn<AssetRequest, String> buildDecisionColumn() {
        TableColumn<AssetRequest, String> col = new TableColumn<>("Aksi");
        col.setMinWidth(140);
        col.setPrefWidth(140);
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                AssetRequest request = getTableRow().getItem();
                String currentUserRole = LoginSession.getRole();
                String status = request.getStatus();

                // Logic untuk visibility tombol Persetujuan
                // Sama seperti sebelumnya, tapi sekarang hanya satu tombol "Persetujuan"
                // Tombol ini membuka dialog untuk Setuju/Tolak/Edit

                boolean canAct = false;
                boolean isEditable = false;

                // Tentukan role yang sedang login (termasuk mapping DIREKTUR -> Direktur)
                String roleToCheck = currentUserRole;
                if ("TIM_MANAJEMEN_ASET".equals(currentUserRole)) {
                    roleToCheck = "Tim Manajemen Aset";
                } else if ("DIREKTUR".equals(currentUserRole)) {
                    roleToCheck = "Direktur";
                }
                
                // TIM_MANAJEMEN_ASET can act on behalf of all roles
                boolean isTMA = "TIM_MANAJEMEN_ASET".equals(currentUserRole);

                // Cek apakah user bisa melakukan aksi berdasarkan status workflow
                if ("Pending".equalsIgnoreCase(status)) {
                    // Pending -> visible to PPBJ
                    if ("PPBJ".equals(currentUserRole) || isTMA) {
                        canAct = true;
                    }
                } else if ("Disetujui PPBJ".equalsIgnoreCase(status) || status.contains("Disetujui PPBJ")) {
                    // Disetujui PPBJ -> PPK/TMA can approve, PPBJ can view/edit
                    if ("PPK".equals(currentUserRole) || isTMA) {
                        canAct = true;
                    }
                    if ("PPBJ".equals(currentUserRole)) {
                        isEditable = true;
                    }
                } else if ("Disetujui PPK".equalsIgnoreCase(status) || status.contains("Disetujui PPK")) {
                    // Disetujui PPK -> DIREKTUR/TMA can approve, PPBJ and PPK can view/edit
                    if ("DIREKTUR".equals(currentUserRole) || isTMA) {
                        canAct = true;
                    }
                    if ("PPBJ".equals(currentUserRole) || "PPK".equals(currentUserRole)) {
                        isEditable = true;
                    }
                } else if ("Disetujui Direktur".equalsIgnoreCase(status) || status.contains("Disetujui Direktur")) {
                    // Fully approved - PPBJ, PPK, and DIREKTUR can view/edit
                    if ("PPBJ".equals(currentUserRole) || "PPK".equals(currentUserRole) || 
                        "DIREKTUR".equals(currentUserRole) || isTMA) {
                        isEditable = true;
                    }
                } else if (status.startsWith("Ditolak")) {
                   // Jika ditolak, semua role yang terlibat bisa view/edit
                   if ("PPBJ".equals(currentUserRole) || "PPK".equals(currentUserRole) || 
                       "DIREKTUR".equals(currentUserRole) || isTMA) {
                       isEditable = true;
                   }
                }
                
                // Jika DEV atau Admin, mungkin bisa semua? (Optional, ikut logic lama: DEV can act if simulation set correctly)
                // Kita anggap logic server filter sudah memastikan user hanya melihat yang relevan.
                // Jadi jika request muncul di list, user "mungkin" bisa bertindak.
                
                // Determine button text and style based on action type
                Button btnAction;
                if (canAct) {
                    // User can make new approval decision
                    btnAction = new Button("Persetujuan");
                    btnAction.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
                } else if (isEditable) {
                    // User can view/edit existing approval
                    btnAction = new Button("✏ Edit");
                    btnAction.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    // User can only view (no action available)
                    btnAction = new Button("👁 Lihat");
                    btnAction.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold;");
                }
                
                btnAction.getStyleClass().add("action-button-approve");
                btnAction.setOnAction(e -> showApprovalDialog(request));
                
                setGraphic(btnAction);
            }
        });
        return col;
    }

    private void showApprovalDialog(AssetRequest request) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.setTitle("Proses Persetujuan Aset");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(620);
        modalContent.setMaxWidth(620);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button (matching other modals)
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        Label title = new Label("Proses Persetujuan");
        title.getStyleClass().add("modal-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());
        
        headerBox.getChildren().addAll(title, spacer, closeButton);

        // Main content area
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxHeight(550);
        
        VBox contentArea = new VBox(16);
        contentArea.setPadding(new Insets(0, 24, 24, 24));

        // 1. Info Permohonan (Read-only)
        VBox infoBox = new VBox(8);
        Label lblInfo = new Label("Informasi Permohonan:");
        lblInfo.getStyleClass().add("form-label");
        lblInfo.setStyle("-fx-font-weight: bold;");
        
        String infoText = "Tujuan: " + (request.getTujuanPenggunaan() != null ? request.getTujuanPenggunaan() : "-");
        if (request.getDeskripsi() != null && !request.getDeskripsi().isEmpty()) {
            infoText += "\nDeskripsi: " + request.getDeskripsi();
        }
        
        TextArea txtInfo = new TextArea(infoText);
        txtInfo.setEditable(false);
        txtInfo.setWrapText(true);
        txtInfo.setPrefRowCount(2);
        txtInfo.getStyleClass().add("form-textarea");
        txtInfo.setStyle("-fx-background-color: #f1f5f9;");
        
        infoBox.getChildren().addAll(lblInfo, txtInfo);
        contentArea.getChildren().add(infoBox);

        String currentRole = LoginSession.getRole();
        
        // Check if TIM_MANAJEMEN_ASET - special multi-role approval
        if ("TIM_MANAJEMEN_ASET".equals(currentRole)) {
            // Get pending roles
            java.util.List<String> pendingRoles = getPendingRoles(request);
            
            if (pendingRoles.isEmpty()) {
                Label lblNoAction = new Label("✓ Semua persetujuan sudah lengkap.");
                lblNoAction.setStyle("-fx-font-size: 14px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                contentArea.getChildren().add(lblNoAction);
            } else {
                // Info label for TMA
                Label lblTmaInfo = new Label("Sebagai Tim Manajemen Aset, centang persetujuan yang ingin diwakilkan dan isi nomor surat yang telah ditandatangani.");
                lblTmaInfo.setWrapText(true);
                lblTmaInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");
                contentArea.getChildren().add(lblTmaInfo);
                
                Separator sepInfo = new Separator();
                sepInfo.setPadding(new Insets(8, 0, 8, 0));
                contentArea.getChildren().add(sepInfo);
                
                // Create approval fields for each pending role with radio buttons for approve/reject
                java.util.List<java.util.Map<String, Object>> roleFieldsList = new java.util.ArrayList<>();
                
                for (String pendingRole : pendingRoles) {
                    VBox roleBox = new VBox(8);
                    roleBox.setPadding(new Insets(12));
                    roleBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");
                    
                    // Role header label
                    Label lblRole = new Label("Atas nama " + pendingRole + ":");
                    lblRole.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #2c3e50;");
                    
                    // Radio buttons for action selection
                    ToggleGroup actionGroup = new ToggleGroup();
                    
                    RadioButton rbNone = new RadioButton("Tidak ada aksi");
                    rbNone.setToggleGroup(actionGroup);
                    rbNone.setSelected(true);
                    rbNone.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                    
                    RadioButton rbApprove = new RadioButton("✓ Setujui");
                    rbApprove.setToggleGroup(actionGroup);
                    rbApprove.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a; -fx-font-weight: 600;");
                    
                    RadioButton rbReject = new RadioButton("✗ Tolak");
                    rbReject.setToggleGroup(actionGroup);
                    rbReject.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: 600;");
                    
                    HBox radioBox = new HBox(16);
                    radioBox.getChildren().addAll(rbNone, rbApprove, rbReject);
                    
                    // Fields container (hidden by default)
                    VBox fieldsContainer = new VBox(8);
                    fieldsContainer.setVisible(false);
                    fieldsContainer.setManaged(false);
                    
                    // Nomor Surat for this role
                    Label lblNomorSurat = new Label("Nomor Surat " + pendingRole + " (wajib):");
                    lblNomorSurat.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #475569;");
                    
                    TextField txtNomorSurat = new TextField();
                    txtNomorSurat.setPromptText("Contoh: SURAT/" + pendingRole + "/2024/001");
                    txtNomorSurat.getStyleClass().add("form-input");
                    
                    // Catatan for this role (optional for approve, required for reject)
                    Label lblCatatan = new Label("Catatan:");
                    lblCatatan.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #475569;");
                    
                    TextField txtCatatan = new TextField();
                    txtCatatan.setPromptText("Catatan tambahan...");
                    txtCatatan.getStyleClass().add("form-input");
                    
                    fieldsContainer.getChildren().addAll(lblNomorSurat, txtNomorSurat, lblCatatan, txtCatatan);
                    
                    // Toggle fields visibility and style based on radio selection
                    actionGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                        boolean showFields = newVal == rbApprove || newVal == rbReject;
                        fieldsContainer.setVisible(showFields);
                        fieldsContainer.setManaged(showFields);
                        
                        if (newVal == rbApprove) {
                            roleBox.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #16a34a; -fx-border-radius: 8; -fx-background-radius: 8;");
                            lblCatatan.setText("Catatan (opsional):");
                        } else if (newVal == rbReject) {
                            roleBox.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #dc2626; -fx-border-radius: 8; -fx-background-radius: 8;");
                            lblCatatan.setText("Alasan Penolakan (wajib):");
                        } else {
                            roleBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");
                        }
                    });
                    
                    roleBox.getChildren().addAll(lblRole, radioBox, fieldsContainer);
                    contentArea.getChildren().add(roleBox);
                    
                    // Store references
                    java.util.Map<String, Object> roleFields = new java.util.HashMap<>();
                    roleFields.put("role", pendingRole);
                    roleFields.put("actionGroup", actionGroup);
                    roleFields.put("rbApprove", rbApprove);
                    roleFields.put("rbReject", rbReject);
                    roleFields.put("nomorSurat", txtNomorSurat);
                    roleFields.put("catatan", txtCatatan);
                    roleFieldsList.add(roleFields);
                }
                
                // Footer Buttons for TMA
                HBox buttonBox = new HBox(12);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(16, 0, 0, 0));
                
                Button btnCancel = new Button("Batal");
                btnCancel.getStyleClass().add("secondary-button");
                btnCancel.setOnAction(e -> modalStage.close());
                
                Button btnSave = new Button("Simpan Keputusan");
                btnSave.getStyleClass().add("primary-button");
                btnSave.setStyle("-fx-background-color: #3b82f6;");
                
                btnSave.setOnAction(e -> {
                    // Collect selected actions and validate
                    java.util.List<java.util.Map<String, Object>> selectedActions = new java.util.ArrayList<>();
                    boolean allValid = true;
                    
                    for (java.util.Map<String, Object> fields : roleFieldsList) {
                        RadioButton rbApprove = (RadioButton) fields.get("rbApprove");
                        RadioButton rbReject = (RadioButton) fields.get("rbReject");
                        TextField nomorField = (TextField) fields.get("nomorSurat");
                        TextField catatanField = (TextField) fields.get("catatan");
                        
                        boolean isApprove = rbApprove.isSelected();
                        boolean isReject = rbReject.isSelected();
                        
                        if (isApprove || isReject) {
                            // Validate nomor surat (required for both)
                            if (nomorField.getText() == null || nomorField.getText().trim().isEmpty()) {
                                nomorField.setStyle("-fx-border-color: #dc2626; -fx-border-width: 2;");
                                allValid = false;
                            } else {
                                nomorField.setStyle("");
                            }
                            
                            // Validate catatan for rejection (required)
                            if (isReject && (catatanField.getText() == null || catatanField.getText().trim().isEmpty())) {
                                catatanField.setStyle("-fx-border-color: #dc2626; -fx-border-width: 2;");
                                allValid = false;
                            } else {
                                catatanField.setStyle("");
                            }
                            
                            if (allValid) {
                                fields.put("isApprove", isApprove);
                                selectedActions.add(fields);
                            }
                        }
                    }
                    
                    if (selectedActions.isEmpty()) {
                        showNotification("Peringatan", "Pilih minimal satu aksi (setuju atau tolak)!");
                        return;
                    }
                    
                    if (!allValid) {
                        showNotification("Error", "Lengkapi field yang diperlukan!");
                        return;
                    }
                    
                    // Process selected actions
                    int approveCount = 0;
                    int rejectCount = 0;
                    for (java.util.Map<String, Object> fields : selectedActions) {
                        String role = (String) fields.get("role");
                        boolean isApprove = (Boolean) fields.get("isApprove");
                        TextField nomorField = (TextField) fields.get("nomorSurat");
                        TextField catatanField = (TextField) fields.get("catatan");
                        
                        String nomorSurat = nomorField.getText();
                        String catatan = catatanField.getText();
                        
                        // Determine status based on action and role
                        String newStatus = isApprove ? "Disetujui " + role : "Ditolak " + role;
                        
                        if (isApprove) approveCount++;
                        else rejectCount++;
                        
                        // Call API for each action
                        dataService.updateAssetRequestStatus(request.getId(), request.getTipe(), newStatus, catatan, nomorSurat, success -> {
                            // Handled after loop
                        });
                    }
                    
                    modalStage.close();
                    
                    // Refresh after a short delay
                    final int approves = approveCount;
                    final int rejects = rejectCount;
                    new Thread(() -> {
                        try { Thread.sleep(500); } catch (InterruptedException ex) {}
                        Platform.runLater(() -> {
                            refreshTables();
                            String msg = "";
                            if (approves > 0) msg += approves + " persetujuan";
                            if (rejects > 0) {
                                if (!msg.isEmpty()) msg += " dan ";
                                msg += rejects + " penolakan";
                            }
                            showNotification("Sukses", msg + " berhasil disimpan!");
                        });
                    }).start();
                });
                
                buttonBox.getChildren().addAll(btnCancel, btnSave);
                contentArea.getChildren().add(buttonBox);
            }
        } else {
            // Normal single-role approval (PPBJ, PPK, DIREKTUR)
            
            // 2. Catatan Penyetuju (Editable)
            VBox catatanBox = new VBox(8);
            Label lblCatatan = new Label("Catatan Anda (opsional):");
            lblCatatan.getStyleClass().add("form-label");
            lblCatatan.setStyle("-fx-font-weight: bold;");
            
            TextArea txtCatatan = new TextArea();
            txtCatatan.setPromptText("Tulis catatan atau pesan untuk pemohon...");
            txtCatatan.setWrapText(true);
            txtCatatan.setPrefRowCount(3);
            txtCatatan.getStyleClass().add("form-textarea");
            
            catatanBox.getChildren().addAll(lblCatatan, txtCatatan);

            // 3. Nomor Surat (Optional Text Field)
            VBox nomorSuratBox = new VBox(8);
            Label lblNomorSurat = new Label("Nomor Surat (opsional):");
            lblNomorSurat.getStyleClass().add("form-label");
            lblNomorSurat.setStyle("-fx-font-weight: bold;");
            
            TextField txtNomorSurat = new TextField();
            txtNomorSurat.setPromptText("Contoh: SURAT/2024/001");
            txtNomorSurat.getStyleClass().add("form-input");
            
            nomorSuratBox.getChildren().addAll(lblNomorSurat, txtNomorSurat);

            // Separator
            Separator sep = new Separator();
            sep.setPadding(new Insets(8, 0, 8, 0));

            // 4. Keputusan (Radio Box)
            VBox decisionBox = new VBox(12);
            Label lblKeputusan = new Label("Keputusan Anda:");
            lblKeputusan.getStyleClass().add("form-label");
            lblKeputusan.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            ToggleGroup group = new ToggleGroup();
            
            RadioButton rbSetuju = new RadioButton("Setujui Permohonan");
            rbSetuju.setToggleGroup(group);
            rbSetuju.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 600; -fx-font-size: 13px;");
            
            RadioButton rbTolak = new RadioButton("Tolak Permohonan");
            rbTolak.setToggleGroup(group);
            rbTolak.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 600; -fx-font-size: 13px;");

            decisionBox.getChildren().addAll(lblKeputusan, rbSetuju, rbTolak);

            // Footer Buttons
            HBox buttonBox = new HBox(12);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(16, 0, 0, 0));
            
            Button btnCancel = new Button("Batal");
            btnCancel.getStyleClass().add("secondary-button");
            btnCancel.setOnAction(e -> modalStage.close());
            
            Button btnSave = new Button("Simpan Keputusan");
            btnSave.getStyleClass().add("primary-button");
            btnSave.setDisable(true);
            
            group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> btnSave.setDisable(newVal == null));
            
            btnSave.setOnAction(e -> {
                boolean isApprove = rbSetuju.isSelected();
                String catatan = txtCatatan.getText();
                String nomorSurat = txtNomorSurat.getText();
                handleApproval(request, isApprove, catatan, nomorSurat);
                modalStage.close();
            });
            
            buttonBox.getChildren().addAll(btnCancel, btnSave);

            contentArea.getChildren().addAll(catatanBox, nomorSuratBox, sep, decisionBox, buttonBox);
        }
        
        scrollPane.setContent(contentArea);
        modalContent.getChildren().addAll(headerBox, scrollPane);

        Scene scene = new Scene(modalContent);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }
    
    /**
     * Get list of roles that haven't approved yet (for TIM_MANAJEMEN_ASET)
     */
    private java.util.List<String> getPendingRoles(AssetRequest request) {
        java.util.List<String> pendingRoles = new java.util.ArrayList<>();
        String[] allRoles = {"PPBJ", "PPK", "Direktur"};
        
        // Fetch approval logs for this request
        String cacheKey = request.getId() + "-" + request.getTipe();
        java.util.List<ApprovalLogDto> logs = approvalLogsCache.get(cacheKey);
        
        if (logs == null) {
            ApprovalLogDto[] logsArray = "Permohonan".equals(request.getTipe()) 
                ? logRiwayatApi.getApprovalLogs(request.getId(), null)
                : logRiwayatApi.getApprovalLogs(null, request.getId());
            logs = Arrays.asList(logsArray);
            approvalLogsCache.put(cacheKey, logs);
        }
        
        // Check which roles have NOT approved yet
        for (String role : allRoles) {
            boolean hasApproved = logs.stream()
                .anyMatch(log -> role.equals(log.getRole()) && "Disetujui".equals(log.getStatus()));
            if (!hasApproved) {
                pendingRoles.add(role);
            }
        }
        
        return pendingRoles;
    }

    private void handleApproval(AssetRequest request, boolean isApprove, String catatan, String lampiran) {
        String role = LoginSession.getRole();
        String newStatus = request.getStatus(); // Default backup
        
        // Logic mapping status string (Client Side Prediction / Formatting)
        // Backend service updates status based on role anyway.
        // But we usually send the *Action* or update the string to match backend expectation.
        
        // Existing logic in createStatusCell or similar?
        // DataService.updateAssetRequestStatus expects the NEW status string.
        
        if (isApprove) {
            if ("PPBJ".equals(role)) newStatus = "Disetujui PPBJ";
            else if ("PPK".equals(role)) newStatus = "Disetujui PPK";
            else if ("DIREKTUR".equals(role)) newStatus = "Disetujui Direktur";
            else if ("TIM_MANAJEMEN_ASET".equals(role)) newStatus = "Disetujui Tim Aset"; // Fallback
            else newStatus = "Disetujui";
        } else {
            // Reject
            // Format: "Ditolak [Role]"
             if ("PPBJ".equals(role)) newStatus = "Ditolak PPBJ";
            else if ("PPK".equals(role)) newStatus = "Ditolak PPK";
            else if ("DIREKTUR".equals(role)) newStatus = "Ditolak Direktur";
            else newStatus = "Ditolak";
        }
        
        final String finalStatus = newStatus; // Capture for lambda
        dataService.updateAssetRequestStatus(request.getId(), request.getTipe(), finalStatus, catatan, lampiran, success -> {
            Platform.runLater(() -> {
                if (success) {
                    refreshTables();
                    showNotification("Sukses", "Status berhasil diperbarui menjadi: " + finalStatus);
                } else {
                    showNotification("Error", "Gagal memperbarui status");
                }
            });
        });
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
        modalStage.initStyle(StageStyle.TRANSPARENT);
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
        
        // Alur: TMA -> PPBJ -> PPK -> Direktur
        String[] roleOrder = {"Tim Manajemen Aset", "PPBJ", "PPK", "Direktur"};
        
        // Check if fully approved (all roles have approved including Direktur)
        boolean fullyApproved = request.getStatus() != null && 
            request.getStatus().toLowerCase().contains("disetujui direktur");
        
        VBox approvalCards = new VBox(10);
        
        for (int i = 0; i < roleOrder.length; i++) {
            String role = roleOrder[i];
            ApprovalLogDto log = logs.stream()
                .filter(l -> role.equals(l.getRole()))
                .findFirst()
                .orElse(null);
            
            HBox card = createApprovalCard(role, log, i == 0, fullyApproved);
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
        } else if ("DIREKTUR".equals(currentUserRole)) {
            roleToCheck = "Direktur";
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
        modalStage.initStyle(StageStyle.TRANSPARENT);
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
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private HBox createApprovalCard(String role, ApprovalLogDto log, boolean isInitiator, boolean fullyApproved) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Base style 
        String baseStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1;";
        
        // If fully approved, use green background for all cards
        if (fullyApproved) {
            card.setStyle(baseStyle + " -fx-background-color: #dcfce7; -fx-border-color: #16a34a;");
        } else {
            card.setStyle(baseStyle + " -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");
        }
        
        // Status Icon
        Label icon = new Label();
        icon.setStyle("-fx-font-size: 24px;");
        
        VBox infoBox = new VBox(4);
        
        Label roleLabel = new Label(role);
        roleLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 13px;");
        
        if (isInitiator) {
            // Tim Manajemen Aset - always shows "Mengajukan Aset" in green
            icon.setText("📝");
            icon.setStyle(icon.getStyle() + " -fx-text-fill: #16a34a;");
            statusLabel.setText("Mengajukan Aset");
            statusLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #16a34a; -fx-font-size: 13px;");
            if (!fullyApproved) {
                card.setStyle(baseStyle + " -fx-background-color: #f0fdf4; -fx-border-color: #16a34a;");
            }
            infoBox.getChildren().addAll(roleLabel, statusLabel);
        } else if (log == null) {
            // Pending - belum diproses
            icon.setText("⏰");
            icon.setStyle(icon.getStyle() + " -fx-text-fill: #94a3b8;");
            statusLabel.setText("Menunggu Persetujuan");
            statusLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: #64748b; -fx-font-size: 13px;");
            infoBox.getChildren().addAll(roleLabel, statusLabel);
        } else {
            if ("Disetujui".equals(log.getStatus())) {
                icon.setText("✓");
                icon.setStyle(icon.getStyle() + " -fx-text-fill: #16a34a;");
                if (!fullyApproved) {
                    card.setStyle(baseStyle + " -fx-background-color: #f0fdf4; -fx-border-color: #16a34a;");
                }
                
                statusLabel.setText("Disetujui oleh " + role);
                statusLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #16a34a; -fx-font-size: 13px;");
                
                infoBox.getChildren().addAll(roleLabel, statusLabel);
                
                // Add delegation indicator if delegated by Tim Manajemen Aset
                if (log.isDelegated()) {
                    Label delegatedLabel = new Label("↳ diwakilkan oleh " + log.getNamaPegawai() + " (Tim Manajemen Aset)");
                    delegatedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-font-style: italic;");
                    infoBox.getChildren().add(delegatedLabel);
                }
                
                // Add catatan if exists
                if (log.getCatatan() != null && !log.getCatatan().isEmpty()) {
                    Label catatanLabel = new Label("💬 " + log.getCatatan());
                    catatanLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-wrap-text: true;");
                    catatanLabel.setWrapText(true);
                    catatanLabel.setMaxWidth(400);
                    infoBox.getChildren().add(catatanLabel);
                }
                
                // Add nomor surat if exists
                if (log.getLampiran() != null && !log.getLampiran().isEmpty()) {
                    Label nomorSuratLabel = new Label("📄 No. Surat: " + log.getLampiran());
                    nomorSuratLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
                    infoBox.getChildren().add(nomorSuratLabel);
                }
            } else if ("Ditolak".equals(log.getStatus())) {
                icon.setText("✗");
                icon.setStyle(icon.getStyle() + " -fx-text-fill: #dc2626;");
                card.setStyle(baseStyle + " -fx-background-color: #fef2f2; -fx-border-color: #dc2626;");
                
                statusLabel.setText("Ditolak oleh " + role);
                statusLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #dc2626; -fx-font-size: 13px;");
                
                infoBox.getChildren().addAll(roleLabel, statusLabel);
                
                // Add delegation indicator if delegated by Tim Manajemen Aset
                if (log.isDelegated()) {
                    Label delegatedLabel = new Label("↳ diwakilkan oleh " + log.getNamaPegawai() + " (Tim Manajemen Aset)");
                    delegatedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-font-style: italic;");
                    infoBox.getChildren().add(delegatedLabel);
                }
                
                // Add catatan if exists (especially important for rejection reasons)
                if (log.getCatatan() != null && !log.getCatatan().isEmpty()) {
                    Label catatanLabel = new Label("💬 " + log.getCatatan());
                    catatanLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #991b1b; -fx-wrap-text: true;");
                    catatanLabel.setWrapText(true);
                    catatanLabel.setMaxWidth(400);
                    infoBox.getChildren().add(catatanLabel);
                }
                
                // Add nomor surat if exists
                if (log.getLampiran() != null && !log.getLampiran().isEmpty()) {
                    Label nomorSuratLabel = new Label("📄 No. Surat: " + log.getLampiran());
                    nomorSuratLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f1d1d; -fx-font-weight: 600;");
                    infoBox.getChildren().add(nomorSuratLabel);
                }
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

    public void refreshTables() {
        // Use background task to prevent UI blocking
        Task<Void> refreshTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Fetch data from API
                List<AssetRequest> permohonan = dataService.getPermohonanAset();
                List<AssetRequest> pengajuan = dataService.getPengajuanAset();
                
                javafx.application.Platform.runLater(() -> {
                    // Update UI on JavaFX thread
                    approvalLogsCache.clear(); // Clear cache
                    permohonanList.setAll(permohonan);
                    pengajuanList.setAll(pengajuan);
                });
                return null;
            }
        };

        refreshTask.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            System.err.println("Failed to refresh approval tables: " + e.getSource().getException().getMessage());
        });

        Thread thread = new Thread(refreshTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void rebuildTableColumns() {
        // Rebuild columns untuk force re-render buttons dengan status terbaru
        TableColumn<AssetRequest, String> keputusanColPermohonan = buildDecisionColumn();
        TableColumn<AssetRequest, String> keputusanColPengajuan = buildDecisionColumn();
        
        // Replace keputusan column di kedua table
        permohonanTable.getColumns().set(7, keputusanColPermohonan);
        pengajuanTable.getColumns().set(7, keputusanColPengajuan);
    }
}

