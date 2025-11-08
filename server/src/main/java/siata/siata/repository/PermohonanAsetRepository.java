package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import siata.siata.entity.PermohonanAset;

public interface PermohonanAsetRepository extends JpaRepository<PermohonanAset, Long> {
    long countByStatusPersetujuan(String statusPersetujuan);
}