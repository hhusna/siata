package com.siata.client.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A reusable paginated table component that wraps a TableView with pagination controls.
 * @param <T> The type of items in the table
 */
public class PaginatedTableView<T> extends VBox {

    private final TableView<T> table;
    private final ObservableList<T> allItems;
    private final ObservableList<T> currentPageItems;
    
    private int currentPage = 0;
    private int itemsPerPage = 10;
    
    private Label pageInfoLabel;
    private Button prevButton;
    private Button nextButton;
    private Button firstButton;
    private Button lastButton;
    private ComboBox<Integer> pageSizeCombo;

    public PaginatedTableView() {
        this.allItems = FXCollections.observableArrayList();
        this.currentPageItems = FXCollections.observableArrayList();
        this.table = new TableView<>(currentPageItems);
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        
        setSpacing(12);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        getChildren().addAll(table, createPaginationControls());
    }

    private HBox createPaginationControls() {
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER);
        
        // Items per page selector
        Label pageSizeLabel = new Label("Per halaman:");
        pageSizeCombo = new ComboBox<>();
        pageSizeCombo.getItems().addAll(10, 25, 50, 100);
        pageSizeCombo.setValue(10);
        pageSizeCombo.setOnAction(e -> {
            itemsPerPage = pageSizeCombo.getValue();
            currentPage = 0;
            updatePage();
        });
        
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        // Navigation buttons
        firstButton = new Button("⏮");
        firstButton.getStyleClass().add("ghost-button");
        firstButton.setOnAction(e -> goToFirstPage());
        
        prevButton = new Button("◀");
        prevButton.getStyleClass().add("ghost-button");
        prevButton.setOnAction(e -> goToPreviousPage());
        
        pageInfoLabel = new Label("Halaman 1 dari 1");
        pageInfoLabel.setStyle("-fx-font-weight: bold;");
        
        nextButton = new Button("▶");
        nextButton.getStyleClass().add("ghost-button");
        nextButton.setOnAction(e -> goToNextPage());
        
        lastButton = new Button("⏭");
        lastButton.getStyleClass().add("ghost-button");
        lastButton.setOnAction(e -> goToLastPage());
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        // Total items info
        Label totalLabel = new Label();
        totalLabel.textProperty().bind(
            javafx.beans.binding.Bindings.createStringBinding(
                () -> "Total: " + allItems.size() + " data",
                allItems
            )
        );
        
        controls.getChildren().addAll(
            pageSizeLabel, pageSizeCombo,
            leftSpacer,
            firstButton, prevButton, pageInfoLabel, nextButton, lastButton,
            rightSpacer,
            totalLabel
        );
        
        return controls;
    }

    /**
     * Get the underlying TableView to add columns and customize.
     */
    public TableView<T> getTable() {
        return table;
    }

    /**
     * Set the items for the table. This replaces all existing items.
     */
    public void setItems(List<T> items) {
        allItems.setAll(items);
        currentPage = 0;
        updatePage();
    }

    /**
     * Set the items for the table from an ObservableList.
     */
    public void setItems(ObservableList<T> items) {
        allItems.setAll(items);
        currentPage = 0;
        updatePage();
    }

    /**
     * Get all items (not just current page).
     */
    public ObservableList<T> getAllItems() {
        return allItems;
    }

    /**
     * Refresh the current page display.
     */
    public void refresh() {
        updatePage();
    }

    private void updatePage() {
        int totalItems = allItems.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        
        // Ensure currentPage is valid
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        
        // Calculate start and end indices
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        
        // Update current page items
        currentPageItems.setAll(allItems.subList(startIndex, endIndex));
        
        // Update page info label
        pageInfoLabel.setText("Halaman " + (currentPage + 1) + " dari " + totalPages);
        
        // Update button states
        boolean isFirstPage = currentPage == 0;
        boolean isLastPage = currentPage >= totalPages - 1;
        
        firstButton.setDisable(isFirstPage);
        prevButton.setDisable(isFirstPage);
        nextButton.setDisable(isLastPage);
        lastButton.setDisable(isLastPage);
    }

    private void goToFirstPage() {
        currentPage = 0;
        updatePage();
    }

    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    private void goToNextPage() {
        int totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
    }

    private void goToLastPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allItems.size() / itemsPerPage));
        currentPage = totalPages - 1;
        updatePage();
    }

    /**
     * Set items per page.
     */
    public void setItemsPerPage(int count) {
        this.itemsPerPage = count;
        pageSizeCombo.setValue(count);
        currentPage = 0;
        updatePage();
    }
}
