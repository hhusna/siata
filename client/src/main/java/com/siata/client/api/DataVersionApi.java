package com.siata.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siata.client.config.ApiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * API client for data version endpoint.
 * Used for polling to detect data changes on server.
 */
public class DataVersionApi {
    
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Fetch the current data version from server.
     * This is a lightweight request that returns only a timestamp.
     * 
     * @return The version timestamp, or -1 if failed
     */
    public long getDataVersion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.getDataVersionUrl()))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode json = mapper.readTree(response.body());
                return json.get("version").asLong();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch data version: " + e.getMessage());
        }
        return -1;
    }
}
