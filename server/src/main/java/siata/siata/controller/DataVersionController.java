package siata.siata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import siata.siata.service.DataVersionService;

import java.util.Map;

/**
 * Controller untuk endpoint data version.
 * Endpoint ini sangat ringan (hanya return 1 angka).
 * Digunakan untuk polling dari client setiap 60 detik.
 */
@RestController
@RequestMapping("/api/data-version")
public class DataVersionController {

    @Autowired
    private DataVersionService dataVersionService;

    /**
     * Get current data version.
     * Client polls this endpoint to detect data changes.
     * 
     * @return Map containing version timestamp
     */
    @GetMapping
    public Map<String, Long> getVersion() {
        return Map.of("version", dataVersionService.getVersion());
    }
}
