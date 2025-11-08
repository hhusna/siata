package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.Pegawai;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.PegawaiRepository;
import siata.siata.dto.MatriksAsetDTO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PegawaiService {

    @Autowired
    private PegawaiRepository pegawaiRepository;

    @Autowired
    private LogRiwayatService logRiwayatService;

    public List<Pegawai> getAllPegawai() {
        return pegawaiRepository.findAll();
    }

    // Method baru untuk FR 1.4
    public List<MatriksAsetDTO> getMatriksAset() {
        return pegawaiRepository.findAllWithAset().stream()
                .map(MatriksAsetDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<Pegawai> getPegawaiByNip(Long nip) {
        return pegawaiRepository.findById(nip);
    }

    public Pegawai savePegawai(Pegawai pegawai, Pegawai userPegawai) {
        boolean isNew = pegawai.getNip() == null || !pegawaiRepository.existsById(pegawai.getNip());
        Pegawai savedPegawai = pegawaiRepository.save(pegawai);

        String jenisLog = isNew ? "CREATE_PEGAWAI" : "UPDATE_PEGAWAI";
        String isiLog = (isNew ? "Membuat data pegawai baru: " : "Memperbarui data pegawai: ") + savedPegawai.getNama();
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, jenisLog, isiLog));

        return savedPegawai;
    }

    public void deletePegawai(Long nip, Pegawai userPegawai) {
        Pegawai pegawai = pegawaiRepository.findById(nip).orElseThrow(() -> new RuntimeException("Pegawai not found"));

        String isiLog = "Menghapus data pegawai: " + pegawai.getNama() + " (NIP: " + nip + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, "DELETE_PEGAWAI", isiLog));

        pegawaiRepository.deleteById(nip);
    }
}