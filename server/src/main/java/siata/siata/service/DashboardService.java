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
        
        // Aset siap dilelang: usia > 4 tahun DAN status = "Non Aktif"
        LocalDate empatTahunLalu = LocalDate.now().minusYears(4);
        stats.setAsetSiapDilelang(asetRepository.countAsetSiapDilelang(empatTahunLalu));
        
        stats.setAsetRusakBerat(asetRepository.countByKondisi("Rusak Berat"));

        long permohonanPending = permohonanAsetRepository.countByStatusPersetujuan("Pending");
        long pengajuanPending = pengajuanAsetRepository.countByStatusPersetujuan("Pending");
        stats.setPermohonanPending(permohonanPending);
        stats.setPengajuanPending(pengajuanPending);

        stats.setAsetAktif(asetRepository.countByStatusPemakaian("Aktif"));
        stats.setAsetNonAktif(asetRepository.countByStatusPemakaian("Non Aktif"));
        stats.setAsetDiajukanHapus(asetRepository.countByStatusPemakaian("Diajukan Hapus"));
        stats.setTotalAsetDihapus(penghapusanAsetRepository.count());

        return stats;
    }
}