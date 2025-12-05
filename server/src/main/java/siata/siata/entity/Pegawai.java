package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Entity
@Table(name = "pegawai")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pegawai {

    @Id
    @Column(name = "nip", length = 18, unique = true)
    private Long nip;

    @Column(name = "nama", nullable = false, length = 100)
    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama harus 3-100 karakter")
    @Pattern(regexp = "[a-zA-Z\\s.,'-]+", message = "Nama hanya boleh berisi huruf, spasi, titik, koma, apostrof, dan strip")
    private String nama;

    @Column(name = "nama_subdir", length = 100)
    private String namaSubdir;

    // Status pegawai: ASN (has NIP) or PPNPN (no NIP, uses generated ID)
    @Column(name = "is_ppnpn")
    private Boolean isPpnpn = false;

    // Status aktif pegawai: AKTIF atau NONAKTIF
    @Column(name = "status", length = 20)
    private String status = "AKTIF";

    @JsonIgnore
    @OneToOne(mappedBy = "pegawai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "pegawai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Aset> asetList;

    @JsonIgnore
    @OneToMany(mappedBy = "pegawai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PermohonanAset> permohonanList;

    @JsonIgnore
    @OneToMany(mappedBy = "pegawai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PengajuanAset> pengajuanList;

    @Override
    public String toString() {
        return "Pegawai{nip=" + nip + ", nama=" + nama + ", namaSubdir= " + namaSubdir + ", isPpnpn=" + isPpnpn + "}";
    }
}
