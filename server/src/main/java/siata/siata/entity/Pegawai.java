package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Data
@Entity
@Table(name = "pegawai")
public class Pegawai {

    @Id
    @Column(name = "nip", length = 18, unique = true)
    @NotNull(message = "NIP tidak boleh kosong")
    private Long nip;

    @Column(name = "nama", nullable = false, length = 100)
    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama harus 3-100 karakter")
    @Pattern(regexp = "[a-zA-Z\\s]+", message = "Nama hanya boleh berisi huruf dan spasi")
    private String nama;

    @Column(name = "nama_subdir", length = 100)
    private String namaSubdir;

    @Column(name = "jabatan", length = 100)
    @Size(min = 3, max = 100, message = "Jabatan minimal 3 karakter")
    @Pattern(regexp = "[a-zA-Z\\s]+", message = "Jabatan hanya boleh berisi huruf dan spasi")
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

    @Override
    public String toString() {
        return "Pegawai{nip=" + nip+ ", nama=" + nama + ", namaSubdir= " + namaSubdir +", jabatan=" + jabatan + "}";
    }
}
