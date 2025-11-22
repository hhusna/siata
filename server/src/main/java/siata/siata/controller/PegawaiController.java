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
@PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
public class PegawaiController {

    @Autowired
    private PegawaiService pegawaiService;

    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    @GetMapping
    public List<Pegawai> getAllPegawai() {
        return pegawaiService.getAllPegawai();
    }

    @GetMapping("/matriks")
    @PreAuthorize("hasAnyRole('TIM_MANAJEMEN_ASET', 'PPBJ', 'PPK', 'DIREKTUR')")
    public List<MatriksAsetDTO> getMatriks() {
        return pegawaiService.getMatriksAset();
    }

    @GetMapping("/{nip}")
    public ResponseEntity<Pegawai> getPegawaiByNip(@PathVariable Long nip) {
        return pegawaiService.getPegawaiByNip(nip)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Pegawai createPegawai(@Valid @RequestBody Pegawai pegawai, Authentication authentication) {
        return pegawaiService.savePegawai(pegawai, getPegawaiFromAuth(authentication));
    }

    @PutMapping("/{nip}")
    public ResponseEntity<Pegawai> updatePegawai(@PathVariable Long nip, @Valid @RequestBody Pegawai pegawaiDetails, Authentication authentication) {
        return pegawaiService.getPegawaiByNip(nip)
                .map(pegawai -> {
                    pegawai.setNama(pegawaiDetails.getNama());
                    pegawai.setNamaSubdir(pegawaiDetails.getNamaSubdir());
                    pegawai.setJabatan(pegawaiDetails.getJabatan());
                    return ResponseEntity.ok(pegawaiService.savePegawai(pegawai, getPegawaiFromAuth(authentication)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{nip}")
    public ResponseEntity<Void> deletePegawai(@PathVariable Long nip, Authentication authentication) {
        pegawaiService.deletePegawai(nip, getPegawaiFromAuth(authentication));
        return ResponseEntity.ok().build();
    }
}