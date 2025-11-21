package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class PengajuanDto {
    private Long idPengajuan;
    private String kodePengajuan;

    @JsonProperty("pegawai")
    private PegawaiDto pegawaiDto;

    private String jenisAset;
    private int jumlah;
    private String deskripsi;
    private String tujuanPenggunaan;
    private String prioritas;
    private String statusPersetujuan;
    private LocalDate timestamp;
    private String namaPengaju;
    private String unit;

    public String getKodePengajuan() {
        return kodePengajuan;
    }

    public void setKodePengajuan(String kodePengajuan) {
        this.kodePengajuan = kodePengajuan;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public PengajuanDto() {
    }

    public Long getIdPengajuan() {
        return idPengajuan;
    }

    public void setIdPengajuan(Long idPengajuan) {
        this.idPengajuan = idPengajuan;
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

    public String getNamaPengaju() {
        return namaPengaju;
    }

    public void setNamaPengaju(String namaPengaju) {
        this.namaPengaju = namaPengaju;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
