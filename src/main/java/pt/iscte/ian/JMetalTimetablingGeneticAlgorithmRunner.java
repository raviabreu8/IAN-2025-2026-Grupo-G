package pt.iscte.ian;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class JMetalTimetablingGeneticAlgorithmRunner {

    public OptimizationResult run(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        System.out.println("A executar Genetic Algorithm real com JMetal no problema simplificado de timetabling...");
        System.out.println();

        TimetablingRoomAssignmentProblem problem = new TimetablingRoomAssignmentProblem(
                dataset,
                instance
        );

        Algorithm<IntegerSolution> algorithm = new GeneticAlgorithmBuilder<>(
                problem,
                new IntegerSBXCrossover(config.getCrossoverProbability(), 20.0),
                new IntegerPolynomialMutation(config.getMutationProbability(), 20.0)
        )
                .setPopulationSize(config.getPopulationSize())
                .setMaxEvaluations(config.getMaxEvaluations())
                .setVariant(GeneticAlgorithmBuilder.GeneticAlgorithmVariant.GENERATIONAL)
                .build();

        long startTime = System.currentTimeMillis();

        algorithm.run();

        long endTime = System.currentTimeMillis();

        IntegerSolution bestSolution = algorithm.result();

        long executionTime = endTime - startTime;
        int numberOfSolutions = bestSolution == null ? 0 : 1;

        System.out.println("Genetic Algorithm terminou a execução no problema de timetabling.");
        System.out.println("Tempo de execução: " + executionTime + " ms");
        System.out.println("Número de soluções obtidas: " + numberOfSolutions);

        double bestPenalty = bestSolution == null ? Double.NaN : bestSolution.objectives()[0];

        if (bestSolution != null) {
            System.out.println("Melhor solução encontrada:");
            System.out.println("Penalização = " + bestPenalty);
            System.out.println();
        }

        int[] bestPenaltyRoomIndexes = bestSolution == null
                ? new int[0]
                : extractRoomIndexes(problem, bestSolution);

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

            System.out.println("Avaliação da melhor solução Genetic Algorithm:");
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
