package com.siata.client.view;

import com.siata.client.model.Asset;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

public class AssetRemovalView extends VBox {

    private final TableView<Asset> table;
    private final ObservableList<Asset> deletedAssetList;
    private final DataService dataService;
    private final AtomicInteger assetIdCounter = new AtomicInteger(9);

    public AssetRemovalView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        deletedAssetList = FXCollections.observableArrayList();
        table = new TableView<>();
        
        buildView();
        refreshTable();
    }

    private void buildView() {
        table.setItems(deletedAssetList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");

        TableColumn<Asset, Boolean> checkboxCol = new TableColumn<>("");
        checkboxCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isDeleted())
        );
        checkboxCol.setCellFactory(column -> new TableCell<Asset, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setOnAction(e -> {
                    Asset asset = getTableView().getItems().get(getIndex());
                    asset.setDeleted(checkBox.isSelected());
                });
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Asset asset = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(asset.isDeleted());
                    setGraphic(checkBox);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        checkboxCol.setMinWidth(35);
        checkboxCol.setPrefWidth(80);
        checkboxCol.setMaxWidth(100);

        checkboxCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Asset, String> idCol = new TableColumn<>("Kode");
        idCol.setCellValueFactory(cellData -> {
            // Generate ID based on index or use existing
            Asset asset = cellData.getValue();
            int index = table.getItems().indexOf(asset);
            return new javafx.beans.property.SimpleStringProperty(asset.getKodeAset());
        });

        TableColumn<Asset, String> namaCol = new TableColumn<>("Nama Aset");
        namaCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaAset())
        );

        TableColumn<Asset, String> jenisCol = new TableColumn<>("Jenis");
        jenisCol.setCellValueFactory(new PropertyValueFactory<>("jenisAset"));

        TableColumn<Asset, String> kondisiCol = new TableColumn<>("Kondisi");
        kondisiCol.setCellValueFactory(new PropertyValueFactory<>("kondisi"));

        TableColumn<Asset, String> SubdirCol = new TableColumn<>("Subdirektorat");
        SubdirCol.setCellValueFactory(new PropertyValueFactory<>("Subdir"));

        TableColumn<Asset, String> tanggalCol = new TableColumn<>("Tanggal Perolehan");
        tanggalCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggalPerolehan();
            if (date != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    date.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        table.getColumns().setAll(List.of(checkboxCol, idCol, namaCol, jenisCol, SubdirCol, tanggalCol, kondisiCol));

        VBox tableContainer = new VBox(16);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getStyleClass().add("table-container");
        tableContainer.getChildren().addAll(table);

        getChildren().addAll(buildPageHeader(), tableContainer);
    }

    private Node buildPageHeader() {
        // Title and description now shown in main header
        return new HBox();
    }

    private void refreshTable() {
        deletedAssetList.setAll(dataService.getDeletedAssets());
    }
}

