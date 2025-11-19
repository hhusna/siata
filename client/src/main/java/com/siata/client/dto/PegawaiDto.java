package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class PegawaiDto {
    private int nip;
    private String nama;

    @JsonProperty("nama_subdir")
    private String nama_subdir;
    private String jabatan;

    public PegawaiDto() {
    }

    public PegawaiDto(int nip, String nama, String namaSubdir, String jabatan) {
        this.nip = nip;
        this.nama = nama;
        this.nama_subdir = namaSubdir;
        this.jabatan = jabatan;
    }

    public int getNip() {
        return nip;
    }

    public void setNip(int nip) {
        this.nip = nip;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    @JsonProperty("nama_subdir")
    public String getNamaSubdir() {
        return nama_subdir;
    }

    @JsonProperty("nama_subdir")
    public void setNamaSubdir(String namaSubdir) {
        this.nama_subdir = namaSubdir;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }
}
