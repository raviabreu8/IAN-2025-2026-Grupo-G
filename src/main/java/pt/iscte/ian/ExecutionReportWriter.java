package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class ExecutionReportWriter {

    private final ObjectMapper mapper = new ObjectMapper();

    public void write(
            AlgorithmConfiguration config,
            OptimizationResult result,
            String finalLlmResponse
    ) throws Exception {
        Path outputDirectory = Path.of("outputs");
        Files.createDirectories(outputDirectory);

        ObjectNode report = mapper.createObjectNode();

        report.put("generated_at", LocalDateTime.now().toString());

        JsonNode llmRecommendationNode = mapper.readTree(finalLlmResponse);
        report.set("llm_recommendation", llmRecommendationNode);

        ObjectNode configurationNode = mapper.createObjectNode();
        configurationNode.put("algorithm", config.getAlgorithm());
        configurationNode.put("population_size", config.getPopulationSize());
        configurationNode.put("max_evaluations", config.getMaxEvaluations());
        configurationNode.put("crossover_probability", config.getCrossoverProbability());
        configurationNode.put("mutation_probability", config.getMutationProbability());

        ObjectNode resultNode = mapper.createObjectNode();
        resultNode.put("algorithm", result.getAlgorithm());
        resultNode.put("execution_time_ms", result.getExecutionTimeMs());
        resultNode.put("number_of_solutions", result.getNumberOfSolutions());

        report.set("configuration", configurationNode);
        report.set("result", resultNode);

        Path outputFile = outputDirectory.resolve("last_execution.json");

        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), report);

        System.out.println("Relatório de execução guardado em:");
        System.out.println(outputFile.toAbsolutePath());
    }
}