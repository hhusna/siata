package com.siata.client.dto;

public class PengajuanDto {
    private Long idPengajuan;
    private PegawaiDto pegawaiDto;
    private String namaPengaju;
    private String jenisAset;
    private Long jumlah;
    private String deskripsi;
    private String tujuanPenggunaan;
    private String prioritas;
    private String statusPersetujuan;

    public PengajuanDto() {
    }

    public PengajuanDto(Long idPengajuan, PegawaiDto pegawaiDto, String namaPengaju, String jenisAset, Long jumlah, String deskripsi, String tujuanPenggunaan, String prioritas, String statusPersetujuan) {
        this.idPengajuan = idPengajuan;
        this.pegawaiDto = pegawaiDto;
        this.namaPengaju = namaPengaju;
        this.jenisAset = jenisAset;
        this.jumlah = jumlah;
        this.deskripsi = deskripsi;
        this.tujuanPenggunaan = tujuanPenggunaan;
        this.prioritas = prioritas;
        this.statusPersetujuan = statusPersetujuan;
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

    public String getNamaPengaju() {
        return namaPengaju;
    }

    public void setNamaPengaju(String namaPengaju) {
        this.namaPengaju = namaPengaju;
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

    public String getStatusPersetujuan() {
        return statusPersetujuan;
    }

    public void setStatusPersetujuan(String statusPersetujuan) {
        this.statusPersetujuan = statusPersetujuan;
    }
}
