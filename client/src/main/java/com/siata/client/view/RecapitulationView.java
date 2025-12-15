package com.siata.client.view;

import com.siata.client.api.AssetApi;
import com.siata.client.api.ExportPdfApi;
import com.siata.client.dto.AssetDto;
import com.siata.client.model.Asset;
import com.siata.client.model.Employee;
import com.siata.client.service.DataService;
import com.siata.client.util.AnimationUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.util.Duration;

public class RecapitulationView extends VBox {

    private final DataService dataService;
    private final ExportPdfApi exportApi = new ExportPdfApi();

    // Daftar Jenis Aset Standar untuk Laporan
    private final List<String> JENIS_ASET_LIST = List.of("Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");

    // === CACHED DATA - Load once, use everywhere ===
    private List<Asset> cachedAssets;
    private List<Asset> cachedAllAssetsIncludingDeleted;
    private List<Employee> cachedEmployees;
    
    // === PRE-COMPUTED GROUPINGS for O(1) lookups ===
    private Map<String, List<Asset>> assetsByJenis;
    private Map<String, List<Asset>> assetsBySubdir;
    private Map<String, List<Asset>> assetsByNip;
    private Map<String, Long> employeesByUnit;

    // Tooltip descriptions for each column
    private static final Map<String, String> COLUMN_TOOLTIPS = Map.ofEntries(
        // Tabel Pencatatan BMN
        Map.entry("Jenis Aset", "Semua jenis aset yang tercatat di aplikasi ini"),
        Map.entry("Jumlah", "Jumlah semua aset yang tercatat di aplikasi ini"),
        Map.entry("Sudah Dihapus", "Jumlah semua aset yang sudah masuk ke tabel penghapusan aset"),
        Map.entry("Tercatat Sakti", "Jumlah jenis aset dikurangi aset yang sudah dihapus"),
        // Tabel Rencana Penghapusan
        Map.entry("Habis Pakai", "Jumlah aset yang usianya lebih dari 4 tahun dan masih aktif"),
        Map.entry("Bersih", "Jumlah aset yang tercatat sakti dikurangi habis pakai"),
        Map.entry("Akan Habis", "Jumlah aset yang usianya akan lebih dari 4 tahun di tahun depan"),
        Map.entry("Total Bersih", "Jumlah aset yang tercatat sakti dikurangi habis pakai dan akan habis"),
        // Tabel Rekap Pemakaian
        Map.entry("Dipakai Belum Hapus", "Jumlah aset yang tidak masuk tabel penghapusan aset dan masih dipakai (ada pemegang)"),
        Map.entry("Dipakai Sudah Hapus", "Jumlah aset yang masuk ke tabel penghapusan aset dan tidak dipakai (tidak ada pemegang)"),
        Map.entry("Total Dipakai", "Jumlah dipakai belum hapus ditambah dipakai sudah hapus"),
        // Tabel Keterangan Kondisi
        Map.entry("Rusak Berat", "Jumlah aset yang memiliki status rusak berat dan tidak dipakai (tidak ada pemegang)"),
        Map.entry("Hilang", "Jumlah aset yang memiliki keterangan subdirnya adalah hilang"),
        Map.entry("Gudang", "Jumlah aset yang keterangan subdirnya adalah gudang"),
        // Tabel Rekap Pemegangan
        Map.entry("Tidak Ganda", "Jumlah aset yang jumlah asetnya adalah satu pada matriks aset"),
        Map.entry("Ganda", "Jumlah aset yang jumlah asetnya lebih dari satu pada matriks aset"),
        Map.entry("Total Pemegang", "Tidak ganda + Ganda")
    );

    public RecapitulationView() {
        this.dataService = DataService.getInstance();
        setSpacing(24);
        getStyleClass().add("dashboard-content");
        loadAndCacheData();
        buildView();
    }
    
