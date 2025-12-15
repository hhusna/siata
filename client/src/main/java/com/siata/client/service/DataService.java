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
    private final AtomicInteger permohonanCounter = new AtomicInteger(1);
    private final AtomicInteger pengajuanCounter = new AtomicInteger(1);

    private DataService() {
        
    }

    public static DataService getInstance() {
        return instance;
    }

    public String generatePermohonanNumber() {
        LocalDate now = LocalDate.now();
        String dateStr = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        return "PRM-" + dateStr + "-" + String.format("%03d", permohonanCounter.getAndIncrement());
    }

    public String generatePengajuanNumber() {
        LocalDate now = LocalDate.now();
        String dateStr = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        return "PGJ-" + dateStr + "-" + String.format("%03d", pengajuanCounter.getAndIncrement());
    }

    // Cache for Assets
    private List<Asset> cachedAssets = null;
    private long assetsCacheTimestamp = 0;
    private static final long ASSETS_CACHE_TTL = 30000; // 30 seconds

    // Cache for All Assets Including Deleted (for recapitulation)
    private List<Asset> cachedAllAssetsIncludingDeleted = null;
    private long allAssetsCacheTimestamp = 0;

    // Cache for Employees
    private List<Employee> cachedEmployeesList = null;
    private long employeesCacheTimestamp = 0;
    private static final long EMPLOYEES_CACHE_TTL = 30000; // 30 seconds

    public List<Asset> getAssets() {
        return getAssets(false);
    }

    public List<Asset> getAssets(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && cachedAssets != null && (now - assetsCacheTimestamp < ASSETS_CACHE_TTL)) {
            return new ArrayList<>(cachedAssets);
        }

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
            assetValue.setNoAset(assetDto.getNoAset()); // Map No Aset
            assetValue.setDipakai(assetDto.getDipakai()); // Map Dipakai
            if (assetDto.getPegawaiDto() != null) {
                assetValue.setKeterangan(Long.toString(assetDto.getPegawaiDto().getNip()));
                assetValue.setSubdir(assetDto.getPegawaiDto().getNamaSubdir());
            } else {
                assetValue.setKeterangan("-");
                assetValue.setSubdir(assetDto.getSubdirektorat());
            }

            if (!(assetValue.getStatus().equals("Tandai Dihapus"))) {
                listAsset.add(assetValue);
            }
        }

        cachedAssets = listAsset;
        assetsCacheTimestamp = now;

        return new ArrayList<>(listAsset);
    }

    public void clearAssetCache() {
        cachedAssets = null;
        assetsCacheTimestamp = 0;
        // Also clear all-assets cache since they share the same API data
        cachedAllAssetsIncludingDeleted = null;
        allAssetsCacheTimestamp = 0;
    }

    /**
     * Mendapatkan semua aset TERMASUK yang berstatus "Tandai Dihapus"
     * Digunakan untuk perhitungan rekapitulasi yang akurat
     * Now uses caching for better performance
     */
    public List<Asset> getAllAssetsIncludingDeleted() {
        return getAllAssetsIncludingDeleted(false);
    }

    public List<Asset> getAllAssetsIncludingDeleted(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && cachedAllAssetsIncludingDeleted != null && (now - allAssetsCacheTimestamp < ASSETS_CACHE_TTL)) {
            return new ArrayList<>(cachedAllAssetsIncludingDeleted);
        }

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
            assetValue.setNoAset(assetDto.getNoAset()); // Map No Aset
            assetValue.setDipakai(assetDto.getDipakai()); // Map Dipakai
            if (assetDto.getPegawaiDto() != null) {
                assetValue.setKeterangan(Long.toString(assetDto.getPegawaiDto().getNip()));
                assetValue.setSubdir(assetDto.getPegawaiDto().getNamaSubdir());
            } else {
                assetValue.setKeterangan("-");
                assetValue.setSubdir(assetDto.getSubdirektorat());
            }
            
            // Tambahkan SEMUA aset, termasuk yang "Tandai Dihapus"
            listAsset.add(assetValue);
        }

        cachedAllAssetsIncludingDeleted = listAsset;
        allAssetsCacheTimestamp = now;

        return new ArrayList<>(listAsset);
    }

    public int getAssetBySubdir(String Subdir) {
        List<Asset> assetList = getAssets();
        int count = 0;
        for (Asset asset : assetList) {
            if (asset.getSubdir().equals(Subdir)) {
                count++;
            }
        }

        return count;
    }

    public int getAssetByJenis(String jenis) {
        List<Asset> assetList = getAssets();
        int count = 0;
        for (Asset asset : assetList) {
            // Case-insensitive comparison
            if (asset.getJenisAset() != null && asset.getJenisAset().equalsIgnoreCase(jenis)) {
                count++;
            }
        }
        System.out.println("DataService: " + count);
        return count;
    }

    public boolean addAsset(Asset asset) {
        assets.add(asset);
        AssetDtoForRequest assetToDto = new AssetDtoForRequest();

        // 1. Selalu set Subdirektorat dari input form (Dropdown)
        assetToDto.setSubdirektorat(asset.getSubdir());

        // 2. Cek validasi NIP (Keterangan)
        String nipInput = asset.getKeterangan();
        if (nipInput != null && !nipInput.trim().isEmpty() && !"0".equals(nipInput.trim()) && isNumeric(nipInput)) {
            // Jika ada input NIP valid, ambil data pegawai
            try {
                PegawaiDto pegawaiDto = pegawaiApi.getPegawaiByNip(Long.parseLong(nipInput));
                if (pegawaiDto != null && pegawaiDto.getNama() != null) {
                    assetToDto.setPegawaiDto(pegawaiDto);
                    System.out.println("DataService: Pegawai ditemukan -> " + pegawaiDto.getNama());
                } else {
                    // Jika NIP diinput tapi tidak ditemukan di DB, biarkan null (aset Subdir)
                    System.out.println("DataService: Pegawai tidak ditemukan di DB, set null.");
                    assetToDto.setPegawaiDto(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                assetToDto.setPegawaiDto(null);
            }
        } else {
            // Jika kosong atau bukan angka, berarti aset milik Subdir (tanpa pemegang)
            // System.out.println("DataService: Input pemegang kosong/teks, set pegawai null. Input: " + nipInput);
            assetToDto.setPegawaiDto(null);
        }

        // 3. Set field lainnya
        assetToDto.setKodeAset(asset.getKodeAset());
        assetToDto.setJenisAset(asset.getJenisAset());
        assetToDto.setMerkAset(asset.getMerkBarang());
        assetToDto.setTanggalPerolehan(asset.getTanggalPerolehan());
        assetToDto.setHargaAset(asset.getNilaiRupiah());
        assetToDto.setKondisi(asset.getKondisi());
        assetToDto.setStatusPemakaian(asset.getStatus());
        assetToDto.setDipakai(asset.getDipakai()); // Map Dipakai
        
        // Handle No Aset
        assetToDto.setNoAset(asset.getNoAset());

        // 4. Kirim ke API
        long newId = assetApi.tambahAsset(assetToDto);
        if (newId != 0) {
            asset.setIdAset(newId); // Update ID local asset
            clearAssetCache(); // Invalidate cache
            logActivity("admin", "Create", "Menambahkan aset baru", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
            return true;
        }
        return false;
    }

    public int batchAddAssets(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) return 0;
        
        // Pre-fetch all employees to avoid N+1 API calls logic
        PegawaiDto[] allPegawaiDtos = pegawaiApi.getPegawai();
        java.util.Map<String, PegawaiDto> nipToPegawaiMap = new java.util.HashMap<>();
        if (allPegawaiDtos != null) {
            for (PegawaiDto p : allPegawaiDtos) {
                nipToPegawaiMap.put(String.valueOf(p.getNip()), p);
            }
        }

        List<AssetDtoForRequest> batchPayload = new ArrayList<>();

        for (Asset asset : assets) {
             AssetDtoForRequest assetToDto = new AssetDtoForRequest();
             
             // Mapping logic
             assetToDto.setSubdirektorat(asset.getSubdir());
             
             String nipInput = asset.getKeterangan();
             if (nipInput != null && !nipInput.trim().isEmpty() && !"0".equals(nipInput.trim()) && isNumeric(nipInput)) {
                 PegawaiDto p = nipToPegawaiMap.get(nipInput.trim());
                 if (p != null) {
                     assetToDto.setPegawaiDto(p);
                 } else {
                     assetToDto.setPegawaiDto(null);
                 }
             } else {
                 assetToDto.setPegawaiDto(null);
             }

             assetToDto.setKodeAset(asset.getKodeAset());
             assetToDto.setJenisAset(asset.getJenisAset());
             assetToDto.setMerkAset(asset.getMerkBarang());
             assetToDto.setTanggalPerolehan(asset.getTanggalPerolehan());
             assetToDto.setHargaAset(asset.getNilaiRupiah());
             assetToDto.setKondisi(asset.getKondisi());
             assetToDto.setStatusPemakaian(asset.getStatus());
             assetToDto.setDipakai(asset.getDipakai());
             assetToDto.setNoAset(asset.getNoAset());
             
             batchPayload.add(assetToDto);
        }

        int count = assetApi.batchAddAsset(batchPayload);
        if (count > 0) {
            clearAssetCache();
            logActivity("admin", "Create", "Import Batch " + count + " aset", "-", "Import Excel");
        }
        return count;
    }

    // Helper kecil untuk cek angka
    private boolean isNumeric(String str) {
        if (str == null) return false;
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Validasi NIP: Cek apakah NIP ada di database DAN sesuai dengan subdirektorat yang dipilih
     * @param nip NIP yang akan divalidasi
     * @param expectedSubdir Subdirektorat yang diharapkan
     * @return true jika NIP valid dan sesuai subdir, false jika tidak
     */
    public boolean validateNipInSubdir(String nip, String expectedSubdir) {
        if (nip == null || nip.trim().isEmpty() || expectedSubdir == null || expectedSubdir.trim().isEmpty()) {
            return false;
        }
        
        if (!isNumeric(nip)) {
            return false;
        }
        
        try {
            PegawaiDto pegawaiDto = pegawaiApi.getPegawaiByNip(Long.parseLong(nip));
            if (pegawaiDto == null || pegawaiDto.getNama() == null) {
                // NIP tidak ditemukan di database
                return false;
            }
            
            // Cek apakah subdir pegawai sesuai dengan yang dipilih
            String pegawaiSubdir = pegawaiDto.getNamaSubdir();
            if (pegawaiSubdir == null || !pegawaiSubdir.equals(expectedSubdir)) {
                // Subdir tidak sesuai
                return false;
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validasi apakah NIP sudah ada di database (untuk tambah pegawai baru)
     * @param nip NIP yang akan dicek
     * @return true jika NIP sudah ada, false jika belum
     */
    public boolean isNipExists(String nip) {
        if (nip == null || nip.trim().isEmpty() || !isNumeric(nip) || nip.length() != 18) {
            return false;
        }
        
        try {
            PegawaiDto pegawaiDto = pegawaiApi.getPegawaiByNip(Long.parseLong(nip));
            return pegawaiDto != null && pegawaiDto.getNama() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAsset(Asset asset) {
        AssetDto assetDto = new AssetDto();
        assetDto.setIdAset(asset.getIdAset());
        System.out.println("======= ID ASET: "+assetDto.getIdAset());
        
        // 1. Set Subdirektorat dari input form
        assetDto.setSubdirektorat(asset.getSubdir());
        
        // 2. Handle NIP (sama seperti addAsset)
        String nipInput = asset.getKeterangan();
        if (nipInput != null && !nipInput.trim().isEmpty() && !"0".equals(nipInput.trim()) && isNumeric(nipInput)) {
            try {
                PegawaiDto pegawaiDto = pegawaiApi.getPegawaiByNip(Long.parseLong(nipInput));
                if (pegawaiDto != null && pegawaiDto.getNama() != null) {
                    assetDto.setPegawaiDto(pegawaiDto);
                    System.out.println("DataService: Pegawai ditemukan saat update -> " + pegawaiDto.getNama());
                } else {
                    System.out.println("DataService: Pegawai tidak ditemukan saat update, set null.");
                    assetDto.setPegawaiDto(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                assetDto.setPegawaiDto(null);
            }
        } else {
            System.out.println("DataService: Input pemegang kosong/teks saat update, set pegawai null.");
            assetDto.setPegawaiDto(null);
        }
        
        // 3. Set field lainnya (termasuk kodeAset yang bisa diedit)
        assetDto.setKodeAset(asset.getKodeAset());
        assetDto.setJenisAset(asset.getJenisAset());
        assetDto.setMerkAset(asset.getMerkBarang());
        assetDto.setTanggalPerolehan(asset.getTanggalPerolehan());
        assetDto.setHargaAset(asset.getNilaiRupiah());
        assetDto.setKondisi(asset.getKondisi());
        assetDto.setStatusPemakaian(asset.getStatus());
        assetDto.setDipakai(asset.getDipakai()); // Map Dipakai
        
        // Handle No Aset update
        assetDto.setNoAset(asset.getNoAset());
        
        boolean success = assetApi.putAsset(assetDto);
        if (success) {
            clearAssetCache(); // Invalidate cache
            logActivity("admin", "Update", "Memperbarui aset", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
            return true;
        }
        return false;
    }

    public void deleteAsset(Asset asset) {
        // Status dan kondisi tetap seperti sebelumnya (tidak diubah)
        asset.setDeleted(true);
        // TIDAK mengubah status: tetap "Non Aktif"
        // TIDAK mengubah kondisi: tetap sesuai kondisi asli
        assetApi.deleteAssetById(asset.getIdAset());
        clearAssetCache(); // Invalidate cache
        logActivity("admin", "Delete", "Menghapus aset", "Aset #" + asset.getKodeAset(), asset.getNamaAset());
    }

    public int batchDeleteAssets(List<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) return 0;
        
        int result = assetApi.batchDeleteAset(assetIds);
        if (result >= 0) {
            clearAssetCache(); // Invalidate cache
            logActivity("admin", "Delete", "Menghapus " + result + " aset", "-", "Bulk Delete");
        }
        return result;
    }

    public void removeAsset(Asset asset) {
        assets.remove(asset);
        clearAssetCache(); // Invalidate cache
    }

    public List<Asset> getDeletedAssets() {
        List<Asset> listAsset = new ArrayList<>();
        AssetDto[] apiResult = assetApi.getDeletedAsset();
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
            assetValue.setDeleted(true);

            if (assetDto.getPegawaiDto() != null) {
                assetValue.setKeterangan(Long.toString(assetDto.getPegawaiDto().getNip()));
                assetValue.setSubdir(assetDto.getPegawaiDto().getNamaSubdir());
            } else {
                assetValue.setKeterangan("-");
                assetValue.setSubdir(assetDto.getSubdirektorat());
            }

            listAsset.add(assetValue);
        }

        return listAsset;
    }

    public List<Employee> getEmployees() {
        return getEmployees(false);
    }

    public List<Employee> getEmployees(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && cachedEmployeesList != null && (now - employeesCacheTimestamp < EMPLOYEES_CACHE_TTL)) {
            return new ArrayList<>(cachedEmployeesList);
        }

        PegawaiDto[] pegawaiDto = pegawaiApi.getPegawai();
        List<Employee> employeeList = new ArrayList<>();
        for (PegawaiDto dto : pegawaiDto) {
            // Constructor dengan status: Employee(nip, nama, unit, status)
            String status = dto.getStatus() != null ? dto.getStatus() : "AKTIF";
            Employee emp = new Employee(Long.toString(dto.getNip()), dto.getNama(), dto.getNamaSubdir(), status);
            emp.setPpnpn(dto.getIsPpnpn() != null && dto.getIsPpnpn());
            employeeList.add(emp);
        }

        cachedEmployeesList = employeeList;
        employeesCacheTimestamp = now;

        return new ArrayList<>(employeeList);
    }

    public void clearEmployeeCache() {
        cachedEmployeesList = null;
        employeesCacheTimestamp = 0;
    }

    /**
     * Clears all caches. Call this after bulk operations like import.
     */
    public void clearAllCaches() {
        clearAssetCache();
        clearEmployeeCache();
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

        for (LogDto log : logDtos) {
            if (log.getPegawaiDto().getNama().equals(LoginSession.getPegawaiDto().getNama())) {
                break;
            }
        }

        if ("Permohonan".equals(request.getTipe())) {
            PermohonanDto permohonanDto = new PermohonanDto();
            // Kode permohonan akan di-generate otomatis oleh server
            permohonanDto.setPegawaiDto(LoginSession.getPegawaiDto());
            
            // Set NIP dan Subdirektorat dari input user (previously mapped to pemohon/unit in model)
            permohonanDto.setNipPengguna(request.getPemohon());
            permohonanDto.setSubdirektoratPengguna(request.getUnit());

            permohonanDto.setJenisAset(request.getJenisAset());
            permohonanDto.setJumlah(request.getJumlah());
            permohonanDto.setDeskripsi(request.getDeskripsi());
            permohonanDto.setTujuanPenggunaan(request.getTujuanPenggunaan());
            // Prioritas removed
            permohonanDto.setTimestamp(LocalDate.now());
            permohonanApi.createPermohonan(permohonanDto);
        } else {
            PengajuanDto pengajuanDto = new PengajuanDto();
            // Kode pengajuan akan di-generate otomatis oleh server
            pengajuanDto.setPegawaiDto(LoginSession.getPegawaiDto());
            
            // request.getPemohon() now contains NIP, we need to look up the name
            String nipOrName = request.getPemohon();
            String namaPengaju = nipOrName; // Default to input value
            
            // Try to look up employee name from NIP
            if (nipOrName != null && !nipOrName.trim().isEmpty()) {
                try {
                    PegawaiDto pegawai = pegawaiApi.getPegawaiByNip(Long.parseLong(nipOrName.trim()));
                    if (pegawai != null && pegawai.getNama() != null) {
                        namaPengaju = pegawai.getNama();
                    }
                } catch (NumberFormatException e) {
                    // Input is not a number (might already be a name), use as-is
                    namaPengaju = nipOrName;
                }
            }
            
            pengajuanDto.setNamaPengaju(namaPengaju);
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
            permohonanApi.patchStatus(request.getId(), newStatus, null, null);
        } else {
            pengajuanApi.patchStatus(request.getId(), newStatus, null, null);
        }
        String actionType = newStatus.contains("Disetujui") ? "Approve" : "Reject";
        logActivity(approver, actionType,
                newStatus.contains("Disetujui") ? "Menyetujui permohonan aset" : "Menolak permohonan aset",
                request.getNoPermohonan(),
                request.getDeskripsi() != null ? request.getDeskripsi() : request.getJenisAset());
    }

    public void updateAssetRequestStatus(Long id, String tipe, String newStatus, String catatan, String lampiran, java.util.function.Consumer<Boolean> callback) {
        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    boolean success;
                    if ("Permohonan".equals(tipe)) {
                        success = permohonanApi.patchStatus(id, newStatus, catatan, lampiran);
                    } else {
                        success = pengajuanApi.patchStatus(id, newStatus, catatan, lampiran);
                    }
                    
                    if (success) {
                        // Log activity
                        String approver = LoginSession.getPegawaiDto() != null ? LoginSession.getPegawaiDto().getNama() : "Admin";
                        String actionType = newStatus.contains("Disetujui") ? "Approve" : "Reject";
                        // Note: We don't have full request details here easily for logging without fetching, 
                        // but we can log basic info or fetch. For now, basic log.
                        logActivity(approver, actionType, 
                                actionType.equals("Approve") ? "Menyetujui request" : "Menolak request", 
                                tipe + " #" + id, 
                                "Status: " + newStatus);
                    }
                    return success;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            if (callback != null) callback.accept(task.getValue());
        });
        
        task.setOnFailed(e -> {
            if (callback != null) callback.accept(false);
        });
        
        new Thread(task).start();
    }

    public void updateAssetRequest(AssetRequest request) {
        if ("Permohonan".equals(request.getTipe())) {
            // Logika untuk permohonan
            PermohonanDto dto = new PermohonanDto();
            dto.setIdPermohonan(request.getId());
            dto.setKodePermohonan(request.getNoPermohonan());
            
            // Update NIP dan Subdir
            dto.setNipPengguna(request.getPemohon());
            dto.setSubdirektoratPengguna(request.getUnit());

            dto.setJenisAset(request.getJenisAset());
            dto.setJumlah(request.getJumlah());
            dto.setDeskripsi(request.getDeskripsi());
            dto.setTujuanPenggunaan(request.getTujuanPenggunaan());
            // Prioritas removed
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
        
        // Fetch all employees for name lookup to avoid N+1 issues
        List<Employee> allEmployees = getEmployees();
        java.util.Map<String, String> nipToNameMap = new java.util.HashMap<>();
        for (Employee e : allEmployees) {
            nipToNameMap.put(e.getNip(), e.getNamaLengkap());
        }

        for (PermohonanDto dto : permohonanDtos) {
            AssetRequest assetRequest = new AssetRequest();
            assetRequest.setId(dto.getIdPermohonan());
            assetRequest.setNoPermohonan(dto.getKodePermohonan());
            assetRequest.setTanggal(dto.getTimestamp());
            
            // Lookup Name from NIP for display
            String nip = dto.getNipPengguna();
            String beneficiaryName = nip; // default to NIP
            if (nip != null) {
                String foundName = nipToNameMap.get(nip.trim());
                if (foundName != null) {
                    beneficiaryName = foundName;
                }
            }
            assetRequest.setPemohon(beneficiaryName);
            
            assetRequest.setUnit(dto.getSubdirektoratPengguna());
            assetRequest.setJenisAset(dto.getJenisAset());
            assetRequest.setJumlah(dto.getJumlah());
            // Prioritas removed from model so we skip setting it
            assetRequest.setTipe("Permohonan");
            assetRequest.setDeskripsi(dto.getDeskripsi());
            assetRequest.setTujuanPenggunaan(dto.getTujuanPenggunaan());
            assetRequest.setStatus(dto.getStatusPersetujuan());
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
            assetRequest.setPemohon(dto.getNamaPengaju());
            assetRequest.setUnit(dto.getUnit());
            assetRequest.setJenisAset(dto.getJenisAset());
            assetRequest.setJumlah(dto.getJumlah());
            assetRequest.setPrioritas(dto.getPrioritas());
            assetRequest.setTipe("Pengajuan");
            assetRequest.setDeskripsi(dto.getDeskripsi());
            assetRequest.setTujuanPenggunaan(dto.getTujuanPenggunaan());
            assetRequest.setStatus(dto.getStatusPersetujuan());
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

    /**
     * Get recent activities with API-side limit for performance
     * @param limit Number of activities to fetch from server
     */
    public List<Activity> getRecentActivities(int limit) {
        List<Activity> activityList = new ArrayList<>();
        // Use API with limit for better performance
        LogDto[] logDtos = logApi.getLogWithLimit(limit);

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
    
    /**
     * Get activities within a date range for export
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     */
    public List<Activity> getActivitiesByDateRange(LocalDate fromDate, LocalDate toDate) {
        List<Activity> activityList = new ArrayList<>();
        // Use API with date range
        LogDto[] logDtos = logApi.getLogByDateRange(fromDate, toDate);

        if (logDtos != null) {
            for (LogDto dto : logDtos) {
                Activity activity = mapLogDtoToActivity(dto);
                activityList.add(activity);
            }
        }

        return activityList.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
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
}