package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PermohonanAset;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.PermohonanAsetRepository;
import siata.siata.repository.PegawaiRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class PermohonanAsetService {

    @Autowired
    private PermohonanAsetRepository repository;

    @Autowired
    private LogRiwayatService logRiwayatService;

    @Autowired
    private PegawaiRepository pegawaiRepository;

    /**
     * Validasi apakah NIP pemohon terdaftar di unit yang sesuai
     * @return error message jika tidak valid, null jika valid
     */
    public String validatePemohonByUnit(String nipPengguna, String subdirektoratPengguna) {
        if (nipPengguna == null || subdirektoratPengguna == null) {
            return "NIP dan subdirektorat tidak boleh kosong";
        }
        
        // Cek apakah ada pegawai dengan NIP dan unit yang sesuai
        boolean exists = pegawaiRepository.findAll().stream()
            .anyMatch(p -> String.valueOf(p.getNip()).equals(nipPengguna.trim()) && 
                          p.getNamaSubdir().equals(subdirektoratPengguna));
        
        if (!exists) {
            return "NIP '" + nipPengguna + "' tidak terdaftar di subdirektorat " + subdirektoratPengguna;
        }
        
        return null; // Valid
    }

    @Cacheable("permohonanList")
    public List<PermohonanAset> getAll() {
        return repository.findAllWithPegawai();
    }

    public List<PermohonanAset> getAllByRole(String role) {
        List<PermohonanAset> all = repository.findAllWithPegawai();
        
        if ("TIM_MANAJEMEN_ASET".equals(role) || "DEV".equals(role) || "ADMIN".equals(role)) {
            return all;
        }

        return all.stream()
            .filter(p -> {
                String status = p.getStatusPersetujuan();
                if (status == null) return false;
                
                if ("PPBJ".equals(role)) {
                    // PPBJ sees Pending, and their own decisions
                    return "Pending".equalsIgnoreCase(status) || 
                           status.toUpperCase().contains("PPBJ");
                }
                
                if ("PPK".equals(role)) {
                    // PPK sees Approved by PPBJ, and their own decisions
                    return "Disetujui PPBJ".equalsIgnoreCase(status) || 
                           status.toUpperCase().contains("PPK");
                }
                
                if ("DIREKTUR".equals(role)) {
                    // Direktur sees Approved by PPK, and their own decisions
                    return "Disetujui PPK".equalsIgnoreCase(status) || 
                           status.toUpperCase().contains("DIREKTUR");
                }
                
                return false;
            })
            .toList();
    }

    public Optional<PermohonanAset> getById(Long id) {
        return repository.findById(id);
    }

    @CacheEvict(value = {"permohonanList", "approvalLogs"}, allEntries = true)
    public PermohonanAset save(PermohonanAset data, Pegawai userPegawai) {
        boolean isNew = data.getIdPermohonan() == null;

        // Status awal
        if (isNew) {
            data.setStatusPersetujuan("Pending"); // Status awal
            LocalDate now = LocalDate.now();
            String dateStr = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
            long count = repository.count() + 1;
            String kode = String.format("PRM-%s-%03d", dateStr, count);
            data.setKodePermohonan(kode);
        }

        PermohonanAset savedData = repository.save(data);

        String jenisLog = isNew ? "CREATE_PERMOHONAN" : "UPDATE_PERMOHONAN";
        String isiLog = (isNew ? "Membuat permohonan baru: " : "Memperbarui permohonan: ") + savedData.getJenisAset() + " (ID: " + savedData.getIdPermohonan() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, savedData, jenisLog, isiLog));

        return savedData;
    }

    @CacheEvict(value = {"permohonanList", "approvalLogs"}, allEntries = true)
    public PermohonanAset updateStatus(Long id, String status, String catatan, String lampiran, Pegawai userPegawai) {
        PermohonanAset data = repository.findById(id).orElseThrow(() -> new RuntimeException("Permohonan not found"));
        data.setStatusPersetujuan(status);
        PermohonanAset savedData = repository.save(data);

        String isiLog = "Memperbarui status permohonan (ID: " + id + ") menjadi: " + status;
        LogRiwayat log = new LogRiwayat(userPegawai, savedData, "UPDATE_STATUS_PERMOHONAN", isiLog);
        log.setCatatan(catatan);
        log.setLampiran(lampiran);
        logRiwayatService.saveLog(log);

        return savedData;
    }

    @CacheEvict(value = {"permohonanList", "approvalLogs"}, allEntries = true)
    public void delete(Long id, Pegawai userPegawai) {
        PermohonanAset data = repository.findById(id).orElseThrow(() -> new RuntimeException("Permohonan not found"));

        String isiLog = "Menghapus permohonan: " + data.getJenisAset() + " (ID: " + id + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, data, "DELETE_PERMOHONAN", isiLog));

        repository.deleteById(id);
    }
}