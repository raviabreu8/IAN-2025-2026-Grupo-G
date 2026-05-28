package pt.iscte.ian;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.List;

/**
 * Executa o NSGA-II do JMetal para o problema simplificado de timetabling.
 *
 * <p>Este runner cria a formulacao JMetal, executa o NSGA-II com os parametros
 * recomendados pelo LLM e seleciona a solucao com menor penalizacao para ser
 * aplicada e registada no resultado da execucao.</p>
 */
public class JMetalTimetablingNsgaIIRunner {

    /**
     * Executa o NSGA-II com os parametros recebidos.
     *
     * @param config configuracao validada com algoritmo e parametros
     * @param dataset dataset completo de salas e horarios
     * @param instance instancia simplificada com as aulas a otimizar
     * @return resultado da execucao, incluindo melhor penalizacao e atribuicao encontrada
     */
    public OptimizationResult run(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        System.out.println("A executar NSGA-II real com JMetal no problema simplificado de timetabling...");
        System.out.println();

        TimetablingRoomAssignmentProblem problem = new TimetablingRoomAssignmentProblem(
                dataset,
                instance
        );

        Algorithm<List<IntegerSolution>> algorithm = new NSGAIIBuilder<>(
                problem,
                new IntegerSBXCrossover(config.getCrossoverProbability(), 20.0),
                new IntegerPolynomialMutation(config.getMutationProbability(), 20.0),
                config.getPopulationSize()
        )
                .setMaxEvaluations(config.getMaxEvaluations())
                .build();

        long startTime = System.currentTimeMillis();

        algorithm.run();

        long endTime = System.currentTimeMillis();

        List<IntegerSolution> result = algorithm.result();

        long executionTime = endTime - startTime;
        int numberOfSolutions = result.size();

        System.out.println("NSGA-II terminou a execução no problema de timetabling.");
        System.out.println("Tempo de execução: " + executionTime + " ms");
        System.out.println("Número de soluções obtidas: " + numberOfSolutions);

        System.out.println("Primeiras soluções encontradas:");

        int limit = Math.min(5, result.size());

        for (int i = 0; i < limit; i++) {
            IntegerSolution solution = result.get(i);

            System.out.println(
                    "Solução " + (i + 1) +
                            " -> penalização = " + solution.objectives()[0]
            );
        }
        System.out.println(); //coonfirmar

        IntegerSolution bestPenaltySolution = result.stream()
                .min((s1, s2) -> Double.compare(s1.objectives()[0], s2.objectives()[0]))
                .orElse(null);


        double bestPenalty = bestPenaltySolution == null ? Double.NaN : bestPenaltySolution.objectives()[0];


        int[] bestPenaltyRoomIndexes = bestPenaltySolution == null
                ? new int[0]
                : extractRoomIndexes(problem, bestPenaltySolution);

        TimetablingAssignmentEvaluation bestPenaltyEvaluation = null;
        double unusedCapacityOfBestPenaltySolution = Double.NaN;

        if (bestPenaltyRoomIndexes.length > 0) {
            TimetablingAssignment bestPenaltyAssignment =
                    new TimetablingAssignment(bestPenaltyRoomIndexes);

            TimetablingAssignmentEvaluator evaluator =
                    new TimetablingAssignmentEvaluator(dataset, instance);

            bestPenaltyEvaluation = evaluator.evaluate(bestPenaltyAssignment);

            unusedCapacityOfBestPenaltySolution =
                    bestPenaltyEvaluation.getTotalUnusedCapacity();

            System.out.println("Avaliação da melhor solução NSGA-II:");
            System.out.println(bestPenaltyEvaluation);
            System.out.println();

        }

        return new OptimizationResult(
                config.getAlgorithm(),
                executionTime,
                numberOfSolutions,
                bestPenalty,
                unusedCapacityOfBestPenaltySolution,
                bestPenaltyRoomIndexes,
                bestPenaltyEvaluation
        );
    }

    private int[] extractRoomIndexes(
            TimetablingRoomAssignmentProblem problem,
            IntegerSolution solution
    ) {
        return problem.decodeSolution(solution).getRoomIndexes();
    }
}
