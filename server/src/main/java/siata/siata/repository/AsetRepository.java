package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import siata.siata.entity.Aset;

import java.time.LocalDate;
import java.util.List;

public interface AsetRepository extends JpaRepository<Aset, Long> {

    List<Aset> findByJenisAsetContainingIgnoreCase(String jenisAset);
    List<Aset> findByStatusPemakaianContainingIgnoreCase(String statusPemakaian);
    List<Aset> findByPegawaiNamaContainingIgnoreCase(String namaPegawai);

    @Query("SELECT a FROM Aset a LEFT JOIN a.pegawai p " +
            "WHERE (:jenis IS NULL OR a.jenisAset = :jenis) " +
            "AND (:status IS NULL OR a.statusPemakaian = :status) " +
            "AND (:namaPegawai IS NULL OR p.nama LIKE %:namaPegawai%) " +
            "AND (:namaSubdir IS NULL OR p.namaSubdir = :namaSubdir)") // Tambahkan kondisi baru
    List<Aset> searchAset(@Param("jenis") String jenis,
                          @Param("status") String status,
                          @Param("namaPegawai") String namaPegawai,
                          @Param("namaSubdir") String namaSubdir); // Tambahkan parameter baru

    @Query("SELECT a FROM Aset a WHERE a.tanggalPerolehan <= :tanggalBatas " +
            "AND a.jenisAset NOT IN ('Motor', 'Mobil') " +
            "AND (a.statusPemakaian IS NULL OR a.statusPemakaian != 'Siap Dilelang')")
    List<Aset> findAsetSiapLelang(@Param("tanggalBatas") LocalDate tanggalBatas);

    long countByStatusPemakaian(String statusPemakaian);
    long countByKondisi(String kondisi);

    @Query("SELECT COUNT(a) FROM Aset a WHERE a.statusPemakaian = 'Siap Dilelang'")
    long countSiapDilelang();
    
    /**
     * Menghitung aset siap dilelang:
     * - Usia aset > 4 tahun (tanggal perolehan <= tanggalBatas)
     * - DAN status pemakaian = "Non Aktif"
     */
    @Query("SELECT COUNT(a) FROM Aset a WHERE a.tanggalPerolehan <= :tanggalBatas AND a.statusPemakaian = 'Non Aktif'")
    long countAsetSiapDilelang(@Param("tanggalBatas") LocalDate tanggalBatas);
}