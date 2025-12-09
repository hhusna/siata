package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import siata.siata.entity.Aset;

import java.time.LocalDate;
import java.util.List;

public interface AsetRepository extends JpaRepository<Aset, Long> {

    @Query("SELECT DISTINCT a FROM Aset a LEFT JOIN FETCH a.pegawai WHERE a.apakahDihapus = 0")
    List<Aset> findAllWithPegawai();

    List<Aset> findByJenisAsetContainingIgnoreCase(String jenisAset);
    List<Aset> findByStatusPemakaianContainingIgnoreCase(String statusPemakaian);
    List<Aset> findByPegawaiNamaContainingIgnoreCase(String namaPegawai);

    @Query("SELECT DISTINCT a FROM Aset a LEFT JOIN FETCH a.pegawai p " +
            "WHERE a.apakahDihapus = 0 " +
            "AND (:jenis IS NULL OR LOWER(a.jenisAset) = LOWER(:jenis)) " +
            "AND (:status IS NULL OR LOWER(a.statusPemakaian) = LOWER(:status)) " +
            "AND (:namaPegawai IS NULL OR LOWER(p.nama) LIKE LOWER(CONCAT('%', :namaPegawai, '%'))) " +
            "AND (:namaSubdir IS NULL OR LOWER(p.namaSubdir) = LOWER(:namaSubdir))")
    List<Aset> searchAset(@Param("jenis") String jenis,
                          @Param("status") String status,
                          @Param("namaPegawai") String namaPegawai,
                          @Param("namaSubdir") String namaSubdir);

    @Query("SELECT a FROM Aset a WHERE a.tanggalPerolehan <= :tanggalBatas " +
            "AND a.jenisAset NOT IN ('Motor', 'Mobil') " +
            "AND (a.statusPemakaian IS NULL OR a.statusPemakaian != 'Siap Dilelang')")
    List<Aset> findAsetSiapLelang(@Param("tanggalBatas") LocalDate tanggalBatas);

    long countByStatusPemakaianIgnoreCase(String statusPemakaian);
    
    /**
     * Count assets with kondisi "Rusak Berat" or "R. Berat" (case-insensitive)
     */
    @Query("SELECT COUNT(a) FROM Aset a WHERE LOWER(a.kondisi) LIKE '%rusak berat%' OR LOWER(a.kondisi) LIKE '%r. berat%' OR LOWER(a.kondisi) = 'r.berat'")
    long countByKondisiRusakBerat();

    @Query("SELECT COUNT(a) FROM Aset a WHERE LOWER(a.statusPemakaian) = LOWER('Siap Dilelang')")
    long countSiapDilelang();
    
    /**
     * Menghitung aset siap lelang (NEW LOGIC):
     * - Tua = 1 (usia > 4 tahun, status = AKTIF, bukan mobil/motor)
     * - DAN apakahDihapus = 1
     */
    @Query("SELECT COUNT(a) FROM Aset a WHERE a.tanggalPerolehan <= :tanggalBatas " +
           "AND UPPER(REPLACE(a.statusPemakaian, ' ', '')) = 'AKTIF' " +
           "AND LOWER(a.jenisAset) NOT LIKE '%mobil%' " +
           "AND LOWER(a.jenisAset) NOT LIKE '%motor%' " +
           "AND a.apakahDihapus = 1")
    long countAsetSiapDilelang(@Param("tanggalBatas") LocalDate tanggalBatas);

    @Query("SELECT DISTINCT a FROM Aset a LEFT JOIN FETCH a.pegawai WHERE a.apakahDihapus = 1")
    List<Aset> findAllDeletedWithPegawai();

    @Query("SELECT MAX(a.noAset) FROM Aset a WHERE a.kodeAset = :kodeAset")
    Integer findMaxNoAsetByKodeAset(@Param("kodeAset") String kodeAset);

    boolean existsByKodeAsetAndNoAset(String kodeAset, Integer noAset);

    java.util.Optional<Aset> findByKodeAsetAndNoAset(String kodeAset, Integer noAset);
}