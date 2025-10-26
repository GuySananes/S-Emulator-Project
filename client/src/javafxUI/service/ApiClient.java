package javafxUI.service;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    // Update this to match your deployed server
    private static final String BASE_URL = "http://localhost:8080/web_SEmulator_Web_exploded";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private static final Gson GSON = new Gson();

    public static class Session {
        public String user;
        public int credits;

        public Session(String user, int credits) {
            this.user = user;
            this.credits = credits;
        }
    }

    public static class LoginRequest {
        public String username;

        public LoginRequest(String username) {
            this.username = username;
        }
    }

    public static class LoginResponse {
        public boolean ok;
        public String username;
        public int credits;
    }

    public static class CreditsRequest {
        public int amount;

        public CreditsRequest(int amount) {
            this.amount = amount;
        }
    }

    public static class CreditsResponse {
        public int credits;
    }

    public static class UserRow {
        public String name;
        public int mainCount;
        public int funcCount;
        public int credits;
        public int used;
        public int runs;

        // Add getters for JavaFX PropertyValueFactory
        public String getName() { return name; }
        public int getMainCount() { return mainCount; }
        public int getFuncCount() { return funcCount; }
        public int getCredits() { return credits; }
        public int getUsed() { return used; }
        public int getRuns() { return runs; }
    }

    public static class UsersResponse {
        public long ts;
        public java.util.List<UserRow> users;
    }

    public Session login(String username) throws Exception {
        LoginResponse resp = sendJson("/api/login", "POST",
                new LoginRequest(username), LoginResponse.class);

        if (!resp.ok) {
            throw new Exception("Login failed: Server returned ok=false");
        }
        return new Session(resp.username, resp.credits);
    }

    public CreditsResponse chargeCredits(int amount) throws Exception {
        return sendJson("/api/credits/charge", "POST",
                new CreditsRequest(amount), CreditsResponse.class);
    }

    public UsersResponse getUsers(Long sinceTs) throws Exception {
        String path = "/api/users/live";
        if (sinceTs != null) {
            path += "?sinceTs=" + sinceTs;
        }
        return sendJson(path, "GET", null, UsersResponse.class);
    }

    private <T> T sendJson(String path, String method, Object body, Class<T> responseType) throws Exception {
        URI uri = URI.create(BASE_URL + path);

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");

        if ("POST".equals(method) && body != null) {
            String jsonBody = GSON.toJson(body);
            builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else if ("GET".equals(method)) {
            builder.GET();
        } else {
            throw new IllegalArgumentException("Unsupported method: " + method);
        }

        HttpResponse<String> response = HTTP.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new Exception("HTTP " + response.statusCode() + ": " + response.body());
        }

        return GSON.fromJson(response.body(), responseType);
    }
}