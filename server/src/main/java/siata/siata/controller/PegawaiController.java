package siata.siata.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.Pegawai;
import siata.siata.entity.User;
import siata.siata.service.PegawaiService;
import siata.siata.dto.MatriksAsetDTO;

import java.util.List;

@RestController
@RequestMapping("/api/pegawai")
public class PegawaiController {

    @Autowired
    private PegawaiService pegawaiService;

    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    // === READ OPERATIONS - All authenticated roles ===

    @GetMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public List<Pegawai> getAllPegawai() {
        return pegawaiService.getAllPegawai();
    }

    @GetMapping("/matriks")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public List<MatriksAsetDTO> getMatriks() {
        return pegawaiService.getMatriksAset();
    }

    @GetMapping("/{nip}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR', 'DEV')")
    public ResponseEntity<Pegawai> getPegawaiByNip(@PathVariable Long nip) {
        return pegawaiService.getPegawaiByNip(nip)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === WRITE OPERATIONS - Only TIM_MANAJEMEN_ASET and DEV ===

    @PostMapping
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public Pegawai createPegawai(@Valid @RequestBody Pegawai pegawai, Authentication authentication) {
        return pegawaiService.savePegawai(pegawai, getPegawaiFromAuth(authentication));
    }

    @PutMapping("/{nip}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Pegawai> updatePegawai(@PathVariable Long nip, @Valid @RequestBody Pegawai pegawaiDetails, Authentication authentication) {
        return pegawaiService.getPegawaiByNip(nip)
                .map(pegawai -> {
                    pegawai.setNama(pegawaiDetails.getNama());
                    pegawai.setNamaSubdir(pegawaiDetails.getNamaSubdir());
                    // jabatan dihapus
                    return ResponseEntity.ok(pegawaiService.savePegawai(pegawai, getPegawaiFromAuth(authentication)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{nip}")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Void> deletePegawai(@PathVariable Long nip, Authentication authentication) {
        pegawaiService.deletePegawai(nip, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<List<Pegawai>> batchCreatePegawai(@RequestBody List<Pegawai> pegawaiList, Authentication authentication) {
        System.out.println("=== BATCH ENDPOINT CALLED ===");
        System.out.println("User: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Authorities: " + (authentication != null ? authentication.getAuthorities() : "null"));
        System.out.println("Pegawai count: " + (pegawaiList != null ? pegawaiList.size() : "null"));
        
        List<Pegawai> savedPegawai = pegawaiService.batchSavePegawai(pegawaiList, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok(savedPegawai);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'DEV')")
    public ResponseEntity<Integer> batchDeletePegawai(@RequestBody List<Long> nipList, Authentication authentication) {
        System.out.println("=== BATCH DELETE CALLED ===");
        System.out.println("NIPs to delete: " + nipList.size());
        
        int deletedCount = pegawaiService.batchDeletePegawai(nipList, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok(deletedCount);
    }
}