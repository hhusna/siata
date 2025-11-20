package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.siata.client.dto.PengajuanDto;
import com.siata.client.dto.PermohonanDto;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PermohonanApi {
    String jwt = LoginSession.getJwt();

    public PermohonanDto[] getPermohonan() {
        PermohonanDto[] permohonanDtos = {};
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/permohonan";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                permohonanDtos = mapper.readValue(response.body(), PermohonanDto[].class);

                return permohonanDtos;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return permohonanDtos;
    }

    public boolean createPermohonan(PermohonanDto permohonanDto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String requestBody = mapper.writeValueAsString(permohonanDto);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/permohonan";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("PermohonanApi: Payload->" + requestBody);
            if (response.statusCode()==200) {
                System.out.println("PermohonanApi: Permohonan berhasil dibuat");
                return true;
            } else {
                System.out.println("PengajuanApi: Pengajuan gagal ditambahkan!" + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
