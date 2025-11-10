package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import siata.siata.service.LaporanService;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/laporan")
@PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR')")
public class LaporanController {

    @Autowired
    private LaporanService laporanService;

    @GetMapping(value = "/aset/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> exportAsetPdf() {
        ByteArrayInputStream bis = laporanService.createAsetPdfReport();

        HttpHeaders headers = new HttpHeaders();
        String tgl = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        headers.add("Content-Disposition", "inline; filename=LaporanAset_" + tgl + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}