package pt.iscte.ian;

public class OptimizationRunner {

    public OptimizationResult execute(AlgorithmConfiguration config) {
        System.out.println("A preparar execução do algoritmo...");

        return switch (config.getAlgorithm()) {
            case "NSGA-II" -> executeNsgaII(config);
            case "NSGA-III" -> executeNsgaIII(config);
            case "MOEA/D" -> executeMoead(config);
            default -> throw new IllegalArgumentException(
                    "Algoritmo não suportado: " + config.getAlgorithm()
            );
        };
    }

    private OptimizationResult executeNsgaII(AlgorithmConfiguration config) {
        System.out.println("A executar o algoritmo NSGA-II com JMetal.");
        printConfiguration(config);

        JMetalNsgaIIRunner nsgaIIRunner = new JMetalNsgaIIRunner();
        return nsgaIIRunner.run(config);
    }

    private OptimizationResult executeNsgaIII(AlgorithmConfiguration config) {
        System.out.println("NSGA-III ainda não está implementado com JMetal neste protótipo.");
        System.out.println("Apenas seria executado dinamicamente numa fase posterior.");
        printConfiguration(config);

        return new OptimizationResult(
                config.getAlgorithm(),
                0,
                0
        );
    }

    private OptimizationResult executeMoead(AlgorithmConfiguration config) {
        System.out.println("MOEA/D ainda não está implementado com JMetal neste protótipo.");
        System.out.println("Apenas seria executado dinamicamente numa fase posterior.");
        printConfiguration(config);

        return new OptimizationResult(
                config.getAlgorithm(),
                0,
                0
        );
    }

    private void printConfiguration(AlgorithmConfiguration config) {
        System.out.println("Algoritmo: " + config.getAlgorithm());
        System.out.println("Population size: " + config.getPopulationSize());
        System.out.println("Max evaluations: " + config.getMaxEvaluations());
        System.out.println("Crossover probability: " + config.getCrossoverProbability());
        System.out.println("Mutation probability: " + config.getMutationProbability());
    }
}