package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties
public class AssetDtoForRequest {

    private String kodeAset;
    private String jenisAset;
    private String merkAset;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate tanggalPerolehan;
    private BigDecimal hargaAset;
    private String kondisi;
    private String statusPemakaian;
    private String subdirektorat;
    private Integer noAset;

    @JsonProperty("pegawai")
    private PegawaiDto pegawaiDto;

    public AssetDtoForRequest() {
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

    public BigDecimal getHargaAset() {
        return hargaAset;
    }

    public void setHargaAset(BigDecimal hargaAset) {
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

    public String getSubdirektorat() {
        return subdirektorat;
    }

    public void setSubdirektorat(String subdirektorat) {
        this.subdirektorat = subdirektorat;
    }

    public Integer getNoAset() {
        return noAset;
    }

    public void setNoAset(Integer noAset) {
        this.noAset = noAset;
    }

    private String dipakai;
    public String getDipakai() { return dipakai; }
    public void setDipakai(String dipakai) { this.dipakai = dipakai; }
}
