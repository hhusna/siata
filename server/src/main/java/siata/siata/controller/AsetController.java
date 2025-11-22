package siata.siata.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.Aset;
import siata.siata.entity.Pegawai;
import siata.siata.entity.User;
import siata.siata.service.AsetService;

import java.util.List;

@RestController
@RequestMapping("/api/aset")
@PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
public class AsetController {

    @Autowired
    private AsetService asetService;

    // Helper untuk mendapatkan Pegawai dari user yang login
    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    @GetMapping
    public List<Aset> getAllAset() {
        return asetService.getAllAset();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aset> getAsetById(@PathVariable Long id) {
        return asetService.getAsetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // FR 2.2: Pencarian Aset - MODIFIKASI METHOD INI
    @GetMapping("/search")
    public List<Aset> searchAset(
            @RequestParam(required = false) String jenis,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String namaPegawai,
            @RequestParam(required = false) String namaSubdir) {
        return asetService.searchAset(jenis, status, namaPegawai, namaSubdir);
    }

    @PostMapping
    public Aset createAset(@Valid @RequestBody Aset aset, Authentication authentication) {
        return asetService.saveAset(aset, getPegawaiFromAuth(authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aset> updateAset(@PathVariable Long id, @Valid @RequestBody Aset asetDetails, Authentication authentication) {
        return asetService.getAsetById(id)
                .map(aset -> {
                    // Update fields
                    aset.setKodeAset(asetDetails.getKodeAset());
                    aset.setJenisAset(asetDetails.getJenisAset());
                    aset.setMerkAset(asetDetails.getMerkAset());
                    aset.setTanggalPerolehan(asetDetails.getTanggalPerolehan());
                    aset.setHargaAset(asetDetails.getHargaAset());
                    aset.setKondisi(asetDetails.getKondisi());
                    aset.setStatusPemakaian(asetDetails.getStatusPemakaian());

                    // Update Subdirektorat & Pegawai
                    aset.setSubdirektorat(asetDetails.getSubdirektorat());
                    aset.setPegawai(asetDetails.getPegawai());

                    return ResponseEntity.ok(asetService.saveAset(aset, getPegawaiFromAuth(authentication)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // FR 2.3: Tandai untuk penghapusan
    @PostMapping("/hapus/{id}")
    public ResponseEntity<Void> tandaiUntukPenghapusan(@PathVariable Long id, Authentication authentication) {
        try {
            asetService.tandaiUntukPenghapusan(id, getPegawaiFromAuth(authentication));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint Hapus Permanen (Hard Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAset(@PathVariable Long id, Authentication authentication) {
        try {
            asetService.deleteAset(id, getPegawaiFromAuth(authentication));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}