package com.siata.client.view;

import com.siata.client.api.AssetApi;
import com.siata.client.dto.AssetDto;
import com.siata.client.model.Activity;
import com.siata.client.service.DataService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardContentView extends VBox {

    private final DataService dataService = DataService.getInstance();
    private final AssetApi assetApi = new AssetApi();

    public DashboardContentView() {
        setSpacing(24);
        buildContent();
    }

    private void buildContent() {
        getChildren().add(buildSummaryGrid());
        getChildren().add(buildChartsColumn());
        getChildren().add(buildRecentActivities());
    }

    private Node buildSummaryGrid() {
        Label title = new Label("Dashboard");
        title.getStyleClass().add("section-heading");
    
        Label sectionTitle = new Label("Ringkasan sistem distribusi aset pegawai");
        sectionTitle.getStyleClass().add("section-subtitle");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.getStyleClass().add("stats-grid");

        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            statsGrid.getColumnConstraints().add(column);
        }

        List<CardData> cards = List.of(
                new CardData("Total Aset", "Semua jenis aset terdaftar", Integer.toString(getTotalAsset()), "🧱"),
                new CardData("Siap Dilelang", "Aset dalam proses lelang", Integer.toString(getSiapDilelang()), "♻"),
                new CardData("Rusak Berat", "Memerlukan penghapusan", Integer.toString(getRusakBerat()), "⚠"),
                new CardData("Sedang Diproses", "Permohonan menunggu persetujuan", "7", "🔄")
        );

        for (int i = 0; i < cards.size(); i++) {
            Node card = createStatCard(cards.get(i));
            statsGrid.add(card, i % 2, i / 2);
        }

        VBox container = new VBox(16, title, sectionTitle, statsGrid);
        return container;
    }

    private int getTotalAsset() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        for (AssetDto dto : assetDtos) {
            count += 1;
        }
        return count;
    }

    private int getSiapDilelang() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        LocalDate timeNow = LocalDate.now();
        for (AssetDto dto : assetDtos) {
            Long selisihHari = ChronoUnit.DAYS.between(dto.getTanggalPerolehan(), timeNow);

            if (selisihHari >= 1460) {
                count += 1;
            }
        }

        return count;
    }

    private int getRusakBerat() {
        AssetDto[] assetDtos = assetApi.getAsset();
        int count = 0;
        for (AssetDto dto : assetDtos) {
            if (dto.getKondisi().equals("Rusak Berat")) {
                count += 1;
            }
        }

        return count;
    }

    private Node buildChartsColumn() {
        VBox column = new VBox(24);
        column.getChildren().addAll(createHistogramCard(), createPieCard());
        column.getChildren().forEach(node -> VBox.setVgrow(node, Priority.ALWAYS));
        return column;
    }

    private Node createHistogramCard() {
        VBox card = createChartShell("Jumlah Aset per Jenis");

        Map<String, Integer> histogramData = new LinkedHashMap<>();
        histogramData.put("Laptop", 45);
        histogramData.put("Printer", 28);
        histogramData.put("Meja", 62);
        histogramData.put("Kursi", 78);
        histogramData.put("AC", 35);
        histogramData.put("Proyektor", 15);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(FXCollections.observableArrayList(histogramData.keySet()));
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setCategoryGap(20);
        barChart.getStyleClass().add("dashboard-bar-chart");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        histogramData.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));
        barChart.getData().add(series);

        card.getChildren().add(barChart);
        return card;
    }

    private Node createPieCard() {
        VBox card = createChartShell("Distribusi Aset per Subdirektorat");

        Map<String, Integer> pieData = new LinkedHashMap<>();
        pieData.put("Subdit Teknis", 32);
        pieData.put("Subdit Operasional", 25);
        pieData.put("Subdit Keamanan", 20);
        pieData.put("Subdit SDM", 22);

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);
        pieChart.getStyleClass().add("dashboard-pie-chart");

        pieData.forEach((label, value) ->
                pieChart.getData().add(new PieChart.Data(label + " " + value + "%", value)));

        card.getChildren().add(pieChart);
        return card;
    }

    private Node buildRecentActivities() {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("table-container", "activity-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Aktivitas Terbaru");
        title.getStyleClass().add("table-title");

        VBox list = new VBox(12);
        list.getStyleClass().add("activity-list");

        List<Activity> recentActivities = dataService.getRecentActivities(4);
        if (recentActivities.isEmpty()) {
            Label emptyState = new Label("Belum ada aktivitas pada akun ini.");
            emptyState.getStyleClass().add("data-grid-cell");
            list.getChildren().add(emptyState);
        } else {
            recentActivities.forEach(activity -> list.getChildren().add(createActivityItem(activity)));
        }

        card.getChildren().addAll(title, list);
        return card;
    }

    private Node createActivityItem(Activity activity) {
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

        Label time = new Label(DashboardTextFormatter.formatRelativeTime(activity.getTimestamp()));
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

    private VBox createChartShell(String title) {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("table-container", "chart-card");
        card.setPadding(new Insets(20));

        Label chartTitle = new Label(title);
        chartTitle.getStyleClass().add("chart-title");
        card.getChildren().add(chartTitle);
        return card;
    }

    private Node createStatCard(CardData data) {
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

    private record CardData(String title, String description, String value, String icon) {
    }

    private static class DashboardTextFormatter {
        private static String formatRelativeTime(java.time.LocalDateTime timestamp) {
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
    }
}

