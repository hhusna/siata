package com.siata.client.view;

import com.siata.client.api.AssetApi;
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
    private final AssetApi assetApi = new AssetApi();
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

        TableColumn<Asset, String> idCol = new TableColumn<>("Kode Aset");
        idCol.setCellValueFactory(cellData -> {
            Asset asset = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(asset.getKodeAset());
        });

        TableColumn<Asset, String> noAsetCol = new TableColumn<>("No Aset");
        noAsetCol.setCellValueFactory(cellData -> {
            Integer no = cellData.getValue().getNoAset();
            return new javafx.beans.property.SimpleStringProperty(no != null ? String.valueOf(no) : "-");
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

        // Kolom Siap Lelang
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
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });

        // Kolom Aksi (Undo dan Hapus Permanen)
        TableColumn<Asset, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(column -> new TableCell<Asset, Void>() {
            private final Button undoBtn = new Button("↩");
            private final Button deleteBtn = new Button("🗑");
            private final HBox actionBox = new HBox(6, undoBtn, deleteBtn);
            
            {
                actionBox.setAlignment(Pos.CENTER);
                undoBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-cursor: hand;");
                
                undoBtn.setTooltip(new Tooltip("Kembalikan ke Manajemen Aset"));
                deleteBtn.setTooltip(new Tooltip("Hapus Permanen dari Database"));
                
                undoBtn.setOnAction(e -> {
                    Asset asset = getTableView().getItems().get(getIndex());
                    handleUndo(asset);
                });
                
                deleteBtn.setOnAction(e -> {
                    Asset asset = getTableView().getItems().get(getIndex());
                    handlePermanentDelete(asset);
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
        aksiCol.setPrefWidth(120);
        
        table.getColumns().setAll(List.of(idCol, noAsetCol, namaCol, jenisCol, SubdirCol, tanggalCol, kondisiCol, siapLelangCol, aksiCol));

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

    private void handleUndo(Asset asset) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Kembalikan Aset");
        confirmAlert.setHeaderText("Kembalikan Aset ke Manajemen Aset?");
        confirmAlert.setContentText("Aset \"" + asset.getNamaAset() + "\" akan dikembalikan ke daftar aset aktif.\n\n" +
                                   "Aset akan muncul kembali di Manajemen Aset.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            MainShellView.showLoading("Mengembalikan aset...");
            
            javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    return assetApi.undoDeleteAset(asset.getIdAset());
                }
            };
            
            task.setOnSucceeded(ev -> {
                MainShellView.hideLoading();
                if (task.getValue()) {
                    dataService.clearAssetCache();
                    MainShellView.showSuccess("Aset berhasil dikembalikan ke Manajemen Aset.");
                    refreshTable();
                } else {
                    MainShellView.showError("Gagal mengembalikan aset. Silakan coba lagi.");
                }
            });
            
            task.setOnFailed(ev -> {
                MainShellView.hideLoading();
                MainShellView.showError("Error: " + task.getException().getMessage());
            });
            
            new Thread(task).start();
        }
    }

    private void handlePermanentDelete(Asset asset) {
        // First warning
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("⚠️ PERINGATAN: Hapus Permanen");
        warningAlert.setHeaderText("PERHATIAN! Aksi ini TIDAK DAPAT DIBATALKAN!");
        warningAlert.setContentText(
            "Anda akan MENGHAPUS PERMANEN aset:\n\n" +
            "📦 " + asset.getNamaAset() + "\n" +
            "🏷️ Kode: " + asset.getKodeAset() + "\n" +
            "📍 Subdirektorat: " + asset.getSubdir() + "\n\n" +
            "⚠️ PERINGATAN BISNIS:\n" +
            "Secara alur bisnis, aset yang sudah ditandai hapus TIDAK BOLEH dihapus permanen.\n" +
            "Fitur ini HANYA untuk memperbaiki kesalahan input data.\n\n" +
            "Apakah Anda BENAR-BENAR yakin ingin melanjutkan?"
        );
        
        ButtonType continueBtn = new ButtonType("Ya, Lanjutkan", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        warningAlert.getButtonTypes().setAll(continueBtn, cancelBtn);
        
        if (warningAlert.showAndWait().orElse(cancelBtn) == continueBtn) {
            // Second confirmation with typed confirmation
            TextInputDialog confirmDialog = new TextInputDialog();
            confirmDialog.setTitle("Konfirmasi Final");
            confirmDialog.setHeaderText("Ketik \"HAPUS\" untuk mengkonfirmasi penghapusan permanen");
            confirmDialog.setContentText("Konfirmasi:");
            
            confirmDialog.showAndWait().ifPresent(input -> {
                if ("HAPUS".equals(input.trim())) {
                    MainShellView.showLoading("Menghapus aset permanen...");
                    
                    javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {
                        @Override
                        protected Boolean call() {
                            return assetApi.permanentDeleteAset(asset.getIdAset());
                        }
                    };
                    
                    task.setOnSucceeded(ev -> {
                        MainShellView.hideLoading();
                        if (task.getValue()) {
                            MainShellView.showSuccess("Aset telah dihapus permanen dari database.");
                            refreshTable();
                        } else {
                            MainShellView.showError("Gagal menghapus aset. Silakan coba lagi.");
                        }
                    });
                    
                    task.setOnFailed(ev -> {
                        MainShellView.hideLoading();
                        MainShellView.showError("Error: " + task.getException().getMessage());
                    });
                    
                    new Thread(task).start();
                } else {
                    MainShellView.showWarning("Konfirmasi tidak valid. Penghapusan dibatalkan.");
                }
            });
        }
    }

    public void refreshTable() {
        deletedAssetList.setAll(dataService.getDeletedAssets());
    }
}

