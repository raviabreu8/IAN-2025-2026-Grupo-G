package pt.iscte.ian;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Escreve o relatorio JSON de cada execucao da aplicacao.
 *
 * <p>O relatorio guarda a informacao enviada ao LLM, a recomendacao recebida,
 * a configuracao executada, as metricas do dataset, as baselines e o resultado
 * da otimizacao. Tambem cria uma copia historica para permitir auditoria das
 * execucoes anteriores.</p>
 */
public class ExecutionReportWriter {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Cria e grava o relatorio completo de uma execucao.
     *
     * @param config configuracao de algoritmo validada e executada
     * @param result resultado produzido pelo algoritmo de otimizacao
     * @param finalLlmResponse resposta final validada do LLM
     * @param systemPrompt prompt de sistema enviado ao LLM
     * @param algorithmRecommendationPrompt prompt principal enviado ao LLM
     * @param datasetQualityReport resumo de qualidade do dataset
     * @param timetablingEvaluation avaliacao do horario original
     * @param optimizedTimetablingEvaluation avaliacao do horario apos aplicacao da solucao
     * @param optimizationInstance instancia de otimizacao usada na execucao
     * @param originalEvaluation avaliacao da atribuicao original nas entradas otimizadas
     * @param greedyEvaluation avaliacao da baseline greedy
     * @throws Exception se ocorrer algum erro ao construir ou escrever os ficheiros JSON
     */
    public void write(
        AlgorithmConfiguration config,
        OptimizationResult result,
        String finalLlmResponse,
        String systemPrompt,
        String algorithmRecommendationPrompt,
        DatasetQualityReport datasetQualityReport,
        TimetablingSolutionEvaluation timetablingEvaluation,
        TimetablingSolutionEvaluation optimizedTimetablingEvaluation,
        TimetablingOptimizationInstance optimizationInstance,
        TimetablingAssignmentEvaluation originalEvaluation,
        TimetablingAssignmentEvaluation greedyEvaluation
    ) throws Exception {
        Path outputDirectory = Path.of("outputs");
        Files.createDirectories(outputDirectory);

        ObjectNode report = mapper.createObjectNode();

        report.put("generated_at", LocalDateTime.now().toString());

        ObjectNode llmRequestNode = mapper.createObjectNode();
        llmRequestNode.put("system_prompt", systemPrompt);
        llmRequestNode.set(
                "algorithm_recommendation_prompt_lines",
                createTextLinesNode(mapper, algorithmRecommendationPrompt)
        );
        report.set("llm_request", llmRequestNode);

        JsonNode llmRecommendationNode = mapper.readTree(finalLlmResponse);
        report.set("llm_recommendation", llmRecommendationNode);

        ObjectNode datasetNode = mapper.createObjectNode();
        datasetNode.put("number_of_rooms", datasetQualityReport.getNumberOfRooms());
        datasetNode.put("number_of_schedule_entries", datasetQualityReport.getNumberOfScheduleEntries());
        datasetNode.put("number_of_distinct_rooms_used", datasetQualityReport.getNumberOfDistinctRoomsUsed());
        datasetNode.put("entries_with_capacity_problem", datasetQualityReport.getEntriesWithCapacityProblem());
        datasetNode.put("entries_without_room", datasetQualityReport.getEntriesWithoutRoom());
        datasetNode.put("entries_with_unknown_room", datasetQualityReport.getEntriesWithUnknownRoom());
        datasetNode.put("entries_not_requiring_room", datasetQualityReport.getEntriesNotRequiringRoom());

        report.set("dataset_quality_report", datasetNode);

        report.set(
                "timetabling_evaluation",
                createTimetablingEvaluationNode(mapper, timetablingEvaluation)
        );

        report.set(
                "optimized_timetabling_evaluation",
                createTimetablingEvaluationNode(mapper, optimizedTimetablingEvaluation)
        );
        
        ObjectNode optimizationInstanceNode = mapper.createObjectNode();
        optimizationInstanceNode.put("entries_to_optimize", optimizationInstance.getNumberOfVariables());
        optimizationInstanceNode.put("candidate_rooms", optimizationInstance.getNumberOfCandidateRooms());

        report.set("optimization_instance", optimizationInstanceNode);

        ObjectNode originalBaselineNode = mapper.createObjectNode();
        originalBaselineNode.put("assigned_entries", originalEvaluation.getAssignedEntries());
        originalBaselineNode.put("invalid_room_assignments", originalEvaluation.getInvalidRoomAssignments());
        originalBaselineNode.put("capacity_violations", originalEvaluation.getCapacityViolations());
        originalBaselineNode.put("total_capacity_shortage", originalEvaluation.getTotalCapacityShortage());
        originalBaselineNode.put("room_time_conflicts", originalEvaluation.getRoomTimeConflicts());
        originalBaselineNode.put("feature_mismatches", originalEvaluation.getFeatureMismatches());
        originalBaselineNode.put("total_unused_capacity", originalEvaluation.getTotalUnusedCapacity());
        originalBaselineNode.put("total_penalty", originalEvaluation.getTotalPenalty());

        report.set("original_baseline", originalBaselineNode);

        ObjectNode greedyNode = mapper.createObjectNode();
        greedyNode.put("assigned_entries", greedyEvaluation.getAssignedEntries());
        greedyNode.put("invalid_room_assignments", greedyEvaluation.getInvalidRoomAssignments());
        greedyNode.put("capacity_violations", greedyEvaluation.getCapacityViolations());
        greedyNode.put("total_capacity_shortage", greedyEvaluation.getTotalCapacityShortage());
        greedyNode.put("room_time_conflicts", greedyEvaluation.getRoomTimeConflicts());
        greedyNode.put("feature_mismatches", greedyEvaluation.getFeatureMismatches());
        greedyNode.put("total_unused_capacity", greedyEvaluation.getTotalUnusedCapacity());
        greedyNode.put("total_penalty", greedyEvaluation.getTotalPenalty());

        report.set("greedy_baseline", greedyNode);

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

        if (result.hasBestObjectiveValues()) {
            resultNode.put("best_penalty", result.getBestPenalty());
            resultNode.put("unused_capacity_of_best_penalty_solution", result.getUnusedCapacityOfBestPenaltySolution());
        }

        if (result.hasBestPenaltyEvaluation()) {
            TimetablingAssignmentEvaluation bestEvaluation = result.getBestPenaltyEvaluation();

            ObjectNode bestSolutionEvaluationNode = mapper.createObjectNode();
            bestSolutionEvaluationNode.put("assigned_entries", bestEvaluation.getAssignedEntries());
            bestSolutionEvaluationNode.put("invalid_room_assignments", bestEvaluation.getInvalidRoomAssignments());
            bestSolutionEvaluationNode.put("capacity_violations", bestEvaluation.getCapacityViolations());
            bestSolutionEvaluationNode.put("total_capacity_shortage", bestEvaluation.getTotalCapacityShortage());
            bestSolutionEvaluationNode.put("room_time_conflicts", bestEvaluation.getRoomTimeConflicts());
            bestSolutionEvaluationNode.put("feature_mismatches", bestEvaluation.getFeatureMismatches());
            bestSolutionEvaluationNode.put("total_unused_capacity", bestEvaluation.getTotalUnusedCapacity());
            bestSolutionEvaluationNode.put("total_penalty", bestEvaluation.getTotalPenalty());

            resultNode.set("best_solution_evaluation", bestSolutionEvaluationNode);
        }

        report.set("configuration", configurationNode);
        report.set("result", resultNode);

        Path lastExecutionFile = outputDirectory.resolve("last_execution.json");

        ObjectWriter prettyWriter = createPrettyWriter();

        prettyWriter.writeValue(lastExecutionFile.toFile(), report);

        Path historyDirectory = outputDirectory.resolve("history");
        Files.createDirectories(historyDirectory);

        String timestamp = LocalDateTime.now()
                .toString()
                .replace(":", "-")
                .replace(".", "-");

        Path historyFile = historyDirectory.resolve("execution_" + timestamp + ".json");

        prettyWriter.writeValue(historyFile.toFile(), report);

        System.out.println("Relatório de execução guardado em:");
        System.out.println(lastExecutionFile.toAbsolutePath());

        System.out.println("Cópia histórica guardada em:");
        System.out.println(historyFile.toAbsolutePath());
    }

