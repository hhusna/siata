package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.siata.client.dto.AssetDto;
import com.siata.client.dto.AssetDtoForRequest;
import com.siata.client.dto.DashboardDto;
import com.siata.client.dto.PegawaiDto;
import com.siata.client.model.Asset;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AssetApi {
    String jwt = LoginSession.getJwt();

    public boolean putAsset(AssetDto payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String requestBodyJson = mapper.writeValueAsString(payload);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            int idAset = payload.getIdAset();

            String targetUrl = "http://localhost:8080/api/aset/"+Long.toString(idAset);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Sukses Mengedit Asset!");

                return true;
            }else {
                System.out.println("AssetApi: Gagal Mengedit "+ payload.getIdAset()+" " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public DashboardDto getDashboard() {
        DashboardDto dashboardDto = new DashboardDto();
        try {
            ObjectMapper mapper = new ObjectMapper();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/dashboard/stats";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                System.out.println("AssetApi: BERHASIL GET DASHBOARD");

                dashboardDto = mapper.readValue(response.body(), DashboardDto.class);
                return dashboardDto;
            } else {
                System.out.println("AssetApi: GAGAL GET DASHBOARD"+ response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dashboardDto;
    }

    public boolean deleteAssetById(int idAset) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/aset/hapus/" + Integer.toString(idAset);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                System.out.println("AssetApi: Berhasil delete!!");
                return true;
            } else {
                System.out.println("AssetApi: Gagal delete "+ response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public AssetDto getAssetById(int idAset) {
        AssetDto assetDto = new AssetDto();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/aset/" + Integer.toString(idAset);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                String jsonResponse = response.body();

                assetDto = mapper.readValue(jsonResponse, AssetDto.class);

                return assetDto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assetDto;
    }

    public AssetDto[] getAsset() {
        AssetDto[] listAsset = {};

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/aset";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                String jsonResponse = response.body();

                listAsset = mapper.readValue(jsonResponse, AssetDto[].class);

                return listAsset;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listAsset;
    }

    public int tambahAsset(AssetDtoForRequest payload) {
        AssetDto assetDto = new AssetDto();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String requestBodyJson = mapper.writeValueAsString(payload);

            System.out.println("AssetApi: Mengirim Payload -> " + requestBodyJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/aset";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Sukses Menambahkan Asset!");

                int idAset = mapper.readValue(response.body(), AssetDto.class).getIdAset();

                return idAset;
            }else {
                System.out.println("AssetApi: Gagal mengirim " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
