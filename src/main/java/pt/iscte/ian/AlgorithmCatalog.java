package pt.iscte.ian;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmCatalog {

    public record AlgorithmInfo(
            String name,
            String framework,
            boolean implemented,
            String description,
            List<String> suitableFor
    ) {
    }

    private final List<AlgorithmInfo> algorithms;

    public AlgorithmCatalog() {
        this.algorithms = loadAlgorithmsFromJson();
    }

    private List<AlgorithmInfo> loadAlgorithmsFromJson() {
        try {
            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("algorithm_catalog.json");

            if (inputStream == null) {
                throw new RuntimeException("Ficheiro algorithm_catalog.json não encontrado em src/main/resources.");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(inputStream);
            JsonNode algorithmsNode = root.get("algorithms");

            if (algorithmsNode == null || !algorithmsNode.isArray()) {
                throw new RuntimeException("O ficheiro algorithm_catalog.json deve conter um array chamado 'algorithms'.");
            }

            List<AlgorithmInfo> loadedAlgorithms = new ArrayList<>();

            for (JsonNode algorithmNode : algorithmsNode) {
                String name = algorithmNode.get("name").asText();
                String framework = algorithmNode.get("framework").asText();
                boolean implemented = algorithmNode.get("implemented").asBoolean();
                String description = algorithmNode.get("description").asText();

                List<String> suitableFor = new ArrayList<>();

                if (algorithmNode.has("suitable_for") && algorithmNode.get("suitable_for").isArray()) {
                    for (JsonNode item : algorithmNode.get("suitable_for")) {
                        suitableFor.add(item.asText());
                    }
                }

                loadedAlgorithms.add(
                        new AlgorithmInfo(
                                name,
                                framework,
                                implemented,
                                description,
                                suitableFor
                        )
                );
            }

            return List.copyOf(loadedAlgorithms);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar catálogo de algoritmos: " + e.getMessage(), e);
        }
    }

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