package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.LogRiwayat;
import siata.siata.service.LogRiwayatService;

import java.util.List;

@RestController
@RequestMapping("/api/logbook")
//@PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK')")
public class LogRiwayatController {

    @Autowired
    private LogRiwayatService logRiwayatService;

    @GetMapping
    public List<LogRiwayat> getAllLogs() {
        return logRiwayatService.getAllLogs();
    }
}