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
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class RecapitulationView extends VBox {

    private final DataService dataService;
    private final ExportPdfApi exportApi = new ExportPdfApi();

    // Daftar Jenis Aset Standar untuk Laporan
    private final List<String> JENIS_ASET_LIST = List.of("Mobil", "Motor", "Scanner", "PC", "Laptop", "Tablet", "Printer", "Speaker", "Parabot");

    // === CACHED DATA - Load once, use everywhere ===
    private List<Asset> cachedAssets;
    private List<Asset> cachedAllAssetsIncludingDeleted;
    private List<Employee> cachedEmployees;
    
    // === PRE-COMPUTED GROUPINGS for O(1) lookups ===
    private Map<String, List<Asset>> assetsByJenis;
    private Map<String, List<Asset>> assetsBySubdir;
    private Map<String, List<Asset>> assetsByNip;
    private Map<String, Long> employeesByUnit;

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
        // Reload cached data
        loadAndCacheData();
        // Clear children dan rebuild view dengan data terbaru
        getChildren().clear();
        buildView();
    }

    private void buildView() {
        // Add content directly without ScrollPane - parent container already handles scrolling
        getChildren().add(buildHeader());
        getChildren().add(buildStatsGrid());

        // Menambahkan Tabel-Tabel dengan Logika (using cached data)
        getChildren().add(createPencatatanBmnTable());
        getChildren().add(createRencanaPenghapusanTable());
        getChildren().add(createRekapPemakaianTable());
        getChildren().add(createKeteranganKondisiTable());
        getChildren().add(createRekapPemeganganTable());
        getChildren().add(createJumlahPegawaiTable());
        getChildren().add(createUsageBySubdirTable()); // Penggunaan per Subdir
        getChildren().add(createEmployeeMatrixTable()); // Matriks Distribusi
    }

    private Node buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textGroup = new VBox(4);
        Label title = new Label("Rekapitulasi & Matriks");
        title.getStyleClass().add("section-heading");
        Label description = new Label("Ringkasan matriks distribusi aset dan pegawai");
        description.getStyleClass().add("section-description");
        textGroup.getChildren().addAll(title, description);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportButton = new Button("Export PDF");
        exportButton.getStyleClass().add("primary-button");
        exportButton.setOnAction(event -> {
            Stage stage = (Stage) exportButton.getScene().getWindow();
            exportApi.handle(stage);
        });

        header.getChildren().addAll(textGroup, spacer, exportButton);
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
        
        // Hitung jumlah aset per kategori
        long asetAktif = cachedAllAssetsIncludingDeleted.stream()
                .filter(a -> "Aktif".equals(a.getStatus()))
                .count();
        
        long asetNonAktif = cachedAllAssetsIncludingDeleted.stream()
                .filter(a -> "Non Aktif".equals(a.getStatus()))
                .count();
        
        // Rusak = Rusak Ringan + Rusak Berat
        long asetRusak = cachedAllAssetsIncludingDeleted.stream()
                .filter(a -> "Rusak Ringan".equals(a.getKondisi()) || "Rusak Berat".equals(a.getKondisi()))
                .count();

        // Hitung persentase
        String aktifPercent = totalAset > 0 ? String.format("%.1f", (double) asetAktif / totalAset * 100) : "0";
        String nonAktifPercent = totalAset > 0 ? String.format("%.1f", (double) asetNonAktif / totalAset * 100) : "0";
        String rusakPercent = totalAset > 0 ? String.format("%.1f", (double) asetRusak / totalAset * 100) : "0";

        List<CardData> cards = List.of(
                new CardData("Total Aset", String.valueOf(totalAset), "unit di seluruh sistem", "🧾"),
                new CardData("Aktif", String.valueOf(asetAktif), aktifPercent + "% dari total", "✅"),
                new CardData("Non Aktif", String.valueOf(asetNonAktif), nonAktifPercent + "% dari total", "📦"),
                new CardData("Rusak", String.valueOf(asetRusak), rusakPercent + "% dari total", "⚠")
        );

        for (int i = 0; i < cards.size(); i++) {
            statsGrid.add(createStatCard(cards.get(i)), i, 0);
        }

        return statsGrid;
    }

    // --- LOGIKA TABEL 1: PENCATATAN BMN ---
    private Node createPencatatanBmnTable() {
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

        return createDynamicTable("Pencatatan BMN", data,
                new String[]{"Jenis Aset", "Jumlah", "Sudah Dihapus", "Tercatat Sakti"},
                new int[]{200, 150, 150, 150});
    }

    // --- LOGIKA TABEL 2: RENCANA PENGHAPUSAN ---
    private Node createRencanaPenghapusanTable() {
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
            row.put("Habis Masa Pakai", String.valueOf(habisMasaPakai));
            row.put("Bersih", String.valueOf(bersih));
            row.put("Akan Habis 1 Tahun", String.valueOf(akanHabis1Thn));
            row.put("Total Bersih", String.valueOf(totalBersih));
            data.add(row);
        }

        return createDynamicTable("Rencana Penghapusan", data,
                new String[]{"Jenis Aset", "Habis Masa Pakai", "Bersih", "Akan Habis 1 Tahun", "Total Bersih"},
                new int[]{200, 150, 100, 180, 120});
    }

    // --- LOGIKA TABEL 3: REKAP PEMAKAIAN ---
    private Node createRekapPemakaianTable() {
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

            long totalDipakai = belumHapus + sudahHapus;

            Map<String, String> row = new HashMap<>();
            row.put("Jenis Aset", jenis);
            row.put("Dipakai Belum Hapus", String.valueOf(belumHapus));
            row.put("Dipakai Sudah Hapus", String.valueOf(sudahHapus));
            row.put("Total Dipakai", String.valueOf(totalDipakai));
            data.add(row);
        }

        return createDynamicTable("Rekap Pemakaian", data,
                new String[]{"Jenis Aset", "Dipakai Belum Hapus", "Dipakai Sudah Hapus", "Total Dipakai"},
                new int[]{200, 180, 180, 150});
    }

    // --- LOGIKA TABEL 4: KETERANGAN KONDISI ---
    private Node createKeteranganKondisiTable() {
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

        return createDynamicTable("Keterangan Kondisi", data,
                new String[]{"Jenis Aset", "Rusak Berat", "Hilang", "Gudang"},
                new int[]{200, 150, 150, 150});
    }

    // --- LOGIKA TABEL 5: REKAP PEMEGANG ---
    private Node createRekapPemeganganTable() {
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
            row.put("Total Pemegangan", String.valueOf(holderCounts.size()));
            data.add(row);
        }

        return createDynamicTable("Rekap Pemakaian", data,
                new String[]{"Jenis Aset", "Tidak Ganda", "Ganda", "Total Pemegangan"},
                new int[]{200, 150, 150, 180});
    }

    // --- LOGIKA TABEL 6: JUMLAH PEGAWAI PER BAGIAN ---
    private Node createJumlahPegawaiTable() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        // Use pre-computed grouping
        employeesByUnit.forEach((unit, count) -> {
            Map<String, String> row = new HashMap<>();
            row.put("Bagian", unit);
            row.put("ASN", String.valueOf(count));
            row.put("PPNPN", "0");
            row.put("Total", String.valueOf(count));
            data.add(row);
        });

        return createDynamicTable("Jumlah Pegawai per Bagian", data,
                new String[]{"Bagian", "ASN", "PPNPN", "Total"},
                new int[]{250, 150, 150, 150});
    }

    // --- LOGIKA TABEL 7: Pemakaian BMN (MATRIKS) ---
    private Node createUsageBySubdirTable() {
        List<String> subdirs = List.of("PPTAU", "AUNB", "AUNTB", "KAU", "SILAU", "Tata Usaha", "Direktur");
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        List<String> displayTypes = List.of("Mobil", "Motor", "Scanner", "PC", "Laptop", "Tablet", "Printer", "Speaker", "Parabot");

        for (String subdir : subdirs) {
            List<Asset> subdirAssets = assetsBySubdir.getOrDefault(subdir.toLowerCase(), Collections.emptyList());
            
            Map<String, String> row = new HashMap<>();
            row.put("Subdirektorat", subdir);

            long totalSubdir = 0;

            // Pre-group this subdir's assets by jenis for faster lookup
            Map<String, Long> subdirJenisCounts = subdirAssets.stream()
                    .collect(Collectors.groupingBy(a -> a.getJenisAset().toLowerCase(), Collectors.counting()));

            for (String jenis : displayTypes) {
                long count = subdirJenisCounts.getOrDefault(jenis.toLowerCase(), 0L);
                row.put(jenis, String.valueOf(count));
                totalSubdir += count;
            }
            row.put("Total", String.valueOf(totalSubdir));
            data.add(row);
        }

        List<String> colList = new ArrayList<>();
        colList.add("Subdirektorat");
        colList.addAll(displayTypes);
        colList.add("Total");

        int[] widths = new int[colList.size()];
        widths[0] = 200;
        for(int i=1; i<widths.length; i++) widths[i] = 80;

        return createDynamicTable("Pemakaian BMN", data,
                colList.toArray(new String[0]), widths);
    }

    // --- LOGIKA TABEL 8: MATRIKS DISTRIBUSI PER PEGAWAI ---
    private Node createEmployeeMatrixTable() {
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        List<String> displayTypes = List.of("Mobil", "Motor", "Scanner", "PC", "Laptop", "Tablet", "Printer", "Speaker", "Parabot");

        for (Employee emp : cachedEmployees) {
            // Get all assets for this employee from pre-computed map (O(1) lookup!)
            List<Asset> empAssets = assetsByNip.getOrDefault(emp.getNip(), Collections.emptyList());
            
            Map<String, String> row = new HashMap<>();
            row.put("Nama Pegawai", emp.getNamaLengkap());
            row.put("Subdir", emp.getUnit());

            long totalEmp = 0;

            // Pre-group employee's assets by jenis for faster counting
            Map<String, Long> empJenisCounts = empAssets.stream()
                    .collect(Collectors.groupingBy(a -> a.getJenisAset().toLowerCase(), Collectors.counting()));

            for (String jenis : displayTypes) {
                long count = empJenisCounts.getOrDefault(jenis.toLowerCase(), 0L);
                row.put(jenis, count > 0 ? String.valueOf(count) : "-");
                totalEmp += count;
            }

            row.put("Total", String.valueOf(totalEmp));
            data.add(row);
        }

        List<String> colList = new ArrayList<>();
        colList.add("Nama Pegawai");
        colList.add("Subdir");
        colList.addAll(displayTypes);
        colList.add("Total");

        int[] widths = new int[colList.size()];
        widths[0] = 200;
        widths[1] = 150;
        for(int i=2; i<widths.length; i++) widths[i] = 70;

        return createDynamicTable("Matriks Distribusi Aset per Pegawai", data,
                colList.toArray(new String[0]), widths);
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

    private record CardData(String title, String value, String description, String icon) {
    }
}