    /**
     * Load all data once and create lookup maps for O(1) access
     */
    private void loadAndCacheData() {
        // Single API calls - cached for reuse
        cachedAssets = dataService.getAssets();
        cachedAllAssetsIncludingDeleted = dataService.getAllAssetsIncludingDeleted();
        cachedEmployees = dataService.getEmployees();
        
        // Pre-compute groupings for fast lookup
        assetsByJenis = cachedAssets.stream()
                .collect(Collectors.groupingBy(a -> a.getJenisAset().toLowerCase()));
        
        assetsBySubdir = cachedAssets.stream()
                .filter(a -> a.getSubdir() != null)
                .collect(Collectors.groupingBy(a -> a.getSubdir().toLowerCase()));
        
        assetsByNip = cachedAssets.stream()
                .filter(a -> a.getKeterangan() != null && isNumeric(a.getKeterangan()))
                .collect(Collectors.groupingBy(Asset::getKeterangan));
        
        employeesByUnit = cachedEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getUnit, Collectors.counting()));
    }
    
    /**
     * Public method untuk refresh rekapitulasi data
     * Dipanggil ketika user kembali ke menu rekapitulasi atau setelah ada perubahan data
     */
    public void refreshData() {
        // Show loading overlay
        MainShellView.showLoading("Memuat rekapitulasi...");
        
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                // Reload cached data in background
                loadAndCacheData();
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            MainShellView.hideLoading();
            // Clear children dan rebuild view dengan data terbaru
            getChildren().clear();
            buildView();
        });
        
        task.setOnFailed(e -> {
            MainShellView.hideLoading();
            e.getSource().getException().printStackTrace();
        });
        
        new Thread(task).start();
    }

    // Carousel state
    private int currentCarouselIndex = 0;
    private List<Node> carouselTables;
    private StackPane carouselContainer;
    private HBox carouselIndicators;
    private Label carouselTitle;

    private void buildView() {
        // Add content directly without ScrollPane - parent container already handles scrolling
        getChildren().add(buildHeader());
        getChildren().add(buildStatsGrid());
        
        // Laptop needs detection card
        getChildren().add(createLaptopNeedsCard());
        
        // Carousel for 5 main tables
        getChildren().add(buildTableCarousel());
        
        // Merged subdirectory table (Pegawai + Pemakaian BMN)
        getChildren().add(createMergedSubdirTable());
        
        // Paginated & searchable employee matrix
        getChildren().add(createEmployeeMatrixTable());
    }

    // === CAROUSEL FOR 5 MAIN TABLES ===
    private Node buildTableCarousel() {
        VBox carouselWrapper = new VBox(16);
        carouselWrapper.getStyleClass().add("table-container");
        carouselWrapper.setPadding(new Insets(20));
        
        // Initialize carousel tables
        carouselTables = List.of(
            createPencatatanBmnTableContent(),
            createRencanaPenghapusanTableContent(),
            createRekapPemakaianTableContent(),
            createKeteranganKondisiTableContent(),
            createRekapPemeganganTableContent()
        );
        
        List<String> tableTitles = List.of(
            "Pencatatan BMN",
            "Rencana Penghapusan", 
            "Rekap Pemakaian",
            "Keterangan Kondisi",
            "Rekap Pemegangan"
        );
        
        // Header with title and navigation
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        
        carouselTitle = new Label(tableTitles.get(0));
        carouselTitle.getStyleClass().add("table-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Navigation buttons
        Button prevBtn = new Button("◀");
        prevBtn.getStyleClass().addAll("ghost-button");
        prevBtn.setStyle("-fx-font-size: 16px; -fx-padding: 8 12;");
        
        Button nextBtn = new Button("▶");
        nextBtn.getStyleClass().addAll("ghost-button");
        nextBtn.setStyle("-fx-font-size: 16px; -fx-padding: 8 12;");
        
        // Indicators (dots)
        carouselIndicators = new HBox(8);
        carouselIndicators.setAlignment(Pos.CENTER);
        for (int i = 0; i < carouselTables.size(); i++) {
            Label dot = new Label("●");
            dot.setStyle(i == 0 ? "-fx-text-fill: #2563eb; -fx-font-size: 10px;" : "-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
            carouselIndicators.getChildren().add(dot);
        }
        
        header.getChildren().addAll(carouselTitle, spacer, prevBtn, carouselIndicators, nextBtn);
        
        // Container for table content
        carouselContainer = new StackPane();
        carouselContainer.setMinHeight(300);
        carouselContainer.getChildren().add(carouselTables.get(0));
        
        // Navigation actions
        prevBtn.setOnAction(e -> {
            if (currentCarouselIndex > 0) {
                currentCarouselIndex--;
                updateCarousel(tableTitles);
            }
        });
        
        nextBtn.setOnAction(e -> {
            if (currentCarouselIndex < carouselTables.size() - 1) {
                currentCarouselIndex++;
                updateCarousel(tableTitles);
            }
        });
        
        carouselWrapper.getChildren().addAll(header, carouselContainer);
        
        return carouselWrapper;
    }
    
    private void updateCarousel(List<String> titles) {
        // Update title
        carouselTitle.setText(titles.get(currentCarouselIndex));
        
        // Update indicators
        for (int i = 0; i < carouselIndicators.getChildren().size(); i++) {
            Label dot = (Label) carouselIndicators.getChildren().get(i);
            dot.setStyle(i == currentCarouselIndex ? "-fx-text-fill: #2563eb; -fx-font-size: 10px;" : "-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
        }
        
        // Animate transition
        Node oldTable = carouselContainer.getChildren().get(0);
        Node newTable = carouselTables.get(currentCarouselIndex);
        
        // Fade out old, fade in new
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(150), oldTable);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            carouselContainer.getChildren().setAll(newTable);
            newTable.setOpacity(0);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(150), newTable);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private Node buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_RIGHT);

        Button exportButton = new Button("Export PDF");
        exportButton.getStyleClass().add("primary-button");
        exportButton.setOnAction(event -> {
            Stage stage = (Stage) exportButton.getScene().getWindow();
            exportApi.handle(stage);
        });

        header.getChildren().add(exportButton);
        return header;
    }

    private Node buildStatsGrid() {
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            statsGrid.getColumnConstraints().add(column);
        }

        // Use cached data
        long totalAset = cachedAllAssetsIncludingDeleted.size();
        
        // 1. Total Aktif (Status = Aktif)
        long asetAktif = cachedAllAssetsIncludingDeleted.stream()
                .filter(a -> "Aktif".equalsIgnoreCase(a.getStatus()))
                .count();

        // 2. Total Nonaktif (Status = Non Aktif / NONAKTIF / Nonaktif)
        long asetNonAktif = cachedAllAssetsIncludingDeleted.stream()
                .filter(a -> {
                   if (a.getStatus() == null) return false;
                   String s = a.getStatus().trim().toLowerCase();
                   return s.equals("non aktif") || s.equals("nonaktif");
                })
                .count();
        
        // Helper untuk filter Laptop & Notebook
        java.util.function.Predicate<Asset> isLaptopOrNotebook = a -> {
            if (a.getJenisAset() == null) return false;
            String j = a.getJenisAset().toLowerCase();
            return j.contains("laptop") || j.contains("notebook");
        };

        long totalLaptopNotebook = cachedAllAssetsIncludingDeleted.stream()
                .filter(isLaptopOrNotebook)
                .count();

        // 3. Total Rusak Berat Laptop Notebook (Kondisi = R. BERAT / Rusak Berat)
        long asetRusakBeratLN = cachedAllAssetsIncludingDeleted.stream()
                .filter(isLaptopOrNotebook)
                .filter(a -> {
                    if (a.getKondisi() == null) return false;
                    String k = a.getKondisi().trim().toLowerCase();
                    return k.equals("rusak berat") || k.equals("r. berat");
                })
                .count();

        // 4. Total Tua Laptop Notebook (isTua = true)
        long asetTuaLN = cachedAllAssetsIncludingDeleted.stream()
                .filter(isLaptopOrNotebook)
                .filter(Asset::isTua)
                .count();

        // Hitung persentase
        String aktifPercent = totalAset > 0 ? String.format("%.1f", (double) asetAktif / totalAset * 100) : "0";
        String nonAktifPercent = totalAset > 0 ? String.format("%.1f", (double) asetNonAktif / totalAset * 100) : "0";
        String rusakLNPercent = totalLaptopNotebook > 0 ? String.format("%.1f", (double) asetRusakBeratLN / totalLaptopNotebook * 100) : "0";
        String tuaLNPercent = totalLaptopNotebook > 0 ? String.format("%.1f", (double) asetTuaLN / totalLaptopNotebook * 100) : "0";

        List<CardData> cards = List.of(
                new CardData("Total Aktif", String.valueOf(asetAktif), "dari " + totalAset + " aset (" + aktifPercent + "%)", "✅"),
                new CardData("Total Nonaktif", String.valueOf(asetNonAktif), "dari " + totalAset + " aset (" + nonAktifPercent + "%)", "📦"),
                new CardData("Total RB Laptop Notebook", String.valueOf(asetRusakBeratLN), "dari " + totalLaptopNotebook + " aset (" + rusakLNPercent + "%)", "⚠"),
                new CardData("Total Tua Laptop Notebook", String.valueOf(asetTuaLN), "dari " + totalLaptopNotebook + " aset (" + tuaLNPercent + "%)", "👴")
        );

        for (int i = 0; i < cards.size(); i++) {
            statsGrid.add(createStatCard(cards.get(i)), i, 0);
        }

        VBox container = new VBox(16, statsGrid);
        
        // Animate stat cards with stagger effect (same as Dashboard)
        Platform.runLater(() -> {
            List<Node> cardNodes = new ArrayList<>(statsGrid.getChildren());
            for (Node card : cardNodes) {
                AnimationUtils.addHoverLiftEffect(card);
            }
            AnimationUtils.staggerSlideInFromBottom(cardNodes, Duration.millis(80));
        });

        return container;
    }

    // --- CAROUSEL TABLE CONTENT METHODS (return TableView only for carousel) ---
    
    private Node createPencatatanBmnTableContent() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        for (String jenis : JENIS_ASET_LIST) {
            List<Asset> jenisAssets = assetsByJenis.getOrDefault(jenis.toLowerCase(), Collections.emptyList());
            long total = jenisAssets.size();
            long dihapus = jenisAssets.stream()
                    .filter(a -> "Non Aktif".equalsIgnoreCase(a.getStatus()))
                    .count();
            long tercatatSakti = total - dihapus;

            if (total > 0) {
                Map<String, String> row = new HashMap<>();
                row.put("Jenis Aset", jenis);
                row.put("Jumlah", String.valueOf(total));
                row.put("Sudah Dihapus", String.valueOf(dihapus));
                row.put("Tercatat Sakti", String.valueOf(tercatatSakti));
                data.add(row);
            }
        }

        return createCarouselTable(data,
                new String[]{"Jenis Aset", "Jumlah", "Sudah Dihapus", "Tercatat Sakti"},
                new int[]{200, 120, 140, 140});
    }

    // --- LOGIKA TABEL 2: RENCANA PENGHAPUSAN ---
    private Node createRencanaPenghapusanTableContent() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        LocalDate now = LocalDate.now();

        for (String jenis : JENIS_ASET_LIST) {
            List<Asset> jenisAssets = assetsByJenis.getOrDefault(jenis.toLowerCase(), Collections.emptyList())
                    .stream()
                    .filter(a -> !a.isDeleted())
                    .toList();

            if (jenisAssets.isEmpty()) continue;

            long total = jenisAssets.size();
            long nonAktif = jenisAssets.stream()
                    .filter(a -> "Non Aktif".equalsIgnoreCase(a.getStatus()))
                    .count();
            long tercatatSakti = total - nonAktif;

            long habisMasaPakai = jenisAssets.stream()
                    .filter(a -> a.getTanggalPerolehan() != null && ChronoUnit.YEARS.between(a.getTanggalPerolehan(), now) >= 4)
                    .count();

            long akanHabis1Thn = jenisAssets.stream()
                    .filter(a -> {
                        if (a.getTanggalPerolehan() == null) return false;
                        long years = ChronoUnit.YEARS.between(a.getTanggalPerolehan(), now);
                        return years >= 3 && years < 4;
                    })
                    .count();

            long bersih = tercatatSakti - habisMasaPakai;
            long totalBersih = bersih - akanHabis1Thn;

            Map<String, String> row = new HashMap<>();
            row.put("Jenis Aset", jenis);
            row.put("Habis Pakai", String.valueOf(habisMasaPakai));
            row.put("Bersih", String.valueOf(bersih));
            row.put("Akan Habis", String.valueOf(akanHabis1Thn));
            row.put("Total Bersih", String.valueOf(totalBersih));
            data.add(row);
        }

        return createCarouselTable(data,
                new String[]{"Jenis Aset", "Habis Pakai", "Bersih", "Akan Habis", "Total Bersih"},
                new int[]{150, 100, 80, 100, 100});
    }

    // --- LOGIKA TABEL 3: REKAP PEMAKAIAN ---
    private Node createRekapPemakaianTableContent() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        for (String jenis : JENIS_ASET_LIST) {
            List<Asset> jenisAssets = assetsByJenis.getOrDefault(jenis.toLowerCase(), Collections.emptyList());
            
            long belumHapus = jenisAssets.stream()
                    .filter(a -> "Aktif".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            long sudahHapus = jenisAssets.stream()
                    .filter(a -> "Non Aktif".equalsIgnoreCase(a.getStatus()))
                    .count();

            if (belumHapus == 0 && sudahHapus == 0) continue;

            Map<String, String> row = new HashMap<>();
            row.put("Jenis Aset", jenis);
            row.put("Dipakai Belum Hapus", String.valueOf(belumHapus));
            row.put("Dipakai Sudah Hapus", String.valueOf(sudahHapus));
            row.put("Total Dipakai", String.valueOf(belumHapus + sudahHapus));
            data.add(row);
        }

        return createCarouselTable(data,
                new String[]{"Jenis Aset", "Dipakai Belum Hapus", "Dipakai Sudah Hapus", "Total Dipakai"},
                new int[]{180, 140, 140, 120});
    }

    // --- LOGIKA TABEL 4: KETERANGAN KONDISI ---
    private Node createKeteranganKondisiTableContent() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        for (String jenis : JENIS_ASET_LIST) {
            List<Asset> jenisAssets = assetsByJenis.getOrDefault(jenis.toLowerCase(), Collections.emptyList());

            if (jenisAssets.isEmpty()) continue;

            long rusakBerat = jenisAssets.stream().filter(a -> "Rusak Berat".equalsIgnoreCase(a.getKondisi())).count();
            long gudang = jenisAssets.stream().filter(a -> "Gudang".equalsIgnoreCase(a.getKondisi())).count();
            long hilang = jenisAssets.stream().filter(a -> "Hilang".equalsIgnoreCase(a.getKondisi())).count();

            Map<String, String> row = new HashMap<>();
            row.put("Jenis Aset", jenis);
            row.put("Rusak Berat", String.valueOf(rusakBerat));
            row.put("Hilang", String.valueOf(hilang));
            row.put("Gudang", String.valueOf(gudang));
            data.add(row);
        }

        return createCarouselTable(data,
                new String[]{"Jenis Aset", "Rusak Berat", "Hilang", "Gudang"},
                new int[]{200, 120, 120, 120});
    }

    // --- LOGIKA TABEL 5: REKAP PEMEGANG ---
    private Node createRekapPemeganganTableContent() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        for (String jenis : JENIS_ASET_LIST) {
            List<Asset> jenisAssets = assetsByJenis.getOrDefault(jenis.toLowerCase(), Collections.emptyList())
                    .stream()
                    .filter(a -> "Aktif".equalsIgnoreCase(a.getStatus()) && isNumeric(a.getKeterangan()))
                    .toList();

            if (jenisAssets.isEmpty()) continue;

            Map<String, Long> holderCounts = jenisAssets.stream()
                    .collect(Collectors.groupingBy(Asset::getKeterangan, Collectors.counting()));

            long ganda = holderCounts.values().stream().filter(count -> count > 1).count();
            long tidakGanda = holderCounts.values().stream().filter(count -> count == 1).count();

            Map<String, String> row = new HashMap<>();
            row.put("Jenis Aset", jenis);
            row.put("Tidak Ganda", String.valueOf(tidakGanda));
            row.put("Ganda", String.valueOf(ganda));
            row.put("Total Pemegang", String.valueOf(holderCounts.size()));
            data.add(row);
        }

        return createCarouselTable(data,
                new String[]{"Jenis Aset", "Tidak Ganda", "Ganda", "Total Pemegang"},
                new int[]{180, 120, 120, 130});
    }

    // Helper for carousel tables (no title wrapper)
    private Node createCarouselTable(ObservableList<Map<String, String>> data, String[] columns, int[] widths) {
        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        table.setItems(data);

        for (int idx = 0; idx < columns.length; idx++) {
            final String colName = columns[idx];
            TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);

            if (idx < widths.length) {
                col.setPrefWidth(widths[idx]);
            }

            // Add instant tooltip to column header if available
            String tooltipText = COLUMN_TOOLTIPS.get(colName);
            if (tooltipText != null) {
                Label headerLabel = new Label(colName);
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.millis(200));
                tooltip.setStyle("-fx-font-size: 11px; -fx-font-weight: normal; -fx-background-color: #1e293b; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6;");
                headerLabel.setTooltip(tooltip);
                col.setGraphic(headerLabel);
                col.setText(""); // Clear text since we use graphic
            }

            col.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getOrDefault(colName, "0"))
            );

            if (idx > 0) {
                col.setStyle("-fx-alignment: CENTER-RIGHT;");
            }

            table.getColumns().add(col);
        }

        int rowCount = data.size();
        double fixedHeight = Math.max(150, (rowCount + 1.5) * 35);
        table.setPrefHeight(fixedHeight);
        table.setMinHeight(150);
        table.setMaxHeight(350);

        return table;
    }

    // === MERGED SUBDIRECTORY TABLE (Pegawai + Pemakaian BMN) ===
    private Node createMergedSubdirTable() {
        List<String> subdirs = List.of("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        List<String> displayTypes = List.of("Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");
        
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        Map<String, Long> asnByUnit = cachedEmployees.stream()
            .filter(emp -> !emp.isPpnpn())
            .collect(Collectors.groupingBy(Employee::getUnit, Collectors.counting()));
        
        Map<String, Long> ppnpnByUnit = cachedEmployees.stream()
            .filter(emp -> emp.isPpnpn())
            .collect(Collectors.groupingBy(Employee::getUnit, Collectors.counting()));

        for (String subdir : subdirs) {
            List<Asset> subdirAssets = assetsBySubdir.getOrDefault(subdir.toLowerCase(), Collections.emptyList());
            
            Map<String, String> row = new HashMap<>();
            row.put("Subdirektorat", subdir);
            
            long asn = asnByUnit.getOrDefault(subdir, 0L);
            long ppnpn = ppnpnByUnit.getOrDefault(subdir, 0L);
            row.put("ASN", String.valueOf(asn));
            row.put("PPNPN", String.valueOf(ppnpn));
            row.put("Total Pegawai", String.valueOf(asn + ppnpn));

            Map<String, Long> subdirJenisCounts = subdirAssets.stream()
                    .collect(Collectors.groupingBy(a -> a.getJenisAset().toLowerCase(), Collectors.counting()));

            long totalAset = 0;
            for (String jenis : displayTypes) {
                long count = subdirJenisCounts.getOrDefault(jenis.toLowerCase(), 0L);
                row.put(jenis, count > 0 ? String.valueOf(count) : "-");
                totalAset += count;
            }
            row.put("Total Aset", String.valueOf(totalAset));
            data.add(row);
        }

        List<String> colList = new ArrayList<>();
        colList.add("Subdirektorat");
        colList.addAll(List.of("ASN", "PPNPN", "Total Pegawai"));
        colList.addAll(displayTypes);
        colList.add("Total Aset");

        int[] widths = new int[colList.size()];
        widths[0] = 100;
        widths[1] = 45; widths[2] = 55; widths[3] = 85;
        for (int i = 4; i < widths.length; i++) widths[i] = 55;

        return createDynamicTable("Rekapitulasi per Subdirektorat", data,
                colList.toArray(new String[0]), widths);
    }

    // === EMPLOYEE MATRIX TABLE (with search & styled name/NIP) ===
    private Node createEmployeeMatrixTable() {
        VBox section = new VBox(12);
        section.getStyleClass().add("table-container");
        section.setPadding(new Insets(20));

        Label sectionTitle = new Label("Matriks Distribusi Aset per Pegawai");
        sectionTitle.getStyleClass().add("table-title");

        javafx.scene.control.TextField searchField = new javafx.scene.control.TextField();
        searchField.setPromptText("🔍 Cari nama atau NIP...");
        searchField.getStyleClass().add("filter-search-field");
        searchField.setMaxWidth(280);

        List<String> displayTypes = List.of("Mobil", "Motor", "Scanner", "PC", "Laptop", "Notebook", "Tablet", "Printer", "Speaker", "Parabot");

        ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
        for (Employee emp : cachedEmployees) {
            List<Asset> empAssets = assetsByNip.getOrDefault(emp.getNip(), Collections.emptyList());
            
            Map<String, String> row = new HashMap<>();
            row.put("Nama", emp.getNamaLengkap());
            row.put("NIP", emp.getNip() != null ? emp.getNip() : "-");
            row.put("Subdir", emp.getUnit());

            long totalEmp = 0;
            Map<String, Long> empJenisCounts = empAssets.stream()
                    .collect(Collectors.groupingBy(a -> a.getJenisAset().toLowerCase(), Collectors.counting()));

            for (String jenis : displayTypes) {
                long count = empJenisCounts.getOrDefault(jenis.toLowerCase(), 0L);
                row.put(jenis, count > 0 ? String.valueOf(count) : "-");
                totalEmp += count;
            }

            row.put("Total", String.valueOf(totalEmp));
            allData.add(row);
        }

        javafx.collections.transformation.FilteredList<Map<String, String>> filteredData = 
            new javafx.collections.transformation.FilteredList<>(allData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(row -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return row.getOrDefault("Nama", "").toLowerCase().contains(lower) ||
                       row.getOrDefault("NIP", "").toLowerCase().contains(lower);
            });
        });

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        table.setItems(filteredData);

        // Name + NIP column with custom cell
        TableColumn<Map<String, String>, String> nameCol = new TableColumn<>("Pegawai");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("Nama")));
        nameCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setGraphic(null);
                } else {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    String nip = row.getOrDefault("NIP", "-");
                    
                    VBox container = new VBox(1);
                    Label nameLabel = new Label(name);
                    nameLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 12px;");
                    
                    Label nipLabel = new Label(nip.length() == 18 ? nip : "No NIP");
                    nipLabel.setStyle("-fx-font-size: 10px; -fx-opacity: 0.5;");
                    
                    container.getChildren().addAll(nameLabel, nipLabel);
                    setGraphic(container);
                }
            }
        });
        table.getColumns().add(nameCol);

        TableColumn<Map<String, String>, String> subdirCol = new TableColumn<>("Subdir");
        subdirCol.setPrefWidth(70);
        subdirCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrDefault("Subdir", "-")));
        table.getColumns().add(subdirCol);

        for (String jenis : displayTypes) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(jenis);
            col.setPrefWidth(55);
            col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrDefault(jenis, "-")));
            col.setStyle("-fx-alignment: CENTER;");
            table.getColumns().add(col);
        }

        TableColumn<Map<String, String>, String> totalCol = new TableColumn<>("Total");
        totalCol.setPrefWidth(50);
        totalCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrDefault("Total", "0")));
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        table.getColumns().add(totalCol);

        table.setPrefHeight(420);
        table.setMinHeight(200);
        table.setMaxHeight(500);

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(sectionTitle, spacer, searchField);

        section.getChildren().addAll(headerRow, table);
        
        return section;
    }

    // --- HELPER: METHOD GENERIK UNTUK MEMBUAT TABEL ---
    private Node createDynamicTable(String title, ObservableList<Map<String, String>> data, String[] columns, int[] widths) {
        VBox section = new VBox(12);
        section.getStyleClass().add("table-container");
        section.setPadding(new Insets(20));

        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("table-title");

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("data-table");
        table.setItems(data);

        for (int idx = 0; idx < columns.length; idx++) {
            final String colName = columns[idx];
            TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);

            if (idx < widths.length) {
                col.setPrefWidth(widths[idx]);
            }

            col.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getOrDefault(colName, "0"))
            );

            if (idx > 0 && !colName.equals("Subdir") && !colName.equals("Subdirektorat")) {
                col.setStyle("-fx-alignment: CENTER-RIGHT;");
            }

            table.getColumns().add(col);
        }

        // Use fixed height calculation instead of binding
        int rowCount = data.size();
        double fixedHeight = Math.max(150, (rowCount + 1.5) * 35);
        table.setPrefHeight(fixedHeight);
        table.setMinHeight(150);
        table.setMaxHeight(600);

        section.getChildren().addAll(sectionTitle, table);
        return section;
    }

    private Node createStatCard(CardData data) {
        VBox card = new VBox(12);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(24));

        HBox heading = new HBox(8);
        heading.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(heading, Priority.ALWAYS);

        Label titleLabel = new Label(data.title());
        titleLabel.getStyleClass().add("stat-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label iconLabel = new Label(data.icon());
        iconLabel.getStyleClass().add("stat-card-icon");

        heading.getChildren().addAll(titleLabel, spacer, iconLabel);

        Label valueLabel = new Label(data.value());
        valueLabel.getStyleClass().add("stat-card-value");

        Label descLabel = new Label(data.description());
        descLabel.getStyleClass().add("stat-card-description");

        card.getChildren().addAll(heading, valueLabel, descLabel);
        
        // Add hover lift animation
        AnimationUtils.addHoverLiftEffect(card);
        
        return card;
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    /**
     * Creates the Laptop Needs Detection card
     * Shows current and projected laptop requirements based on ASN count
     */
    private Node createLaptopNeedsCard() {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("table-container", "chart-card");
        card.setPadding(new javafx.geometry.Insets(16));
        
        java.time.LocalDate now = java.time.LocalDate.now();
        
        // Get total ASN (employees)
        int totalASN = cachedEmployees.size();
        
        // Filter laptops that are in good condition (AKTIF, not Rusak Berat, age < 4 years)
        List<Asset> goodLaptops = cachedAssets.stream()
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
        Label titleLabel = new Label("💻 Deteksi Kebutuhan Laptop");
        titleLabel.getStyleClass().add("chart-title");
        
        // Dropdown to switch versions
        ComboBox<String> versionDropdown = new ComboBox<>();
        versionDropdown.getItems().addAll("Tahun Ini", "Tahun Depan");
        versionDropdown.setValue("Tahun Ini");
        versionDropdown.setStyle("-fx-font-size: 10px;");
        
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        headerRow.getChildren().addAll(titleLabel, spacer, versionDropdown);
        
        // Main value display
        Label valueLabel = new Label();
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        // Description
        Label descLabel = new Label();
        descLabel.setStyle("-fx-font-size: 12px;");
        
        // Details row
        Label detailsLabel = new Label();
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-padding: 8 12; -fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        
        // Explanation section
        Label explanationTitle = new Label("📊 Perhitungan:");
        explanationTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        
        Label explanationLabel = new Label();
        explanationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        explanationLabel.setWrapText(true);
        
        // Update function
        Runnable updateDisplay = () -> {
            boolean isCurrentYear = "Tahun Ini".equals(versionDropdown.getValue());
            int kebutuhan = isCurrentYear ? kebutuhanTahunIni : kebutuhanTahunDepan;
            
            if (kebutuhan > 0) {
                valueLabel.setText("+" + kebutuhan + " Unit");
                valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                descLabel.setText("⚠ Perlu pengadaan " + kebutuhan + " laptop");
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

    private record CardData(String title, String value, String description, String icon) {
    }
}