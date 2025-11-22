package com.siata.client.view;

import com.siata.client.model.AssetRequest;
import com.siata.client.service.DataService;
import com.siata.client.session.LoginSession;
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
public class AssetApprovalView extends VBox {

    private final TableView<AssetRequest> permohonanTable;
    private final TableView<AssetRequest> pengajuanTable;
    private final ObservableList<AssetRequest> permohonanList;
    private final ObservableList<AssetRequest> pengajuanList;
    private final DataService dataService;

    public AssetApprovalView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        permohonanList = FXCollections.observableArrayList();
        pengajuanList = FXCollections.observableArrayList();
        permohonanTable = new TableView<>();
        pengajuanTable = new TableView<>();
        
        buildView();
        refreshTables();
    }

    private void buildView() {
        VBox permohonanSection = createApprovalSection("Daftar Permohonan", permohonanTable, permohonanList, true);
        VBox pengajuanSection = createApprovalSection("Daftar Pengajuan", pengajuanTable, pengajuanList, false);
        getChildren().addAll(buildPageHeader(), permohonanSection, pengajuanSection);
    }

    private Node buildPageHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(4);
        Label title = new Label("Persetujuan Aset");
        title.getStyleClass().add("page-intro-title");
        Label description = new Label("Kelola permohonan dan pengajuan aset dari pegawai");
        description.getStyleClass().add("page-intro-description");
        textGroup.getChildren().addAll(title, description);

        header.getChildren().add(textGroup);
        return header;
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
            private final Button approveButton = new Button("Setujui");
            private final Button rejectButton = new Button("Tolak");
            private final HBox box = new HBox(8, approveButton, rejectButton);

            {
                box.setAlignment(Pos.CENTER);
                approveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 12;");
                rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 12;");

                approveButton.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    if (confirmDecision("Setujui", request)) {
                        dataService.updateAssetRequestStatus(request, "Disetujui "+LoginSession.getRole(), LoginSession.getPegawaiDto().getNama());
                        refreshTables();
                    }
                });

                rejectButton.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    if (confirmDecision("Tolak", request)) {
                        dataService.updateAssetRequestStatus(request, "Ditolak "+LoginSession.getRole(), LoginSession.getPegawaiDto().getNama());
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
                    setGraphic(box);
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

    private boolean confirmDecision(String action, AssetRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(action + " Permohonan");
        alert.setHeaderText(action + " " + request.getTipe());
        alert.setContentText("Apakah Anda yakin ingin " + action.toLowerCase() + " " + request.getTipe().toLowerCase() +
            " untuk " + request.getPemohon() + "?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void refreshTables() {
        permohonanList.setAll(dataService.getPermohonanAset());
        pengajuanList.setAll(dataService.getPengajuanAset());
    }
}

