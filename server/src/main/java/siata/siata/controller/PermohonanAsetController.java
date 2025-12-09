package siata.siata.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import siata.siata.dto.StatusUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PermohonanAset;
import siata.siata.entity.User;
import siata.siata.service.PermohonanAsetService;

import java.util.List;

@RestController
@RequestMapping("/api/permohonan")
public class PermohonanAsetController {

    @Autowired
    private PermohonanAsetService permohonanAsetService;

    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public List<PermohonanAset> getAll(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // Gunakan effective role agar DEV bisa simulasi sebagai role lain
        String role = user.getEffectiveRole();
        return permohonanAsetService.getAllByRole(role);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> create(@Valid @RequestBody PermohonanAset permohonanAset, Authentication authentication) {
        // Validasi: cek apakah nama pemohon terdaftar di unit yang sesuai
        // Menggunakan nipPengguna dan subdirektoratPengguna
        String validationError = permohonanAsetService.validatePemohonByUnit(permohonanAset.getNipPengguna(), permohonanAset.getSubdirektoratPengguna());
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        return ResponseEntity.ok(permohonanAsetService.save(permohonanAset, getPegawaiFromAuth(authentication)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public ResponseEntity<PermohonanAset> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO statusUpdate, Authentication authentication) {
        try {
            String status = statusUpdate.getStatus();
            String catatan = statusUpdate.getCatatan();
            String lampiran = statusUpdate.getLampiran();
            return ResponseEntity.ok(permohonanAsetService.updateStatus(id, status, catatan, lampiran, getPegawaiFromAuth(authentication)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        permohonanAsetService.delete(id, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PermohonanAset permohonanDetails, Authentication authentication) {
        // Validasi: cek apakah nama pemohon terdaftar di unit yang sesuai
        String validationError = permohonanAsetService.validatePemohonByUnit(permohonanDetails.getNipPengguna(), permohonanDetails.getSubdirektoratPengguna());
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        
        return permohonanAsetService.getById(id)
                .map(existing -> {
                    // Update field sesuai ERD
                    existing.setNipPengguna(permohonanDetails.getNipPengguna());
                    existing.setSubdirektoratPengguna(permohonanDetails.getSubdirektoratPengguna());
                    existing.setJenisAset(permohonanDetails.getJenisAset());
                    existing.setJumlah(permohonanDetails.getJumlah());
                    existing.setDeskripsi(permohonanDetails.getDeskripsi());
                    existing.setTujuanPenggunaan(permohonanDetails.getTujuanPenggunaan());
                    // prioritas removed
                    existing.setTimestamp(permohonanDetails.getTimestamp());

                    // Simpan perubahan
                    return ResponseEntity.ok(permohonanAsetService.save(existing, getPegawaiFromAuth(authentication)));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}