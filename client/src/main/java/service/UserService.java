package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserService {
    private static final String API_URL = "http://localhost:8080/api/auth/login";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();


    public boolean login(User user) throws Exception {
        // serialize object menjadi json untuk membuat request body
        String jsonBody = mapper.writeValueAsString(user);

        // membuat request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // kirim request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // proses response
        if (response.statusCode() == 200) {
            System.out.println("(TESTING) Login Sukses! Response: " + response.body());
            return true;
        }  else {
            System.err.println("(TESTING) Login Gagal: " + response.statusCode());
            return false;
        }
    }


}
