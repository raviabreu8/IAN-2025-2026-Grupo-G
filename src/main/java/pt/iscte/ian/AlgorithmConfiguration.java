package pt.iscte.ian;

public class AlgorithmConfiguration {

    private final String algorithm;
    private final int populationSize;
    private final int maxEvaluations;
    private final double crossoverProbability;
    private final double mutationProbability;

    public AlgorithmConfiguration(
            String algorithm,
            int populationSize,
            int maxEvaluations,
            double crossoverProbability,
            double mutationProbability
    ) {
        this.algorithm = algorithm;
        this.populationSize = populationSize;
        this.maxEvaluations = maxEvaluations;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    @Override
    public String toString() {
        return "AlgorithmConfiguration{" +
                "algorithm='" + algorithm + '\'' +
                ", populationSize=" + populationSize +
                ", maxEvaluations=" + maxEvaluations +
                ", crossoverProbability=" + crossoverProbability +
                ", mutationProbability=" + mutationProbability +
                '}';
    }
}