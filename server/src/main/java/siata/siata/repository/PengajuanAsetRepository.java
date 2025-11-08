package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import siata.siata.entity.PengajuanAset;

public interface PengajuanAsetRepository extends JpaRepository<PengajuanAset, Long> {
    long countByStatusPersetujuan(String statusPersetujuan);
}