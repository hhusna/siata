package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PermohonanAset;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.PermohonanAsetRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class PermohonanAsetService {

    @Autowired
    private PermohonanAsetRepository repository;

    @Autowired
    private LogRiwayatService logRiwayatService;

    public List<PermohonanAset> getAll() {
        return repository.findAll();
    }

    public Optional<PermohonanAset> getById(Long id) {
        return repository.findById(id);
    }

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

    public PermohonanAset updateStatus(Long id, String status, Pegawai userPegawai) {
        PermohonanAset data = repository.findById(id).orElseThrow(() -> new RuntimeException("Permohonan not found"));
        data.setStatusPersetujuan(status);
        PermohonanAset savedData = repository.save(data);

        String isiLog = "Memperbarui status permohonan (ID: " + id + ") menjadi: " + status;
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, savedData, "UPDATE_STATUS_PERMOHONAN", isiLog));

        return savedData;
    }

    public void delete(Long id, Pegawai userPegawai) {
        PermohonanAset data = repository.findById(id).orElseThrow(() -> new RuntimeException("Permohonan not found"));

        String isiLog = "Menghapus permohonan: " + data.getJenisAset() + " (ID: " + id + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, data, "DELETE_PERMOHONAN", isiLog));

        repository.deleteById(id);
    }
}