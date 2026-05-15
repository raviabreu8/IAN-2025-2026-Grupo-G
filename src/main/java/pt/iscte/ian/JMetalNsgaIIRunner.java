package pt.iscte.ian;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT1;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.List;

public class JMetalNsgaIIRunner {

    public OptimizationResult run(AlgorithmConfiguration config) {
        System.out.println("A executar NSGA-II real com JMetal no problema de teste ZDT1...");

        Problem<DoubleSolution> problem = new ZDT1();

        Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(
                problem,
                new SBXCrossover(config.getCrossoverProbability(), 20.0),
                new PolynomialMutation(
                        config.getMutationProbability(),
                        20.0
                ),
                config.getPopulationSize()
        )
                .setMaxEvaluations(config.getMaxEvaluations())
                .build();

        long startTime = System.currentTimeMillis();

        algorithm.run();

        long endTime = System.currentTimeMillis();

        List<DoubleSolution> result = algorithm.result();

        long executionTime = endTime - startTime;
        int numberOfSolutions = result.size();

        System.out.println("NSGA-II terminou a execução.");
        System.out.println("Tempo de execução: " + executionTime + " ms");
        System.out.println("Número de soluções obtidas: " + numberOfSolutions);

        System.out.println("Primeiras soluções encontradas:");

        int limit = Math.min(5, result.size());

        for (int i = 0; i < limit; i++) {
            DoubleSolution solution = result.get(i);

            System.out.println(
                    "Solução " + (i + 1) +
                            " -> f1 = " + solution.objectives()[0] +
                            ", f2 = " + solution.objectives()[1]
            );
        }

        return new OptimizationResult(
                config.getAlgorithm(),
                executionTime,
                numberOfSolutions
        );
    }
}