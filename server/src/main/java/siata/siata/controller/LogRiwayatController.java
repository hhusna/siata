package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.LogRiwayat;
import siata.siata.service.LogRiwayatService;
import siata.siata.dto.ApprovalLogDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logbook")
@PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
public class LogRiwayatController {

    @Autowired
    private LogRiwayatService logRiwayatService;

    /**
     * Get logs with optional limit and date range parameters
     * 
     * Examples:
     * - GET /api/logbook - returns all logs (original behavior)
     * - GET /api/logbook?limit=15 - returns 15 most recent logs
     * - GET /api/logbook?from=2024-11-01&to=2024-12-09 - returns logs within date range
     */
    @GetMapping
    public List<LogRiwayat> getAllLogs(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        // If date range is provided, use date range filter
        if (from != null && to != null) {
            System.out.println("LogRiwayatController: Fetching logs from " + from + " to " + to);
            return logRiwayatService.getLogsByDateRange(from, to);
        }
        
        // If limit is provided, use limit
        if (limit != null && limit > 0) {
            System.out.println("LogRiwayatController: Fetching " + limit + " logs");
            return logRiwayatService.getLogsWithLimit(limit);
        }
        
        // Default: return all logs
        System.out.println("LogRiwayatController: Fetching all logs");
        return logRiwayatService.getAllLogs();
    }

    @GetMapping("/approval-logs")
    public List<ApprovalLogDTO> getApprovalLogs(
            @RequestParam(required = false) Long permohonanId,
            @RequestParam(required = false) Long pengajuanId) {
        return logRiwayatService.getApprovalLogs(permohonanId, pengajuanId);
    }
}