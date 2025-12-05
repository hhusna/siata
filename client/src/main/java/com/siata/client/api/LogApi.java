package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.siata.client.config.ApiConfig;
import com.siata.client.dto.LogDto;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LogApi {

    public LogDto[] getLog() {
        LogDto[] logDtos = {};
        try {
            // Get fresh JWT token on each call
            String jwt = LoginSession.getJwt();
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = ApiConfig.getLogbookUrl();
            System.out.println("LogApi: Fetching logs from " + targetUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("LogApi: Response status = " + response.statusCode());
            
            if (response.statusCode() == 200) {
                String jsonResponse = response.body();
                System.out.println("LogApi: Received " + jsonResponse.length() + " bytes");

                logDtos = mapper.readValue(jsonResponse, LogDto[].class);
                System.out.println("LogApi: Parsed " + logDtos.length + " log entries");

                return logDtos;
            } else {
                System.err.println("LogApi: Failed with status " + response.statusCode());
                System.err.println("LogApi: Response body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("LogApi: Exception occurred");
            e.printStackTrace();
        }
        return logDtos;
    }
}
