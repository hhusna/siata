package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "permohonan_aset")
public class PermohonanAset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permohonan")
    private Long idPermohonan;

    @Column(name = "kode_permohonan", length = 100)
    private String kodePermohonan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nip", referencedColumnName = "nip")
    private Pegawai pegawai;

    @Column(name = "nama_pemohon", length = 100)
    @NotBlank(message = "Nama pemohon tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama harus 3-100 karakter")
    @Pattern(regexp = "[a-zA-Z\\s]+", message = "Nama hanya boleh berisi huruf dan spasi")
    private String namaPemohon;

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
    @NotBlank(message = "Prioritas tidak boleh kosong")
    private String prioritas;

    @Column(name = "status_persetujuan", length = 50)
    private String statusPersetujuan;

    @Column(name = "timestamp", nullable = false)
    @NotNull(message = "Tanggal tidak boleh kosong")
    private LocalDate timestamp;

    @JsonIgnore
    @OneToMany(mappedBy = "permohonan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LogRiwayat> logList;
}
