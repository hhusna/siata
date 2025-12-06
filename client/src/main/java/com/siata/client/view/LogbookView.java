package com.siata.client.view;

import com.siata.client.model.Activity;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LogbookView extends VBox {

    private final DataService dataService;
    private final ObservableList<Activity> filteredActivities;
    private final VBox timelineContainer;

    public LogbookView() {
        setSpacing(24);
        dataService = DataService.getInstance();
        filteredActivities = FXCollections.observableArrayList();
        timelineContainer = new VBox(16);
        
        buildView();
        filterActivities("", "Semua Jenis", "Semua User");
    }

    private void buildView() {
        getChildren().add(buildPageHeader());
        // Filter section
        VBox filterSection = new VBox(16);
        filterSection.setPadding(new Insets(20));
        filterSection.getStyleClass().add("table-container");
        
        Label filterTitle = new Label("Filter Aktivitas");
        filterTitle.getStyleClass().add("table-title");
        
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari aktivitas...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("filter-search-field");
        
        ComboBox<String> userCombo = new ComboBox<>();
        userCombo.getItems().addAll("Semua User", "admin", "Admin PPBJ");
        userCombo.setValue("Semua User");
        userCombo.setPrefWidth(150);
        userCombo.getStyleClass().add("filter-combo-box");
        
        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.getItems().addAll("Semua Jenis", "CREATE_ASET", "UPDATE_ASET", "DELETE_ASET", 
                                      "CREATE_PEGAWAI", "UPDATE_PEGAWAI", "DELETE_PEGAWAI",
                                      "CREATE_PERMOHONAN", "UPDATE_PERMOHONAN", "DELETE_PERMOHONAN", "UPDATE_STATUS_PERMOHONAN",
                                      "CREATE_PENGAJUAN", "UPDATE_PENGAJUAN", "DELETE_PENGAJUAN", "UPDATE_STATUS_PENGAJUAN",
                                      "AUTO_LELANG", "HARD_DELETE_ASET");
        jenisCombo.setValue("Semua Jenis");
        jenisCombo.setPrefWidth(180);
        jenisCombo.getStyleClass().add("filter-combo-box");
        jenisCombo.setOnAction(e -> filterActivities(searchField.getText(), jenisCombo.getValue(), userCombo.getValue()));
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterActivities(newVal, jenisCombo.getValue(), userCombo.getValue()));
        userCombo.setOnAction(e -> filterActivities(searchField.getText(), jenisCombo.getValue(), userCombo.getValue()));
        
        filterBar.getChildren().addAll(searchField, jenisCombo, userCombo);
        filterSection.getChildren().addAll(filterTitle, filterBar);

        // Timeline section
        VBox timelineSection = new VBox(16);
        timelineSection.setPadding(new Insets(20));
        timelineSection.getStyleClass().add("table-container");
        
        Label timelineTitle = new Label("Timeline Aktivitas");
        timelineTitle.getStyleClass().add("table-title");
        
        timelineSection.getChildren().addAll(timelineTitle, timelineContainer);

        getChildren().addAll(filterSection, timelineSection);
    }

    private Node buildPageHeader() {
        // Title and description now shown in main header
        return new HBox();
    }

    private void filterActivities(String searchText, String jenisFilter, String userFilter) {
        List<Activity> allActivities = dataService.getActivities();
        
        filteredActivities.setAll(allActivities.stream()
            .filter(activity -> {
                // Search filter
                if (searchText != null && !searchText.isEmpty()) {
                    String search = searchText.toLowerCase();
                    if (!activity.getDescription().toLowerCase().contains(search) &&
                        !activity.getTarget().toLowerCase().contains(search) &&
                        !(activity.getDetails() != null && activity.getDetails().toLowerCase().contains(search))) {
                        return false;
                    }
                }
                
                // Jenis filter
                if (jenisFilter != null && !jenisFilter.equals("Semua Jenis")) {
                    if (!activity.getActionType().equals(jenisFilter)) {
                        return false;
                    }
                }
                
                // User filter
                if (userFilter != null && !userFilter.equals("Semua User")) {
                    if (!activity.getUser().toLowerCase().contains(userFilter.toLowerCase())) {
                        return false;
                    }
                }
                
                return true;
            })
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList()
        );
        
        refreshTimeline();
    }

    private void refreshTimeline() {
        timelineContainer.getChildren().clear();
        
        for (int i = 0; i < filteredActivities.size(); i++) {
            Activity activity = filteredActivities.get(i);
            HBox timelineEntry = createTimelineEntry(activity, i == 0, i == filteredActivities.size() - 1);
            timelineContainer.getChildren().add(timelineEntry);
        }
    }

    private HBox createTimelineEntry(Activity activity, boolean isFirst, boolean isLast) {
        HBox entry = new HBox(16);
        entry.setAlignment(Pos.TOP_LEFT);
        
        // Timeline line and dot
        VBox timelineIndicator = new VBox();
        timelineIndicator.setAlignment(Pos.TOP_CENTER);
        timelineIndicator.setPrefWidth(40);

        if (!isFirst) {
            Region topLine = new Region();
            topLine.setPrefHeight(8);
            topLine.setMinWidth(2);
            topLine.setPrefWidth(2);
            topLine.setMaxWidth(2);
            topLine.setStyle("-fx-background-color: #3498db;");
            timelineIndicator.getChildren().add(topLine);
        }

        Region dot = new Region();
        dot.setPrefSize(12, 12);
        dot.setStyle("-fx-background-color: #3498db; -fx-background-radius: 6;");
        timelineIndicator.getChildren().add(dot);

        if (!isLast) {
            Region bottomLine = new Region();
            bottomLine.setPrefHeight(8);
            bottomLine.setMinWidth(2);
            bottomLine.setPrefWidth(2);
            bottomLine.setMaxWidth(2);
            bottomLine.setStyle("-fx-background-color: #3498db;");
            VBox.setVgrow(bottomLine, Priority.ALWAYS);
            timelineIndicator.getChildren().add(bottomLine);
        }
        
        // Content
        VBox content = new VBox(8);
        content.setAlignment(Pos.TOP_LEFT);
        
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        String icon = getIconForAction(activity.getActionType());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Label userLabel = new Label(activity.getUser());
        userLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1f3058;");
        
        Label actionBadge = new Label(activity.getActionType());
        actionBadge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 600;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLabel = new Label(formatTimeAgo(activity.getTimestamp()));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6f7c9c;");
        
        headerBox.getChildren().addAll(iconLabel, userLabel, actionBadge, spacer, timeLabel);
        
        Label descriptionLabel = new Label(activity.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1f3058;");
        
        Label targetLabel = new Label("Target: " + activity.getTarget());
        targetLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6f7c9c;");
        
        content.getChildren().addAll(headerBox, descriptionLabel, targetLabel);
        
        if (activity.getDetails() != null && !activity.getDetails().isEmpty()) {
            Label detailsLabel = new Label(activity.getDetails());
            detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6f7c9c; -fx-background-color: #f3f5fb; -fx-padding: 8; -fx-background-radius: 8;");
            detailsLabel.setWrapText(true);
            detailsLabel.setMaxWidth(600);
            content.getChildren().add(detailsLabel);
        }
        
        entry.getChildren().addAll(timelineIndicator, content);
        
        return entry;
    }

    private String getIconForAction(String actionType) {
        switch (actionType) {
            case "Approve": return "✔";
            case "Reject": return "✗";
            case "Create": return "+";
            case "Delete": return "🗑";
            case "Update": return "✎";
            default: return "•";
        }
    }

    private String formatTimeAgo(LocalDateTime timestamp) {
        long hoursAgo = ChronoUnit.HOURS.between(timestamp, LocalDateTime.now());
        if (hoursAgo < 24) {
            return hoursAgo + " jam yang lalu";
        } else {
            long daysAgo = ChronoUnit.DAYS.between(timestamp, LocalDateTime.now());
            return daysAgo + " hari yang lalu";
        }
    }
}

