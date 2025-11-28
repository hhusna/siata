package com.siata.client.view;

import com.siata.client.api.PegawaiApi;
import com.siata.client.model.Employee;
import com.siata.client.service.DataService;
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

import java.util.List;

public class EmployeeManagementView extends VBox {

    private TableView<Employee> table;
    private final ObservableList<Employee> employeeList;
    private final DataService dataService;
    private final PegawaiApi pegawaiApi = new PegawaiApi();

    public EmployeeManagementView() {
        setSpacing(20);
        dataService = DataService.getInstance();
        employeeList = FXCollections.observableArrayList();
        buildView();
        refreshTable();
    }

    private void buildView() {
        Button addButton = new Button("+ Tambah Pegawai");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> showEmployeeForm(null));

        getChildren().add(buildPageHeader(addButton));

        // Search and filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("Semua Subdir", "PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        unitCombo.setValue("Semua Subdir");
        unitCombo.setPrefWidth(150);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nama atau NIP...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal, unitCombo.getValue()));
        
        unitCombo.setOnAction(e -> filterTable(searchField.getText(), unitCombo.getValue()));
        
        filterBar.getChildren().addAll(searchField, unitCombo);

        // Table
        table = new TableView<>();
        table.setItems(employeeList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");

        TableColumn<Employee, String> nipCol = new TableColumn<>("NIP");
        nipCol.setCellValueFactory(new PropertyValueFactory<>("nip"));
        
        TableColumn<Employee, String> namaCol = new TableColumn<>("Nama");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        
        TableColumn<Employee, String> unitCol = new TableColumn<>("Subdir");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        
        TableColumn<Employee, String> asetCol = new TableColumn<>("Aset yang Dimiliki");
        asetCol.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue();
            // Ambil jumlah aset dari manajemen aset berdasarkan NIP
            long jumlahAset = dataService.getAssets().stream()
                .filter(asset -> emp.getNip().equals(asset.getKeterangan()))
                .count();
            return new javafx.beans.property.SimpleStringProperty(jumlahAset + " aset");
        });
        asetCol.setCellFactory(column -> new TableCell<>() {
            private final Hyperlink detailLink = new Hyperlink();
            {
                detailLink.setOnAction(e -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    showEmployeeAssets(employee);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    detailLink.setText(item);
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
                        refreshTable();
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
        
        table.getColumns().setAll(List.of(nipCol, namaCol, unitCol, asetCol, aksiCol));

        VBox tableContainer = new VBox(16);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getStyleClass().add("table-container");
        tableContainer.getChildren().addAll(filterBar, table);

        getChildren().addAll(tableContainer);
    }

    private Node buildPageHeader(Button actionButton) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(4);
        Label title = new Label("Manajemen Pegawai");
        title.getStyleClass().add("page-intro-title");
        Label description = new Label("Kelola data pegawai dan aset yang dimiliki");
        description.getStyleClass().add("page-intro-description");
        textGroup.getChildren().addAll(title, description);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(textGroup, spacer, actionButton);
        return header;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        return button;
    }

    private void showEmployeeForm(Employee editableEmployee) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setTitle(editableEmployee == null ? "Tambah Pegawai Baru" : "Edit Pegawai");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(480);
        modalContent.setMaxWidth(480);
        modalContent.setMaxHeight(600);
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
        nipField.setPromptText("Contoh: 199001152015011001");
        Label nipLabel = new Label("NIP");
        nipLabel.getStyleClass().add("form-label");
        if (editableEmployee != null) {
            nipField.setText(editableEmployee.getNip());
            nipField.setDisable(true);
        }

        TextField namaField = new TextField();
        namaField.setPromptText("Contoh: Budi Santoso");
        Label namaLabel = new Label("Nama Lengkap");
        namaLabel.getStyleClass().add("form-label");
        if (editableEmployee != null) {
            namaField.setText(editableEmployee.getNamaLengkap());
        }

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        unitCombo.setPromptText("Pilih unit");
        Label unitLabel = new Label("Unit / Subdirektorat");
        unitLabel.getStyleClass().add("form-label");
        if (editableEmployee != null) {
            unitCombo.setValue(editableEmployee.getUnit());
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

            // Validasi input dasar
            if (nipInput == null || nipInput.trim().isEmpty()) {
                showAlert("NIP tidak boleh kosong");
                return;
            }
            if (nama == null || nama.trim().isEmpty()) {
                showAlert("Nama tidak boleh kosong");
                return;
            }
            if (unit == null || unit.trim().isEmpty()) {
                showAlert("Pilih subdirektorat");
                return;
            }

            // Validasi format NIP (harus angka dan 18 digit)
            if (!nipInput.matches("\\d+")) {
                showAlert("NIP harus berupa angka");
                return;
            }
            if (nipInput.length() != 18) {
                showAlert("NIP harus 18 digit");
                return;
            }

            // Validasi nama minimal 3 karakter dan hanya huruf dan spasi
            if (nama.length() < 3) {
                showAlert("Nama minimal 3 karakter");
                return;
            }
            if (!nama.matches("[a-zA-Z\\s]+")) {
                showAlert("Nama hanya boleh berisi huruf dan spasi");
                return;
            }

            long nip = Long.parseLong(nipInput);
            boolean success;

            if (editableEmployee == null) {
                // Mode Tambah - cek apakah NIP sudah ada
                if (dataService.isNipExists(nipInput)) {
                    showAlert("NIP sudah terdaftar di sistem");
                    return;
                }
                
                // Mode Tambah (POST) - jabatan dihilangkan
                success = pegawaiApi.addPegawai(nip, nama, unit, "");
            } else {
                // Mode Edit (PUT) - jabatan dihilangkan
                success = pegawaiApi.updatePegawai(nip, nama, unit, "");
            }

            if (success) {
                modalStage.close();
                refreshTable();
            } else {
                showAlert("Gagal menyimpan data pegawai");
            }
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(0, 24, 0, 24));
        formContent.getChildren().addAll(
            nipLabel, nipField,
            namaLabel, namaField,
            unitLabel, unitCombo
        );

        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setMaxHeight(400);
        scrollPane.getStyleClass().add("modal-scroll-pane");
        
        modalContent.getChildren().addAll(headerBox, scrollPane, buttonBox);

        Scene modalScene = new Scene(modalContent);
        modalScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }


    private void showEmployeeAssets(Employee employee) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UTILITY);
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
        modalStage.showAndWait();
    }

    private boolean confirmDelete(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penghapusan");
        alert.setHeaderText("Hapus Pegawai");
        alert.setContentText("Apakah Anda yakin ingin menghapus pegawai " + employee.getNamaLengkap() + "?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void refreshTable() {
        employeeList.setAll(dataService.getEmployees());
    }

    private void filterTable(String searchText, String unitFilter) {
        List<Employee> allEmployees = dataService.getEmployees();
        
        employeeList.setAll(allEmployees.stream()
            .filter(employee -> {
                // Search filter
                if (searchText != null && !searchText.isEmpty()) {
                    String search = searchText.toLowerCase();
                    if (!employee.getNip().toLowerCase().contains(search) &&
                        !employee.getNamaLengkap().toLowerCase().contains(search)) {
                        return false;
                    }
                }
                
                // Unit filter
                if (unitFilter != null && !unitFilter.equals("Semua Subdir")) {
                    if (!employee.getUnit().equals(unitFilter)) {
                        return false;
                    }
                }
                
                return true;
            })
            .toList()
        );
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

