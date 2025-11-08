package siata.siata.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Data
@Entity
@Table(name = "pengajuan_aset")
public class PengajuanAset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pengajuan")
    private Long idPengajuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nip", referencedColumnName = "nip")
    private Pegawai pegawai;

    @Column(name = "nama_pengaju", length = 100)
    private String namaPengaju;

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

    @JsonIgnore
    @OneToMany(mappedBy = "pengajuan", cascade = CascadeType.ALL)
    private List<LogRiwayat> logList;
}
