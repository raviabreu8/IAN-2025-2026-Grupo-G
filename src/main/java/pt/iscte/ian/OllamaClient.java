package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OllamaClient {

    private final String apiUrl;
    private final String model;
    private final ObjectMapper mapper;
    private final HttpClient client;

    public OllamaClient(String apiUrl, String model) {
        this.apiUrl = apiUrl;
        this.model = model;
        this.mapper = new ObjectMapper();
        this.client = HttpClient.newHttpClient();
    }

    public String generateResponse(String systemPrompt, String userPrompt) throws Exception {
        ObjectNode requestJson = mapper.createObjectNode();

        requestJson.put("model", model);
        requestJson.put("stream", false);
        requestJson.put("format", "json");
        requestJson.put("system", systemPrompt);
        requestJson.put("prompt", userPrompt);

        String requestBody = mapper.writeValueAsString(requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro na chamada ao Ollama. Código HTTP: " + response.statusCode());
        }

        JsonNode ollamaResponse = mapper.readTree(response.body());

        if (!ollamaResponse.has("response")) {
            throw new RuntimeException("Resposta do Ollama não contém o campo 'response'.");
        }

        return ollamaResponse.get("response").asText();
    }
}