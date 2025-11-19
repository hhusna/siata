package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class PegawaiDto {
    private int nip;
    private String nama;

    @JsonProperty("nama_subdir")
    @JsonAlias("namaSubdir")
    private String namaSubdir;
    private String jabatan;

    public PegawaiDto() {
    }

    public PegawaiDto(int nip, String nama, String namaSubdir, String jabatan) {
        this.nip = nip;
        this.nama = nama;
        this.namaSubdir = namaSubdir;
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
    @JsonAlias("namaSubdir")
    public String getNamaSubdir() {
        return namaSubdir;
    }

    @JsonProperty("nama_subdir")
    @JsonAlias("namaSubdir")
    public void setNamaSubdir(String namaSubdir) {
        this.namaSubdir = namaSubdir;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }
}
