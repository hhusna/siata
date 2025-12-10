package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Entity
@Table(name = "aset")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Aset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aset")
    private Long idAset;

    @Column(name = "kode_aset", nullable = false, length = 100)
    @NotBlank(message = "Kode aset tidak boleh kosong")
    @Size(min = 1, max = 100, message = "Kode aset maksimal 100 karakter")
    private String kodeAset;

    @Column(name = "jenis_aset", nullable = false, length = 100)
    @NotBlank(message = "Jenis aset tidak boleh kosong")
    private String jenisAset;

    @Column(name = "merk_aset", length = 100)
    @Size(min = 2, max = 100, message = "Merk minimal 2 karakter")
    private String merkAset;

    @Column(name = "tanggal_perolehan")
    private LocalDate tanggalPerolehan;

    @Column(name = "harga_aset", precision = 15, scale = 2)
    @NotNull(message = "Harga aset tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Harga aset tidak boleh negatif")
    private BigDecimal hargaAset;

    @Column(name = "kondisi", length = 50)
    private String kondisi;

    @Column(name = "status_pemakaian", length = 50)
    private String statusPemakaian;

    @Column(name = "subdirektorat", length = 100)
    private String subdirektorat;

    @Column(name = "apakah_dihapus", nullable = false)
    private Integer apakahDihapus = 0;

    @Column(name = "no_aset")
    private Integer noAset;

    @Column(name = "dipakai")
    private String dipakai; // Refactored to String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nip_pegawai")
    private Pegawai pegawai;

    @JsonIgnore
    @OneToMany(mappedBy = "aset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LogRiwayat> logList;

    /**
     * Computed: Tua (formerly Siap Lelang)
     * Non-vehicle assets: 1 if age > 4 years AND status = AKTIF
     * Mobil/Motor: always 0
     */
    @Transient
    public Integer getTua() {
        if (tanggalPerolehan == null || jenisAset == null || statusPemakaian == null) return 0;
        String jenis = jenisAset.toLowerCase();
        if (jenis.contains("mobil") || jenis.contains("motor")) return 0;
        // Status must be AKTIF
        if (!"AKTIF".equalsIgnoreCase(statusPemakaian.replace(" ", ""))) return 0;
        long usia = java.time.temporal.ChronoUnit.YEARS.between(tanggalPerolehan, LocalDate.now());
        return usia > 4 ? 1 : 0;
    }

    /**
     * Computed: Akan Tua (formerly Akan Siap Lelang)
     * Non-vehicle assets: 1 if age > 3 && <= 4 years
     * Mobil/Motor: always 0
     */
    @Transient
    public Integer getAkanTua() {
        if (tanggalPerolehan == null || jenisAset == null) return 0;
        String jenis = jenisAset.toLowerCase();
        if (jenis.contains("mobil") || jenis.contains("motor")) return 0;
        long usia = java.time.temporal.ChronoUnit.YEARS.between(tanggalPerolehan, LocalDate.now());
        return (usia > 3 && usia <= 4) ? 1 : 0;
    }

    /**
     * Computed: Siap Lelang (NEW)
     * 1 if Tua = 1 AND apakahDihapus = 1 (marked for deletion)
     */
    @Transient
    public Integer getSiapLelang() {
        return (getTua() == 1 && apakahDihapus != null && apakahDihapus == 1) ? 1 : 0;
    }
}
