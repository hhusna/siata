package com.siata.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExportPdfModalController implements Initializable {

    @FXML private VBox modalContainer;
    @FXML private ComboBox<String> formatComboBox;
    @FXML private VBox checkboxesContainer;
    @FXML private CheckBox includeChartsCheckbox;
    @FXML private CheckBox includeSummaryCheckbox;
    @FXML private CheckBox landscapeOrientationCheckbox;
    @FXML private Button cancelButton;
    @FXML private Button exportButton;

    private List<CheckBox> tableCheckboxes;
    private Stage modalStage;
    private RecapitulationController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFormatComboBox();
        initializeTableCheckboxes();
        setupButtonActions();
    }

    public void setParentController(RecapitulationController controller) {
        this.parentController = controller;
        // Initialize checkboxes based on parent controller's available tables
        initializeTableCheckboxesFromParent();
    }

    private void initializeTableCheckboxesFromParent() {
        if (parentController != null) {
            List<String> availableTables = parentController.getAvailableTables();
            tableCheckboxes = new ArrayList<>();
            checkboxesContainer.getChildren().clear();

            for (String table : availableTables) {
                CheckBox checkBox = new CheckBox(table);
                checkBox.setSelected(true);
                checkBox.getStyleClass().add("modal-checkbox");
                tableCheckboxes.add(checkBox);
                checkboxesContainer.getChildren().add(checkBox);
            }
        }
    }

    private void initializeFormatComboBox() {
        formatComboBox.getItems().addAll(
                "Laporan Lengkap",
                "Ringkasan Statistik",
                "Matriks Distribusi",
                "Rekap Pemakaian",
                "Custom Selection"
        );
        formatComboBox.getSelectionModel().selectFirst();

        formatComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> handleFormatSelection(newVal));
    }

    private void initializeTableCheckboxes() {
        // Default initialization, will be overridden by parent controller
        tableCheckboxes = new ArrayList<>();
    }

    private void setupButtonActions() {
        cancelButton.setOnAction(event -> closeModal());
        exportButton.setOnAction(event -> handleExport());
    }

    private void handleFormatSelection(String format) {
        if (tableCheckboxes.isEmpty()) return;

        switch (format) {
            case "Laporan Lengkap":
                setAllCheckboxes(true);
                includeChartsCheckbox.setSelected(true);
                includeSummaryCheckbox.setSelected(true);
                break;
            case "Ringkasan Statistik":
                setAllCheckboxes(false);
                includeChartsCheckbox.setSelected(true);
                includeSummaryCheckbox.setSelected(true);
                break;
            case "Matriks Distribusi":
                setAllCheckboxes(false);
                setCheckboxSelected("Penggunaan Aset per Subdirektorat", true);
                setCheckboxSelected("Matriks Distribusi Aset per Pegawai", true);
                break;
            case "Rekap Pemakaian":
                setAllCheckboxes(false);
                setCheckboxSelected("Rekap Pemakaian", true);
                setCheckboxSelected("Keterangan Kondisi", true);
                break;
        }
    }

    private void setAllCheckboxes(boolean selected) {
        for (CheckBox checkBox : tableCheckboxes) {
            checkBox.setSelected(selected);
        }
    }

    private void setCheckboxSelected(String text, boolean selected) {
        tableCheckboxes.stream()
                .filter(cb -> cb.getText().equals(text))
                .findFirst()
                .ifPresent(cb -> cb.setSelected(selected));
    }

    private void handleExport() {
        List<String> selectedTables = getSelectedTables();
        boolean includeCharts = includeChartsCheckbox.isSelected();
        boolean includeSummary = includeSummaryCheckbox.isSelected();
        boolean landscape = landscapeOrientationCheckbox.isSelected();

        // Call parent controller to handle the actual export
        if (parentController != null) {
            parentController.handleExportPdf(selectedTables, includeCharts, includeSummary, landscape);
        }

        closeModal();
    }

    private List<String> getSelectedTables() {
        List<String> selected = new ArrayList<>();
        for (CheckBox checkBox : tableCheckboxes) {
            if (checkBox.isSelected()) {
                selected.add(checkBox.getText());
            }
        }
        return selected;
    }

    private void closeModal() {
        if (modalStage != null) {
            modalStage.close();
        }
    }

    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }
}