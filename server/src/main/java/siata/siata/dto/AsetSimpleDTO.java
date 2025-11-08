package siata.siata.dto;

import lombok.Data;
import siata.siata.entity.Aset;

import java.time.LocalDate;

@Data
public class AsetSimpleDTO {
    private Long idAset;
    private String kodeAset;
    private String jenisAset;
    private String merkAset;
    private String kondisi;
    private String statusPemakaian;
    private LocalDate tanggalPerolehan;

    public AsetSimpleDTO(Aset aset) {
        this.idAset = aset.getIdAset();
        this.kodeAset = aset.getKodeAset();
        this.jenisAset = aset.getJenisAset();
        this.merkAset = aset.getMerkAset();
        this.kondisi = aset.getKondisi();
        this.statusPemakaian = aset.getStatusPemakaian();
        this.tanggalPerolehan = aset.getTanggalPerolehan();
    }
}