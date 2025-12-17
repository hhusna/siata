package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ApprovalLogDto {
    @JsonProperty("namaPegawai")
    private String namaPegawai;
    
    @JsonProperty("role")
    private String role; // The role being approved (PPBJ, PPK, Direktur)
    
    @JsonProperty("status")
    private String status; // "Disetujui" or "Ditolak"
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("catatan")
    private String catatan; // Approver's message
    
    @JsonProperty("lampiran")
    private String lampiran; // Nomor surat
    
    @JsonProperty("actualApproverRole")
    private String actualApproverRole; // The actual role of person who made approval

    public ApprovalLogDto() {}

    public ApprovalLogDto(String namaPegawai, String role, String status, LocalDateTime timestamp, 
                          String catatan, String lampiran, String actualApproverRole) {
        this.namaPegawai = namaPegawai;
        this.role = role;
        this.status = status;
        this.timestamp = timestamp;
        this.catatan = catatan;
        this.lampiran = lampiran;
        this.actualApproverRole = actualApproverRole;
    }

    public String getNamaPegawai() {
        return namaPegawai;
    }

    public void setNamaPegawai(String namaPegawai) {
        this.namaPegawai = namaPegawai;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public String getLampiran() {
        return lampiran;
    }

    public void setLampiran(String lampiran) {
        this.lampiran = lampiran;
    }
    
    public String getActualApproverRole() {
        return actualApproverRole;
    }

    public void setActualApproverRole(String actualApproverRole) {
        this.actualApproverRole = actualApproverRole;
    }
    
    /**
     * Check if this approval was delegated (made by Tim Manajemen Aset on behalf of another role)
     */
    public boolean isDelegated() {
        // Delegated ONLY if the actual approver is "Tim Manajemen Aset" AND target role is different
        return actualApproverRole != null && 
               "Tim Manajemen Aset".equals(actualApproverRole) && 
               role != null && !actualApproverRole.equals(role);
    }
}
