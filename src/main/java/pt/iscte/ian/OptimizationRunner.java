package pt.iscte.ian;

public class OptimizationRunner {

    public OptimizationResult execute(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance optimizationInstance
    ) {
        System.out.println("A delegar a tarefa para o algoritmo certo...");

        return switch (config.getAlgorithm()) {
            case "NSGA-II" -> executeNsgaII(config, dataset, optimizationInstance);
            case "NSGA-III" -> executeNsgaIII(config);
            case "MOEA/D" -> executeMoead(config);
            default -> throw new IllegalArgumentException(
                    "Algoritmo não suportado: " + config.getAlgorithm()
            );
        };
    }

    private OptimizationResult executeNsgaII(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance optimizationInstance
    ) {
        System.out.println("A preparar execução do algoritmo NSGA-II com JMetal.");
        printConfiguration(config);
        System.out.println();

        JMetalTimetablingNsgaIIRunner runner = new JMetalTimetablingNsgaIIRunner();

        return runner.run(
                config,
                dataset,
                optimizationInstance
        );
    }

    private OptimizationResult executeNsgaIII(AlgorithmConfiguration config) {
        System.out.println("NSGA-III ainda não está implementado com JMetal neste protótipo.");
        printConfiguration(config);
        System.out.println();

        return new OptimizationResult(
                config.getAlgorithm(),
                0,
                0
        );
    }

    private OptimizationResult executeMoead(AlgorithmConfiguration config) {
        System.out.println("MOEA/D ainda não está implementado com JMetal neste protótipo.");
        printConfiguration(config);
        System.out.println();

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