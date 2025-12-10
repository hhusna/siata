package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.LogRiwayatRepository;
import siata.siata.dto.ApprovalLogDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    
    /**
     * Get logs with limit for performance optimization
     * @param limit Maximum number of logs to return
     */
    public List<LogRiwayat> getLogsWithLimit(int limit) {
        if (limit <= 0) {
            return getAllLogs();
        }
        return logRiwayatRepository.findTopNByOrderByTimestampDesc(PageRequest.of(0, limit));
    }
    
    /**
     * Get logs within date range for export
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     */
    public List<LogRiwayat> getLogsByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
        return logRiwayatRepository.findByTimestampBetween(fromDateTime, toDateTime);
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
                String role = "";
                
                // Extract status and role from isiLog
                // Format: "Memperbarui status ... menjadi: Disetujui PPBJ" or "... Ditolak PPK"
                if (isiLog.contains("Ditolak")) {
                    status = "Ditolak";
                    role = extractRoleFromStatus(isiLog);
                } else if (isiLog.contains("Disetujui")) {
                    status = "Disetujui";
                    role = extractRoleFromStatus(isiLog);
                }
                
                // Fallback to user's role if extraction failed
                if (role.isEmpty()) {
                    role = extractRole(log.getPegawai().getUser().getRole());
                }
                
                // Get the actual role of the person who made the approval
                String actualApproverRole = extractRole(log.getPegawai().getUser().getRole());
                
                return new ApprovalLogDTO(
                    log.getPegawai().getNama(),
                    role,
                    status,
                    log.getTimestamp(),
                    log.getCatatan(),
                    log.getLampiran(),
                    actualApproverRole
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Extract role from status string in log content
     * e.g., "menjadi: Disetujui PPBJ" -> "PPBJ"
     */
    private String extractRoleFromStatus(String isiLog) {
        // Look for patterns like "Disetujui PPBJ", "Ditolak PPK", "Disetujui Direktur"
        if (isiLog.contains("PPBJ")) return "PPBJ";
        if (isiLog.contains("PPK")) return "PPK";
        if (isiLog.contains("Direktur")) return "Direktur";
        if (isiLog.contains("Tim Manajemen Aset") || isiLog.contains("TIM_MANAJEMEN_ASET")) return "Tim Manajemen Aset";
        if (isiLog.contains("Tim Aset")) return "Tim Manajemen Aset";
        return "";
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