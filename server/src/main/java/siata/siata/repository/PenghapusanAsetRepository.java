package siata.siata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import siata.siata.entity.Aset;
import siata.siata.entity.PenghapusanAset;

import java.time.LocalDate;

public interface PenghapusanAsetRepository extends JpaRepository<PenghapusanAset, Long> {
    
    /**
     * Menghitung aset siap dilelang:
     * - Usia aset > 4 tahun (tanggal perolehan <= tanggalBatas)
     * - DAN sudah ada di tabel penghapusan_aset (non aktif)
     */
    @Query("SELECT COUNT(p) FROM PenghapusanAset p WHERE p.tanggalPerolehan <= :tanggalBatas")
    long countAsetSiapDilelang(@Param("tanggalBatas") LocalDate tanggalBatas);

    /**
     * Delete PenghapusanAset entry by Aset
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM PenghapusanAset p WHERE p.aset = :aset")
    void deleteByAset(@Param("aset") Aset aset);

    /**
     * Delete all PenghapusanAset entries for assets owned by a Pegawai
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM PenghapusanAset p WHERE p.aset.pegawai.nip = :nip")
    void deleteByPegawaiNip(@Param("nip") Long nip);
}
