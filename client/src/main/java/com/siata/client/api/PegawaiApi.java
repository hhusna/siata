package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siata.client.config.ApiConfig;
import com.siata.client.dto.PegawaiDto;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PegawaiApi {
    String jwt = LoginSession.getJwt();

    public PegawaiDto getPegawaiByNip(long nip) {
        System.out.println("PegawaiApi:getPegawaiByNip BISMILLAH JALAN NI BANG");
        PegawaiDto pegawaiDto = new PegawaiDto();

        try {
            ObjectMapper mapper = new ObjectMapper();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = ApiConfig.getPegawaiUrl() + "/" + nip;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()==200) {
                System.out.println("PegawaiApi: Pegawai ditemukan!");

                String jsonResponse = response.body();

                pegawaiDto = mapper.readValue(jsonResponse, PegawaiDto.class);

                return pegawaiDto;
            } else {
                System.out.println("PegawaiApi: Pegawai gagal ditemukan!" + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pegawaiDto;
    }

    public PegawaiDto[] getPegawai() {
        PegawaiDto[] listPegawai = {};

        try {
            ObjectMapper mapper = new ObjectMapper();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = ApiConfig.getPegawaiUrl();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonResponse = response.body();

                listPegawai = mapper.readValue(jsonResponse, PegawaiDto[].class);

                return listPegawai;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return listPegawai;
    }


    public boolean addPegawai(long nip, String nama, String namaSubdir, String status) {
        PegawaiDto dto = new PegawaiDto();
        dto.setNip(nip);
        dto.setNama(nama);
        dto.setNamaSubdir(namaSubdir);
        dto.setStatus(status);
        return addPegawai(dto);
    }

    public boolean addPegawai(PegawaiDto payload) {
        try {
            // Default status if null
            if (payload.getStatus() == null) {
                payload.setStatus("AKTIF");
            }

            ObjectMapper mapper = new ObjectMapper();

            String requestBodyJson = mapper.writeValueAsString(payload);
            System.out.println("Mengirim payload: " + requestBodyJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            System.out.println("JWT DI PEGAWAIAPI:"+jwt);
            String targetUrl = ApiConfig.getPegawaiUrl();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("Sukses! Status: " + response.statusCode());
                return true;
            } else {
                System.err.println("Gagal mengirim data. Status: " + response.statusCode());
                System.err.println("Response Body: " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean deletePegawai(String nip) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = ApiConfig.getPegawaiUrl() + "/" + nip;
            String jwt = LoginSession.getJwt();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("PegawaiApi: DELETE BERHASIL");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePegawai(long nip, String nama, String namaSubdir, String status) {
        PegawaiDto dto = new PegawaiDto();
        dto.setNip(nip);
        dto.setNama(nama);
        dto.setNamaSubdir(namaSubdir);
        dto.setStatus(status);
        return updatePegawai(nip, dto); // Use String for existing signature if needed, or long
    }

    public boolean updatePegawai(String nip, PegawaiDto payload) {
        // Redirect to long version if possible or handle here
        try {
            return updatePegawai(Long.parseLong(nip), payload);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePegawai(long nip, PegawaiDto payload) {
        try {
            // Ensure payload has NIP set
            payload.setNip(nip);
            if (payload.getStatus() == null) payload.setStatus("AKTIF");

            // Using batch endpoint instead of PUT because PUT endpoint doesn't copy status field
            ObjectMapper mapper = new ObjectMapper();
            // Wrap single pegawai in a list for batch endpoint
            java.util.List<PegawaiDto> batchList = java.util.Collections.singletonList(payload);
            String requestBodyJson = mapper.writeValueAsString(batchList);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Use batch endpoint which properly saves all fields including status
            String targetUrl = ApiConfig.getPegawaiUrl() + "/batch";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("PegawaiApi: Update Berhasil!");
                return true;
            } else {
                System.err.println("Gagal update data. Status: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Batch add multiple pegawai in one API call.
     * Returns the number of successfully added pegawai, or -1 on error.
     */
    public int batchAddPegawai(java.util.List<PegawaiDto> pegawaiList) {
        try {
            // Get fresh JWT token
            String currentJwt = LoginSession.getJwt();
            
            // Debug logging
            System.out.println("=== BATCH IMPORT DEBUG ===");
            System.out.println("JWT Token exists: " + (currentJwt != null && !currentJwt.isEmpty()));
            if (currentJwt != null && currentJwt.length() > 20) {
                System.out.println("JWT Token (first 20 chars): " + currentJwt.substring(0, 20) + "...");
            }
            System.out.println("Jumlah pegawai: " + pegawaiList.size());
            
            ObjectMapper mapper = new ObjectMapper();
            String requestBodyJson = mapper.writeValueAsString(pegawaiList);
            System.out.println("Payload size: " + requestBodyJson.length() + " bytes");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            String targetUrl = ApiConfig.getPegawaiUrl() + "/batch";
            System.out.println("Target URL: " + targetUrl);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + currentJwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response status: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("Response body: " + response.body());
            }
            System.out.println("=== END DEBUG ===");

            if (response.statusCode() == 200) {
                System.out.println("PegawaiApi: Batch import berhasil!");
                PegawaiDto[] saved = mapper.readValue(response.body(), PegawaiDto[].class);
                return saved.length;
            } else {
                System.err.println("Gagal batch import. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Exception during batch import:");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Batch delete multiple pegawai in one API call.
     * Returns the number of successfully deleted pegawai, or -1 on error.
     */
    public int batchDeletePegawai(java.util.List<Long> nipList) {
        try {
            String currentJwt = LoginSession.getJwt();
            
            ObjectMapper mapper = new ObjectMapper();
            String requestBodyJson = mapper.writeValueAsString(nipList);
            System.out.println("Batch delete payload: " + nipList.size() + " NIPs");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            String targetUrl = ApiConfig.getPegawaiUrl() + "/batch";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + currentJwt)
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("PegawaiApi: Batch delete berhasil!");
                return mapper.readValue(response.body(), Integer.class);
            } else {
                System.err.println("Gagal batch delete. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Exception during batch delete:");
            e.printStackTrace();
            return -1;
        }
    }

}

