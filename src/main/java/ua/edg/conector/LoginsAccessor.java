package ua.edg.conector;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class LoginsAccessor{

  private static Gson gson = new Gson();

  @Getter
  @Setter
  private static class LoginDTO{
    private List<String> logins;
  }

  public static List<String> parseLogins(){
    try(HttpClient client = HttpClient.newHttpClient()){
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("http://192.168.1.10:7590/api/getLoginsJson"))
          .header("Accept", "application/json")
          .timeout(Duration.ofSeconds(10))
          .GET()
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return gson.fromJson(response.body(), LoginDTO.class).getLogins();
    }
    catch(Exception e){
      System.err.println("Error fetching logins: " + e.getMessage());
      return null;
    }
  }
}