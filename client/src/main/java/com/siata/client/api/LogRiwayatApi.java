package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.siata.client.config.ApiConfig;
import com.siata.client.dto.ApprovalLogDto;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LogRiwayatApi {
    String jwt = LoginSession.getJwt();

    public ApprovalLogDto[] getApprovalLogs(Long permohonanId, Long pengajuanId) {
        ApprovalLogDto[] logs = {};
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = ApiConfig.getLogbookUrl() + "/approval-logs?";
            if (permohonanId != null) {
                targetUrl += "permohonanId=" + permohonanId;
            } else if (pengajuanId != null) {
                targetUrl += "pengajuanId=" + pengajuanId;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                logs = mapper.readValue(response.body(), ApprovalLogDto[].class);
                System.out.println("LogRiwayatApi: Berhasil ambil " + logs.length + " approval logs");
            } else {
                System.out.println("LogRiwayatApi: Gagal ambil logs. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }
}
