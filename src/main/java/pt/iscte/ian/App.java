package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("Aplicação IAN iniciada.");
        System.out.println("A pedir recomendação de algoritmo ao LLM...");

        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode requestJson = mapper.createObjectNode();
            requestJson.put("model", "llama3.2:3b");
            requestJson.put("stream", false);
            requestJson.put("format", "json");

            requestJson.put(
                    "system",
                    "És um assistente especializado em algoritmos de otimização multiobjetivo disponíveis na framework JMetal 6.1. " +
                    "Responde sempre em JSON válido. Não escrevas texto fora do JSON."
            );

            String prompt = """
                    Analisa o seguinte problema de otimização:

                    Tipo de problema: timetabling universitário
                    Descrição: alocação de aulas a salas e horários.

                    Objetivos:
                    - minimizar conflitos de horários
                    - minimizar conflitos de professores
                    - maximizar uso adequado da capacidade das salas
                    - minimizar penalizações por violações de restrições

                    Restrições:
                    - um professor não pode estar em duas aulas ao mesmo tempo
                    - uma sala não pode ter duas aulas ao mesmo tempo
                    - a capacidade da sala deve ser suficiente para a turma
                    - aulas da mesma turma não devem sobrepor-se

                    Algoritmos disponíveis na framework JMetal 6.1:
                    - NSGA-II
                    - NSGA-III
                    - MOEA/D

                    Escolhe exatamente UM algoritmo principal.

                    Responde obrigatoriamente neste formato JSON:
                    {
                      "task": "algorithm_recommendation",
                      "problem_type": "timetabling",
                      "recommended_algorithm": "NSGA-II | NSGA-III | MOEA/D",
                      "justification": "explicação curta",
                      "parameters": {
                        "population_size": 100,
                        "max_evaluations": 25000,
                        "crossover_probability": 0.9,
                        "mutation_probability": 0.01
                      },
                      "alternatives": [
                        {
                          "algorithm": "nome",
                          "reason_not_selected": "explicação curta"
                        }
                      ]
                    }
                    """;

            requestJson.put("prompt", prompt);

            String requestBody = mapper.writeValueAsString(requestJson);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("Código HTTP: " + response.statusCode());

            JsonNode ollamaResponse = mapper.readTree(response.body());
            String llmResponse = ollamaResponse.get("response").asText();

            System.out.println("Resposta JSON do LLM:");
            System.out.println(llmResponse);

            JsonNode recommendation = mapper.readTree(llmResponse);

            List<String> allowedAlgorithms = List.of("NSGA-II", "NSGA-III", "MOEA/D");

            boolean valid = true;

            if (!recommendation.has("recommended_algorithm")) {
                System.out.println("Erro: campo recommended_algorithm em falta.");
                valid = false;
            }

            if (!recommendation.has("parameters")) {
                System.out.println("Erro: campo parameters em falta.");
                valid = false;
            }

            if (valid) {
                String algorithm = recommendation.get("recommended_algorithm").asText();

                if (!allowedAlgorithms.contains(algorithm)) {
                    System.out.println("Erro: algoritmo não permitido: " + algorithm);
                    valid = false;
                }

                JsonNode parameters = recommendation.get("parameters");

                int populationSize = parameters.get("population_size").asInt();
                int maxEvaluations = parameters.get("max_evaluations").asInt();
                double crossoverProbability = parameters.get("crossover_probability").asDouble();
                double mutationProbability = parameters.get("mutation_probability").asDouble();

                if (populationSize <= 0) {
                    System.out.println("Erro: population_size inválido.");
                    valid = false;
                }

                if (maxEvaluations <= 0) {
                    System.out.println("Erro: max_evaluations inválido.");
                    valid = false;
                }

                if (crossoverProbability < 0 || crossoverProbability > 1) {
                    System.out.println("Erro: crossover_probability inválido.");
                    valid = false;
                }

                if (mutationProbability < 0 || mutationProbability > 1) {
                    System.out.println("Erro: mutation_probability inválido.");
                    valid = false;
                }

                if (valid) {
                    System.out.println("Resposta validada com sucesso.");
                    System.out.println("Algoritmo escolhido: " + algorithm);
                    System.out.println("Population size: " + populationSize);
                    System.out.println("Max evaluations: " + maxEvaluations);
                    System.out.println("Crossover probability: " + crossoverProbability);
                    System.out.println("Mutation probability: " + mutationProbability);
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao comunicar com o Ollama:");
            System.out.println(e.getMessage());
        }
    }
}