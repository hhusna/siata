package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import siata.siata.entity.LogRiwayat;

import java.util.List;

public interface LogRiwayatRepository extends JpaRepository<LogRiwayat, Long> {
    List<LogRiwayat> findAllByOrderByTimestampDesc();
    List<LogRiwayat> findByPegawaiNip(Long nip);
}