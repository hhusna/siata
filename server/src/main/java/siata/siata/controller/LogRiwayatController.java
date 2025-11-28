package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.LogRiwayat;
import siata.siata.service.LogRiwayatService;
import siata.siata.dto.ApprovalLogDTO;

import java.util.List;

@RestController
@RequestMapping("/api/logbook")
@PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR')")
public class LogRiwayatController {

    @Autowired
    private LogRiwayatService logRiwayatService;

    @GetMapping
    public List<LogRiwayat> getAllLogs() {
        return logRiwayatService.getAllLogs();
    }

    @GetMapping("/approval-logs")
    public List<ApprovalLogDTO> getApprovalLogs(
            @RequestParam(required = false) Long permohonanId,
            @RequestParam(required = false) Long pengajuanId) {
        return logRiwayatService.getApprovalLogs(permohonanId, pengajuanId);
    }
}