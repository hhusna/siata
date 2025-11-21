package com.siata.client.service;

import com.siata.client.api.*;
import com.siata.client.dto.*;
import com.siata.client.model.Activity;
import com.siata.client.model.Asset;
import com.siata.client.model.AssetRequest;
import com.siata.client.model.Employee;
import com.siata.client.session.LoginSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataService {
    private static final DataService instance = new DataService();
    private final PegawaiApi pegawaiApi = new PegawaiApi();
    private final AssetApi assetApi = new AssetApi();
    private final LogApi logApi = new LogApi();
    private final PermohonanApi permohonanApi = new PermohonanApi();
    private final PengajuanApi pengajuanApi = new PengajuanApi();

    private final List<Asset> assets = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final List<AssetRequest> assetRequests = new ArrayList<>();
    private final List<Activity> activities = new ArrayList<>();
    private final AtomicInteger activityCounter = new AtomicInteger(1);

    private DataService() {
        initDummyData();
    }

    public static DataService getInstance() {
        return instance;
    }

    public List<Asset> getAssets() {
        List<Asset> listAsset = new ArrayList<>();
        for (AssetDto assetDto : assetApi.getAsset()) {
            Asset assetValue = new Asset();
            assetValue.setIdAset(assetDto.getIdAset());
            assetValue.setKodeAset(assetDto.getKodeAset());
            assetValue.setJenisAset(assetDto.getJenisAset());
            assetValue.setMerkBarang(assetDto.getMerkAset());
            assetValue.setTanggalPerolehan(assetDto.getTanggalPerolehan());
            assetValue.setNilaiRupiah(assetDto.getHargaAset());
            assetValue.setKondisi(assetDto.getKondisi());
            assetValue.setStatus(assetDto.getStatusPemakaian());
            if (assetDto.getPegawaiDto() != null) {
                assetValue.setKeterangan(Integer.toString(assetDto.getPegawaiDto().getNip()));
                assetValue.setSubdit(assetDto.getPegawaiDto().getNamaSubdir());
            } else {
                assetValue.setKeterangan("-");
                assetValue.setSubdit(assetDto.getSubdirektorat());
            }

            if (!(assetValue.getStatus().equals("Diajukan Hapus"))) {
                listAsset.add(assetValue);
            }

        }

        return listAsset;
    }

    public int getAssetBySubdit(String subdit) {
        List<Asset> assetList = getAssets();
        int count = 0;
        for (Asset asset : assetList) {
            if (asset.getSubdit().equals(subdit)) {
                count++;
            }
        }

        return count;
    }

    public int getAssetByJenis(String jenis) {
        List<Asset> assetList = getAssets();
        int count = 0;
        for (Asset asset : assetList) {
            if (asset.getJenisAset().equals(jenis)) {
                count++;
            }
        }
        System.out.println("DataService: " + count);
        return count;
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
        AssetDtoForRequest assetToDto = new AssetDtoForRequest();

        // 1. Selalu set Subdirektorat dari input form (Dropdown)
        assetToDto.setSubdirektorat(asset.getSubdit());

        // 2. Cek validasi NIP (Keterangan)
        String nipInput = asset.getKeterangan();
        if (nipInput != null && !nipInput.trim().isEmpty() && isNumeric(nipInput)) {
            // Jika ada input NIP valid, ambil data pegawai
            try {
                PegawaiDto pegawaiDto = pegawaiApi.getPegawaiByNip(Integer.parseInt(nipInput));
                if (pegawaiDto != null && pegawaiDto.getNama() != null) {
                    assetToDto.setPegawaiDto(pegawaiDto);
                    System.out.println("DataService: Pegawai ditemukan -> " + pegawaiDto.getNama());
                } else {
                    // Jika NIP diinput tapi tidak ditemukan di DB, biarkan null (aset subdit)
                    System.out.println("DataService: Pegawai tidak ditemukan di DB, set null.");
                    assetToDto.setPegawaiDto(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                assetToDto.setPegawaiDto(null);
            }
        } else {
            // Jika kosong atau bukan angka, berarti aset milik Subdit (tanpa pemegang)
            System.out.println("DataService: Input pemegang kosong/teks, set pegawai null.");
            assetToDto.setPegawaiDto(null);
        }

        // 3. Set field lainnya
        assetToDto.setKodeAset(asset.getKodeAset());
        assetToDto.setJenisAset(asset.getJenisAset());
        assetToDto.setMerkAset(asset.getMerkBarang());
        assetToDto.setTanggalPerolehan(asset.getTanggalPerolehan());
        assetToDto.setHargaAset((long) asset.getNilaiRupiah());
        assetToDto.setKondisi(asset.getKondisi());
        assetToDto.setStatusPemakaian(asset.getStatus());

        // 4. Kirim ke API
        assetApi.tambahAsset(assetToDto);
        logActivity("admin", "Create", "Menambahkan aset baru", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    // Helper kecil untuk cek angka
    private boolean isNumeric(String str) {
        if (str == null) return false;
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public void updateAsset(Asset asset) {
        AssetDto assetDto = new AssetDto();
        assetDto.setIdAset(asset.getIdAset());
        System.out.println("======= ID ASET: "+assetDto.getIdAset());
        assetDto.setKodeAset(asset.getKodeAset());
        assetDto.setJenisAset(asset.getJenisAset());
        assetDto.setMerkAset(asset.getMerkBarang());
        assetDto.setTanggalPerolehan(asset.getTanggalPerolehan());
        assetDto.setHargaAset((long) asset.getNilaiRupiah());
        assetDto.setKondisi(asset.getKondisi());
        assetDto.setStatusPemakaian(asset.getStatus());
        assetDto.setPegawaiDto(LoginSession.getPegawaiDto());
        assetApi.putAsset(assetDto);
        logActivity("admin", "Update", "Memperbarui aset", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    public void deleteAsset(Asset asset) {
        asset.setDeleted(true);
        assetApi.deleteAssetById(asset.getIdAset());
        logActivity("admin", "Delete", "Menghapus aset", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    public void removeAsset(Asset asset) {
        assets.remove(asset);
    }

    public List<Asset> getDeletedAssets() {
        List<Asset> listAsset = new ArrayList<>();
        AssetDto[] apiResult = assetApi.getAsset();
        for (AssetDto assetDto : apiResult) {
            Asset assetValue = new Asset();
            assetValue.setIdAset(assetDto.getIdAset());
            assetValue.setKodeAset(assetDto.getKodeAset());
            assetValue.setJenisAset(assetDto.getJenisAset());
            assetValue.setMerkBarang(assetDto.getMerkAset());
            assetValue.setTanggalPerolehan(assetDto.getTanggalPerolehan());
            assetValue.setNilaiRupiah(assetDto.getHargaAset());
            assetValue.setKondisi(assetDto.getKondisi());
            assetValue.setStatus(assetDto.getStatusPemakaian());

            if (assetDto.getPegawaiDto() != null) {
                assetValue.setKeterangan(Integer.toString(assetDto.getPegawaiDto().getNip()));
                assetValue.setSubdit(assetDto.getPegawaiDto().getNamaSubdir());
            } else {
                assetValue.setKeterangan("-");
                assetValue.setSubdit(assetDto.getSubdirektorat());
            }

            if (assetValue.getStatus().equals("Diajukan Hapus")) {
                listAsset.add(assetValue);
            }
        }

        return listAsset;
    }

    public List<Employee> getEmployees() {
        PegawaiDto[] pegawaiDto = pegawaiApi.getPegawai();
        List<Employee> employeeList = new ArrayList<>();
        for (PegawaiDto dto : pegawaiDto) {
            Employee emp = new Employee(Integer.toString(dto.getNip()), dto.getNama(), dto.getJabatan(), dto.getNamaSubdir());
            employeeList.add(emp);
        }
        return employeeList;
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        logActivity("admin", "Create", "Menambahkan pegawai baru", "Pegawai #" + employee.getNip(), employee.getNamaLengkap());
    }

    public void updateEmployee(Employee employee) {
        logActivity("admin", "Update", "Memperbarui data pegawai", "Pegawai #" + employee.getNip(), employee.getNamaLengkap());
    }

    public void removeEmployee(Employee employee) {
        pegawaiApi.deletePegawai(employee.getNip());
    }

    public void deleteEmployee(Employee employee) {
        removeEmployee(employee);
        logActivity("admin", "Delete", "Menghapus pegawai", "Pegawai #" + employee.getNip(), employee.getNamaLengkap());
    }

    public List<AssetRequest> getAssetRequests() {
        return new ArrayList<>(assetRequests);
    }

    public void addAssetRequest(AssetRequest request) {
        LogDto[] logDtos = logApi.getLog();
        LogDto logDto = new LogDto();

        for (LogDto log : logDtos) {
            if (log.getPegawaiDto().getNama().equals(LoginSession.getPegawaiDto().getNama())) {
                logDto = log;
                break;
            }
        }

        if ("Permohonan".equals(request.getTipe())) {
            PermohonanDto permohonanDto = new PermohonanDto();
            // Kode permohonan akan di-generate otomatis oleh server
            permohonanDto.setPegawaiDto(LoginSession.getPegawaiDto());
            // Set nama pemohon dari input user
            permohonanDto.setNamaPemohon(request.getPemohon());
            // Set unit dari input user, bukan dari pegawai yang login
            permohonanDto.setUnit(request.getUnit());

            permohonanDto.setJenisAset(request.getJenisAset());
            permohonanDto.setJumlah(request.getJumlah());
            permohonanDto.setDeskripsi(request.getDeskripsi());
            permohonanDto.setTujuanPenggunaan(request.getTujuanPenggunaan());
            permohonanDto.setPrioritas(request.getPrioritas());
            permohonanDto.setTimestamp(LocalDate.now());
            permohonanApi.createPermohonan(permohonanDto);
        } else {
            PengajuanDto pengajuanDto = new PengajuanDto();
            // Kode pengajuan akan di-generate otomatis oleh server
            pengajuanDto.setPegawaiDto(LoginSession.getPegawaiDto());
            // Set nama pengaju dari input user
            pengajuanDto.setNamaPengaju(request.getPemohon());
            // Set unit dari input user, bukan dari pegawai yang login
            pengajuanDto.setUnit(request.getUnit());

            pengajuanDto.setJenisAset(request.getJenisAset());
            pengajuanDto.setJumlah(request.getJumlah());
            pengajuanDto.setDeskripsi(request.getDeskripsi());
            pengajuanDto.setTujuanPenggunaan(request.getTujuanPenggunaan());
            pengajuanDto.setPrioritas(request.getPrioritas());
            pengajuanDto.setStatusPersetujuan(request.getStatus());
            pengajuanDto.setTimestamp(LocalDate.now());
            pengajuanApi.createPengajuan(pengajuanDto);
        }

        assetRequests.add(request);
        String actionType = "Permohonan".equals(request.getTipe()) ? "Create" : "Create";
        logActivity("admin", actionType,
                "Permohonan".equals(request.getTipe()) ? "Membuat permohonan aset" : "Membuat pengajuan aset",
                request.getNoPermohonan(),
                request.getJenisAset() + " untuk " + request.getPemohon());
    }

    public void updateAssetRequestStatus(AssetRequest request, String newStatus, String approver) {
        request.setStatus(newStatus);
        if (request.getTipe().equals("Permohonan")) {
            permohonanApi.patchStatus(request.getId(), newStatus);
        } else {
            pengajuanApi.patchStatus(request.getId(), newStatus);
        }
        String actionType = newStatus.contains("Disetujui") ? "Approve" : "Reject";
        logActivity(approver, actionType,
                newStatus.contains("Disetujui") ? "Menyetujui permohonan aset" : "Menolak permohonan aset",
                request.getNoPermohonan(),
                request.getDeskripsi() != null ? request.getDeskripsi() : request.getJenisAset());
    }

    public void updateAssetRequest(AssetRequest request) {
        if ("Permohonan".equals(request.getTipe())) {
            // Logika untuk permohonan
            PermohonanDto dto = new PermohonanDto();
            dto.setIdPermohonan(request.getId());
            dto.setKodePermohonan(request.getNoPermohonan());
            // Update nama pemohon jika diedit
            dto.setNamaPemohon(request.getPemohon());
            // Update unit dari input user
            dto.setUnit(request.getUnit());

            dto.setJenisAset(request.getJenisAset());
            dto.setJumlah(request.getJumlah());
            dto.setDeskripsi(request.getDeskripsi());
            dto.setTujuanPenggunaan(request.getTujuanPenggunaan());
            dto.setPrioritas(request.getPrioritas());
            dto.setTimestamp(request.getTanggal());

            // Panggil API
            permohonanApi.editPermohonan(dto);
        } else {
            // Logika untuk Pengajuan
            PengajuanDto dto = new PengajuanDto();
            dto.setIdPengajuan(request.getId());
            dto.setKodePengajuan(request.getNoPermohonan());
            // Update nama pengaju jika diedit
            dto.setNamaPengaju(request.getPemohon());
            // Update unit dari input user
            dto.setUnit(request.getUnit());

            dto.setJenisAset(request.getJenisAset());
            dto.setJumlah(request.getJumlah());
            dto.setDeskripsi(request.getDeskripsi());
            dto.setTujuanPenggunaan(request.getTujuanPenggunaan());
            dto.setPrioritas(request.getPrioritas());
            dto.setTimestamp(request.getTanggal());

            // Panggil API
            pengajuanApi.editPengajuan(dto);
        }

        logActivity("admin", "Update", "Memperbarui data " + request.getTipe().toLowerCase(), request.getNoPermohonan(), request.getJenisAset());
    }

    public void deleteAssetRequest(AssetRequest request) {
        assetRequests.remove(request);
        if (request.getTipe().equals("Permohonan")) {
            permohonanApi.deletePengajuan(request.getId());
        } else {
            pengajuanApi.deletePengajuan(request.getId());
        }
        logActivity("admin", "Delete", "Menghapus " + request.getTipe().toLowerCase(), request.getNoPermohonan(), request.getJenisAset());
    }

    public List<AssetRequest> getPermohonanAset() {
        PermohonanDto[] permohonanDtos = permohonanApi.getPermohonan();
        List<AssetRequest> assetRequestList = new ArrayList<>();

        for (PermohonanDto dto : permohonanDtos) {
            AssetRequest assetRequest = new AssetRequest();
            assetRequest.setId(dto.getIdPermohonan());
            assetRequest.setNoPermohonan(dto.getKodePermohonan());
            assetRequest.setTanggal(dto.getTimestamp());

            // Ambil nama dari field namaPemohon, fallback ke nama pegawai jika null
            if (dto.getNamaPemohon() != null && !dto.getNamaPemohon().isEmpty()) {
                assetRequest.setPemohon(dto.getNamaPemohon());
            } else if (dto.getPegawaiDto() != null) {
                assetRequest.setPemohon(dto.getPegawaiDto().getNama());
            } else {
                assetRequest.setPemohon("-");
            }

            assetRequest.setJenisAset(dto.getJenisAset());
            // Ambil unit dari field unit yang diinput user, bukan dari pegawai yang login
            assetRequest.setUnit(dto.getUnit() != null && !dto.getUnit().isEmpty() ? dto.getUnit() : "-");
            assetRequest.setJumlah(dto.getJumlah());
            assetRequest.setPrioritas(dto.getPrioritas());
            assetRequest.setTipe("Permohonan");
            assetRequest.setStatus(dto.getStatusPersetujuan());
            assetRequest.setDeskripsi(dto.getDeskripsi());
            assetRequest.setTujuanPenggunaan(dto.getTujuanPenggunaan());
            assetRequestList.add(assetRequest);
        }

        return assetRequestList;
    }

    public List<AssetRequest> getPengajuanAset() {
        PengajuanDto[] pengajuanDtos = pengajuanApi.getPengajuan();
        List<AssetRequest> assetRequestList = new ArrayList<>();

        for (PengajuanDto dto : pengajuanDtos) {
            AssetRequest assetRequest = new AssetRequest();
            assetRequest.setId(dto.getIdPengajuan());
            assetRequest.setNoPermohonan(dto.getKodePengajuan());
            assetRequest.setTanggal(dto.getTimestamp());
            // Ambil unit dari field unit yang diinput user, bukan dari pegawai yang login
            assetRequest.setUnit(dto.getUnit() != null && !dto.getUnit().isEmpty() ? dto.getUnit() : "-");
            assetRequest.setJenisAset(dto.getJenisAset());

            // Ambil nama dari field namaPengaju, fallback ke nama pegawai jika null
            if (dto.getNamaPengaju() != null && !dto.getNamaPengaju().isEmpty()) {
                assetRequest.setPemohon(dto.getNamaPengaju());
            } else if (dto.getPegawaiDto() != null) {
                assetRequest.setPemohon(dto.getPegawaiDto().getNama());
            } else {
                assetRequest.setPemohon("-");
            }

            assetRequest.setJumlah(dto.getJumlah());
            assetRequest.setPrioritas(dto.getPrioritas());
            assetRequest.setTipe("Pengajuan");
            assetRequest.setStatus(dto.getStatusPersetujuan());
            assetRequest.setDeskripsi(dto.getDeskripsi());
            assetRequest.setTujuanPenggunaan(dto.getTujuanPenggunaan());
            assetRequestList.add(assetRequest);
        }

        return assetRequestList;
    }

    public List<Activity> getActivities() {
        List<Activity> activityList = new ArrayList<>();
        LogDto[] logDtos = logApi.getLog();

        if (logDtos != null) {
            for (LogDto dto : logDtos) {
                Activity activity = mapLogDtoToActivity(dto);
                activityList.add(activity);
            }
        }

        return activityList;
    }

    public List<Activity> getRecentActivities(int limit) {
        List<Activity> activityList = new ArrayList<>();
        LogDto[] logDtos = logApi.getLog();

        if (logDtos != null) {
            for (LogDto dto : logDtos) {
                Activity activity = mapLogDtoToActivity(dto);
                activityList.add(activity);
            }
        }

        return activityList.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    private Activity mapLogDtoToActivity(LogDto dto) {
        Activity activity = new Activity();
        activity.setId(Long.toString(dto.getIdLog()));
        activity.setTimestamp(dto.getTimestamp());
        activity.setActionType(dto.getJenisLog());
        activity.setDescription(dto.getIsiLog());

        // 1. Tentukan User (Pelaku)
        if (dto.getPegawaiDto() != null) {
            activity.setUser(dto.getPegawaiDto().getNama());
        } else {
            activity.setUser("System/Admin");
        }

        // 2. Tentukan Target & Details berdasarkan konteks data yang ada di LogDto
        if (dto.getAssetDto() != null) {
            String namaAset = dto.getAssetDto().getJenisAset();
            if (dto.getAssetDto().getMerkAset() != null) {
                namaAset += " " + dto.getAssetDto().getMerkAset();
            }
            activity.setTarget("Aset: " + namaAset);

            String lokasi = "-";
            if (dto.getAssetDto().getPegawaiDto() != null) {
                lokasi = dto.getAssetDto().getPegawaiDto().getNamaSubdir();
            }

            activity.setDetails(String.format("Kondisi: %s | Status: %s | Lokasi: %s",
                    dto.getAssetDto().getKondisi(),
                    dto.getAssetDto().getStatusPemakaian(),
                    lokasi));

        } else if (dto.getPermohonanDto() != null) {
            activity.setTarget("Permohonan: " + dto.getPermohonanDto().getKodePermohonan());
            activity.setDetails(String.format("Jenis: %s | Jumlah: %d | Status: %s",
                    dto.getPermohonanDto().getJenisAset(),
                    dto.getPermohonanDto().getJumlah(),
                    dto.getPermohonanDto().getStatusPersetujuan()));

        } else if (dto.getPengajuanDto() != null) {
            activity.setTarget("Pengajuan: " + dto.getPengajuanDto().getKodePengajuan());
            activity.setDetails(String.format("Jenis: %s | Jumlah: %d | Prioritas: %s",
                    dto.getPengajuanDto().getJenisAset(),
                    dto.getPengajuanDto().getJumlah(),
                    dto.getPengajuanDto().getPrioritas()));

        } else if (dto.getPegawaiDto() != null && dto.getJenisLog().contains("PEGAWAI")) {
            activity.setTarget("Pegawai: " + dto.getPegawaiDto().getNama());
            activity.setDetails("NIP: " + dto.getPegawaiDto().getNip() + " | Jabatan: " + dto.getPegawaiDto().getJabatan());

        } else {
            activity.setTarget("-");
            activity.setDetails("");
        }

        return activity;
    }

    private void logActivity(String user, String actionType, String description, String target, String details) {
        logActivity(user, actionType, description, target, details, LocalDateTime.now());
    }

    private void logActivity(String user, String actionType, String description, String target, String details, LocalDateTime timestamp) {
        String id = "ACT-" + String.format("%06d", activityCounter.getAndIncrement());
        Activity activity = new Activity(id, user, actionType, description, target, details, timestamp);
        activities.add(activity);
    }

    private void initDummyData() {
        if (!assets.isEmpty() || !employees.isEmpty() || !assetRequests.isEmpty()) {
            return;
        }

        assets.addAll(List.of(
                new Asset("AST-0001", "Laptop", "Dell Latitude 5420", "Budi Santoso", "Subdit Teknis",
                        LocalDate.of(2023, 3, 12), 22000000, "Baik", "Digunakan"),
                new Asset("AST-0002", "Meja", "Meja Kayu Jati", "Gudang A", "Subdit Operasional",
                        LocalDate.of(2022, 11, 2), 7500000, "Sangat Baik", "Tersedia"),
                new Asset("AST-0003", "Printer", "Canon G3000", "Siti Rahayu", "Subdit SDM",
                        LocalDate.of(2024, 1, 6), 4500000, "Baik", "Digunakan"),
                new Asset("AST-0004", "Monitor", "Dell 24\"", "Cadangan", "Subdit Teknis",
                        LocalDate.of(2021, 8, 15), 3200000, "Cukup", "Tersedia")
        ));

        assets.get(3).setDeleted(true);

        employees.addAll(List.of(
                new Employee("199001152015011001", "Budi Santoso", "Kepala Subdit Teknis", "Subdit Teknis",
                        List.of("Laptop Dell Latitude 5420", "Meja Kerja Kayu Jati")),
                new Employee("199102182016041002", "Siti Rahayu", "Analis Operasional", "Subdit Operasional",
                        List.of("Printer Canon G3000")),
                new Employee("198912302014021003", "Ahmad Yani", "Staf Keamanan", "Subdit Keamanan",
                        List.of()),
                new Employee("199305052017051004", "Dewi Kusuma", "Analis SDM", "Subdit SDM",
                        List.of("Monitor Dell 24\"", "Laptop Lenovo Thinkpad"))
        ));

        assetRequests.addAll(List.of(
                new AssetRequest("REQ-2025-001", LocalDate.of(2025, 10, 1), "Budi Santoso", "Subdit Teknis",
                        "Laptop", 2, "Tinggi", "Permohonan",
                        "Laptop untuk tim analisis data", "Pengembangan sistem baru"),
                new AssetRequest("REQ-2025-002", LocalDate.of(2025, 10, 3), "Siti Rahayu", "Subdit Operasional",
                        "Printer", 1, "Sedang", "Permohonan",
                        "Printer tambahan untuk tim operasional", "Cetak dokumen harian"),
                new AssetRequest("REQ-2025-003", LocalDate.of(2025, 10, 5), "Ahmad Yani", "Subdit Keamanan",
                        "Proyektor", 1, "Sedang", "Pengajuan",
                        "Proyektor untuk pelatihan keamanan", "Pelatihan internal"),
                new AssetRequest("REQ-2025-004", LocalDate.of(2025, 10, 2), "Dewi Kusuma", "Subdit SDM",
                        "Kursi", 5, "Rendah", "Pengajuan",
                        "Kursi ergonomis untuk tim SDM", "Kenyamanan kerja")
        ));

        assetRequests.get(0).setStatus("Disetujui Direktur");
        assetRequests.get(1).setStatus("Disetujui PPK");
        assetRequests.get(2).setStatus("Pending");
        assetRequests.get(3).setStatus("Ditolak");

        seedInitialActivities();
    }

    private void seedInitialActivities() {
        if (!activities.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        logActivity("Admin PPBJ", "Approve", "Menyetujui permohonan aset", "Permohonan #REQ002",
                "Permohonan Printer untuk keperluan administrasi", now.minusMinutes(5));
        logActivity("Budi Santoso", "Create", "Mengajukan permohonan aset baru", "Permohonan #REQ003",
                "Mengajukan permohonan Proyektor untuk ruang rapat", now.minusHours(1));
        logActivity("Admin PPK", "Delete", "Menandai aset untuk dihapus", "Aset #AST005",
                "AC Daikin 1.5 PK - Kondisi rusak berat", now.minusHours(2));
        logActivity("Siti Rahayu", "Create", "Menambahkan aset baru", "Aset #AST007",
                "Proyektor Epson untuk ruang rapat utama", now.minusHours(3));
    }
}