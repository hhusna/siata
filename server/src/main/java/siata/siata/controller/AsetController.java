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
import siata.siata.repository.PegawaiRepository;

import java.util.List;

@RestController
@RequestMapping("/api/aset")
public class AsetController {

    @Autowired
    private AsetService asetService;

    @Autowired
    private PegawaiRepository pegawaiRepository;

    // Helper untuk mendapatkan Pegawai dari user yang login
    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    // === READ OPERATIONS - All authenticated users ===
    
    @GetMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public List<Aset> getAllAset() {
        return asetService.getAllAset();
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public List<Aset> getDeletedAset() {
        return asetService.getAllDeletedAset();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public ResponseEntity<Aset> getAsetById(@PathVariable Long id) {
        return asetService.getAsetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/next-no")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Integer> getNextNoAset(@RequestParam String kodeAset) {
        return ResponseEntity.ok(asetService.getNextNoAset(kodeAset));
    }

    // FR 2.2: Pencarian Aset
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public List<Aset> searchAset(
            @RequestParam(required = false) String jenis,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String namaPegawai,
            @RequestParam(required = false) String namaSubdir) {
        return asetService.searchAset(jenis, status, namaPegawai, namaSubdir);
    }

    // === WRITE OPERATIONS - Only TIM_MANAJEMEN_ASET and DEV ===

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Integer> batchCreateAset(@RequestBody List<Aset> assets, Authentication authentication) {
        int count = asetService.batchSaveAset(assets, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok(count);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public Aset createAset(@Valid @RequestBody Aset aset, Authentication authentication) {
        // Properly lookup Pegawai from database by NIP
        if (aset.getPegawai() != null && aset.getPegawai().getNip() != null) {
            Pegawai pegawai = pegawaiRepository.findById(aset.getPegawai().getNip()).orElse(null);
            aset.setPegawai(pegawai);
        }
        return asetService.saveAset(aset, getPegawaiFromAuth(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Aset> updateAset(@PathVariable Long id, @Valid @RequestBody Aset asetDetails, Authentication authentication) {
        return asetService.getAsetById(id)
                .map(aset -> {
                    // Update fields
                    aset.setKodeAset(asetDetails.getKodeAset());
                    aset.setNoAset(asetDetails.getNoAset());
                    aset.setJenisAset(asetDetails.getJenisAset());
                    aset.setMerkAset(asetDetails.getMerkAset());
                    aset.setTanggalPerolehan(asetDetails.getTanggalPerolehan());
                    aset.setHargaAset(asetDetails.getHargaAset());
                    aset.setKondisi(asetDetails.getKondisi());
                    aset.setStatusPemakaian(asetDetails.getStatusPemakaian());
                    aset.setDipakai(asetDetails.getDipakai()); // Update Dipakai status

                    // Update Subdirektorat
                    aset.setSubdirektorat(asetDetails.getSubdirektorat());
                    
                    // Update Pegawai - properly lookup from database by NIP
                    if (asetDetails.getPegawai() != null && asetDetails.getPegawai().getNip() != null) {
                        Pegawai pegawai = pegawaiRepository.findById(asetDetails.getPegawai().getNip()).orElse(null);
                        aset.setPegawai(pegawai);
                    } else {
                        aset.setPegawai(null);
                    }

                    return ResponseEntity.ok(asetService.saveAset(aset, getPegawaiFromAuth(authentication)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // FR 2.3: Tandai untuk penghapusan
    @PostMapping("/hapus/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> tandaiUntukPenghapusan(@PathVariable Long id, Authentication authentication) {
        try {
            asetService.tandaiUntukPenghapusan(id, getPegawaiFromAuth(authentication));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // Kembalikan error message yang jelas
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Terjadi kesalahan sistem: " + e.getMessage());
        }
    }

    // Endpoint Hapus Permanen (Hard Delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Void> deleteAset(@PathVariable Long id, Authentication authentication) {
        try {
            asetService.deleteAset(id, getPegawaiFromAuth(authentication));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // FR: Pembersih Data Duplikat
    @GetMapping("/duplicates")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<List<Aset>> getDuplicates() {
        List<Aset> duplicates = asetService.findDuplicates();
        return ResponseEntity.ok(duplicates);
    }

    @PostMapping("/clean-duplicates")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Integer> cleanDuplicates(Authentication authentication) {
        int deletedCount = asetService.cleanDuplicates(getPegawaiFromAuth(authentication));
        return ResponseEntity.ok(deletedCount);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Integer> batchDeleteAset(@RequestBody List<Long> idList, Authentication authentication) {
        System.out.println("=== BATCH DELETE ASET CALLED ===");
        System.out.println("IDs to delete: " + idList.size());
        
        int deletedCount = asetService.batchDeleteAset(idList, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok(deletedCount);
    }

    // Undo delete: Set apakahDihapus = 0 to restore asset
    @PostMapping("/undo/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> undoDeleteAset(@PathVariable Long id, Authentication authentication) {
        try {
            asetService.undoDeleteAset(id, getPegawaiFromAuth(authentication));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Terjadi kesalahan sistem: " + e.getMessage());
        }
    }

    // Permanent delete: Actually remove from database
    @DeleteMapping("/permanent/{id}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<?> permanentDeleteAset(@PathVariable Long id, Authentication authentication) {
        try {
            asetService.permanentDeleteAset(id, getPegawaiFromAuth(authentication));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Terjadi kesalahan sistem: " + e.getMessage());
        }
    }
}