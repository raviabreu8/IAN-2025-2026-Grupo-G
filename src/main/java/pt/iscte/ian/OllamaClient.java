package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente responsavel pela comunicacao com a API local do Ollama.
 *
 * <p>Esta classe envia os prompts construidos pela aplicacao para o modelo LLM
 * configurado e devolve apenas o conteudo textual presente no campo
 * {@code response} da resposta do Ollama.</p>
 */
public class OllamaClient {

    private final String apiUrl;
    private final String model;
    private final ObjectMapper mapper;
    private final HttpClient client;

    /**
     * Cria um cliente para chamar o endpoint local do Ollama.
     *
     * @param apiUrl URL da API do Ollama, por exemplo {@code http://localhost:11434/api/generate}
     * @param model nome do modelo local a usar na geracao da resposta
     */
    public OllamaClient(String apiUrl, String model) {
        this.apiUrl = apiUrl;
        this.model = model;
        this.mapper = new ObjectMapper();
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Envia um pedido ao Ollama com o prompt de sistema e o prompt principal.
     *
     * <p>O pedido e enviado em modo nao-streaming e com {@code format=json},
     * para incentivar o modelo a devolver uma resposta diretamente tratavel pela
     * aplicacao.</p>
     *
     * @param systemPrompt prompt de sistema que define o papel e as regras gerais do LLM
     * @param userPrompt prompt principal com o pedido de recomendacao ou correcao
     * @return conteudo do campo {@code response} devolvido pelo Ollama
     * @throws Exception se a chamada HTTP falhar, se o estado HTTP nao for 200 ou se a resposta nao tiver o campo esperado
     */
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
