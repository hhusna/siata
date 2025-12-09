package siata.siata.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import siata.siata.entity.LogRiwayat;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRiwayatRepository extends JpaRepository<LogRiwayat, Long> {
    @Query("SELECT DISTINCT l FROM LogRiwayat l LEFT JOIN FETCH l.pegawai LEFT JOIN FETCH l.aset LEFT JOIN FETCH l.permohonan LEFT JOIN FETCH l.pengajuan ORDER BY l.timestamp DESC")
    List<LogRiwayat> findAllByOrderByTimestampDesc();
    
    @Query("SELECT DISTINCT l FROM LogRiwayat l LEFT JOIN FETCH l.pegawai LEFT JOIN FETCH l.aset WHERE l.pegawai.nip = :nip")
    List<LogRiwayat> findByPegawaiNip(Long nip);
    
    /**
     * Get top N logs (for limited display)
     */
    @Query("SELECT DISTINCT l FROM LogRiwayat l LEFT JOIN FETCH l.pegawai LEFT JOIN FETCH l.aset LEFT JOIN FETCH l.permohonan LEFT JOIN FETCH l.pengajuan ORDER BY l.timestamp DESC")
    List<LogRiwayat> findTopNByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * Get logs within date range (for export)
     */
    @Query("SELECT DISTINCT l FROM LogRiwayat l LEFT JOIN FETCH l.pegawai LEFT JOIN FETCH l.aset LEFT JOIN FETCH l.permohonan LEFT JOIN FETCH l.pengajuan WHERE l.timestamp >= :fromDate AND l.timestamp <= :toDate ORDER BY l.timestamp DESC")
    List<LogRiwayat> findByTimestampBetween(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}