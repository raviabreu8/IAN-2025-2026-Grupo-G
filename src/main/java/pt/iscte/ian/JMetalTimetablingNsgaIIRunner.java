package pt.iscte.ian;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.List;

public class JMetalTimetablingNsgaIIRunner {

    public OptimizationResult run(
            AlgorithmConfiguration config,
            TimetablingDataset dataset,
            TimetablingOptimizationInstance instance
    ) {
        System.out.println("A executar NSGA-II real com JMetal no problema simplificado de timetabling...");

        Problem<IntegerSolution> problem = new TimetablingRoomAssignmentProblem(
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

        IntegerSolution bestPenaltySolution = result.stream()
                .min((s1, s2) -> Double.compare(s1.objectives()[0], s2.objectives()[0]))
                .orElse(null);


        double bestPenalty = bestPenaltySolution == null ? Double.NaN : bestPenaltySolution.objectives()[0];


        int[] bestPenaltyRoomIndexes = bestPenaltySolution == null
                ? new int[0]
                : extractRoomIndexes(bestPenaltySolution);

        double unusedCapacityOfBestPenaltySolution = Double.NaN;

        if (bestPenaltyRoomIndexes.length > 0) {
            TimetablingAssignment bestPenaltyAssignment =
                    new TimetablingAssignment(bestPenaltyRoomIndexes);

            TimetablingAssignmentEvaluator evaluator =
                    new TimetablingAssignmentEvaluator(dataset, instance);

            TimetablingAssignmentEvaluation bestPenaltyEvaluation =
                    evaluator.evaluate(bestPenaltyAssignment);

            unusedCapacityOfBestPenaltySolution =
                    bestPenaltyEvaluation.getTotalUnusedCapacity();
        }

        return new OptimizationResult(
                config.getAlgorithm(),
                executionTime,
                numberOfSolutions,
                bestPenalty,
                unusedCapacityOfBestPenaltySolution,
                bestPenaltyRoomIndexes
        );
    }

    private int[] extractRoomIndexes(IntegerSolution solution) {
        int[] roomIndexes = new int[solution.variables().size()];

        for (int i = 0; i < solution.variables().size(); i++) {
            roomIndexes[i] = solution.variables().get(i);
        }

        return roomIndexes;
    }
}
