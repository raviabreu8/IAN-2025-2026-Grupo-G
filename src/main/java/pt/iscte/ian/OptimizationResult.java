package pt.iscte.ian;

public class OptimizationResult {

    private final String algorithm;
    private final long executionTimeMs;
    private final int numberOfSolutions;

    private final double bestPenalty;
    private final double unusedCapacityOfBestPenaltySolution;

    private final double bestUnusedCapacity;
    private final double penaltyOfBestUnusedCapacitySolution;

    public OptimizationResult(String algorithm, long executionTimeMs, int numberOfSolutions) {
        this(
                algorithm,
                executionTimeMs,
                numberOfSolutions,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN
        );
    }

    public OptimizationResult(
            String algorithm,
            long executionTimeMs,
            int numberOfSolutions,
            double bestPenalty,
            double unusedCapacityOfBestPenaltySolution,
            double bestUnusedCapacity,
            double penaltyOfBestUnusedCapacitySolution
    ) {
        this.algorithm = algorithm;
        this.executionTimeMs = executionTimeMs;
        this.numberOfSolutions = numberOfSolutions;
        this.bestPenalty = bestPenalty;
        this.unusedCapacityOfBestPenaltySolution = unusedCapacityOfBestPenaltySolution;
        this.bestUnusedCapacity = bestUnusedCapacity;
        this.penaltyOfBestUnusedCapacitySolution = penaltyOfBestUnusedCapacitySolution;
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

    public double getBestUnusedCapacity() {
        return bestUnusedCapacity;
    }

    public double getPenaltyOfBestUnusedCapacitySolution() {
        return penaltyOfBestUnusedCapacitySolution;
    }

    public boolean hasBestObjectiveValues() {
        return !Double.isNaN(bestPenalty)
                && !Double.isNaN(unusedCapacityOfBestPenaltySolution)
                && !Double.isNaN(bestUnusedCapacity)
                && !Double.isNaN(penaltyOfBestUnusedCapacitySolution);
    }

    @Override
    public String toString() {
        return "OptimizationResult{" +
                "algorithm='" + algorithm + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", numberOfSolutions=" + numberOfSolutions +
                ", bestPenalty=" + bestPenalty +
                ", unusedCapacityOfBestPenaltySolution=" + unusedCapacityOfBestPenaltySolution +
                ", bestUnusedCapacity=" + bestUnusedCapacity +
                ", penaltyOfBestUnusedCapacitySolution=" + penaltyOfBestUnusedCapacitySolution +
                '}';
    }
}