package com.siata.client.view;

import com.siata.client.api.AssetApi;
import com.siata.client.dto.AssetDto;
import com.siata.client.model.Activity;
import com.siata.client.service.DataService;
import com.siata.client.util.AnimationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.siata.client.model.Asset;

public class DashboardContentView extends VBox {

    private final DataService dataService = DataService.getInstance();
    private final AssetApi assetApi = new AssetApi();
    
    // Smart caching dengan TTL
    private com.siata.client.dto.DashboardDto cachedDashboardData = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL_MILLIS = 30_000; // 30 seconds
    private boolean isInitialized = false;
    private boolean isCurrentlyLoading = false;

    public DashboardContentView() {
        setSpacing(20);
        // Lazy initialization - load saat pertama kali visible
    }
    
    /**
     * Initialize dashboard - dipanggil pertama kali view visible
     */
    public void initialize() {
        if (!isInitialized) {
            isInitialized = true;
            loadDashboardData(false);
        }
    }
    
    /**
     * Refresh dashboard hanya jika cache expired atau force refresh
     */
    public void refreshDashboard() {
        refreshDashboard(false);
    }
    
    /**
     * Refresh dashboard dengan opsi force
     * @param force - true untuk force refresh, false untuk check cache dulu
     */
    public void refreshDashboard(boolean force) {
        long now = System.currentTimeMillis();
        boolean cacheExpired = (now - cacheTimestamp) > CACHE_TTL_MILLIS;
        
        if (force || cacheExpired || cachedDashboardData == null) {
            loadDashboardData(force);
        } else if (isInitialized && !getChildren().isEmpty()) {
            // Cache masih valid, tidak perlu reload
            // Just fade in untuk smooth transition
            AnimationUtils.fadeIn(this, AnimationUtils.FAST, Duration.ZERO);
        } else if (cachedDashboardData != null) {
            // Ada cached data tapi UI belum dibangun
            buildContentFromCache();
        }
    }
    
    /**
     * Mark cache as stale - dipanggil setelah ada perubahan data
     */
    public void markAsStale() {
        cacheTimestamp = 0; // Force cache expired
    }
    
