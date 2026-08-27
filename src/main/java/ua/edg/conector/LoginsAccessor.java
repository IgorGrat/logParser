package ua.edg.conector;

import com.google.gson.Gson;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LoginsAccessor{

	@Getter
	private static final List<String> loginsList = new ArrayList<>(parseLogins());

	private static final Gson gson = new Gson();

	private record LoginDTO(List<String> logins){

	}

	private static List<String> parseLogins(){
		try(HttpClient client = HttpClient.newHttpClient()){
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://192.168.1.10:7590/api/getLoginsJson"))
					.header("Accept", "application/json")
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if(gson != null) return gson.fromJson(response.body(), LoginDTO.class).logins();
		}
		catch(Exception e){
			System.err.println("Error fetching logins: " + e.getMessage());
			return new ArrayList<>();
		}
		return new ArrayList<>();
	}
}