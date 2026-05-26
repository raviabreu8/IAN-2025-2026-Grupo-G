package pt.iscte.ian;

import java.util.Arrays;

public class OptimizationResult {

    private final String algorithm;
    private final long executionTimeMs;
    private final int numberOfSolutions;
    private final double bestPenalty;
    private final double unusedCapacityOfBestPenaltySolution;
    private final int[] bestPenaltyRoomIndexes;
    private final TimetablingAssignmentEvaluation bestPenaltyEvaluation;

    public OptimizationResult(String algorithm, long executionTimeMs, int numberOfSolutions) {
        this(
                algorithm,
                executionTimeMs,
                numberOfSolutions,
                Double.NaN,
                Double.NaN,
                new int[0],
                null
        );
    }

    public OptimizationResult(
            String algorithm,
            long executionTimeMs,
            int numberOfSolutions,
            double bestPenalty,
            double unusedCapacityOfBestPenaltySolution,
            int[] bestPenaltyRoomIndexes
    ) {
        this(
                algorithm,
                executionTimeMs,
                numberOfSolutions,
                bestPenalty,
                unusedCapacityOfBestPenaltySolution,
                bestPenaltyRoomIndexes,
                null
        );
    }

    public OptimizationResult(
            String algorithm,
            long executionTimeMs,
            int numberOfSolutions,
            double bestPenalty,
            double unusedCapacityOfBestPenaltySolution,
            int[] bestPenaltyRoomIndexes,
            TimetablingAssignmentEvaluation bestPenaltyEvaluation
    ) {
        this.algorithm = algorithm;
        this.executionTimeMs = executionTimeMs;
        this.numberOfSolutions = numberOfSolutions;
        this.bestPenalty = bestPenalty;
        this.unusedCapacityOfBestPenaltySolution = unusedCapacityOfBestPenaltySolution;
        this.bestPenaltyRoomIndexes = Arrays.copyOf(bestPenaltyRoomIndexes, bestPenaltyRoomIndexes.length);
        this.bestPenaltyEvaluation = bestPenaltyEvaluation;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public int getNumberOfSolutions() {
        return numberOfSolutions;
    }

    public double getBestPenalty() {
        return bestPenalty;
    }

    public double getUnusedCapacityOfBestPenaltySolution() {
        return unusedCapacityOfBestPenaltySolution;
    }

    public int[] getBestPenaltyRoomIndexes() {
        return Arrays.copyOf(bestPenaltyRoomIndexes, bestPenaltyRoomIndexes.length);
    }

    public TimetablingAssignmentEvaluation getBestPenaltyEvaluation() {
        return bestPenaltyEvaluation;
    }

    public boolean hasBestPenaltyRoomIndexes() {
        return bestPenaltyRoomIndexes.length > 0;
    }

    public boolean hasBestObjectiveValues() {
        return !Double.isNaN(bestPenalty);
    }

    public boolean hasBestPenaltyEvaluation() {
        return bestPenaltyEvaluation != null;
    }

    @Override
    public String toString() {
        return "OptimizationResult{" +
                "algorithm='" + algorithm + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", numberOfSolutions=" + numberOfSolutions +
                ", bestPenalty=" + bestPenalty +
                ", unusedCapacityOfBestPenaltySolution=" + unusedCapacityOfBestPenaltySolution +
                ", bestPenaltyRoomIndexes=" + bestPenaltyRoomIndexes.length +
                '}';
    }
}
