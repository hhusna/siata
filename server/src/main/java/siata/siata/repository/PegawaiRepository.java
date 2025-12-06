package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Tambahkan import
import siata.siata.entity.Pegawai;
import java.util.List; // Tambahkan import

public interface PegawaiRepository extends JpaRepository<Pegawai, Long> {
    @Query("SELECT p FROM Pegawai p LEFT JOIN FETCH p.asetList")
    List<Pegawai> findAllWithAset();

    boolean existsByNamaIgnoreCaseAndNamaSubdirIgnoreCase(String nama, String namaSubdir);
}