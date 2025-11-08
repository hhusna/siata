package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.Pegawai;
import siata.siata.entity.PermohonanAset;
import siata.siata.entity.User;
import siata.siata.service.PermohonanAsetService;

import java.util.List;
import java.util.Map;

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
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR')")
    public List<PermohonanAset> getAll() {
        return permohonanAsetService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
    public PermohonanAset create(@RequestBody PermohonanAset permohonanAset, Authentication authentication) {
        return permohonanAsetService.save(permohonanAset, getPegawaiFromAuth(authentication));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PPBJ', 'PPK', 'DIREKTUR')")
    public ResponseEntity<PermohonanAset> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate, Authentication authentication) {
        try {
            String status = statusUpdate.get("status");
            return ResponseEntity.ok(permohonanAsetService.updateStatus(id, status, getPegawaiFromAuth(authentication)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        permohonanAsetService.delete(id, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok().build();
    }
}