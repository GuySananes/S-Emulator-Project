package javafxUI.service.v2;

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
        public String user;

        public LoginRequest(String user) {
            this.user = user;
        }
    }

    public static class LoginResponse {
        public boolean ok;
        public String user;
        public int credits;
    }

    public static class CreditsAddRequest {
        public String user;
        public int amount;

        public CreditsAddRequest(String user, int amount) {
            this.user = user;
            this.amount = amount;
        }
    }

    public static class CreditsResponse {
        public int credits;
        public int used;
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
        LoginResponse resp = sendJson("/api/v2/login", "POST",
                new LoginRequest(username), LoginResponse.class);

        if (!resp.ok) {
            throw new Exception("Login failed: Server returned ok=false");
        }
        return new Session(resp.user, resp.credits);
    }

    public CreditsResponse addCredits(String user, int amount) throws Exception {
        return sendJson("/api/v2/credits/add", "POST",
                new CreditsAddRequest(user, amount), CreditsResponse.class);
    }

    public UsersResponse getUsers(Long sinceTs) throws Exception {
        String path = "/api/v2/users";
        if (sinceTs != null) {
            path += "?sinceTs=" + sinceTs;
        }
        return sendJson(path, "GET", null, UsersResponse.class);
    }

    private <T> T sendJson(String path, String method, Object body, Class<T> responseType) throws Exception {
        URI uri = URI.create(BASE_URL + path);
        // Remove or comment out these debug lines:
        // System.out.println("Sending " + method + " to: " + uri);

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");

        if ("POST".equals(method) && body != null) {
            String jsonBody = GSON.toJson(body);
            // System.out.println("Request body: " + jsonBody);
            builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else if ("GET".equals(method)) {
            builder.GET();
        } else {
            throw new IllegalArgumentException("Unsupported method: " + method);
        }

        HttpResponse<String> response = HTTP.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

        // Remove or comment out these debug lines:
        // System.out.println("Response status: " + response.statusCode());
        // System.out.println("Response body: " + response.body());

        if (response.statusCode() >= 400) {
            throw new Exception("HTTP " + response.statusCode() + ": " + response.body());
        }

        return GSON.fromJson(response.body(), responseType);
    }
}