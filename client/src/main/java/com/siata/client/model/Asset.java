package com.siata.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Asset {
    private int idAset;
    private String kodeAset;
    private String jenisAset;
    private String merkBarang;

    private LocalDate tanggalPerolehan;
    private double nilaiRupiah;
    private String kondisi;
    private String status;
    private String keterangan; // Pemegang
    private String subdit;
    private boolean deleted; // Flag untuk penghapusan

    public Asset() {
        this.deleted = false;
        this.kondisi = "Baik";
        this.status = "Tersedia";
    }

    public Asset(String kodeAset, String jenisAset, String merkBarang, String keterangan,
                 String subdit, LocalDate tanggalPerolehan, double nilaiRupiah,
                 String kondisi, String status) {
        this.kodeAset = kodeAset;
        this.jenisAset = jenisAset;
        this.merkBarang = merkBarang;
        this.keterangan = keterangan;
        this.subdit = subdit;
        this.tanggalPerolehan = tanggalPerolehan;
        this.nilaiRupiah = nilaiRupiah;
        this.kondisi = kondisi;
        this.status = status;
        this.deleted = false;
    }

    public int getIdAset() {
        return idAset;
    }

    public void setIdAset(int idAset) {
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

    public String getSubdit() {
        return subdit;
    }

    public void setSubdit(String subdit) {
        this.subdit = subdit;
    }

    public LocalDate getTanggalPerolehan() {
        return tanggalPerolehan;
    }

    public void setTanggalPerolehan(LocalDate tanggalPerolehan) {
        this.tanggalPerolehan = tanggalPerolehan;
    }

    public double getNilaiRupiah() {
        return nilaiRupiah;
    }

    public void setNilaiRupiah(double nilaiRupiah) {
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
}

