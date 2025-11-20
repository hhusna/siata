package com.siata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class LogDto {
    private Long idLog;

    @JsonProperty("permohonan")
    private PermohonanDto permohonanDto;

    @JsonProperty("pengajuan")
    private PengajuanDto pengajuanDto;

    @JsonProperty("aset")
    private AssetDto assetDto;

    @JsonProperty("pegawai")
    private PegawaiDto pegawaiDto;

    private String jenisLog;
    private String isiLog;
    private LocalDateTime timestamp;

    public LogDto() {
    }

    public Long getIdLog() {
        return idLog;
    }

    public void setIdLog(Long idLog) {
        this.idLog = idLog;
    }

    public PermohonanDto getPermohonanDto() {
        return permohonanDto;
    }

    public void setPermohonanDto(PermohonanDto permohonanDto) {
        this.permohonanDto = permohonanDto;
    }

    public PengajuanDto getPengajuanDto() {
        return pengajuanDto;
    }

    public void setPengajuanDto(PengajuanDto pengajuanDto) {
        this.pengajuanDto = pengajuanDto;
    }

    public AssetDto getAssetDto() {
        return assetDto;
    }

    public void setAssetDto(AssetDto assetDto) {
        this.assetDto = assetDto;
    }

    public PegawaiDto getPegawaiDto() {
        return pegawaiDto;
    }

    public void setPegawaiDto(PegawaiDto pegawaiDto) {
        this.pegawaiDto = pegawaiDto;
    }

    public String getJenisLog() {
        return jenisLog;
    }

    public void setJenisLog(String jenisLog) {
        this.jenisLog = jenisLog;
    }

    public String getIsiLog() {
        return isiLog;
    }

    public void setIsiLog(String isiLog) {
        this.isiLog = isiLog;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
