package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import siata.siata.entity.PenghapusanAset;
import siata.siata.service.PenghapusanAsetService;

import java.util.List;

@RestController
@RequestMapping("/api/penghapusan")
@PreAuthorize("hasRole('TIM_MANAJEMEN_ASET')")
public class PenghapusanAsetController {

    @Autowired
    private PenghapusanAsetService penghapusanAsetService;

    @GetMapping
    public List<PenghapusanAset> getAllPenghapusan() {
        return penghapusanAsetService.getAll();
    }
}