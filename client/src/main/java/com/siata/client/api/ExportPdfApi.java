package com.siata.client.api;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class ExportPdfApi {

    public void handle(Stage stage) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String targetUrl = "http://localhost:8080/api/laporan/aset/pdf";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
//                java.nio.file.Path path = java.nio.file.Paths.get("laporan.pdf");
//                java.nio.file.Files.write(path, response.body());
//                System.out.println("PDF berhasil disimpan: " + path.toAbsolutePath());

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Simpan PDF");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                fileChooser.setInitialFileName("laporan.pdf");

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        Files.write(file.toPath(), response.body());
                        System.out.println("PDF berhasil disimpan: " + file.getAbsolutePath());
                    } catch (Exception e) {
                        System.err.println("Gagal menyimpan file: " + e.getMessage());
                    }
                } else {
                    System.out.println("User membatalkan simpan file");
                }
            } else {
                System.err.println("Gagal. Status Code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Terjadi kesalahan koneksi: " + e.getMessage());
        }
    }
}