package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PengajuanAset;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.PengajuanAsetRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class PengajuanAsetService {

    @Autowired
    private PengajuanAsetRepository repository;

    @Autowired
    private LogRiwayatService logRiwayatService;


    public List<PengajuanAset> getAll() {
        return repository.findAll();
    }

    public Optional<PengajuanAset> getById(Long id) {
        return repository.findById(id);
    }

    public PengajuanAset save(PengajuanAset data, Pegawai userPegawai) {
        boolean isNew = data.getIdPengajuan() == null;

        if (isNew) {
            data.setStatusPersetujuan("Pending"); // Status awal
            LocalDate now = LocalDate.now();
            String dateStr = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
            long count = repository.count() + 1;
            String kode = String.format("PGJ-%s-%03d", dateStr, count);
            data.setKodePengajuan(kode);
        }

        PengajuanAset savedData = repository.save(data);

        String jenisLog = isNew ? "CREATE_PENGAJUAN" : "UPDATE_PENGAJUAN";
        String isiLog = (isNew ? "Membuat pengajuan baru: " : "Memperbarui pengajuan: ") + savedData.getJenisAset() + " (ID: " + savedData.getIdPengajuan() + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, savedData, jenisLog, isiLog));

        return savedData;
    }

    public PengajuanAset updateStatus(Long id, String status, Pegawai userPegawai) {
        PengajuanAset data = repository.findById(id).orElseThrow(() -> new RuntimeException("Pengajuan not found"));
        data.setStatusPersetujuan(status);
        PengajuanAset savedData = repository.save(data);

        String isiLog = "Memperbarui status pengajuan (ID: " + id + ") menjadi: " + status;
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, savedData, "UPDATE_STATUS_PENGAJUAN", isiLog));

        return savedData;
    }

    public void delete(Long id, Pegawai userPegawai) {
        PengajuanAset data = repository.findById(id).orElseThrow(() -> new RuntimeException("Pengajuan not found"));

        String isiLog = "Menghapus pengajuan: " + data.getJenisAset() + " (ID: " + id + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, data, "DELETE_PENGAJUAN", isiLog));

        repository.deleteById(id);
    }
}