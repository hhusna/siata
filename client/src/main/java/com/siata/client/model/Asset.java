package com.siata.client.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Asset {
    private long idAset;
    private String kodeAset;
    private String jenisAset;
    private String merkBarang;

    private LocalDate tanggalPerolehan;
    private BigDecimal nilaiRupiah;
    private String kondisi;
    private String status;
    private String keterangan; // Pemegang
    private String Subdir;
    private Integer noAset;
    private boolean deleted; // Flag untuk penghapusan

    public Asset() {
        this.deleted = false;
        this.kondisi = "Baik";
        this.status = "Aktif";
    }

    public Asset(String kodeAset, String jenisAset, String merkBarang, String keterangan,
                 String Subdir, LocalDate tanggalPerolehan, BigDecimal nilaiRupiah,
                 String kondisi, String status) {
        this.kodeAset = kodeAset;
        this.jenisAset = jenisAset;
        this.merkBarang = merkBarang;
        this.keterangan = keterangan;
        this.Subdir = Subdir;
        this.tanggalPerolehan = tanggalPerolehan;
        this.nilaiRupiah = nilaiRupiah;
        this.kondisi = kondisi;
        this.status = status;
        this.deleted = false;
    }

    public long getIdAset() {
        return idAset;
    }

    public void setIdAset(long idAset) {
        this.idAset = idAset;
    }

    public String getKodeAset() {
        return kodeAset;
    }

    public void setKodeAset(String kodeAset) {
        this.kodeAset = kodeAset;
    }

    public String getJenisAset() {
        return jenisAset;
    }

    public void setJenisAset(String jenisAset) {
        this.jenisAset = jenisAset;
    }

    public String getMerkBarang() {
        return merkBarang;
    }

    public void setMerkBarang(String merkBarang) {
        this.merkBarang = merkBarang;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getSubdir() {
        return Subdir;
    }

    public void setSubdir(String Subdir) {
        this.Subdir = Subdir;
    }

    public LocalDate getTanggalPerolehan() {
        return tanggalPerolehan;
    }

    public void setTanggalPerolehan(LocalDate tanggalPerolehan) {
        this.tanggalPerolehan = tanggalPerolehan;
    }

    public BigDecimal getNilaiRupiah() {
        return nilaiRupiah;
    }

    public void setNilaiRupiah(BigDecimal nilaiRupiah) {
        this.nilaiRupiah = nilaiRupiah;
    }

    public String getKondisi() {
        return kondisi;
    }

    public void setKondisi(String kondisi) {
        this.kondisi = kondisi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public String getNamaAset() {
        // Gabungkan jenis dan merk untuk nama aset
        if (merkBarang != null && !merkBarang.isEmpty()) {
            return jenisAset + " " + merkBarang;
        }
        return jenisAset;
    }

    /**
     * Menghitung kesiapan lelang aset
     * Logika: Usia > 4 tahun DAN status = "Non Aktif" -> "Siap"
     * @return "Siap" atau "Belum"
     */
    public String getKesiapanLelang() {
        if (tanggalPerolehan == null || status == null) {
            return "Belum";
        }
        
        // Hitung usia aset dalam tahun
        LocalDate now = LocalDate.now();
        long usiaTahun = java.time.temporal.ChronoUnit.YEARS.between(tanggalPerolehan, now);
        
        // Cek kondisi: usia > 4 tahun DAN status = "Non Aktif"
        if (usiaTahun > 4 && "Non Aktif".equals(status)) {
            return "Siap";
        }
        
        return "Belum";
    }

    public Integer getNoAset() {
        return noAset;
    }

    public void setNoAset(Integer noAset) {
        this.noAset = noAset;
    }

    private String dipakai = "FALSE"; // Default FALSE

    public String getDipakai() {
        return dipakai;
    }

    public void setDipakai(String dipakai) {
        this.dipakai = dipakai;
    }
    
    public String getDipakaiString() {
        return "TRUE".equalsIgnoreCase(dipakai) ? "Ya" : "Tidak";
    }

    /**
     * Computed: Tua
     * Untuk semua jenis aset SELAIN Mobil dan Motor: 1 jika umur > 4 tahun DAN status = AKTIF
     * Mobil dan Motor selalu 0
     */
    public Integer getTua() {
        if (tanggalPerolehan == null || jenisAset == null || status == null) {
            return 0;
        }
        // Mobil dan Motor selalu 0
        String jenis = jenisAset.toLowerCase();
        if (jenis.contains("mobil") || jenis.contains("motor")) {
            return 0;
        }
        // Status harus AKTIF
        if (!"AKTIF".equalsIgnoreCase(status.replace(" ", ""))) {
            return 0;
        }
        long usiaTahun = java.time.temporal.ChronoUnit.YEARS.between(tanggalPerolehan, LocalDate.now());
        return usiaTahun > 4 ? 1 : 0;
    }

    public String getTuaString() {
        return getTua() == 1 ? "Ya" : "Tidak";
    }

    public boolean isTua() {
        return getTua() == 1;
    }

    /**
     * Computed: Akan Tua (formerly akanSiapLelang)
     * Untuk semua jenis aset SELAIN Mobil dan Motor: 1 jika umur > 3 tahun DAN <= 4 tahun
     * Mobil dan Motor selalu 0
     */
    public Integer getAkanTua() {
        if (tanggalPerolehan == null || jenisAset == null) {
            return 0;
        }
        // Mobil dan Motor selalu 0
        String jenis = jenisAset.toLowerCase();
        if (jenis.contains("mobil") || jenis.contains("motor")) {
            return 0;
        }
        long usiaTahun = java.time.temporal.ChronoUnit.YEARS.between(tanggalPerolehan, LocalDate.now());
        return (usiaTahun > 3 && usiaTahun <= 4) ? 1 : 0;
    }

    public String getAkanTuaString() {
        return getAkanTua() == 1 ? "Ya" : "Tidak";
    }

    /**
     * Computed: Siap Lelang (NEW)
     * Validasi: umur > 4 tahun AND nonaktif AND dihapus
     * 1 jika umur > 4 tahun DAN status NON AKTIF DAN deleted = true
     */
    public Integer getSiapLelang() {
        if (tanggalPerolehan == null || jenisAset == null || status == null) {
            return 0;
        }
        // Mobil dan Motor selalu 0
        String jenis = jenisAset.toLowerCase();
        if (jenis.contains("mobil") || jenis.contains("motor")) {
            return 0;
        }
        // Hitung usia
        long usiaTahun = java.time.temporal.ChronoUnit.YEARS.between(tanggalPerolehan, LocalDate.now());
        // Cek: umur > 4 tahun AND non-aktif AND deleted
        boolean isOld = usiaTahun > 4;
        boolean isNonActive = "NON AKTIF".equalsIgnoreCase(status) || "NONAKTIF".equalsIgnoreCase(status.replace(" ", ""));
        return (isOld && isNonActive && deleted) ? 1 : 0;
    }

    public String getSiapLelangString() {
        return getSiapLelang() == 1 ? "Ya" : "Tidak";
    }
}
