package com.siata.client.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String nip;
    private String namaLengkap;
    private String unit;
    private List<String> asetDimiliki; // Deprecated - akan diambil dari manajemen aset

    public Employee() {
        this.asetDimiliki = new ArrayList<>();
    }

    public Employee(String nip, String namaLengkap, String unit) {
        this.nip = nip;
        this.namaLengkap = namaLengkap;
        this.unit = unit;
        this.asetDimiliki = new ArrayList<>();
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * @deprecated Gunakan getAsetDimilikiFromManagement() dari DataService
     */
    @Deprecated
    public List<String> getAsetDimiliki() {
        if (asetDimiliki == null) {
            asetDimiliki = new ArrayList<>();
        }
        return asetDimiliki;
    }

    public void setAsetDimiliki(List<String> asetDimiliki) {
        this.asetDimiliki = asetDimiliki == null ? new ArrayList<>() : new ArrayList<>(asetDimiliki);
    }

    /**
     * Summary aset yang dimiliki - akan diisi dari DataService
     * Format: "5 aset" atau "0 aset"
     */
    public String getAsetDimilikiSummary() {
        // Akan di-override di EmployeeManagementView dengan data dari DataService
        int total = getAsetDimiliki().size();
        return total + " aset";
    }
}

