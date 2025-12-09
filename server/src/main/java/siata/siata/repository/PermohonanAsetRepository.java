package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import siata.siata.entity.PermohonanAset;
import java.util.List;

public interface PermohonanAsetRepository extends JpaRepository<PermohonanAset, Long> {
    long countByStatusPersetujuanIgnoreCase(String statusPersetujuan);
    
    @Query("SELECT DISTINCT p FROM PermohonanAset p LEFT JOIN FETCH p.pegawai")
    List<PermohonanAset> findAllWithPegawai();
}