package pt.iscte.ian;

import java.util.List;

public class AlgorithmCatalog {

    public record AlgorithmInfo(
            String name,
            String framework,
            boolean implemented,
            String description
    ) {
    }

    private final List<AlgorithmInfo> algorithms = List.of(
            new AlgorithmInfo(
                    "NSGA-II",
                    "JMetal 6.1",
                    true,
                    "Algoritmo multiobjetivo baseado em ordenação não-dominada."
            ),
            new AlgorithmInfo(
                    "NSGA-III",
                    "JMetal 6.1",
                    false,
                    "Algoritmo multiobjetivo indicado para problemas com muitos objetivos."
            ),
            new AlgorithmInfo(
                    "MOEA/D",
                    "JMetal 6.1",
                    false,
                    "Algoritmo multiobjetivo baseado em decomposição."
            )
    );

    public List<AlgorithmInfo> getAlgorithms() {
        return algorithms;
    }

    public List<String> getAlgorithmNames() {
        return algorithms.stream()
                .map(AlgorithmInfo::name)
                .toList();
    }

    public List<String> getImplementedAlgorithmNames() {
        return algorithms.stream()
                .filter(AlgorithmInfo::implemented)
                .map(AlgorithmInfo::name)
                .toList();
    }

    public boolean isKnownAlgorithm(String algorithmName) {
        return getAlgorithmNames().contains(algorithmName);
    }

    public boolean isImplemented(String algorithmName) {
        return algorithms.stream()
                .anyMatch(algorithm ->
                        algorithm.name().equals(algorithmName) && algorithm.implemented()
                );
    }
}