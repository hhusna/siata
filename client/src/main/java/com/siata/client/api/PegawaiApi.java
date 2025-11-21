package com.siata.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siata.client.MainApplication;
import com.siata.client.dto.PegawaiDto;
import com.siata.client.session.LoginSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class PegawaiApi {
    String jwt = LoginSession.getJwt();

    public PegawaiDto getPegawaiByNip(int nip) {
        System.out.println("PegawaiApi:getPegawaiByNip BISMILLAH JALAN NI BANG");
        PegawaiDto pegawaiDto = new PegawaiDto();

        try {
            ObjectMapper mapper = new ObjectMapper();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String targetUrl = "http://localhost:8080/api/pegawai/" + Integer.toString(nip);

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

            String targetUrl = "http://localhost:8080/api/pegawai";

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


    public boolean addPegawai(int nip, String nama, String namaSubdir, String jabatan) {
        try {
            PegawaiDto payload = new PegawaiDto();
            payload.setNip(nip);
            payload.setNama(nama);
            payload.setNamaSubdir(namaSubdir);
            payload.setJabatan(jabatan);

            ObjectMapper mapper = new ObjectMapper();

            String requestBodyJson = mapper.writeValueAsString(payload);
            System.out.println("Mengirim payload: " + requestBodyJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            System.out.println("JWT DI PEGAWAIAPI:"+jwt);
            String targetUrl = "http://localhost:8080/api/pegawai";
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

            String targetUrl = "http://localhost:8080/api/pegawai/"+nip;
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

    public boolean updatePegawai(int nip, String nama, String namaSubdir, String jabatan) {
        try {
            PegawaiDto payload = new PegawaiDto();
            payload.setNip(nip);
            payload.setNama(nama);
            payload.setNamaSubdir(namaSubdir);
            payload.setJabatan(jabatan);

            ObjectMapper mapper = new ObjectMapper();
            String requestBodyJson = mapper.writeValueAsString(payload);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // URL endpoint PUT: /api/pegawai/{nip}
            String targetUrl = "http://localhost:8080/api/pegawai/" + nip;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson)) // Menggunakan Method PUT
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

}
