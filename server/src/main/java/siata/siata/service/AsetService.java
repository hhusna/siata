package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Aset> getAllAset() {
        return asetRepository.findAll();
    }

    public Optional<Aset> getAsetById(Long id) {
        return asetRepository.findById(id);
    }

    // MODIFIKASI METHOD INI
    public List<Aset> searchAset(String jenis, String status, String namaPegawai, String namaSubdir) {
        return asetRepository.searchAset(jenis, status, namaPegawai, namaSubdir);
    }

    public Aset saveAset(Aset aset, Pegawai userPegawai) {
        boolean isNew = aset.getIdAset() == null;
        Aset savedAset = asetRepository.save(aset);

        // FR 3.1: Logbook
        String jenisLog = isNew ? "CREATE_ASET" : "UPDATE_ASET";
        String isiLog = (isNew ? "Membuat aset baru: " : "Memperbarui aset: ") + savedAset.getKodeAset() + " (" + savedAset.getJenisAset() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, savedAset, jenisLog, isiLog));

        return savedAset;
    }

    public void tandaiUntukPenghapusan(Long id, Pegawai userPegawai) {
        Aset aset = asetRepository.findById(id).orElseThrow(() -> new RuntimeException("Aset not found"));

        // 1. Update status aset
        aset.setKondisi("Rusak Berat");
        aset.setStatusPemakaian("Diajukan Hapus");
        asetRepository.save(aset);

        // 2. Buat entri di tabel PenghapusanAset
        PenghapusanAset hapus = new PenghapusanAset();
        hapus.setAset(aset);
        hapus.setNamaAset(aset.getJenisAset() + " " + aset.getMerkAset());
        hapus.setJenisAset(aset.getJenisAset());
        hapus.setTanggalPerolehan(aset.getTanggalPerolehan());
        hapus.setHargaAset(aset.getHargaAset());
        hapus.setKondisi(aset.getKondisi());
        penghapusanAsetRepository.save(hapus);

        String isiLog = "Menandai aset untuk dihapus: " + aset.getKodeAset() + " (" + aset.getJenisAset() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, aset, "DELETE_ASET", isiLog));
    }

    // Otomatisasi Status Aset (dijalankan setiap hari jam 1 pagi)
    @Scheduled(cron = "0 0 1 * * ?")
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
    public void deleteAset(Long id, Pegawai userPegawai) {
        Aset aset = asetRepository.findById(id).orElseThrow(() -> new RuntimeException("Aset not found"));

        // Log penghapusan permanen
        String isiLog = "Menghapus aset PERMANEN: " + aset.getKodeAset() + " (" + aset.getJenisAset() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, aset, "HARD_DELETE_ASET", isiLog));

        asetRepository.deleteById(id);
    }
}