package pt.iscte.ian;

public class PromptBuilder {

    private final AlgorithmCatalog algorithmCatalog;

    public PromptBuilder(AlgorithmCatalog algorithmCatalog) {
        this.algorithmCatalog = algorithmCatalog;
    }

    public String buildSystemPrompt() {
        return "És um assistente especializado em algoritmos de otimização multiobjetivo disponíveis na framework JMetal 6.1. " +
                "Responde sempre em JSON válido. Não escrevas texto fora do JSON.";
    }

   public String buildAlgorithmRecommendationPrompt(
            String problemDescriptionJson,
            DatasetQualityReport datasetQualityReport
    ) {
        StringBuilder algorithmsDescription = new StringBuilder();

        for (AlgorithmCatalog.AlgorithmInfo algorithm : algorithmCatalog.getAlgorithms()) {
            algorithmsDescription
                    .append("- ")
                    .append(algorithm.name())
                    .append(" | Framework: ")
                    .append(algorithm.framework())
                    .append(" | Implementado nesta aplicação: ")
                    .append(algorithm.implemented() ? "sim" : "não")
                    .append(" | Descrição: ")
                    .append(algorithm.description())
                    .append("\n");
        }

        String datasetSummary = """
                Resumo dos datasets reais carregados:
                - Número de salas: %d
                - Número de entradas de horário: %d
                - Número de salas distintas usadas no horário: %d
                - Entradas com problema de capacidade: %d
                - Entradas sem sala atribuída: %d
                - Entradas com sala não encontrada no cadastro de salas: %d
                """.formatted(
                datasetQualityReport.getNumberOfRooms(),
                datasetQualityReport.getNumberOfScheduleEntries(),
                datasetQualityReport.getNumberOfDistinctRoomsUsed(),
                datasetQualityReport.getEntriesWithCapacityProblem(),
                datasetQualityReport.getEntriesWithoutRoom(),
                datasetQualityReport.getEntriesWithUnknownRoom()
        );

        return """
                A aplicação vai enviar-te uma descrição estruturada de um problema de otimização em JSON.

                Descrição do problema:
                %s

                Dados reais disponíveis:
                %s

                Catálogo de algoritmos conhecidos pela aplicação:
                %s

                Tarefa:
                Analisa o problema de otimização e recomenda exatamente UM algoritmo principal.

                Regras importantes:
                - Recomenda apenas algoritmos presentes no catálogo.
                - Dá preferência a algoritmos já implementados nesta aplicação.
                - Só recomenda um algoritmo não implementado se houver uma razão técnica muito forte.
                - Considera o resumo dos datasets reais na justificação da escolha.
                - Responde apenas em JSON válido.
                - Não escrevas texto fora do JSON.

                Responde obrigatoriamente neste formato JSON:
                {
                "task": "algorithm_recommendation",
                "problem_type": "timetabling",
                "recommended_algorithm": "nome_exato_do_algoritmo",
                "justification": "explicação curta considerando o problema e os dados reais",
                "parameters": {
                    "population_size": 30,
                    "max_evaluations": 1000,
                    "crossover_probability": 0.9,
                    "mutation_probability": 0.01
                },
                "alternatives": [
                    {
                    "algorithm": "nome",
                    "reason_not_selected": "explicação curta"
                    }
                ]
                }
                """.formatted(
                problemDescriptionJson,
                datasetSummary,
                algorithmsDescription.toString()
        );
    }
    
    public String buildCorrectionPrompt(String previousResponse, String validationError) {
        StringBuilder algorithmsDescription = new StringBuilder();

        for (AlgorithmCatalog.AlgorithmInfo algorithm : algorithmCatalog.getAlgorithms()) {
            algorithmsDescription
                    .append("- ")
                    .append(algorithm.name())
                    .append(" | Framework: ")
                    .append(algorithm.framework())
                    .append(" | Implementado nesta aplicação: ")
                    .append(algorithm.implemented() ? "sim" : "não")
                    .append(" | Descrição: ")
                    .append(algorithm.description())
                    .append("\n");
        }

        return """
                A resposta anterior do LLM foi considerada inválida pela aplicação.

                Erro de validação:
                %s

                Resposta anterior:
                %s

                Catálogo de algoritmos conhecidos pela aplicação:
                %s

                Gera uma nova resposta corrigida.

                Regras obrigatórias:
                - Recomenda apenas algoritmos presentes no catálogo.
                - O campo recommended_algorithm deve conter exatamente o nome de um algoritmo implementado nesta aplicação.
                - Não recomendes algoritmos marcados como "Implementado nesta aplicação: não".
                - Responde apenas em JSON válido.
                - Não escrevas texto fora do JSON.

                Formato obrigatório:
                {
                "task": "algorithm_recommendation",
                "problem_type": "timetabling",
                "recommended_algorithm": "nome_exato_do_algoritmo",
                "justification": "explicação curta",
                "parameters": {
                    "population_size": 30,
                    "max_evaluations": 1000,
                    "crossover_probability": 0.9,
                    "mutation_probability": 0.01
                },
                "alternatives": [
                    {
                    "algorithm": "nome",
                    "reason_not_selected": "explicação curta"
                    }
                ]
                }
                """.formatted(validationError, previousResponse, algorithmsDescription.toString());
    }
}