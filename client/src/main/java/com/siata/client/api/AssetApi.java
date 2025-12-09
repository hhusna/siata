package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.siata.client.config.ApiConfig;
import com.siata.client.dto.AssetDto;
import com.siata.client.dto.AssetDtoForRequest;
import com.siata.client.dto.DashboardDto;
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
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            long idAset = payload.getIdAset();

            String targetUrl = ApiConfig.getAsetUrl() + "/" + idAset;

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
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getDashboardUrl() + "/stats";

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

    public boolean deleteAssetById(long idAset) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/hapus/" + idAset;

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

    public AssetDto getAssetById(long idAset) {
        AssetDto assetDto = new AssetDto();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/" + idAset;

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
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl();

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

    public AssetDto[] getDeletedAsset() {
        AssetDto[] listAsset = {};

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/deleted";

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

    public long tambahAsset(AssetDtoForRequest payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String requestBodyJson = mapper.writeValueAsString(payload);

            System.out.println("AssetApi: Mengirim Payload -> " + requestBodyJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Sukses Menambahkan Asset!");

                long idAset = mapper.readValue(response.body(), AssetDto.class).getIdAset();

                return idAset;
            }else {
                System.out.println("AssetApi: Gagal mengirim " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int batchAddAsset(java.util.List<AssetDtoForRequest> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String requestBodyJson = mapper.writeValueAsString(payload);

            System.out.println("AssetApi: Mengirim Batch Payload (" + payload.size() + " records)");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60)) // Longer timeout for batch
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/batch";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Sukses Batch Menambahkan Asset!");
                return mapper.readValue(response.body(), Integer.class);
            } else {
                System.out.println("AssetApi: Gagal batch mengirim " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Integer getNextNoAset(String kodeAset) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/next-no?kodeAset=" + kodeAset;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), Integer.class);
            } else {
                System.out.println("AssetApi: Gagal getNextNoAset " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Default fallback
    }

    public int cleanDuplicates() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/clean-duplicates";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Berhasil membersihkan duplikat!");
                return Integer.parseInt(response.body());
            } else {
                System.out.println("AssetApi: Gagal membersihkan duplikat " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Batch delete multiple assets in one API call.
     */
    public int batchDeleteAset(java.util.List<Long> idList) {
        try {
            String currentJwt = LoginSession.getJwt();
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String requestBodyJson = mapper.writeValueAsString(idList);
            System.out.println("Batch delete aset payload: " + idList.size() + " IDs");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/batch";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + currentJwt)
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Batch delete berhasil!");
                return mapper.readValue(response.body(), Integer.class);
            } else {
                System.err.println("Gagal batch delete aset. Status: " + response.statusCode());
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Exception during batch delete aset:");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Undo delete: Set apakahDihapus = 0 to restore asset
     */
    public boolean undoDeleteAset(long idAset) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/undo/" + idAset;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Berhasil undo delete!");
                return true;
            } else {
                System.out.println("AssetApi: Gagal undo delete " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Permanent delete: Actually remove asset from database
     */
    public boolean permanentDeleteAset(long idAset) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String targetUrl = ApiConfig.getAsetUrl() + "/permanent/" + idAset;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("AssetApi: Berhasil permanent delete!");
                return true;
            } else {
                System.out.println("AssetApi: Gagal permanent delete " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
