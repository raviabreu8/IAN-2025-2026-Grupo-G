package pt.iscte.ian;

public class OptimizationRunner {

    public void execute(AlgorithmConfiguration config) {
        System.out.println("A preparar execução do algoritmo...");

        switch (config.getAlgorithm()) {
            case "NSGA-II" -> executeNsgaII(config);
            case "NSGA-III" -> executeNsgaIII(config);
            case "MOEA/D" -> executeMoead(config);
            default -> throw new IllegalArgumentException(
                    "Algoritmo não suportado: " + config.getAlgorithm()
            );
        }
    }

    private void executeNsgaII(AlgorithmConfiguration config) {
        System.out.println("A executar o algoritmo NSGA-II com JMetal.");
        printConfiguration(config);

        JMetalNsgaIIRunner nsgaIIRunner = new JMetalNsgaIIRunner();
        nsgaIIRunner.run(config);
    }

    private void executeNsgaIII(AlgorithmConfiguration config) {
        System.out.println("Executaria o algoritmo NSGA-III com os seguintes parâmetros:");
        printConfiguration(config);
    }

    private void executeMoead(AlgorithmConfiguration config) {
        System.out.println("Executaria o algoritmo MOEA/D com os seguintes parâmetros:");
        printConfiguration(config);
    }

    private void printConfiguration(AlgorithmConfiguration config) {
        System.out.println("Algoritmo: " + config.getAlgorithm());
        System.out.println("Population size: " + config.getPopulationSize());
        System.out.println("Max evaluations: " + config.getMaxEvaluations());
        System.out.println("Crossover probability: " + config.getCrossoverProbability());
        System.out.println("Mutation probability: " + config.getMutationProbability());
    }
}