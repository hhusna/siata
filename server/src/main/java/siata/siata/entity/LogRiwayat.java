package siata.siata.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "log_riwayat")
public class LogRiwayat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.EAGER) // AWALNYA LAZY
    @JoinColumn(name = "id_permohonan", referencedColumnName = "id_permohonan")
    private PermohonanAset permohonan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pengajuan", referencedColumnName = "id_pengajuan")
    private PengajuanAset pengajuan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_aset", referencedColumnName = "id_aset")
    private Aset aset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nip_pegawai", referencedColumnName = "nip")
    private Pegawai pegawai;

    @Column(name = "jenis_log", length = 100)
    private String jenisLog;

    @Column(name = "isi_log", columnDefinition = "TEXT")
    private String isiLog;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Constructor untuk log umum
    public LogRiwayat(Pegawai pegawai, String jenisLog, String isiLog) {
        this.pegawai = pegawai;
        this.jenisLog = jenisLog;
        this.isiLog = isiLog;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor untuk log terkait Aset
    public LogRiwayat(Pegawai pegawai, Aset aset, String jenisLog, String isiLog) {
        this(pegawai, jenisLog, isiLog);
        this.aset = aset;
    }

    // Constructor untuk log terkait Permohonan
    public LogRiwayat(Pegawai pegawai, PermohonanAset permohonan, String jenisLog, String isiLog) {
        this(pegawai, jenisLog, isiLog);
        this.permohonan = permohonan;
    }

    // Constructor untuk log terkait Pengajuan
    public LogRiwayat(Pegawai pegawai, PengajuanAset pengajuan, String jenisLog, String isiLog) {
        this(pegawai, jenisLog, isiLog);
        this.pengajuan = pengajuan;
    }
}