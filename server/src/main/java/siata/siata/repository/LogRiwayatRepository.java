package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import siata.siata.entity.LogRiwayat;

import java.util.List;

public interface LogRiwayatRepository extends JpaRepository<LogRiwayat, Long> {
    @Query("SELECT DISTINCT l FROM LogRiwayat l LEFT JOIN FETCH l.pegawai LEFT JOIN FETCH l.aset LEFT JOIN FETCH l.permohonan LEFT JOIN FETCH l.pengajuan ORDER BY l.timestamp DESC")
    List<LogRiwayat> findAllByOrderByTimestampDesc();
    
    @Query("SELECT DISTINCT l FROM LogRiwayat l LEFT JOIN FETCH l.pegawai LEFT JOIN FETCH l.aset WHERE l.pegawai.nip = :nip")
    List<LogRiwayat> findByPegawaiNip(Long nip);
}