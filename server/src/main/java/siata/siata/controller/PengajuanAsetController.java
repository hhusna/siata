package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PengajuanAset;
import siata.siata.entity.User;
import siata.siata.service.PengajuanAsetService;

import java.util.List;
import java.util.Map;

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
//    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR')")
    public List<PengajuanAset> getAll() {
        return pengajuanAsetService.getAll();
    }

    @PostMapping
//    @PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
    public PengajuanAset create(@RequestBody PengajuanAset pengajuanAset, Authentication authentication) {
        return pengajuanAsetService.save(pengajuanAset, getPegawaiFromAuth(authentication));
    }

    @PatchMapping("/{id}/status")
//    @PreAuthorize("hasAnyRole('PPBJ', 'PPK', 'DIREKTUR')")
    public ResponseEntity<PengajuanAset> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate, Authentication authentication) {
        try {
            String status = statusUpdate.get("statusPersetujuan");
            return ResponseEntity.ok(pengajuanAsetService.updateStatus(id, status, getPegawaiFromAuth(authentication)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        pengajuanAsetService.delete(id, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')") // Uncomment jika perlu otorisasi
    public ResponseEntity<PengajuanAset> update(@PathVariable Long id, @RequestBody PengajuanAset pengajuanDetails, Authentication authentication) {
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