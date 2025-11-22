package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class PegawaiDto {

    @JsonProperty("nip")
    @JsonAlias("nip_pegawai")
    private long nip;
    private String nama;

    @JsonProperty("namaSubdir")
    @JsonAlias("nama_subdir")
    private String namaSubdir;
    private String jabatan;

    public PegawaiDto() {
    }

    public PegawaiDto(long nip, String nama, String namaSubdir, String jabatan) {
        this.nip = nip;
        this.nama = nama;
        this.namaSubdir = namaSubdir;
        this.jabatan = jabatan;
    }

    public long getNip() {
        return nip;
    }

    public void setNip(long nip) {
        this.nip = nip;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    @JsonProperty("namaSubdir")
    @JsonAlias("nama_subdir")
    public String getNamaSubdir() {
        return namaSubdir;
    }

    @JsonProperty("namaSubdir")
    @JsonAlias("nama_subdir")
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
