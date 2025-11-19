package com.siata.client.service;

import com.siata.client.api.AssetApi;
import com.siata.client.api.PegawaiApi;
import com.siata.client.dto.AssetDto;
import com.siata.client.dto.AssetDtoForRequest;
import com.siata.client.dto.PegawaiDto;
import com.siata.client.model.Activity;
import com.siata.client.model.Asset;
import com.siata.client.model.AssetRequest;
import com.siata.client.model.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataService {
    private static final DataService instance = new DataService();
    private final PegawaiApi pegawaiApi = new PegawaiApi();
    private final AssetApi assetApi = new AssetApi();

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
//        return assets.stream()
//            .filter(asset -> !asset.isDeleted())
//            .toList();

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
            assetValue.setKeterangan(Integer.toString(assetDto.getPegawaiDto().getNip()));
            assetValue.setSubdit(assetDto.getPegawaiDto().getNamaSubdir());
            listAsset.add(assetValue);
        }

        return listAsset;
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
        AssetDtoForRequest assetToDto = new AssetDtoForRequest();
        boolean result;
        try {
            Integer.parseInt(asset.getKeterangan());
            result = true;
        } catch (NumberFormatException e) {
            System.out.println("====== FALSE");
            result = false;
        }
        if (result == true) {
            PegawaiDto pegawaiDto = pegawaiApi.getPegawaiByNip(Integer.parseInt(asset.getKeterangan()));
            assetToDto.setPegawaiDto(pegawaiDto);

            System.out.println("======" + pegawaiDto.getNama());
        }

        assetToDto.setKodeAset(asset.getKodeAset());
        assetToDto.setJenisAset(asset.getJenisAset());
        assetToDto.setMerkAset(asset.getMerkBarang());
        assetToDto.setTanggalPerolehan(asset.getTanggalPerolehan());
        assetToDto.setHargaAset((long) asset.getNilaiRupiah());
        assetToDto.setKondisi(asset.getKondisi());
        assetToDto.setStatusPemakaian(asset.getStatus());

        assetApi.tambahAsset(assetToDto);
        logActivity("admin", "Create", "Menambahkan aset baru", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    public void updateAsset(Asset asset) {
        logActivity("admin", "Update", "Memperbarui aset", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    public void deleteAsset(Asset asset) {
//        asset.setDeleted(true);

        asset.setDeleted(true);
        assetApi.deleteAssetById(asset.getIdAset());
        logActivity("admin", "Delete", "Menghapus aset", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    public void removeAsset(Asset asset) {
        assets.remove(asset);
    }
    
    public List<Asset> getDeletedAssets() {
        return assets.stream().filter(Asset::isDeleted).toList();
    }

//    public List<Employee> getEmployees() {
//        return new ArrayList<>(employees);
//    }

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
//        employees.remove(employee);
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
        assetRequests.add(request);
        String actionType = "Permohonan".equals(request.getTipe()) ? "Create" : "Create";
        logActivity("admin", actionType, 
            "Permohonan".equals(request.getTipe()) ? "Membuat permohonan aset" : "Membuat pengajuan aset",
            request.getNoPermohonan(),
            request.getJenisAset() + " untuk " + request.getPemohon());
    }

    public void updateAssetRequestStatus(AssetRequest request, String newStatus, String approver) {
        request.setStatus(newStatus);
        String actionType = newStatus.contains("Disetujui") ? "Approve" : "Reject";
        logActivity(approver, actionType, 
            newStatus.contains("Disetujui") ? "Menyetujui permohonan aset" : "Menolak permohonan aset",
            request.getNoPermohonan(),
            request.getDeskripsi() != null ? request.getDeskripsi() : request.getJenisAset());
    }

    public void updateAssetRequest(AssetRequest request) {
        logActivity("admin", "Update", "Memperbarui data " + request.getTipe().toLowerCase(), request.getNoPermohonan(), request.getJenisAset());
    }

    public void deleteAssetRequest(AssetRequest request) {
        assetRequests.remove(request);
        logActivity("admin", "Delete", "Menghapus " + request.getTipe().toLowerCase(), request.getNoPermohonan(), request.getJenisAset());
    }

    public List<AssetRequest> getPermohonanAset() {
        return assetRequests.stream().filter(r -> "Permohonan".equals(r.getTipe())).toList();
    }

    public List<AssetRequest> getPengajuanAset() {
        return assetRequests.stream().filter(r -> "Pengajuan".equals(r.getTipe())).toList();
    }

    public List<Activity> getActivities() {
        return new ArrayList<>(activities);
    }

    public List<Activity> getRecentActivities(int limit) {
        return activities.stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .toList();
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

        assets.get(3).setDeleted(true); // contoh dihapus

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

