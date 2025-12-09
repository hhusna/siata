package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ApprovalLogDto {
    @JsonProperty("namaPegawai")
    private String namaPegawai;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("status")
    private String status; // "Disetujui" or "Ditolak"
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("catatan")
    private String catatan; // Approver's message
    
    @JsonProperty("lampiran")
    private String lampiran; // Attachment file path

    public ApprovalLogDto() {}

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
}
