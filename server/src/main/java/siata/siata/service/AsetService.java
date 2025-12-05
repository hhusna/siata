package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import siata.siata.entity.Aset;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PenghapusanAset;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.AsetRepository;
import siata.siata.repository.PenghapusanAsetRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AsetService {

    @Autowired
    private AsetRepository asetRepository;

    @Autowired
    private LogRiwayatService logRiwayatService;

    @Autowired
    private PenghapusanAsetRepository penghapusanAsetRepository;

    @Cacheable(value = "asetList", unless = "#result == null || #result.isEmpty()")
    public List<Aset> getAllAset() {
        return asetRepository.findAllWithPegawai();
    }

    public Optional<Aset> getAsetById(Long id) {
        return asetRepository.findById(id);
    }

    // MODIFIKASI METHOD INI
    @Cacheable(value = "asetSearch", key = "#jenis + '_' + #status + '_' + #namaPegawai + '_' + #namaSubdir", unless = "#result == null || #result.isEmpty()")
    public List<Aset> searchAset(String jenis, String status, String namaPegawai, String namaSubdir) {
        return asetRepository.searchAset(jenis, status, namaPegawai, namaSubdir);
    }

    @CacheEvict(value = {"asetList", "asetSearch", "dashboardStats"}, allEntries = true)
    public Aset saveAset(Aset aset, Pegawai userPegawai) {
        boolean isNew = aset.getIdAset() == null;
        Aset savedAset = asetRepository.save(aset);

        // FR 3.1: Logbook
        String jenisLog = isNew ? "CREATE_ASET" : "UPDATE_ASET";
        String isiLog = (isNew ? "Membuat aset baru: " : "Memperbarui aset: ") + savedAset.getKodeAset() + " (" + savedAset.getJenisAset() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, savedAset, jenisLog, isiLog));

        return savedAset;
    }

    @CacheEvict(value = {"asetList", "asetSearch", "dashboardStats"}, allEntries = true)
    public void tandaiUntukPenghapusan(Long id, Pegawai userPegawai) {
        Aset aset = asetRepository.findById(id).orElseThrow(() -> new RuntimeException("Aset not found"));

        // Validasi 1: Hanya aset dengan status "Non Aktif" yang bisa dihapus
        if (!"Non Aktif".equals(aset.getStatusPemakaian())) {
            throw new RuntimeException("Hanya aset dengan status Non Aktif yang dapat dihapus");
        }

        // Simpan kondisi dan status asli sebelum penghapusan
        String kondisiAsli = aset.getKondisi();
        String statusAsli = aset.getStatusPemakaian();

        // 1. Ubah status aset menjadi "Tandai Dihapus"
        aset.setStatusPemakaian("Tandai Dihapus");
        asetRepository.save(aset);

        // 2. Buat entri di tabel PenghapusanAset dengan kondisi asli
        PenghapusanAset hapus = new PenghapusanAset();
        hapus.setAset(aset);
        hapus.setNamaAset(aset.getJenisAset() + " " + aset.getMerkAset());
        hapus.setJenisAset(aset.getJenisAset());
        hapus.setTanggalPerolehan(aset.getTanggalPerolehan());
        hapus.setHargaAset(aset.getHargaAset());
        hapus.setKondisi(kondisiAsli); // Gunakan kondisi asli
        penghapusanAsetRepository.save(hapus);

        // 3. Log penghapusan
        String isiLog = "Menandai aset untuk dihapus: " + aset.getKodeAset() + " (" + aset.getJenisAset() + ") - Status awal: " + statusAsli + ", Kondisi: " + kondisiAsli + " -> Status baru: Tandai Dihapus";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, aset, "DELETE_ASET", isiLog));
    }

    // Otomatisasi Status Aset (dijalankan setiap hari jam 1 pagi)
    @Scheduled(cron = "0 0 1 * * ?")
    @CacheEvict(value = {"asetList", "asetSearch", "dashboardStats"}, allEntries = true)
    public void updateStatusAsetSiapLelang() {
        LocalDate tanggalBatas = LocalDate.now().minusYears(4);
        List<Aset> asetUntukDilelang = asetRepository.findAsetSiapLelang(tanggalBatas);

        for (Aset aset : asetUntukDilelang) {
            aset.setStatusPemakaian("Siap Dilelang");
            asetRepository.save(aset);

            // Log otomatisasi (pegawai = null karena ini sistem)
            String isiLog = "Sistem otomatis mengubah status aset " + aset.getKodeAset() + " menjadi Siap Dilelang.";
            logRiwayatService.saveLog(new LogRiwayat(null, aset, "AUTO_LELANG", isiLog));
        }
    }

    // Ini adalah HAPUS permanen (jika diperlukan admin)
    @CacheEvict(value = {"asetList", "asetSearch", "dashboardStats"}, allEntries = true)
    public void deleteAset(Long id, Pegawai userPegawai) {
        Aset aset = asetRepository.findById(id).orElseThrow(() -> new RuntimeException("Aset not found"));

        // Log penghapusan permanen
        String isiLog = "Menghapus aset PERMANEN: " + aset.getKodeAset() + " (" + aset.getJenisAset() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, aset, "HARD_DELETE_ASET", isiLog));

        asetRepository.deleteById(id);
    }

    /**
     * Mencari aset duplikat berdasarkan semua field yang sama persis
     * (kecuali ID dan relasi)
     */
    public List<Aset> findDuplicates() {
        List<Aset> allAsets = asetRepository.findAll();
        List<Aset> duplicates = new java.util.ArrayList<>();
        
        for (int i = 0; i < allAsets.size(); i++) {
            Aset aset1 = allAsets.get(i);
            for (int j = i + 1; j < allAsets.size(); j++) {
                Aset aset2 = allAsets.get(j);
                if (areAssetsDuplicate(aset1, aset2)) {
                    if (!duplicates.contains(aset2)) {
                        duplicates.add(aset2); // Tambahkan duplikat (yang kedua)
                    }
                }
            }
        }
        
        return duplicates;
    }

    /**
     * Membersihkan aset duplikat
     * Strategi: Simpan yang pertama, hapus yang duplikat
     */
    public int cleanDuplicates(Pegawai userPegawai) {
        List<Aset> allAsets = asetRepository.findAll();
        List<Long> idsToDelete = new java.util.ArrayList<>();
        java.util.Set<Integer> processedIndices = new java.util.HashSet<>();
        
        for (int i = 0; i < allAsets.size(); i++) {
            if (processedIndices.contains(i)) continue;
            
            Aset aset1 = allAsets.get(i);
            for (int j = i + 1; j < allAsets.size(); j++) {
                if (processedIndices.contains(j)) continue;
                
                Aset aset2 = allAsets.get(j);
                if (areAssetsDuplicate(aset1, aset2)) {
                    // Tandai yang kedua untuk dihapus
                    idsToDelete.add(aset2.getIdAset());
                    processedIndices.add(j);
                }
            }
        }
        
        // Hapus semua duplikat
        for (Long id : idsToDelete) {
            try {
                Aset aset = asetRepository.findById(id).orElse(null);
                if (aset != null) {
                    String isiLog = "Membersihkan aset duplikat: " + aset.getKodeAset() + " (" + aset.getJenisAset() + ")";
                    logRiwayatService.saveLog(new LogRiwayat(userPegawai, aset, "CLEAN_DUPLICATE", isiLog));
                    asetRepository.deleteById(id);
                }
            } catch (Exception e) {
                System.err.println("Error deleting duplicate asset " + id + ": " + e.getMessage());
            }
        }
        
        return idsToDelete.size();
    }

    /**
     * Helper method untuk cek apakah 2 aset duplikat
     * Membandingkan semua field kecuali ID
     */
    private boolean areAssetsDuplicate(Aset a1, Aset a2) {
        if (a1.getIdAset().equals(a2.getIdAset())) return false; // Bukan duplikat jika ID sama
        
        return java.util.Objects.equals(a1.getKodeAset(), a2.getKodeAset()) &&
               java.util.Objects.equals(a1.getJenisAset(), a2.getJenisAset()) &&
               java.util.Objects.equals(a1.getMerkAset(), a2.getMerkAset()) &&
               java.util.Objects.equals(a1.getTanggalPerolehan(), a2.getTanggalPerolehan()) &&
               java.util.Objects.equals(a1.getHargaAset(), a2.getHargaAset()) &&
               java.util.Objects.equals(a1.getKondisi(), a2.getKondisi()) &&
               java.util.Objects.equals(a1.getStatusPemakaian(), a2.getStatusPemakaian()) &&
               java.util.Objects.equals(a1.getSubdirektorat(), a2.getSubdirektorat()) &&
               // Bandingkan pegawai berdasarkan NIP
               java.util.Objects.equals(
                   a1.getPegawai() != null ? a1.getPegawai().getNip() : null,
                   a2.getPegawai() != null ? a2.getPegawai().getNip() : null
               );
    }

    @CacheEvict(value = {"asetList", "asetSearch", "dashboardStats"}, allEntries = true)
    public int batchDeleteAset(List<Long> idList, Pegawai userPegawai) {
        int deletedCount = 0;
        StringBuilder deletedCodes = new StringBuilder();
        
        for (Long id : idList) {
            try {
                Optional<Aset> asetOpt = asetRepository.findById(id);
                if (asetOpt.isPresent()) {
                    Aset aset = asetOpt.get();
                    if (deletedCodes.length() > 0) deletedCodes.append(", ");
                    deletedCodes.append(aset.getKodeAset());
                    asetRepository.deleteById(id);
                    deletedCount++;
                }
            } catch (Exception e) {
                System.err.println("Failed to delete Aset ID: " + id + " - " + e.getMessage());
            }
        }
        
        if (deletedCount > 0) {
            String isiLog = "Hapus batch " + deletedCount + " aset: " + (deletedCodes.length() > 100 
                ? deletedCodes.substring(0, 100) + "..." 
                : deletedCodes.toString());
            logRiwayatService.saveLog(new LogRiwayat(userPegawai, (Aset) null, "BATCH_DELETE_ASET", isiLog));
        }
        
        return deletedCount;
    }
}