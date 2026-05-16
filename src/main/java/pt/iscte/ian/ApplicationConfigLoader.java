package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class ApplicationConfigLoader {

    public ApplicationConfig load() throws Exception {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("application_config.json");

        if (inputStream == null) {
            throw new RuntimeException("Ficheiro application_config.json não encontrado em src/main/resources.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputStream);

        JsonNode ollamaNode = root.get("ollama");
        JsonNode executionNode = root.get("execution");
        JsonNode timetablingNode = root.get("timetabling");

        if (ollamaNode == null) {
            throw new RuntimeException("Configuração inválida: campo 'ollama' em falta.");
        }

        String apiUrl = ollamaNode.get("api_url").asText();
        String model = ollamaNode.get("model").asText();

        int maxCorrectionAttempts = 1;

        if (executionNode != null && executionNode.has("max_correction_attempts")) {
            maxCorrectionAttempts = executionNode.get("max_correction_attempts").asInt();
        }

        int maxEntriesForOptimization = 300;

        if (timetablingNode != null && timetablingNode.has("max_entries_for_optimization")) {
            maxEntriesForOptimization = timetablingNode.get("max_entries_for_optimization").asInt();
        }

        return new ApplicationConfig(
                apiUrl,
                model,
                maxCorrectionAttempts,
                maxEntriesForOptimization
        );
    }
}