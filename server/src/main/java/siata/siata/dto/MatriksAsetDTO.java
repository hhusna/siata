package siata.siata.dto;

import lombok.Data;
import siata.siata.entity.Pegawai;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MatriksAsetDTO {
    private Long nip;
    private String nama;
    private String namaSubdir;
    private String jabatan;
    private List<AsetSimpleDTO> asetList;

    public MatriksAsetDTO(Pegawai pegawai) {
        this.nip = pegawai.getNip();
        this.nama = pegawai.getNama();
        this.namaSubdir = pegawai.getNamaSubdir();
        this.jabatan = pegawai.getJabatan();

        if (pegawai.getAsetList() != null) {
            this.asetList = pegawai.getAsetList().stream()
                    .map(AsetSimpleDTO::new)
                    .collect(Collectors.toList());
        }
    }
}