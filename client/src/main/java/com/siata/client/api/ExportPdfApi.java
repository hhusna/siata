package com.siata.client.api;

import com.siata.client.config.ApiConfig;
import com.siata.client.session.LoginSession;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

public class ExportPdfApi {

    public void handle(Stage stage) {
        String jwt = LoginSession.getJwt();
        if (jwt == null || jwt.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Anda harus login terlebih dahulu.");
            return;
        }
        
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        // Use laptop-needs endpoint for formal letter PDF
        String targetUrl = ApiConfig.getLaporanUrl() + "/laptop-needs/pdf";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwt)
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Simpan Surat Kebutuhan Laptop");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                fileChooser.setInitialFileName("SuratKebutuhanLaptop.pdf");

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        Files.write(file.toPath(), response.body());
                        showAlert(Alert.AlertType.INFORMATION, "Sukses", "PDF berhasil disimpan:\n" + file.getAbsolutePath());
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan file: " + e.getMessage());
                    }
                }
            } else if (response.statusCode() == 403) {
                showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Anda tidak memiliki izin untuk mengakses fitur ini.");
            } else if (response.statusCode() == 401) {
                showAlert(Alert.AlertType.ERROR, "Unauthorized", "Sesi login Anda telah berakhir. Silakan login ulang.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal mengunduh PDF. Status: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error Koneksi", "Gagal menghubungi server: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}

