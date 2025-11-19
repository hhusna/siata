package siata.siata.entity;

import jakarta.persistence.*;
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

    @Column(name = "kode_aset", nullable = false, unique = true, length = 100)
    private String kodeAset;

    @Column(name = "jenis_aset", nullable = false, length = 100)
    private String jenisAset;

    @Column(name = "merk_aset", length = 100)
    private String merkAset;

    @Column(name = "tanggal_perolehan")
    private LocalDate tanggalPerolehan;

    @Column(name = "harga_aset", precision = 15, scale = 2)
    private BigDecimal hargaAset;

    @Column(name = "kondisi", length = 50)
    private String kondisi;

    @Column(name = "status_pemakaian", length = 50)
    private String statusPemakaian;

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
