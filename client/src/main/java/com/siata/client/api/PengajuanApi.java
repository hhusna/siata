package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.siata.client.dto.PengajuanDto;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class PengajuanApi {
    String jwt = LoginSession.getJwt();

    public boolean deletePengajuan(Long idPengajuan) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/pengajuan/"+Long.toString(idPengajuan);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("PengajuanApi: BERHASIL DIHAPUS!");

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean patchStatus(Long idPengajuan, String status) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            Map<String, String> data = Map.of(
                    "statusPersetujuan", status
            );

            String requestBody = mapper.writeValueAsString(data);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/pengajuan/"+Long.toString(idPengajuan)+"/status";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                System.out.println("PengajuanApi: BERHASIL DI PATCH!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public PengajuanDto[] getPengajuan() {
        PengajuanDto[] pengajuanDtos = {};
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/pengajuan";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                pengajuanDtos = mapper.readValue(response.body(), PengajuanDto[].class);

                return pengajuanDtos;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pengajuanDtos;
    }

    public boolean createPengajuan(PengajuanDto payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String requestBodyJson = mapper.writeValueAsString(payload);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/pengajuan";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("PengajuanApi: Pengajuan berhasil ditambahkan!");
                return true;
            } else {
                System.out.println("PengajuanApi: Pengajuan gagal ditambahkan!" + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
