package siata.siata.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "jenis_aset", length = 100)
    private String jenisAset;

    @Column(name = "jumlah")
    private Integer jumlah;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "tujuan_penggunaan", columnDefinition = "TEXT")
    private String tujuanPenggunaan;

    @Column(name = "prioritas", length = 50)
    private String prioritas;

    @Column(name = "status_persetujuan", length = 50)
    private String statusPersetujuan;

    @Column(name = "timestamp", nullable = false)
    private LocalDate timestamp;

    @JsonIgnore
    @OneToMany(mappedBy = "permohonan", cascade = CascadeType.ALL)
    private List<LogRiwayat> logList;
}
