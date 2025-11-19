package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.siata.client.model.Employee;

import java.util.Date;

@JsonIgnoreProperties
public class AssetDto {
    private int idAset;
    private String KodeAset;
    private String jenisAset;
    private String merkAset;
    private Date tanggalPerolehan;
    private int hargaAset;
    private String kondisi;
    private String statusPemakaian;
    private PegawaiDto pegawai;

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

    public Date getTanggalPerolehan() {
        return tanggalPerolehan;
    }

    public void setTanggalPerolehan(Date tanggalPerolehan) {
        this.tanggalPerolehan = tanggalPerolehan;
    }

    public int getHargaAset() {
        return hargaAset;
    }

    public void setHargaAset(int hargaAset) {
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

    public PegawaiDto getPegawai() {
        return pegawai;
    }

    public void setPegawai(PegawaiDto pegawai) {
        this.pegawai = pegawai;
    }
}
