package siata.siata.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Singleton service untuk tracking versi data.
 * Setiap kali ada perubahan data (add/edit/delete), panggil incrementVersion().
 * Client akan poll endpoint /api/data-version untuk mendapatkan versi terkini.
 */
@Service
public class DataVersionService {

    // Atomic untuk thread-safety
    private final AtomicLong version = new AtomicLong(System.currentTimeMillis());

    /**
     * Get current data version (timestamp).
     * @return Current version timestamp
     */
    public long getVersion() {
        return version.get();
    }

    /**
     * Increment version (called after any data modification).
     * Sets version to current timestamp.
     */
    public void incrementVersion() {
        version.set(System.currentTimeMillis());
    }
}
