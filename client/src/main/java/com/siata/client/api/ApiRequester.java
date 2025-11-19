//package com.siata.client.api;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.siata.client.dto.AssetDto;
//import com.siata.client.dto.UserDto;
//import com.siata.client.service.DataServiceAPI;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//
//public class ApiRequester {
//
//    public static void main(String[] args) {
//        // 1. Setup Dependensi Manual (Pengganti @Autowired)
//        DataServiceAPI dataService = new DataServiceAPI();
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        // 2. Konfigurasi HttpClient
//        HttpClient client = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_2)
//                .connectTimeout(Duration.ofSeconds(10))
//                .build();
//
//        // 3. Membangun Request
//        String targetUrl = "http://localhost:8080/api/aset"; // API Publik Contoh
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(targetUrl))
//                .header("Content-Type", "application/json")
//                // Jika butuh Auth: .header("Authorization", "Bearer xyz123")
//                .GET()
//                .build();
//
//        try {
//            System.out.println("Mengirim request ke: " + targetUrl);
//
//            // 4. Eksekusi Request (Synchronous)
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            // 5. Cek Status Code
//            if (response.statusCode() == 200) {
//                String jsonResponse = response.body();
//
//                // 6. Deserialisasi (JSON -> DTO)
//                AssetDto[] assetDto = objectMapper.readValue(jsonResponse, AssetDto[].class);
//
//                // 7. Kirim ke Middleware
//                dataService.processAsetData(assetDto);
//
//            } else {
//                System.err.println("Gagal. Status Code: " + response.statusCode());
//            }
//
//        } catch (IOException | InterruptedException e) {
//            System.err.println("Terjadi kesalahan koneksi: " + e.getMessage());
//        }
//    }
//
//    public static void main(String[] args) {
//        try {
//            // 1. Setup Objek dan Mapper
//            AssetDto payload = new AssetDto();
//            payload.setName("New Lenovo Laptop");
//            payload.setDescription("Asset inventaris baru 2025");
//            // Jangan set ID jika server yang men-generate ID (auto-increment)
//
//            ObjectMapper mapper = new ObjectMapper();
//
//            // 2. Serialisasi: Konversi DTO ke JSON String
//            String requestBodyJson = mapper.writeValueAsString(payload);
//            System.out.println("Mengirim Payload: " + requestBodyJson);
//
//            // 3. Setup HttpClient
//            HttpClient client = HttpClient.newBuilder()
//                    .connectTimeout(Duration.ofSeconds(10))
//                    .build();
//
//            // 4. Build Request (POST)
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://jsonplaceholder.typicode.com/posts")) // URL Contoh
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
//                    .build();
//
//            // 5. Kirim dan Terima Respons
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            // 6. Validasi Respons
//            // Biasanya POST sukses mengembalikan 201 (Created) atau 200 (OK)
//            if (response.statusCode() == 201 || response.statusCode() == 200) {
//                System.out.println("Sukses! Status: " + response.statusCode());
//
//                // (Opsional) Deserialisasi respons jika server mengembalikan data yang baru dibuat
//                AssetDto createdAsset = mapper.readValue(response.body(), AssetDto.class);
//                System.out.println("ID Aset Baru: " + createdAsset.getId());
//
//            } else {
//                System.err.println("Gagal mengirim data. Status: " + response.statusCode());
//                System.err.println("Response Body: " + response.body());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}