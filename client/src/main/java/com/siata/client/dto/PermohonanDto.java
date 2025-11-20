package com.siata.client.dto;

public class PermohonanDto {
    private Long idPermohonan;
    private PegawaiDto pegawaiDto;
    private String jenisAset;
    private Long jumlah;
    private String deskripsi;
    private String tujuanPenggunaan;
    private String prioritas;
    private String statusPengajuan;

    public PermohonanDto(Long idPermohonan, PegawaiDto pegawaiDto, String jenisAset, Long jumlah, String deskripsi, String tujuanPenggunaan, String prioritas, String statusPengajuan) {
        this.idPermohonan = idPermohonan;
        this.pegawaiDto = pegawaiDto;
        this.jenisAset = jenisAset;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
        this.tujuanPenggunaan = tujuanPenggunaan;
        this.prioritas = prioritas;
        this.statusPengajuan = statusPengajuan;
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

    public Long getJumlah() {
        return jumlah;
    }

    public void setJumlah(Long jumlah) {
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

    public String getStatusPengajuan() {
        return statusPengajuan;
    }

    public void setStatusPengajuan(String statusPengajuan) {
        this.statusPengajuan = statusPengajuan;
    }
}
