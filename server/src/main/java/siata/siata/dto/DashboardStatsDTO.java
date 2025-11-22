package siata.siata.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardStatsDTO {
    private long totalAset;
    private long asetSiapDilelang;
    private long asetRusakBerat;
    private long permohonanPending;
    private long pengajuanPending;

    private long asetNonAktif;
    private long asetAktif;
    private long asetDiajukanHapus;
    private long totalAsetDihapus;

    // Mungkin data lain yang relevan untuk dashboard
    // Map<String, Long> asetByJenis;
}