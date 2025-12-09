package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import siata.siata.dto.DashboardStatsDTO;
import siata.siata.repository.AsetRepository;
import siata.siata.repository.PengajuanAsetRepository;
import siata.siata.repository.PenghapusanAsetRepository;
import siata.siata.repository.PermohonanAsetRepository;

import java.time.LocalDate;

@Service
public class DashboardService {

    @Autowired
    private AsetRepository asetRepository;

    @Autowired
    private PermohonanAsetRepository permohonanAsetRepository;

    @Autowired
    private PengajuanAsetRepository pengajuanAsetRepository;

    @Autowired
    private PenghapusanAsetRepository penghapusanAsetRepository;

    @Cacheable(value = "dashboardStats", unless = "#result == null")
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Optimize: Execute all counts in parallel batches
        stats.setTotalAset(asetRepository.count());
        
        // Aset siap dilelang: tua=1 (usia > 4 tahun, status=AKTIF, bukan mobil/motor) DAN apakahDihapus=1
        LocalDate empatTahunLalu = LocalDate.now().minusYears(4);
        stats.setAsetSiapDilelang(asetRepository.countAsetSiapDilelang(empatTahunLalu));
        
        // Rusak Berat: includes "Rusak Berat", "R. Berat", etc (case-insensitive)
        stats.setAsetRusakBerat(asetRepository.countByKondisiRusakBerat());

        long permohonanPending = permohonanAsetRepository.countByStatusPersetujuanIgnoreCase("Pending");
        long pengajuanPending = pengajuanAsetRepository.countByStatusPersetujuanIgnoreCase("Pending");
        stats.setPermohonanPending(permohonanPending);
        stats.setPengajuanPending(pengajuanPending);

        stats.setAsetAktif(asetRepository.countByStatusPemakaianIgnoreCase("Aktif"));
        stats.setAsetNonAktif(asetRepository.countByStatusPemakaianIgnoreCase("Non Aktif"));
        stats.setAsetDiajukanHapus(asetRepository.countByStatusPemakaianIgnoreCase("Diajukan Hapus"));
        stats.setTotalAsetDihapus(penghapusanAsetRepository.count());

        return stats;
    }
}