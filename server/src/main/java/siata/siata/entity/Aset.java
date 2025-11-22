package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "aset")
public class Aset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aset")
    private Long idAset;

    @Column(name = "kode_aset", nullable = false, length = 100)
    @NotBlank(message = "Kode aset tidak boleh kosong")
    @Pattern(regexp = "[0-9]{10}", message = "Kode aset harus 10 digit angka")
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nip_pegawai", referencedColumnName = "nip")
    private Pegawai pegawai;

    @JsonIgnore
    @OneToOne(mappedBy = "aset", cascade = CascadeType.ALL)
    private PenghapusanAset penghapusanAset;

    @JsonIgnore
    @OneToMany(mappedBy = "aset", cascade = CascadeType.ALL)
    private List<LogRiwayat> logList;
}
