package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siata.client.config.ApiConfig;
import com.siata.client.dto.PegawaiDto;
import com.siata.client.dto.UserDto;
import com.siata.client.response.LoginResponse;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UserApi {

    public PegawaiDto getPegawaionSession() {

        String jwt = LoginSession.getJwt();
        PegawaiDto pegawaiDto = new PegawaiDto();
        try {
            ObjectMapper mapper = new ObjectMapper();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAuthUrl() + "/pegawai";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                pegawaiDto = mapper.readValue(response.body(), PegawaiDto.class);
                System.out.println("UserApi: PEGAWAI NAMANYA "+pegawaiDto.getNama());
                LoginSession.setPegawaiDto(pegawaiDto);
                return pegawaiDto;
            } else {
                System.out.println("UserApi: GAGAL MENDAPATKAN PEGAWAI "+ response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pegawaiDto;
    }
    public void login(String username, String password) {
        try {
            UserDto payload = new UserDto();
            payload.setUsername(username);
            payload.setPassword(password);

            ObjectMapper mapper = new ObjectMapper();

            String requestBodyJson = mapper.writeValueAsString(payload);
            System.out.println("Mengirim Payload: " + requestBodyJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAuthUrl() + "/login";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                LoginResponse loginResponse = mapper.readValue(response.body(), LoginResponse.class);
                System.out.println("Sukses! Login sebagai: " + loginResponse.getRole());

                LoginSession.setJwt(loginResponse.getAccessToken());
                LoginSession.setRole(loginResponse.getRole());
                LoginSession.setOriginalRole(loginResponse.getRole()); // Store original role from backend
                LoginSession.setUsername(loginResponse.getUsername());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean simulateRole(String newRole) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("role", newRole);
            
            String requestBody = mapper.writeValueAsString(payload);
            
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // Note: DevController mapped to /api/dev
            String targetUrl = ApiConfig.getBaseUrl() + "/api/dev/simulate-role";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + LoginSession.getJwt())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}