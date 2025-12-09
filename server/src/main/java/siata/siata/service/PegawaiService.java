package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.Pegawai;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.PegawaiRepository;
import siata.siata.repository.PenghapusanAsetRepository;
import siata.siata.dto.MatriksAsetDTO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PegawaiService {

    @Autowired
    private PegawaiRepository pegawaiRepository;

    @Autowired
    private PenghapusanAsetRepository penghapusanAsetRepository;

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
        // If NIP is null (PPNPN), generate a unique ID based on timestamp
        if (pegawai.getNip() == null) {
            // Validation: Check if Name + Subdir already exists for new PPNPN
            if (pegawaiRepository.existsByNamaIgnoreCaseAndNamaSubdirIgnoreCase(pegawai.getNama(), pegawai.getNamaSubdir())) {
                throw new IllegalArgumentException("Pegawai dengan nama '" + pegawai.getNama() + "' di unit '" + pegawai.getNamaSubdir() + "' sudah terdaftar.");
            }
            pegawai.setNip(System.currentTimeMillis());
            pegawai.setIsPpnpn(true);
        } else {
             // Validation for new ASN (if NIP is provided but record doesn't exist yet)
             if (!pegawaiRepository.existsById(pegawai.getNip())) {
                 if (pegawaiRepository.existsByNamaIgnoreCaseAndNamaSubdirIgnoreCase(pegawai.getNama(), pegawai.getNamaSubdir())) {
                     throw new IllegalArgumentException("Pegawai dengan nama '" + pegawai.getNama() + "' di unit '" + pegawai.getNamaSubdir() + "' sudah terdaftar.");
                 }
             }
        }

        boolean isNew = !pegawaiRepository.existsById(pegawai.getNip());
        Pegawai savedPegawai = pegawaiRepository.save(pegawai);

        String jenisLog = isNew ? "CREATE_PEGAWAI" : "UPDATE_PEGAWAI";
        String isiLog = (isNew ? "Membuat data pegawai baru: " : "Memperbarui data pegawai: ") + savedPegawai.getNama();
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, jenisLog, isiLog));

        return savedPegawai;
    }

    public void deletePegawai(Long nip, Pegawai userPegawai) {
        Pegawai pegawai = pegawaiRepository.findById(nip).orElseThrow(() -> new RuntimeException("Pegawai not found"));

        // Delete any PenghapusanAset entries for assets owned by this pegawai first
        penghapusanAsetRepository.deleteByPegawaiNip(nip);

        String isiLog = "Menghapus data pegawai: " + pegawai.getNama() + " (NIP: " + nip + ")";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, "DELETE_PEGAWAI", isiLog));

        pegawaiRepository.deleteById(nip);
    }

    public List<Pegawai> batchSavePegawai(List<Pegawai> pegawaiList, Pegawai userPegawai) {
        List<Pegawai> savedList = new java.util.ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        
        for (Pegawai pegawai : pegawaiList) {
            boolean isNew = pegawai.getNip() == null || !pegawaiRepository.existsById(pegawai.getNip());
            
            // Check for duplicate Name + Subdir for new records
            if (isNew) {
                if (pegawaiRepository.existsByNamaIgnoreCaseAndNamaSubdirIgnoreCase(pegawai.getNama(), pegawai.getNamaSubdir())) {
                    // Skip duplicate
                    System.out.println("Batch import: Skipping duplicate pegawai " + pegawai.getNama() + " in " + pegawai.getNamaSubdir());
                    continue;
                }
            }
            
            // Handle PPNPN ID generation if needed (logic similar to savePegawai)
            if (pegawai.getNip() == null) {
                pegawai.setNip(System.currentTimeMillis());
                pegawai.setIsPpnpn(true);
                // Validation: ensure generated ID doesn't collision (very unlikely but good practice to sleep 1ms or use Atomic in a real concurrent env, but for batch serial loop likely ok)
                // For safety in batch loop:
                try { Thread.sleep(1); } catch (InterruptedException e) {}
            }

            Pegawai saved = pegawaiRepository.save(pegawai);
            savedList.add(saved);
            
            if (isNew) newCount++;
            else updateCount++;
        }
        
        // Single log entry for batch operation
        String isiLog = "Import batch pegawai: " + newCount + " baru, " + updateCount + " diperbarui";
        logRiwayatService.saveLog(new LogRiwayat(userPegawai, "BATCH_IMPORT_PEGAWAI", isiLog));
        
        return savedList;
    }

    public int batchDeletePegawai(List<Long> nipList, Pegawai userPegawai) {
        int deletedCount = 0;
        StringBuilder deletedNames = new StringBuilder();
        
        for (Long nip : nipList) {
            try {
                Optional<Pegawai> pegawaiOpt = pegawaiRepository.findById(nip);
                if (pegawaiOpt.isPresent()) {
                    if (deletedNames.length() > 0) deletedNames.append(", ");
                    deletedNames.append(pegawaiOpt.get().getNama());
                    // Delete any PenghapusanAset entries for assets owned by this pegawai first
                    penghapusanAsetRepository.deleteByPegawaiNip(nip);
                    pegawaiRepository.deleteById(nip);
                    deletedCount++;
                }
            } catch (Exception e) {
                System.err.println("Failed to delete NIP: " + nip + " - " + e.getMessage());
            }
        }
        
        if (deletedCount > 0) {
            String isiLog = "Hapus batch " + deletedCount + " pegawai: " + (deletedNames.length() > 100 
                ? deletedNames.substring(0, 100) + "..." 
                : deletedNames.toString());
            logRiwayatService.saveLog(new LogRiwayat(userPegawai, "BATCH_DELETE_PEGAWAI", isiLog));
        }
        
        return deletedCount;
    }
}