    private ObjectNode createTimetablingEvaluationNode(
            ObjectMapper mapper,
            TimetablingSolutionEvaluation evaluation
    ) {
        ObjectNode node = mapper.createObjectNode();

        node.put("total_entries", evaluation.getTotalEntries());
        node.put("capacity_violations", evaluation.getCapacityViolations());
        node.put("total_capacity_shortage", evaluation.getTotalCapacityShortage());
        node.put("missing_room_assignments", evaluation.getMissingRoomAssignments());
        node.put("unknown_room_assignments", evaluation.getUnknownRoomAssignments());
        node.put("room_time_conflicts", evaluation.getRoomTimeConflicts());
        node.put("class_group_time_conflicts", evaluation.getClassGroupTimeConflicts());
        node.put("feature_mismatches", evaluation.getFeatureMismatches());
        node.put("total_unused_capacity", evaluation.getTotalUnusedCapacity());
        node.put("total_penalty", evaluation.getTotalPenalty());
        node.put("capacity_violation_rate", evaluation.getCapacityViolationRate());
        node.put("missing_room_rate", evaluation.getMissingRoomRate());
        node.put("unknown_room_rate", evaluation.getUnknownRoomRate());
        node.put("room_time_conflict_rate", evaluation.getRoomTimeConflictRate());
        node.put("class_group_time_conflict_rate", evaluation.getClassGroupTimeConflictRate());
        node.put("feature_mismatch_rate", evaluation.getFeatureMismatchRate());

        return node;
    }

    private ArrayNode createTextLinesNode(ObjectMapper mapper, String text) {
        ArrayNode linesNode = mapper.createArrayNode();

        if (text == null || text.isBlank()) {
            return linesNode;
        }

        String[] lines = text.split("\\R", -1);

        for (String line : lines) {
            linesNode.add(line);
        }

        return linesNode;
    }

    private ObjectWriter createPrettyWriter() {
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        return mapper.writer(prettyPrinter);
    }
}
