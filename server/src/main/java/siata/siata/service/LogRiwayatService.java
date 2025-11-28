package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.LogRiwayatRepository;
import siata.siata.dto.ApprovalLogDTO;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogRiwayatService {

    @Autowired
    private LogRiwayatRepository logRiwayatRepository;

    @CacheEvict(value = "approvalLogs", allEntries = true)
    public void saveLog(LogRiwayat log) {
        logRiwayatRepository.save(log);
    }

    public List<LogRiwayat> getAllLogs() {
        return logRiwayatRepository.findAllByOrderByTimestampDesc();
    }

    @Cacheable(value = "approvalLogs", key = "#permohonanId + '-' + #pengajuanId")
    public List<ApprovalLogDTO> getApprovalLogs(Long permohonanId, Long pengajuanId) {
        List<LogRiwayat> logs;
        
        if (permohonanId != null) {
            logs = logRiwayatRepository.findAll().stream()
                .filter(log -> log.getPermohonan() != null && 
                              log.getPermohonan().getIdPermohonan().equals(permohonanId) &&
                              log.getJenisLog().equals("UPDATE_STATUS_PERMOHONAN"))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Sort descending by timestamp
                .collect(Collectors.toList());
        } else if (pengajuanId != null) {
            logs = logRiwayatRepository.findAll().stream()
                .filter(log -> log.getPengajuan() != null && 
                              log.getPengajuan().getIdPengajuan().equals(pengajuanId) &&
                              log.getJenisLog().equals("UPDATE_STATUS_PENGAJUAN"))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Sort descending by timestamp
                .collect(Collectors.toList());
        } else {
            return List.of();
        }

        return logs.stream()
            .map(log -> {
                String isiLog = log.getIsiLog();
                String status = "Pending";
                
                // PENTING: Cek Ditolak dulu sebelum Disetujui
                if (isiLog.contains("Ditolak")) {
                    status = "Ditolak";
                } else if (isiLog.contains("Disetujui")) {
                    status = "Disetujui";
                }
                
                String role = extractRole(log.getPegawai().getUser().getRole());
                
                return new ApprovalLogDTO(
                    log.getPegawai().getNama(),
                    role,
                    status,
                    log.getTimestamp()
                );
            })
            .collect(Collectors.toList());
    }

    private String extractRole(String roleString) {
        if (roleString == null) return "";
        
        if (roleString.contains("TIM_MANAJEMEN_ASET")) return "Tim Manajemen Aset";
        if (roleString.contains("PPK")) return "PPK";
        if (roleString.contains("PPBJ")) return "PPBJ";
        if (roleString.contains("DIREKTUR")) return "Direktur";
        
        return roleString;
    }
}