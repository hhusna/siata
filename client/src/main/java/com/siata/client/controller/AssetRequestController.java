package com.siata.client.controller;

import com.siata.client.model.AssetRequest;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssetRequestController {

    @FXML private VBox permohonanSection;
    @FXML private VBox pengajuanSection;
    @FXML private Button addPengajuanButton;
    @FXML private Button addPermohonanButton;

    private final DataService dataService = DataService.getInstance();
    private final ObservableList<AssetRequest> permohonanList = FXCollections.observableArrayList();
    private final ObservableList<AssetRequest> pengajuanList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        addPengajuanButton.setOnAction(e -> showAssetRequestModal("Pengajuan", null));
        addPermohonanButton.setOnAction(e -> showAssetRequestModal("Permohonan", null));

        buildTableSections();
        refreshTables();
    }

    private void buildTableSections() {
        TableView<AssetRequest> permohonanTable = new TableView<>();
        TableView<AssetRequest> pengajuanTable = new TableView<>();

        permohonanSection.getChildren().add(buildTableSection("Permohonan Aset", permohonanTable, permohonanList));
        pengajuanSection.getChildren().add(buildTableSection("Daftar Pengajuan Aset", pengajuanTable, pengajuanList));
    }

    private VBox buildTableSection(String title, TableView<AssetRequest> table, ObservableList<AssetRequest> list) {
        VBox section = new VBox(16);

        TextField searchField = new TextField();
        searchField.setPromptText("Cari...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, list));

        table.setItems(list);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AssetRequest, String> nomorCol = new TableColumn<>("No.");
        nomorCol.setCellValueFactory(new PropertyValueFactory<>("noPermohonan"));
        TableColumn<AssetRequest, String> pemohonCol = new TableColumn<>("Pemohon");
        pemohonCol.setCellValueFactory(new PropertyValueFactory<>("pemohon"));
        TableColumn<AssetRequest, String> tanggalCol = new TableColumn<>("Tanggal");
        tanggalCol.setCellValueFactory(cd -> {
            LocalDate date = cd.getValue().getTanggal();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("d/M/yyyy")) : "-");
        });
        TableColumn<AssetRequest, Void> aksiCol = buildActionColumn();

        table.getColumns().setAll(nomorCol, tanggalCol, pemohonCol, aksiCol);

        section.getChildren().addAll(searchField, table);
        return section;
    }

    private TableColumn<AssetRequest, Void> buildActionColumn() {
        TableColumn<AssetRequest, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏");
            {
                editBtn.setOnAction(e -> {
                    AssetRequest request = getTableView().getItems().get(getIndex());
                    showAssetRequestModal(request.getTipe(), request);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });
        return aksiCol;
    }

    private void filterTable(String searchText, ObservableList<AssetRequest> list) {
        List<AssetRequest> source = list;
        if (searchText == null || searchText.isEmpty()) return;
        list.setAll(source.stream()
                .filter(r -> r.getNoPermohonan().toLowerCase().contains(searchText.toLowerCase())
                        || r.getPemohon().toLowerCase().contains(searchText.toLowerCase()))
                .toList());
    }

    private void refreshTables() {
        permohonanList.setAll(dataService.getPermohonanAset());
        pengajuanList.setAll(dataService.getPengajuanAset());
    }

    private void showAssetRequestModal(String tipe, AssetRequest editableRequest) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.siata.client/controller/AssetRequestModal.fxml"));
            Parent modalRoot = loader.load();
            AssetRequestModalController controller = loader.getController();
            controller.setEditableRequest(editableRequest, tipe);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(modalRoot));
            modalStage.showAndWait();
            refreshTables();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
