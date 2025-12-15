package com.siata.client.config;

/**
 * Centralized API configuration for managing base URLs and endpoints.
 * This makes it easy to change the server URL for different environments
 * (development, staging, production) without modifying multiple files.
 */
public class ApiConfig {
    
    /**
     * Base URL for the API server.
     * Change this value to switch between environments:
     * - Development: http://localhost:8080
     * - Production: https://your-domain.com
     */
    private static final String BASE_URL = "http://localhost:8080";
    
    /**
     * Get the base URL for API requests.
     * @return The base URL (e.g., "http://localhost:8080")
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }
    
    /**
     * Get the full URL for a specific API endpoint.
     * @param endpoint The endpoint path (e.g., "/api/aset")
     * @return The complete URL (e.g., "http://localhost:8080/api/aset")
     */
    public static String getUrl(String endpoint) {
        // Remove leading slash if present to avoid double slashes
        if (endpoint.startsWith("/")) {
            return BASE_URL + endpoint;
        }
        return BASE_URL + "/" + endpoint;
    }
    
    // Convenience methods for common endpoints
    public static String getAuthUrl() {
        return BASE_URL + "/api/auth";
    }
    
    public static String getAsetUrl() {
        return BASE_URL + "/api/aset";
    }
    
    public static String getPegawaiUrl() {
        return BASE_URL + "/api/pegawai";
    }
    
    public static String getPermohonanUrl() {
        return BASE_URL + "/api/permohonan";
    }
    
    public static String getPengajuanUrl() {
        return BASE_URL + "/api/pengajuan";
    }
    
    public static String getDashboardUrl() {
        return BASE_URL + "/api/dashboard";
    }
    
    public static String getLogbookUrl() {
        return BASE_URL + "/api/logbook";
    }
    
    public static String getLaporanUrl() {
        return BASE_URL + "/api/laporan";
    }

    public static String getDataVersionUrl() {
        return BASE_URL + "/api/data-version";
    }
}
