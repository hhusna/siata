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
        return getLogWithLimit(0); // 0 = no limit
    }
    
    /**
     * Fetches log entries with a limit for display
     * @param limit Maximum number of entries to fetch (0 = all)
     */
    public LogDto[] getLogWithLimit(int limit) {
        LogDto[] logDtos = {};
        try {
            String jwt = LoginSession.getJwt();
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = ApiConfig.getLogbookUrl();
            if (limit > 0) {
                targetUrl += "?limit=" + limit;
            }
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
    
    /**
     * Fetches log entries within a date range for export
     */
    public LogDto[] getLogByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        LogDto[] logDtos = {};
        try {
            String jwt = LoginSession.getJwt();
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30)) // Longer timeout for potentially large exports
                    .build();

            String targetUrl = ApiConfig.getLogbookUrl() + "?from=" + fromDate + "&to=" + toDate;
            System.out.println("LogApi: Fetching logs by date range from " + targetUrl);

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
            }
        } catch (Exception e) {
            System.err.println("LogApi: Exception occurred");
            e.printStackTrace();
        }
        return logDtos;
    }
}
