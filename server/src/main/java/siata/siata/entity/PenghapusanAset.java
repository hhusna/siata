package siata.siata.entity;

import jakarta.persistence.*;
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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_aset", referencedColumnName = "id_aset")
    private Aset aset;

    @Column(name = "nama_aset", nullable = false, length = 100)
    private String namaAset;

    @Column(name = "jenis_aset", length = 100)
    private String jenisAset;

    @Column(name = "tanggal_perolehan")
    private LocalDate tanggalPerolehan;

    @Column(name = "harga_aset", precision = 15, scale = 2)
    private BigDecimal hargaAset;

    @Column(name = "kondisi", length = 50)
    private String kondisi;
}
