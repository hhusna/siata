package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siata.client.MainApplication;
import com.siata.client.dto.UserDto;
import com.siata.client.response.LoginResponse;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.prefs.Preferences;

public class UserApi {
    public void login(String username, String password) {
        try {
            UserDto payload = new UserDto();
            payload.setUsername(username);
            payload.setPassword(password);

            ObjectMapper mapper = new ObjectMapper();

            String requestBodyJson = mapper.writeValueAsString(payload);
            System.out.println("Mengirim Payload: " + requestBodyJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/auth/login";

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
                LoginSession.setUsername(loginResponse.getUsername());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}