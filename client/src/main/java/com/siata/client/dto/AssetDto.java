package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siata.client.model.Employee;

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

@JsonIgnoreProperties
public class AssetDto {
    private int idAset;
    private String KodeAset;
    private String jenisAset;
    private String merkAset;
    private LocalDate tanggalPerolehan;
    private long hargaAset;
    private String kondisi;
    private String statusPemakaian;

    @JsonProperty("pegawai")
    private PegawaiDto pegawaiDto;

    public AssetDto() {
    }

    public int getIdAset() {
        return idAset;
    }

    public void setIdAset(int idAset) {
        this.idAset = idAset;
    }

    public String getKodeAset() {
        return KodeAset;
    }

    public void setKodeAset(String kodeAset) {
        KodeAset = kodeAset;
    }

    public String getJenisAset() {
        return jenisAset;
    }

    public void setJenisAset(String jenisAset) {
        this.jenisAset = jenisAset;
    }

    public String getMerkAset() {
        return merkAset;
    }

    public void setMerkAset(String merkAset) {
        this.merkAset = merkAset;
    }

    public LocalDate getTanggalPerolehan() {
        return tanggalPerolehan;
    }

    public void setTanggalPerolehan(LocalDate tanggalPerolehan) {
        this.tanggalPerolehan = tanggalPerolehan;
    }

    public long getHargaAset() {
        return hargaAset;
    }

    public void setHargaAset(long hargaAset) {
        this.hargaAset = hargaAset;
    }

    public String getKondisi() {
        return kondisi;
    }

    public void setKondisi(String kondisi) {
        this.kondisi = kondisi;
    }

    public String getStatusPemakaian() {
        return statusPemakaian;
    }

    public void setStatusPemakaian(String statusPemakaian) {
        this.statusPemakaian = statusPemakaian;
    }

    public PegawaiDto getPegawaiDto() {
        return pegawaiDto;
    }

    public void setPegawaiDto(PegawaiDto pegawaiDto) {
        this.pegawaiDto = pegawaiDto;
    }
}
