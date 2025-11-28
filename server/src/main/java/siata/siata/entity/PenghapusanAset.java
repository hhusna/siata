package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "penghapusan_aset")
public class PenghapusanAset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penghapusan")
    private Long idPenghapusan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aset", referencedColumnName = "id_aset")
    private Aset aset;

    @Column(name = "nama_aset", nullable = false, length = 100)
    @NotBlank(message = "Nama aset tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama aset harus 3-100 karakter")
    private String namaAset;

    @Column(name = "jenis_aset", length = 100)
    @NotBlank(message = "Jenis aset tidak boleh kosong")
    @Size(max = 100, message = "Jenis aset maksimal 100 karakter")
    private String jenisAset;

    @Column(name = "tanggal_perolehan")
    private LocalDate tanggalPerolehan;

    @Column(name = "harga_aset", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Harga aset tidak boleh negatif")
    private BigDecimal hargaAset;

    @Column(name = "kondisi", length = 50)
    @NotBlank(message = "Kondisi tidak boleh kosong")
    @Pattern(regexp = "^(Baik|Rusak Ringan|Rusak Berat|Hilang|Gudang)$",
             message = "Kondisi harus salah satu dari: Baik, Rusak Ringan, Rusak Berat, Hilang, Gudang")
    private String kondisi;
}
