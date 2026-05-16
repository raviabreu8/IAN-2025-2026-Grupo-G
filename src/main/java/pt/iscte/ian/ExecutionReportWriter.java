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
            String finalLlmResponse,
            DatasetQualityReport datasetQualityReport,
            TimetablingEvaluation timetablingEvaluation
    ) throws Exception {
        Path outputDirectory = Path.of("outputs");
        Files.createDirectories(outputDirectory);

        ObjectNode report = mapper.createObjectNode();

        report.put("generated_at", LocalDateTime.now().toString());

        JsonNode llmRecommendationNode = mapper.readTree(finalLlmResponse);
        report.set("llm_recommendation", llmRecommendationNode);

        ObjectNode datasetNode = mapper.createObjectNode();
        datasetNode.put("number_of_rooms", datasetQualityReport.getNumberOfRooms());
        datasetNode.put("number_of_schedule_entries", datasetQualityReport.getNumberOfScheduleEntries());
        datasetNode.put("number_of_distinct_rooms_used", datasetQualityReport.getNumberOfDistinctRoomsUsed());
        datasetNode.put("entries_with_capacity_problem", datasetQualityReport.getEntriesWithCapacityProblem());
        datasetNode.put("entries_without_room", datasetQualityReport.getEntriesWithoutRoom());
        datasetNode.put("entries_with_unknown_room", datasetQualityReport.getEntriesWithUnknownRoom());

        report.set("dataset_quality_report", datasetNode);

        ObjectNode timetablingEvaluationNode = mapper.createObjectNode();
        timetablingEvaluationNode.put("total_entries", timetablingEvaluation.getTotalEntries());
        timetablingEvaluationNode.put("capacity_violations", timetablingEvaluation.getCapacityViolations());
        timetablingEvaluationNode.put("missing_room_assignments", timetablingEvaluation.getMissingRoomAssignments());
        timetablingEvaluationNode.put("unknown_room_assignments", timetablingEvaluation.getUnknownRoomAssignments());
        timetablingEvaluationNode.put("room_time_conflicts", timetablingEvaluation.getRoomTimeConflicts());
        timetablingEvaluationNode.put("class_group_time_conflicts", timetablingEvaluation.getClassGroupTimeConflicts());
        timetablingEvaluationNode.put("total_penalty", timetablingEvaluation.getTotalPenalty());
        timetablingEvaluationNode.put("capacity_violation_rate", timetablingEvaluation.getCapacityViolationRate());
        timetablingEvaluationNode.put("missing_room_rate", timetablingEvaluation.getMissingRoomRate());
        timetablingEvaluationNode.put("unknown_room_rate", timetablingEvaluation.getUnknownRoomRate());
        timetablingEvaluationNode.put("room_time_conflict_rate", timetablingEvaluation.getRoomTimeConflictRate());
        timetablingEvaluationNode.put("class_group_time_conflict_rate", timetablingEvaluation.getClassGroupTimeConflictRate());

        report.set("timetabling_evaluation", timetablingEvaluationNode);
        
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
            resultNode.put("best_unused_capacity", result.getBestUnusedCapacity());
            resultNode.put("penalty_of_best_unused_capacity_solution", result.getPenaltyOfBestUnusedCapacitySolution());
        }

        report.set("configuration", configurationNode);
        report.set("result", resultNode);

        Path lastExecutionFile = outputDirectory.resolve("last_execution.json");

        mapper.writerWithDefaultPrettyPrinter().writeValue(lastExecutionFile.toFile(), report);

        Path historyDirectory = outputDirectory.resolve("history");
        Files.createDirectories(historyDirectory);

        String timestamp = LocalDateTime.now()
                .toString()
                .replace(":", "-")
                .replace(".", "-");

        Path historyFile = historyDirectory.resolve("execution_" + timestamp + ".json");

        mapper.writerWithDefaultPrettyPrinter().writeValue(historyFile.toFile(), report);

        System.out.println("Relatório de execução guardado em:");
        System.out.println(lastExecutionFile.toAbsolutePath());

        System.out.println("Cópia histórica guardada em:");
        System.out.println(historyFile.toAbsolutePath());
    }
}