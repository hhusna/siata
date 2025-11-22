package com.siata.client.dto;

public class DashboardDto {
    private long totalAset;
    private long asetSiapDilelang;
    private long asetRusakBerat;
    private long permohonanPending;
    private long pengajuanPending;
    private long asetNonAktif;
    private long asetAktif;
    private long asetDiajukanHapus;
    private long totalAsetDihapus;

    public DashboardDto() {
    }

    public long getTotalAset() {
        return totalAset;
    }

    public void setTotalAset(long totalAset) {
        this.totalAset = totalAset;
    }

    public long getAsetSiapDilelang() {
        return asetSiapDilelang;
    }

    public void setAsetSiapDilelang(long asetSiapDilelang) {
        this.asetSiapDilelang = asetSiapDilelang;
    }

    public long getAsetRusakBerat() {
        return asetRusakBerat;
    }

    public void setAsetRusakBerat(long asetRusakBerat) {
        this.asetRusakBerat = asetRusakBerat;
    }

    public long getPermohonanPending() {
        return permohonanPending;
    }

    public void setPermohonanPending(long permohonanPending) {
        this.permohonanPending = permohonanPending;
    }

    public long getPengajuanPending() {
        return pengajuanPending;
    }

    public void setPengajuanPending(long pengajuanPending) {
        this.pengajuanPending = pengajuanPending;
    }

    public long getAsetNonAktif() {
        return asetNonAktif;
    }

    public void setAsetNonAktif(long asetNonAktif) {
        this.asetNonAktif = asetNonAktif;
    }

    public long getAsetAktif() {
        return asetAktif;
    }

    public void setAsetAktif(long asetAktif) {
        this.asetAktif = asetAktif;
    }

    public long getAsetDiajukanHapus() {
        return asetDiajukanHapus;
    }

    public void setAsetDiajukanHapus(long asetDiajukanHapus) {
        this.asetDiajukanHapus = asetDiajukanHapus;
    }

    public long getTotalAsetDihapus() {
        return totalAsetDihapus;
    }

    public void setTotalAsetDihapus(long totalAsetDihapus) {
        this.totalAsetDihapus = totalAsetDihapus;
    }
}
