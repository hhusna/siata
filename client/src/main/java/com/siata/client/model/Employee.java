package com.siata.client.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String nip;
    private String namaLengkap;
    private String jabatan;
    private String unit;
    private List<String> asetDimiliki;

    public Employee() {
        this.asetDimiliki = new ArrayList<>();
    }

    public Employee(String nip, String namaLengkap, String jabatan, String unit) {
        this.nip = nip;
        this.namaLengkap = namaLengkap;
        this.jabatan = jabatan;
        this.unit = unit;
        this.asetDimiliki = new ArrayList<>();
    }

    public Employee(String nip, String namaLengkap, String jabatan, String unit, List<String> asetDimiliki) {
        this.nip = nip;
        this.namaLengkap = namaLengkap;
        this.jabatan = jabatan;
        this.unit = unit;
        this.asetDimiliki = asetDimiliki == null ? new ArrayList<>() : new ArrayList<>(asetDimiliki);
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<String> getAsetDimiliki() {
        if (asetDimiliki == null) {
            asetDimiliki = new ArrayList<>();
        }
        return asetDimiliki;
    }

    public void setAsetDimiliki(List<String> asetDimiliki) {
        this.asetDimiliki = asetDimiliki == null ? new ArrayList<>() : new ArrayList<>(asetDimiliki);
    }

    public String getAsetDimilikiSummary() {
        int total = getAsetDimiliki().size();
        return total + " aset";
    }
}

