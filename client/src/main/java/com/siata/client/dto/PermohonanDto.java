package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class PermohonanDto {
    private Long idPermohonan;
    private String kodePermohonan;

    @JsonProperty("pegawai")
    private PegawaiDto pegawaiDto;
    private String jenisAset;
    private int jumlah;
    private String deskripsi;
    private String tujuanPenggunaan;
    private String prioritas;
    private String statusPersetujuan;
    private LocalDate timestamp;
    private String namaPemohon;
    private String unit;

    public String getKodePermohonan() {
        return kodePermohonan;
    }

    public void setKodePermohonan(String kodePermohonan) {
        this.kodePermohonan = kodePermohonan;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public PermohonanDto() {
    }

    public Long getIdPermohonan() {
        return idPermohonan;
    }

    public void setIdPermohonan(Long idPermohonan) {
        this.idPermohonan = idPermohonan;
    }

    public PegawaiDto getPegawaiDto() {
        return pegawaiDto;
    }

    public void setPegawaiDto(PegawaiDto pegawaiDto) {
        this.pegawaiDto = pegawaiDto;
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

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getStatusPersetujuan() {
        return statusPersetujuan;
    }

    public void setStatusPersetujuan(String statusPersetujuan) {
        this.statusPersetujuan = statusPersetujuan;
    }

    public String getNamaPemohon() {
        return namaPemohon;
    }

    public void setNamaPemohon(String namaPemohon) {
        this.namaPemohon = namaPemohon;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
