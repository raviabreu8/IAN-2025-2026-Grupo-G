package pt.iscte.ian;

public class OptimizationResult {

    private final String algorithm;
    private final long executionTimeMs;
    private final int numberOfSolutions;

    public OptimizationResult(String algorithm, long executionTimeMs, int numberOfSolutions) {
        this.algorithm = algorithm;
        this.executionTimeMs = executionTimeMs;
        this.numberOfSolutions = numberOfSolutions;
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

    @Override
    public String toString() {
        return "OptimizationResult{" +
                "algorithm='" + algorithm + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", numberOfSolutions=" + numberOfSolutions +
                '}';
    }
}