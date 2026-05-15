package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlgorithmRecommendationValidator {

    private final AlgorithmCatalog algorithmCatalog;

    public AlgorithmRecommendationValidator(AlgorithmCatalog algorithmCatalog) {
        this.algorithmCatalog = algorithmCatalog;
    }

    public AlgorithmConfiguration validateAndCreateConfiguration(String llmResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode recommendation = mapper.readTree(llmResponse);

        if (!recommendation.has("recommended_algorithm")) {
            throw new IllegalArgumentException("Campo recommended_algorithm em falta.");
        }

        if (!recommendation.has("parameters")) {
            throw new IllegalArgumentException("Campo parameters em falta.");
        }

        String algorithm = recommendation.get("recommended_algorithm").asText();

        if (!algorithmCatalog.isKnownAlgorithm(algorithm)) {
            throw new IllegalArgumentException("Algoritmo desconhecido pela aplicação: " + algorithm);
        }

        if (!algorithmCatalog.isImplemented(algorithm)) {
            throw new IllegalArgumentException(
                    "Algoritmo conhecido, mas ainda não implementado nesta aplicação: " + algorithm
            );
        }

        JsonNode parameters = recommendation.get("parameters");

        if (!parameters.has("population_size")) {
            throw new IllegalArgumentException("Campo population_size em falta.");
        }

        if (!parameters.has("max_evaluations")) {
            throw new IllegalArgumentException("Campo max_evaluations em falta.");
        }

        if (!parameters.has("crossover_probability")) {
            throw new IllegalArgumentException("Campo crossover_probability em falta.");
        }

        if (!parameters.has("mutation_probability")) {
            throw new IllegalArgumentException("Campo mutation_probability em falta.");
        }

        int populationSize = parameters.get("population_size").asInt();
        int maxEvaluations = parameters.get("max_evaluations").asInt();
        double crossoverProbability = parameters.get("crossover_probability").asDouble();
        double mutationProbability = parameters.get("mutation_probability").asDouble();

        if (populationSize <= 0) {
            throw new IllegalArgumentException("population_size inválido.");
        }

        if (maxEvaluations <= 0) {
            throw new IllegalArgumentException("max_evaluations inválido.");
        }

        if (crossoverProbability < 0 || crossoverProbability > 1) {
            throw new IllegalArgumentException("crossover_probability inválido.");
        }

        if (mutationProbability < 0 || mutationProbability > 1) {
            throw new IllegalArgumentException("mutation_probability inválido.");
        }

        return new AlgorithmConfiguration(
                algorithm,
                populationSize,
                maxEvaluations,
                crossoverProbability,
                mutationProbability
        );
    }
}