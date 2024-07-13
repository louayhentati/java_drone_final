package API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class api {
    private String endpoint;
    private String domain;
    private String token;
    private String agent;
    private HttpURLConnection connection;

    public api(String endpoint, String domain, String token, String agent) {
        this.endpoint = endpoint;
        this.domain = domain;
        this.token = token;
        this.agent = agent;
    }

    public void createConnection(String endpoint) throws IOException {
        try {
            URL url = new URL(domain + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
           // connection.setRequestProperty("Authorization", token); This line was originally implemented to secure the web entrance using our token. However, due to a recent hack at the university, it is no longer usable.
            connection.setRequestProperty("User-Agent", agent);
        } catch (IOException e) {
            System.err.println("Error creating connection: " + e.getMessage());
            throw e;
        }
    }

    public String retrieveResponse() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            connection.disconnect();
            return content.toString();
        } catch (IOException e) {
            System.err.println("Error retrieving response: " + e.getMessage());
            throw e;
        }
    }



}
