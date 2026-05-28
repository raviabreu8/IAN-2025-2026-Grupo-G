package pt.iscte.ian;

/**
 * Encaminha a execucao para o algoritmo de otimizacao escolhido pelo LLM.
 *
 * <p>Depois de a resposta do LLM ser validada e convertida numa
 * {@link AlgorithmConfiguration}, esta classe seleciona o runner correspondente
 * e executa o algoritmo com JMetal sobre a instancia de timetabling.</p>
 */
public class OptimizationRunner {

    /**
     * Executa o algoritmo indicado na configuracao recebida.
     *
     * @param config configuracao validada com o algoritmo e os parametros recomendados
     * @param dataset dataset completo de salas e horarios
     * @param optimizationInstance instancia simplificada a otimizar
     * @return resultado da execucao do algoritmo escolhido
     */
    public OptimizationResult execute(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance optimizationInstance
    ) {
        System.out.println("A delegar a tarefa para o algoritmo certo...");

        return switch (config.getAlgorithm()) {
            case "NSGA-II" -> executeNsgaII(config, dataset, optimizationInstance);
            case "Genetic Algorithm" -> executeGeneticAlgorithm(config, dataset, optimizationInstance);
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

    private OptimizationResult executeGeneticAlgorithm(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance optimizationInstance
    ) {
        System.out.println("A preparar execução do algoritmo Genetic Algorithm com JMetal.");
        printConfiguration(config);
        System.out.println();

        JMetalTimetablingGeneticAlgorithmRunner runner = new JMetalTimetablingGeneticAlgorithmRunner();

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