    private void loadDashboardData(boolean force) {
        // Prevent multiple simultaneous loads
        if (isCurrentlyLoading) {
            return;
        }
        
        isCurrentlyLoading = true;
        
        // Show loading indicator hanya jika belum ada content
        if (getChildren().isEmpty()) {
            Label loadingLabel = new Label("Loading dashboard...");
            loadingLabel.getStyleClass().add("section-heading");
            getChildren().clear();
            getChildren().add(loadingLabel);
        }
        
        javafx.concurrent.Task<Void> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                // Fetch dashboard data dari API
                cachedDashboardData = assetApi.getDashboard();
                
                // Pre-fetch assets in background to prime cache
                // This ensures buildContent() on UI thread gets data instantly from cache
                dataService.getAssets(force);
                
                cacheTimestamp = System.currentTimeMillis();
                return null;
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            isCurrentlyLoading = false;
            getChildren().clear();
            buildContent();
        });
        
        loadTask.setOnFailed(e -> {
            isCurrentlyLoading = false;
            getChildren().clear();
            Label errorLabel = new Label("Gagal memuat dashboard. Silakan refresh.");
            errorLabel.getStyleClass().add("section-heading");
            errorLabel.setStyle("-fx-text-fill: #dc2626;");
            getChildren().add(errorLabel);
        });
        
        new Thread(loadTask).start();
    }
    
    /**
     * Build UI dari cached data (tanpa API call)
     */
    private void buildContentFromCache() {
        if (cachedDashboardData == null) {
            loadDashboardData(false);
            return;
        }
        
        getChildren().clear();
        buildContent();
    }

    private void buildContent() {
        // Get all assets once for multiple uses
        List<Asset> allAssets = dataService.getAssets();
        
        Node summarySection = buildSummaryGrid(allAssets);
        Node chartsSection = buildChartsColumn();
        Node lineChartSection = createAcquisitionLineChart();
        Node activitySection = buildRecentActivities();
        
        getChildren().addAll(summarySection, chartsSection, lineChartSection, activitySection);
        
        // Animate page content
        Platform.runLater(() -> {
            AnimationUtils.fadeIn(summarySection, AnimationUtils.NORMAL, Duration.ZERO);
            AnimationUtils.fadeIn(chartsSection, AnimationUtils.NORMAL, Duration.millis(100));
            AnimationUtils.fadeIn(lineChartSection, AnimationUtils.NORMAL, Duration.millis(150));
            AnimationUtils.fadeIn(activitySection, AnimationUtils.NORMAL, Duration.millis(200));
        });
    }

    private Node buildSummaryGrid(List<Asset> allAssets) {
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);
        statsGrid.getStyleClass().add("stats-grid");

        // 4 columns for single row layout
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            column.setHgrow(Priority.ALWAYS);
            statsGrid.getColumnConstraints().add(column);
        }

        // Calculate total nilai and highest contributor
        java.math.BigDecimal totalNilai = java.math.BigDecimal.ZERO;
        Map<String, java.math.BigDecimal> nilaiPerJenis = new HashMap<>();
        
        for (Asset asset : allAssets) {
            java.math.BigDecimal nilai = asset.getNilaiRupiah();
            if (nilai != null) {
                totalNilai = totalNilai.add(nilai);
                String jenis = asset.getJenisAset() != null ? asset.getJenisAset() : "Lainnya";
                nilaiPerJenis.merge(jenis, nilai, java.math.BigDecimal::add);
            }
        }
        
        // Find highest contributor
        String highestJenis = "N/A";
        java.math.BigDecimal highestNilai = java.math.BigDecimal.ZERO;
        for (Map.Entry<String, java.math.BigDecimal> entry : nilaiPerJenis.entrySet()) {
            if (entry.getValue().compareTo(highestNilai) > 0) {
                highestNilai = entry.getValue();
                highestJenis = entry.getKey();
            }
        }
        
        // Calculate percentage
        double percentage = totalNilai.compareTo(java.math.BigDecimal.ZERO) > 0 
            ? highestNilai.divide(totalNilai, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100 
            : 0;
        
        // Format total nilai in compact Indonesian format (juta, milyar, triliun)
        String formattedTotal = formatCompactCurrency(totalNilai);
        String contributorInfo = highestJenis + " (" + String.format("%.1f", percentage) + "%)";

        // Calculate held asset ratio
        // Asset "dipegang" = has holder (keterangan is NIP) OR subdir is valid (not Gudang, Hilang, or empty)
        long totalAktif = allAssets.stream()
            .filter(a -> "AKTIF".equalsIgnoreCase(a.getStatus()))
            .count();
        long heldAssets = allAssets.stream()
            .filter(a -> "AKTIF".equalsIgnoreCase(a.getStatus()))
            .filter(a -> {
                String keterangan = a.getKeterangan();
                String subdir = a.getSubdir();
                
                // Has holder (keterangan is NIP, not "-" or empty)
                boolean hasHolder = keterangan != null && !keterangan.isEmpty() && !"-".equals(keterangan);
                
                // Has valid subdir (not Gudang, Hilang, or empty)
                boolean hasValidSubdir = subdir != null && !subdir.isEmpty() 
                    && !"Gudang".equalsIgnoreCase(subdir) 
                    && !"Hilang".equalsIgnoreCase(subdir);
                
                return hasHolder || hasValidSubdir;
            })
            .count();
        
        double heldRatio = totalAktif > 0 ? (heldAssets * 100.0 / totalAktif) : 0;
        String heldRatioText = String.format("%.1f%%", heldRatio);
        String heldInfo = heldAssets + " dari " + totalAktif + " aset aktif";

        // Gunakan cachedDashboardData yang sudah di-fetch sekali
        List<CardData> cards = List.of(
                new CardData("Total Aset", "Semua jenis aset terdaftar",
                    Long.toString(cachedDashboardData != null ? cachedDashboardData.getTotalAset() : 0) , "◇"),
                new CardData("Total Nilai", contributorInfo, formattedTotal, "◎"),
                // Umur Rata-rata card is handled separately
                new CardData("Rasio Dipegang", heldInfo, heldRatioText, "○")
        );

        // Add cards to grid - insert Umur Rata-rata card at position 2
        statsGrid.add(createStatCard(cards.get(0)), 0, 0);
        statsGrid.add(createStatCard(cards.get(1)), 1, 0);
        statsGrid.add(createAverageAgeCard(allAssets), 2, 0); // Special card with dropdown
        statsGrid.add(createStatCard(cards.get(2)), 3, 0);

        VBox container = new VBox(16, statsGrid);
        
        // Animate stat cards with stagger effect
        Platform.runLater(() -> {
            List<Node> cardNodes = new ArrayList<>();
            for (int i = 0; i < statsGrid.getChildren().size(); i++) {
                Node card = statsGrid.getChildren().get(i);
                cardNodes.add(card);
                // Add hover lift effect
                AnimationUtils.addHoverLiftEffect(card);
            }
            AnimationUtils.staggerSlideInFromBottom(cardNodes, Duration.millis(80));
        });
        
        return container;
    }

    private Node buildChartsColumn() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_CENTER);
        
        Node histogramCard = createHistogramCard();
        Node pieCard = createPieCard();
        
        // Set each chart to take 50% width
        HBox.setHgrow(histogramCard, Priority.ALWAYS);
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        
        row.getChildren().addAll(histogramCard, pieCard);
        return row;
    }

    private Node createHistogramCard() {
        VBox card = createChartShell("Jumlah Aset per Jenis");

        Map<String, Integer> histogramData = new LinkedHashMap<>();
        histogramData.put("Mobil", dataService.getAssetByJenis("Mobil"));
        histogramData.put("Motor", dataService.getAssetByJenis("Motor"));
        histogramData.put("Scanner", dataService.getAssetByJenis("Scanner"));
        histogramData.put("PC", dataService.getAssetByJenis("PC"));
        histogramData.put("Laptop", dataService.getAssetByJenis("Laptop"));
        histogramData.put("Notebook", dataService.getAssetByJenis("Notebook"));
        histogramData.put("Tablet", dataService.getAssetByJenis("Tablet"));
        histogramData.put("Printer", dataService.getAssetByJenis("Printer"));
        histogramData.put("Speaker", dataService.getAssetByJenis("Speaker"));
        histogramData.put("Parabot", dataService.getAssetByJenis("Parabot"));

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
        pieData.put("PPTAU", dataService.getAssetBySubdir("PPTAU"));
        pieData.put("AUNB", dataService.getAssetBySubdir("AUNB"));
        pieData.put("AUNTB", dataService.getAssetBySubdir("AUNTB"));
        pieData.put("KAU", dataService.getAssetBySubdir("KAU"));
        pieData.put("SILAU", dataService.getAssetBySubdir("SILAU"));
        pieData.put("Tata Usaha", dataService.getAssetBySubdir("Tata Usaha"));
        pieData.put("Direktur", dataService.getAssetBySubdir("Direktur"));

        // Hitung total aset untuk menghitung persentase
        int totalAset = pieData.values().stream().mapToInt(Integer::intValue).sum();

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);
        pieChart.getStyleClass().add("dashboard-pie-chart");

        // Hitung persentase yang akurat
        pieData.forEach((label, value) -> {
            double percentage = totalAset > 0 ? (value * 100.0 / totalAset) : 0;
            String formattedPercentage = String.format("%.1f", percentage);
            pieChart.getData().add(new PieChart.Data(label + " (" + value + ") " + formattedPercentage + "%", value));
        });

        card.getChildren().add(pieChart);
        return card;
    }

    private Node createAcquisitionLineChart() {
        VBox card = createChartShell("Tren Pengadaan Aset per Tahun");

        // Get all assets
        List<Asset> allAssets = dataService.getAssets();

        // Group by jenisAset and count total per jenis
        Map<String, Long> totalPerJenis = allAssets.stream()
            .filter(a -> a.getTanggalPerolehan() != null && a.getJenisAset() != null)
            .collect(Collectors.groupingBy(Asset::getJenisAset, Collectors.counting()));

        // Filter jenis with total > 10
        List<String> eligibleJenis = totalPerJenis.entrySet().stream()
            .filter(e -> e.getValue() > 10)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());

        // Group by year and jenis
        // Map<Year, Map<Jenis, Count>>
        Map<Integer, Map<String, Long>> yearJenisCount = new TreeMap<>();
        
        for (Asset asset : allAssets) {
            if (asset.getTanggalPerolehan() == null || asset.getJenisAset() == null) continue;
            if (!eligibleJenis.contains(asset.getJenisAset())) continue;
            
            int year = asset.getTanggalPerolehan().getYear();
            String jenis = asset.getJenisAset();
            
            yearJenisCount.computeIfAbsent(year, k -> new HashMap<>());
            yearJenisCount.get(year).merge(jenis, 1L, Long::sum);
        }

        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Tahun");
        
        // Find max value for Y-axis scaling
        long maxCount = 0;
        for (Map<String, Long> jenisMap : yearJenisCount.values()) {
            for (Long count : jenisMap.values()) {
                if (count > maxCount) maxCount = count;
            }
        }
        
        NumberAxis yAxis = new NumberAxis(0, maxCount + 10, 5); // lowerBound, upperBound, tickUnit
        yAxis.setLabel("Jumlah Aset");
        yAxis.setMinorTickVisible(true);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(null);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.getStyleClass().add("dashboard-line-chart");
        lineChart.setLegendVisible(true);

        // Create series for each eligible jenis
        for (String jenis : eligibleJenis) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(jenis);
            
            for (Integer year : yearJenisCount.keySet()) {
                Long count = yearJenisCount.get(year).getOrDefault(jenis, 0L);
                series.getData().add(new XYChart.Data<>(String.valueOf(year), count));
            }
            
            lineChart.getData().add(series);
        }

        lineChart.setMinHeight(400);
        lineChart.setPrefHeight(500);
        lineChart.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(450);

        card.getChildren().add(lineChart);
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
        VBox card = new VBox(12);
        card.getStyleClass().addAll("table-container", "chart-card");
        card.setPadding(new Insets(16));
        card.setMaxHeight(280);
        card.setPrefHeight(280);

        Label chartTitle = new Label(title);
        chartTitle.getStyleClass().add("chart-title");
        card.getChildren().add(chartTitle);
        return card;
    }

    private Node createStatCard(CardData data) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");

        // Top label with icon (small gray text like "Total customers")
        HBox labelRow = new HBox(6);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(data.icon());
        iconLabel.getStyleClass().add("stat-card-icon");
        
        Label titleLabel = new Label(data.title());
        titleLabel.getStyleClass().add("stat-card-title");
        
        labelRow.getChildren().addAll(iconLabel, titleLabel);

        // Large metric value
        Label valueLabel = new Label(data.value());
        valueLabel.getStyleClass().add("stat-card-value");

        // Change indicator (percentage with arrow)
        Label changeLabel = new Label("↗ " + data.description());
        changeLabel.getStyleClass().addAll("stat-card-change", "stat-card-change-positive");

        card.getChildren().addAll(labelRow, valueLabel, changeLabel);
        return card;
    }

    /**
     * Creates the 'Umur Rata-rata' card with a dropdown to filter by asset type
     */
    private Node createAverageAgeCard(List<Asset> allAssets) {
        HBox card = new HBox(8);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Left section: card content
        VBox leftContent = new VBox(4);
        leftContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftContent, Priority.ALWAYS);
        
        // Top label with icon
        HBox labelRow = new HBox(6);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("⏱");
        iconLabel.getStyleClass().add("stat-card-icon");
        
        Label titleLabel = new Label("Umur Rata-rata");
        titleLabel.getStyleClass().add("stat-card-title");
        
        labelRow.getChildren().addAll(iconLabel, titleLabel);
        
        // Value label (will be updated dynamically)
        Label valueLabel = new Label("0 Tahun");
        valueLabel.getStyleClass().add("stat-card-value");
        
        // Change indicator
        Label changeLabel = new Label("↗ Semua aset aktif");
        changeLabel.getStyleClass().addAll("stat-card-change", "stat-card-change-positive");
        
        leftContent.getChildren().addAll(labelRow, valueLabel, changeLabel);
        
        // Right section: dropdown
        VBox rightContent = new VBox();
        rightContent.setAlignment(Pos.TOP_RIGHT);
        
        ComboBox<String> jenisDropdown = new ComboBox<>();
        jenisDropdown.getItems().add("Semua");
        jenisDropdown.getItems().addAll("Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");
        jenisDropdown.setValue("Semua");
        jenisDropdown.setMinHeight(20);
        jenisDropdown.setMaxHeight(20);
        jenisDropdown.setPrefHeight(20);
        jenisDropdown.setMaxWidth(65);
        jenisDropdown.setStyle("-fx-font-size: 9px; -fx-padding: 1 3 1 3;");
        
        // Update average age when dropdown changes
        jenisDropdown.setOnAction(e -> {
            String selected = jenisDropdown.getValue();
            double avgAge = calculateAverageAge(allAssets, selected);
            valueLabel.setText(String.format("%.1f Th", avgAge));
            changeLabel.setText("↗ " + ("Semua".equals(selected) ? "Semua jenis aset aktif" : selected + " (Aktif)"));
        });
        
        // Initial calculation
        double initialAvg = calculateAverageAge(allAssets, "Semua");
        valueLabel.setText(String.format("%.1f Th", initialAvg));
        
        rightContent.getChildren().add(jenisDropdown);
        
        card.getChildren().addAll(leftContent, rightContent);
        return card;
    }
    
    /**
     * Calculate average age of assets in years
     */
    private double calculateAverageAge(List<Asset> assets, String jenisFilter) {
        java.time.LocalDate now = java.time.LocalDate.now();
        
        List<Asset> filtered = assets.stream()
            .filter(a -> a.getTanggalPerolehan() != null)
            .filter(a -> "AKTIF".equalsIgnoreCase(a.getStatus()))
            .filter(a -> "Semua".equals(jenisFilter) || jenisFilter.equalsIgnoreCase(a.getJenisAset()))
            .collect(Collectors.toList());
        
        if (filtered.isEmpty()) return 0;
        
        double totalYears = 0;
        for (Asset asset : filtered) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(asset.getTanggalPerolehan(), now);
            totalYears += days / 365.25;
        }
        
        return totalYears / filtered.size();
    }

    /**
     * Creates the Laptop Needs Detection card
     * Shows current and projected laptop requirements based on ASN count
     */
    private Node createLaptopNeedsCard(List<Asset> allAssets) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("table-container", "chart-card");
        card.setPadding(new Insets(16));
        
        java.time.LocalDate now = java.time.LocalDate.now();
        
        // Get total ASN (employees)
        int totalASN = dataService.getEmployees().size();
        
        // Filter laptops that are in good condition (AKTIF, not Rusak Berat, age < 4 years)
        List<Asset> goodLaptops = allAssets.stream()
            .filter(a -> a.getJenisAset() != null && a.getJenisAset().equalsIgnoreCase("Laptop"))
            .filter(a -> "AKTIF".equalsIgnoreCase(a.getStatus()))
            .filter(a -> a.getKondisi() != null && !a.getKondisi().equalsIgnoreCase("Rusak Berat"))
            .filter(a -> {
                if (a.getTanggalPerolehan() == null) return false;
                long days = java.time.temporal.ChronoUnit.DAYS.between(a.getTanggalPerolehan(), now);
                double years = days / 365.25;
                return years < 4;
            })
            .collect(Collectors.toList());
        
        int laptopBaik = goodLaptops.size();
        
        // Find laptops that will expire next year (3 <= age < 4 years now, will be >= 4 next year)
        long laptopExpiring = goodLaptops.stream()
            .filter(a -> {
                long days = java.time.temporal.ChronoUnit.DAYS.between(a.getTanggalPerolehan(), now);
                double years = days / 365.25;
                return years >= 3 && years < 4;
            })
            .count();
        
        // Calculate needs
        int kebutuhanTahunIni = totalASN - laptopBaik;
        int laptopBaikTahunDepan = laptopBaik - (int) laptopExpiring;
        int kebutuhanTahunDepan = totalASN - laptopBaikTahunDepan;
        
        // Title
        Label titleLabel = new Label("Deteksi Kebutuhan Laptop");
        titleLabel.getStyleClass().add("chart-title");
        
        // Dropdown to switch versions
        ComboBox<String> versionDropdown = new ComboBox<>();
        versionDropdown.getItems().addAll("Tahun Ini", "Tahun Depan");
        versionDropdown.setValue("Tahun Ini");
        versionDropdown.setStyle("-fx-font-size: 10px;");
        
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleLabel, spacer, versionDropdown);
        
        // Main value display
        Label valueLabel = new Label();
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        // Description
        Label descLabel = new Label();
        descLabel.getStyleClass().addAll("stat-card-change", "stat-card-change-positive");
        
        // Details row
        Label detailsLabel = new Label();
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-padding: 8 12; -fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        
        // Explanation section
        Label explanationTitle = new Label("Perhitungan:");
        explanationTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        
        Label explanationLabel = new Label();
        explanationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        explanationLabel.setWrapText(true);
        
        // Update function
        Runnable updateDisplay = () -> {
            boolean isCurrentYear = "Tahun Ini".equals(versionDropdown.getValue());
            int kebutuhan = isCurrentYear ? kebutuhanTahunIni : kebutuhanTahunDepan;
            int laptopUsed = isCurrentYear ? laptopBaik : laptopBaikTahunDepan;
            
            if (kebutuhan > 0) {
                valueLabel.setText("+" + kebutuhan + " Unit");
                valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                descLabel.setText("Perlu pengadaan " + kebutuhan + " laptop");
                descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
            } else if (kebutuhan < 0) {
                valueLabel.setText(kebutuhan + " Unit");
                valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
                descLabel.setText("✓ Kelebihan " + Math.abs(kebutuhan) + " laptop");
                descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #22c55e;");
            } else {
                valueLabel.setText("0 Unit");
                valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
                descLabel.setText("✓ Jumlah laptop sudah sesuai kebutuhan");
                descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b82f6;");
            }
            
            if (isCurrentYear) {
                detailsLabel.setText("Total ASN: " + totalASN + "  |  Laptop Baik: " + laptopBaik);
                explanationLabel.setText(
                    "Kebutuhan = Total ASN - Laptop Kondisi Baik\n" +
                    "Kebutuhan = " + totalASN + " - " + laptopBaik + " = " + kebutuhanTahunIni + "\n\n" +
                    "Kriteria Laptop Baik: AKTIF, Kondisi ≠ Rusak Berat, Umur < 4 tahun"
                );
            } else {
                detailsLabel.setText("Total ASN: " + totalASN + "  |  Laptop Baik: " + laptopBaik + "  |  Expiring: " + laptopExpiring);
                explanationLabel.setText(
                    "Laptop Tahun Depan = Laptop Baik - Laptop Expiring\n" +
                    "Laptop Tahun Depan = " + laptopBaik + " - " + laptopExpiring + " = " + laptopBaikTahunDepan + "\n" +
                    "Kebutuhan = " + totalASN + " - " + laptopBaikTahunDepan + " = " + kebutuhanTahunDepan + "\n\n" +
                    "Laptop Expiring: umur 3-4 tahun (akan melewati batas 4 tahun)"
                );
            }
        };
        
        // Initial display
        updateDisplay.run();
        
        // Update on dropdown change
        versionDropdown.setOnAction(e -> updateDisplay.run());
        
        VBox explanationBox = new VBox(4, explanationTitle, explanationLabel);
        explanationBox.setStyle("-fx-padding: 8; -fx-background-color: #fefce8; -fx-background-radius: 6; -fx-border-color: #fef08a; -fx-border-radius: 6;");
        
        card.getChildren().addAll(headerRow, valueLabel, descLabel, detailsLabel, explanationBox);
        return card;
    }

    /**
     * Creates an Aging Breakdown stacked bar chart with risk bands
     * Only for Laptop and Notebook
     * Low Risk: 0-4 years (green)
     * Medium Risk: 4-7 years (orange)
     * High Risk: 7+ years (red)
     */
    private Node createAgingBreakdownChart(List<Asset> allAssets) {
        VBox card = createChartShell("Aging Laptop & Notebook");
        card.setMaxHeight(300);
        card.setPrefHeight(300);
        card.setMinWidth(250);
        
        java.time.LocalDate now = java.time.LocalDate.now();
        
        // Group assets by jenis and risk band - ONLY Laptop and Notebook
        // Map<JenisAset, Map<RiskBand, Count>>
        Map<String, Map<String, Long>> agingData = new LinkedHashMap<>();
        
        for (Asset asset : allAssets) {
            if (!"AKTIF".equalsIgnoreCase(asset.getStatus())) continue;
            if (asset.getTanggalPerolehan() == null) continue;
            
            String jenis = asset.getJenisAset();
            // Only include Laptop and Notebook
            if (jenis == null) continue;
            if (!jenis.equalsIgnoreCase("Laptop") && !jenis.equalsIgnoreCase("Notebook")) continue;
            
            long days = java.time.temporal.ChronoUnit.DAYS.between(asset.getTanggalPerolehan(), now);
            double years = days / 365.25;
            
            String riskBand;
            if (years < 4) {
                riskBand = "Low";
            } else if (years < 7) {
                riskBand = "Medium";
            } else {
                riskBand = "High";
            }
            
            agingData.computeIfAbsent(jenis, k -> new LinkedHashMap<>());
            agingData.get(jenis).merge(riskBand, 1L, Long::sum);
        }
        
        // Get Laptop and Notebook only
        List<String> eligibleJenis = new ArrayList<>();
        if (agingData.containsKey("Laptop") || agingData.keySet().stream().anyMatch(k -> k.equalsIgnoreCase("Laptop"))) {
            eligibleJenis.add(agingData.keySet().stream().filter(k -> k.equalsIgnoreCase("Laptop")).findFirst().orElse("Laptop"));
        }
        if (agingData.containsKey("Notebook") || agingData.keySet().stream().anyMatch(k -> k.equalsIgnoreCase("Notebook"))) {
            eligibleJenis.add(agingData.keySet().stream().filter(k -> k.equalsIgnoreCase("Notebook")).findFirst().orElse("Notebook"));
        }
        
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Jenis Aset");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah");
        
        // Create stacked bar chart
        javafx.scene.chart.StackedBarChart<String, Number> stackedBarChart = new javafx.scene.chart.StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle(null);
        stackedBarChart.setAnimated(false);
        stackedBarChart.setLegendVisible(true);
        stackedBarChart.setPrefHeight(280);
        
        // Create series for each risk band
        XYChart.Series<String, Number> lowSeries = new XYChart.Series<>();
        lowSeries.setName("Low (0-4 Th)");
        
        XYChart.Series<String, Number> mediumSeries = new XYChart.Series<>();
        mediumSeries.setName("Medium (4-7 Th)");
        
        XYChart.Series<String, Number> highSeries = new XYChart.Series<>();
        highSeries.setName("High (7+ Th)");
        
        // Populate series with data
        for (String jenis : eligibleJenis) {
            Map<String, Long> riskCounts = agingData.getOrDefault(jenis, new HashMap<>());
            
            lowSeries.getData().add(new XYChart.Data<>(jenis, riskCounts.getOrDefault("Low", 0L)));
            mediumSeries.getData().add(new XYChart.Data<>(jenis, riskCounts.getOrDefault("Medium", 0L)));
            highSeries.getData().add(new XYChart.Data<>(jenis, riskCounts.getOrDefault("High", 0L)));
        }
        
        stackedBarChart.getData().addAll(lowSeries, mediumSeries, highSeries);
        
        // Apply colors after adding to chart
        Platform.runLater(() -> {
            // Low risk - Green
            for (XYChart.Data<String, Number> data : lowSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #22c55e;");
                }
            }
            // Medium risk - Orange
            for (XYChart.Data<String, Number> data : mediumSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #f59e0b;");
                }
            }
            // High risk - Red
            for (XYChart.Data<String, Number> data : highSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #ef4444;");
                }
            }
        });
        
        card.getChildren().add(stackedBarChart);
        return card;
    }

    private record CardData(String title, String description, String value, String icon) {
    }

    /**
     * Format large numbers in compact Indonesian format
     * Examples: 9.200.000 -> Rp 9,2 Jt, 124.400.000.000 -> Rp 124,4 M
     */
    private String formatCompactCurrency(java.math.BigDecimal value) {
        if (value == null) return "Rp 0";
        
        double doubleVal = value.doubleValue();
        
        if (doubleVal >= 1_000_000_000_000L) {
            // Triliun
            double triliun = doubleVal / 1_000_000_000_000L;
            return String.format("Rp %.1f T", triliun);
        } else if (doubleVal >= 1_000_000_000L) {
            // Milyar
            double milyar = doubleVal / 1_000_000_000L;
            return String.format("Rp %.1f M", milyar);
        } else if (doubleVal >= 1_000_000L) {
            // Juta
            double juta = doubleVal / 1_000_000L;
            return String.format("Rp %.1f Jt", juta);
        } else if (doubleVal >= 1_000L) {
            // Ribu
            double ribu = doubleVal / 1_000L;
            return String.format("Rp %.1f Rb", ribu);
        } else {
            return String.format("Rp %.0f", doubleVal);
        }
    }

    private static class DashboardTextFormatter {
        private static String formatRelativeTime(java.time.LocalDateTime timestamp) {
            java.time.Duration duration = java.time.Duration.between(timestamp, java.time.LocalDateTime.now());
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

