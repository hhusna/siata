package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "pengajuan_aset")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PengajuanAset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pengajuan")
    private Long idPengajuan;

    @Column(name = "kode_pengajuan", length = 100)
    private String kodePengajuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nip", referencedColumnName = "nip")
    private Pegawai pegawai;

    @Column(name = "nama_pengaju", length = 100)
    @NotBlank(message = "Nama pengaju tidak boleh kosong")
    @Size(min = 2, max = 100, message = "Nama harus 2-100 karakter")
    // Relaxed pattern: allow letters, numbers, spaces, dots, commas, apostrophes
    @Pattern(regexp = "[a-zA-Z0-9\\s'.,-]+", message = "Nama hanya boleh berisi huruf, angka, spasi, dan tanda baca umum")
    private String namaPengaju;

    @Column(name = "unit", length = 100)
    private String unit;

    @Column(name = "jenis_aset", length = 100)
    @NotBlank(message = "Jenis aset tidak boleh kosong")
    private String jenisAset;

    @Column(name = "jumlah")
    @NotNull(message = "Jumlah tidak boleh kosong")
    @Min(value = 1, message = "Jumlah minimal 1")
    @Max(value = 1000, message = "Jumlah maksimal 1000")
    private Integer jumlah;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    @Size(max = 500, message = "Deskripsi maksimal 500 karakter")
    private String deskripsi;

    @Column(name = "tujuan_penggunaan", columnDefinition = "TEXT")
    @Size(max = 500, message = "Tujuan maksimal 500 karakter")
    private String tujuanPenggunaan;

    @Column(name = "prioritas", length = 50)
    private String prioritas;

    @Column(name = "status_persetujuan", length = 50)
    private String statusPersetujuan;

    @Column(name = "timestamp", nullable = false)
    @NotNull(message = "Tanggal tidak boleh kosong")
    private LocalDate timestamp;

    @JsonIgnore
    @OneToMany(mappedBy = "pengajuan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LogRiwayat> logList;
}
