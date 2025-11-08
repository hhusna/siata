package siata.siata.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Data
@Entity
@Table(name = "pegawai")
public class Pegawai {

    @Id
    @Column(name = "nip", length = 20)
    private Long nip;

    @Column(name = "nama", nullable = false, length = 100)
    private String nama;

    @Column(name = "nama_subdir", length = 100)
    private String namaSubdir;

    @Column(name = "jabatan", length = 100)
    private String jabatan;

    @JsonIgnore
    @OneToOne(mappedBy = "pegawai", cascade = CascadeType.ALL)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "pegawai", cascade = CascadeType.ALL)
    private List<Aset> asetList;

    @JsonIgnore
    @OneToMany(mappedBy = "pegawai", cascade = CascadeType.ALL)
    private List<PermohonanAset> permohonanList;

    @JsonIgnore
    @OneToMany(mappedBy = "pegawai", cascade = CascadeType.ALL)
    private List<PengajuanAset> pengajuanList;
}
