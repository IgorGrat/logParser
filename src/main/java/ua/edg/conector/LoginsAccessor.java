package ua.edg.conector;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoginsAccessor {

    private static String requestLogins() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://192.168.1.10:7590/api/getLoginsJson"))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.err.println("Error fetching logins: " + e.getMessage());
            return null;
        }
    }

    public static List<String> parseLogins() {
        String[] body = Objects.requireNonNull(requestLogins())
                .replaceAll("[\\[\\]]", "")
                .replace("\"", "")
                .split(",");
        return Arrays.asList(body);
    }
}