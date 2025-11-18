package com.siata.client.controller;

import com.siata.client.model.Employee;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EmployeeManagementController implements Initializable {

    @FXML private Button addButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> unitCombo;
    @FXML private TableView<Employee> employeeTable;

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final DataService dataService = DataService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilterBar();
        addButton.setOnAction(e -> showEmployeeForm(null));
        refreshTable();
    }

    private void setupTable() {
        employeeTable.setItems(employeeList);
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Employee, String> nipCol = new TableColumn<>("NIP");
        nipCol.setCellValueFactory(new PropertyValueFactory<>("nip"));

        TableColumn<Employee, String> namaCol = new TableColumn<>("Nama");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));

        TableColumn<Employee, String> jabatanCol = new TableColumn<>("Jabatan");
        jabatanCol.setCellValueFactory(new PropertyValueFactory<>("jabatan"));

        TableColumn<Employee, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<Employee, String> asetCol = new TableColumn<>("Aset yang Dimiliki");
        asetCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAsetDimilikiSummary())
        );
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
                if (empty || item == null) setGraphic(null);
                else {
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
                editButton.setOnAction(e -> showEmployeeForm(getTableView().getItems().get(getIndex())));
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
                setGraphic(empty ? null : actionBox);
            }
        });
        aksiCol.setPrefWidth(150);

        employeeTable.getColumns().setAll(nipCol, namaCol, jabatanCol, unitCol, asetCol, aksiCol);
    }

    private void setupFilterBar() {
        unitCombo.getItems().addAll("Semua Unit", "Subdit Teknis", "Subdit Operasional", "Subdit Keamanan", "Subdit SDM");
        unitCombo.setValue("Semua Unit");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("ghost-button");
        button.setStyle("-fx-font-size: 14px; -fx-padding: 6 10;");
        return button;
    }

    private void refreshTable() {
        employeeList.setAll(dataService.getEmployees());
    }

    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refreshTable();
            return;
        }
        employeeList.setAll(dataService.getEmployees().stream()
                .filter(emp ->
                        emp.getNip().toLowerCase().contains(searchText.toLowerCase()) ||
                                emp.getNamaLengkap().toLowerCase().contains(searchText.toLowerCase())
                ).toList()
        );
    }

    private void showEmployeeForm(Employee editableEmployee) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UTILITY);
        modalStage.setTitle(editableEmployee == null ? "Tambah Pegawai Baru" : "Edit Pegawai");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(480);
        modalContent.setMaxWidth(480);
        modalContent.setMaxHeight(600);
        modalContent.getStyleClass().add("modal-content");

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24,24,16,24));
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

        // Form fields
        TextField nipField = new TextField();
        nipField.setPromptText("Contoh: 199001152015011001");
        Label nipLabel = new Label("NIP"); nipLabel.getStyleClass().add("form-label");
        if(editableEmployee != null){nipField.setText(editableEmployee.getNip()); nipField.setDisable(true);}

        TextField namaField = new TextField();
        namaField.setPromptText("Contoh: Budi Santoso");
        Label namaLabel = new Label("Nama Lengkap"); namaLabel.getStyleClass().add("form-label");
        if(editableEmployee!=null) namaField.setText(editableEmployee.getNamaLengkap());

        TextField jabatanField = new TextField();
        jabatanField.setPromptText("Contoh: Analis Data");
        Label jabatanLabel = new Label("Jabatan"); jabatanLabel.getStyleClass().add("form-label");
        if(editableEmployee!=null) jabatanField.setText(editableEmployee.getJabatan());

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("Subdit Teknis", "Subdit Operasional", "Subdit Keamanan", "Subdit SDM");
        unitCombo.setPromptText("Pilih unit");
        Label unitLabel = new Label("Unit / Subdirektorat"); unitLabel.getStyleClass().add("form-label");
        if(editableEmployee!=null) unitCombo.setValue(editableEmployee.getUnit());

        TextArea asetArea = new TextArea();
        asetArea.setPromptText("Pisahkan dengan enter, contoh:\nLaptop Dell Latitude 5420\nMeja Kerja Kayu Jati");
        asetArea.setPrefRowCount(4);
        Label asetLabel = new Label("Daftar Aset yang Dimiliki"); asetLabel.getStyleClass().add("form-label");
        if(editableEmployee!=null && !editableEmployee.getAsetDimiliki().isEmpty())
            asetArea.setText(String.join("\n", editableEmployee.getAsetDimiliki()));

        VBox formContent = new VBox(16, nipLabel, nipField, namaLabel, namaField,
                jabatanLabel, jabatanField, unitLabel, unitCombo, asetLabel, asetArea);
        formContent.setPadding(new Insets(0,24,0,24));

        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setMaxHeight(400);
        scrollPane.getStyleClass().add("modal-scroll-pane");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20,24,24,24));
        Button cancelButton = new Button("Batal"); cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> modalStage.close());
        Button saveButton = new Button(editableEmployee == null ? "Simpan" : "Simpan Perubahan");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            if(saveEmployee(editableEmployee, nipField.getText(), namaField.getText(),
                    jabatanField.getText(), unitCombo.getValue(), asetArea.getText()))
                modalStage.close();
        });
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        modalContent.getChildren().addAll(headerBox, scrollPane, buttonBox);

        Stage modalStageFinal = modalStage;
        modalStage.setScene(new javafx.scene.Scene(modalContent));
        modalStage.showAndWait();
    }

    private boolean saveEmployee(Employee editableEmployee, String nip, String nama, String jabatan,
                                 String unit, String asetText) {
        if (nip == null || nip.isBlank()) { showAlert("Masukkan NIP"); return false; }
        if (nama == null || nama.isBlank()) { showAlert("Masukkan nama lengkap"); return false; }
        if (jabatan == null || jabatan.isBlank()) { showAlert("Masukkan jabatan"); return false; }
        if (unit == null || unit.isBlank()) { showAlert("Pilih unit/subdirektorat"); return false; }

        List<String> asetList = Arrays.stream(asetText==null?new String[0]:asetText.split("\\r?\\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();

        if(editableEmployee==null)
            dataService.addEmployee(new Employee(nip.trim(), nama.trim(), jabatan.trim(), unit, asetList));
        else {
            editableEmployee.setNamaLengkap(nama.trim());
            editableEmployee.setJabatan(jabatan.trim());
            editableEmployee.setUnit(unit);
            editableEmployee.setAsetDimiliki(asetList);
            dataService.updateEmployee(editableEmployee);
        }
        refreshTable();
        return true;
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

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24,24,16,24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);

        VBox titleBox = new VBox(4);
        Label title = new Label("Aset yang Dimiliki"); title.getStyleClass().add("modal-title");
        Label subtitle = new Label(employee.getNamaLengkap() + " - " + employee.getJabatan());
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());
        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(employee.getAsetDimiliki().isEmpty() ?
                List.of("Belum ada aset yang tercatat") : employee.getAsetDimiliki());
        listView.setPrefHeight(300); listView.setPadding(new Insets(0,24,0,24));

        modalContent.getChildren().addAll(headerBox, listView);

        Stage stage = modalStage;
        stage.setScene(new javafx.scene.Scene(modalContent));
        stage.showAndWait();
    }

    private boolean confirmDelete(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penghapusan");
        alert.setHeaderText("Hapus Pegawai");
        alert.setContentText("Apakah Anda yakin ingin menghapus pegawai " + employee.getNamaLengkap() + "?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
