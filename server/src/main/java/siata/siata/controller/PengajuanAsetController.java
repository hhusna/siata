package siata.siata.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import siata.siata.dto.StatusUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PengajuanAset;
import siata.siata.entity.User;
import siata.siata.service.PengajuanAsetService;

import java.util.List;

@RestController
@RequestMapping("/api/pengajuan")
public class PengajuanAsetController {

    @Autowired
    private PengajuanAsetService pengajuanAsetService;

    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public List<PengajuanAset> getAll() {
        return pengajuanAsetService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> create(@Valid @RequestBody PengajuanAset pengajuanAset, Authentication authentication) {
        // Validasi: cek apakah nama pengaju terdaftar di unit yang sesuai
        String validationError = pengajuanAsetService.validatePengajuByUnit(pengajuanAset.getNamaPengaju(), pengajuanAset.getUnit());
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        return ResponseEntity.ok(pengajuanAsetService.save(pengajuanAset, getPegawaiFromAuth(authentication)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public ResponseEntity<PengajuanAset> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO statusUpdate, Authentication authentication) {
        try {
            String status = statusUpdate.getStatus();
            return ResponseEntity.ok(pengajuanAsetService.updateStatus(id, status, getPegawaiFromAuth(authentication)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        pengajuanAsetService.delete(id, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PengajuanAset pengajuanDetails, Authentication authentication) {
        // Validasi: cek apakah nama pengaju terdaftar di unit yang sesuai
        String validationError = pengajuanAsetService.validatePengajuByUnit(pengajuanDetails.getNamaPengaju(), pengajuanDetails.getUnit());
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        
        return pengajuanAsetService.getById(id)
                .map(existing -> {
                    // Update field yang diizinkan
                    existing.setNamaPengaju(pengajuanDetails.getNamaPengaju());
                    existing.setUnit(pengajuanDetails.getUnit());
                    existing.setJenisAset(pengajuanDetails.getJenisAset());
                    existing.setJumlah(pengajuanDetails.getJumlah());
                    existing.setDeskripsi(pengajuanDetails.getDeskripsi());
                    existing.setTujuanPenggunaan(pengajuanDetails.getTujuanPenggunaan());
                    existing.setPrioritas(pengajuanDetails.getPrioritas());
                    existing.setTimestamp(pengajuanDetails.getTimestamp());

                    // Simpan perubahan
                    return ResponseEntity.ok(pengajuanAsetService.save(existing, getPegawaiFromAuth(authentication)));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}