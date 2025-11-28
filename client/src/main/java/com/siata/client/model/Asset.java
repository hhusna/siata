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
}

