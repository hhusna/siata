package com.siata.client.controller;

import com.siata.client.model.Activity;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private GridPane statsGrid;
    @FXML private VBox chartsColumn;
    @FXML private BarChart<String, Number> assetBarChart;
    @FXML private CategoryAxis categoryAxis;
    @FXML private NumberAxis numberAxis;
    @FXML private PieChart distributionPieChart;
    @FXML private VBox recentActivitiesCard;
    @FXML private VBox activitiesList;

    private final DataService dataService = DataService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSummaryGrid();
        initializeCharts();
        initializeRecentActivities();
    }

    private void initializeSummaryGrid() {
        List<CardData> cards = List.of(
                new CardData("Total Aset", "Semua jenis aset terdaftar", "263", "🧱"),
                new CardData("Siap Dilelang", "Aset dalam proses lelang", "18", "♻"),
                new CardData("Rusak Berat", "Memerlukan penghapusan", "12", "⚠"),
                new CardData("Sedang Diproses", "Permohonan menunggu persetujuan", "7", "🔄")
        );

        for (int i = 0; i < cards.size(); i++) {
            VBox card = createStatCard(cards.get(i));
            statsGrid.add(card, i % 2, i / 2);
        }
    }

    private void initializeCharts() {
        // Initialize Bar Chart
        Map<String, Integer> histogramData = new LinkedHashMap<>();
        histogramData.put("Laptop", 45);
        histogramData.put("Printer", 28);
        histogramData.put("Meja", 62);
        histogramData.put("Kursi", 78);
        histogramData.put("AC", 35);
        histogramData.put("Proyektor", 15);

        categoryAxis.setCategories(FXCollections.observableArrayList(histogramData.keySet()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        histogramData.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));
        assetBarChart.getData().add(series);

        // Initialize Pie Chart
        Map<String, Integer> pieData = new LinkedHashMap<>();
        pieData.put("Subdit Teknis", 32);
        pieData.put("Subdit Operasional", 25);
        pieData.put("Subdit Keamanan", 20);
        pieData.put("Subdit SDM", 22);

        pieData.forEach((label, value) ->
                distributionPieChart.getData().add(new PieChart.Data(label + " " + value + "%", value)));
    }

    private void initializeRecentActivities() {
        List<Activity> recentActivities = dataService.getRecentActivities(4);
        if (recentActivities.isEmpty()) {
            Label emptyState = new Label("Belum ada aktivitas pada akun ini.");
            emptyState.getStyleClass().add("data-grid-cell");
            activitiesList.getChildren().add(emptyState);
        } else {
            recentActivities.forEach(activity ->
                    activitiesList.getChildren().add(createActivityItem(activity)));
        }
    }

    private VBox createStatCard(CardData data) {
        VBox card = new VBox(12);
        card.getStyleClass().add("stat-card");

        HBox heading = new HBox(8);
        heading.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(data.icon());
        iconLabel.getStyleClass().add("stat-card-icon");

        Label title = new Label(data.title());
        title.getStyleClass().add("stat-card-title");

        heading.getChildren().addAll(iconLabel, title);

        Label value = new Label(data.value());
        value.getStyleClass().add("stat-card-value");

        Label description = new Label(data.description());
        description.getStyleClass().add("stat-card-description");

        card.getChildren().addAll(heading, value, description);
        return card;
    }

    private VBox createActivityItem(Activity activity) {
        VBox item = new VBox(6);
        item.getStyleClass().add("activity-item");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("•");
        dot.getStyleClass().add("activity-dot");

        Label actor = new Label(activity.getUser());
        actor.getStyleClass().add("activity-actor");

        Label badge = new Label(activity.getActionType());
        badge.getStyleClass().add("activity-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(formatRelativeTime(activity.getTimestamp()));
        time.getStyleClass().add("activity-time");

        header.getChildren().addAll(dot, actor, badge, spacer, time);

        String descriptionText = activity.getDescription() != null ? activity.getDescription() : "";
        if (activity.getTarget() != null && !activity.getTarget().isBlank()) {
            descriptionText = descriptionText + " " + activity.getTarget();
        }
        Label description = new Label(descriptionText);
        description.getStyleClass().add("activity-description");

        item.getChildren().addAll(header, description);

        if (activity.getDetails() != null && !activity.getDetails().isBlank()) {
            Label details = new Label(activity.getDetails());
            details.getStyleClass().add("activity-details");
            item.getChildren().add(details);
        }
        return item;
    }

    private String formatRelativeTime(java.time.LocalDateTime timestamp) {
        Duration duration = Duration.between(timestamp, java.time.LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " menit yang lalu";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " jam yang lalu";
        }
        long days = duration.toDays();
        return days + " hari yang lalu";
    }

    private record CardData(String title, String description, String value, String icon) {
    }
}