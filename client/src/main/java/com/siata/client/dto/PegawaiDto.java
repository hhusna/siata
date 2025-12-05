package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PegawaiDto {

    @JsonProperty("nip")
    @JsonAlias("nip_pegawai")
    private Long nip;
    private String nama;

    @JsonProperty("namaSubdir")
    @JsonAlias("nama_subdir")
    private String namaSubdir;
    private String jabatan;

    @JsonProperty("isPpnpn")
    private Boolean isPpnpn = false;

    @JsonProperty("status")
    private String status = "AKTIF";

    public PegawaiDto() {
    }

    public PegawaiDto(Long nip, String nama, String namaSubdir, String jabatan) {
        this.nip = nip;
        this.nama = nama;
        this.namaSubdir = namaSubdir;
        this.jabatan = jabatan;
    }

    public Long getNip() {
        return nip;
    }

    public void setNip(Long nip) {
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

    public Boolean getIsPpnpn() {
        return isPpnpn;
    }

    public void setIsPpnpn(Boolean isPpnpn) {
        this.isPpnpn = isPpnpn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
