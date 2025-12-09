package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import siata.siata.entity.PengajuanAset;
import java.util.List;

public interface PengajuanAsetRepository extends JpaRepository<PengajuanAset, Long> {
    long countByStatusPersetujuanIgnoreCase(String statusPersetujuan);
    
    @Query("SELECT DISTINCT p FROM PengajuanAset p LEFT JOIN FETCH p.pegawai")
    List<PengajuanAset> findAllWithPegawai();
}