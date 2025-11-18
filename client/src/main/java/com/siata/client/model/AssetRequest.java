package com.siata.client.model;

import java.time.LocalDate;

public class AssetRequest {
    private String noPermohonan;
    private LocalDate tanggal;
    private String pemohon;
    private String unit;
    private String jenisAset;
    private int jumlah;
    private String prioritas; // Tinggi, Sedang, Rendah
    private String tipe; // "Permohonan" atau "Pengajuan"
    private String status; // Pending, Disetujui Direktur, Disetujui PPK, Ditolak
    private String deskripsi;
    private String tujuanPenggunaan;

    public AssetRequest() {
    }

    public AssetRequest(String noPermohonan, LocalDate tanggal, String pemohon, String unit, String jenisAset, int jumlah, String prioritas) {
        this.noPermohonan = noPermohonan;
        this.tanggal = tanggal;
        this.pemohon = pemohon;
        this.unit = unit;
        this.jenisAset = jenisAset;
        this.jumlah = jumlah;
        this.prioritas = prioritas;
        this.status = "Pending";
    }

    public AssetRequest(String noPermohonan, LocalDate tanggal, String pemohon, String unit, String jenisAset, int jumlah, String prioritas, String tipe, String deskripsi, String tujuanPenggunaan) {
        this.noPermohonan = noPermohonan;
        this.tanggal = tanggal;
        this.pemohon = pemohon;
        this.unit = unit;
        this.jenisAset = jenisAset;
        this.jumlah = jumlah;
        this.prioritas = prioritas;
        this.tipe = tipe;
        this.deskripsi = deskripsi;
        this.tujuanPenggunaan = tujuanPenggunaan;
        this.status = "Pending";
    }

    public String getNoPermohonan() {
        return noPermohonan;
    }

    public void setNoPermohonan(String noPermohonan) {
        this.noPermohonan = noPermohonan;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public String getPemohon() {
        return pemohon;
    }

    public void setPemohon(String pemohon) {
        this.pemohon = pemohon;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getJenisAset() {
        return jenisAset;
    }

    public void setJenisAset(String jenisAset) {
        this.jenisAset = jenisAset;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getTujuanPenggunaan() {
        return tujuanPenggunaan;
    }

    public void setTujuanPenggunaan(String tujuanPenggunaan) {
        this.tujuanPenggunaan = tujuanPenggunaan;
    }
}

