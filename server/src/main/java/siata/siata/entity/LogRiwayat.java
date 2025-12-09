package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "log_riwayat")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LogRiwayat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permohonan", referencedColumnName = "id_permohonan")
    private PermohonanAset permohonan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pengajuan", referencedColumnName = "id_pengajuan")
    private PengajuanAset pengajuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aset", referencedColumnName = "id_aset")
    private Aset aset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nip_pegawai", referencedColumnName = "nip")
    private Pegawai pegawai;

    @Column(name = "jenis_log", length = 100)
    @NotBlank(message = "Jenis log tidak boleh kosong")
    @Size(max = 100, message = "Jenis log maksimal 100 karakter")
    private String jenisLog;

    @Column(name = "isi_log", columnDefinition = "TEXT")
    @NotBlank(message = "Isi log tidak boleh kosong")
    @Size(max = 5000, message = "Isi log maksimal 5000 karakter")
    private String isiLog;

    @Column(name = "timestamp", nullable = false)
    @NotNull(message = "Timestamp tidak boleh kosong")
    private LocalDateTime timestamp;

    @Column(name = "catatan", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Catatan maksimal 1000 karakter")
    private String catatan; // Approver's message/notes

    @Column(name = "lampiran", length = 500)
    @Size(max = 500, message = "Path lampiran maksimal 500 karakter")
    private String lampiran; // File path to attachment

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