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
            DatasetQualityReport datasetQualityReport,
            TimetablingOptimizationInstance optimizationInstance
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("És um assistente especializado em otimização com JMetal.\n");
        prompt.append("A tua tarefa é recomendar um algoritmo executável e parâmetros iniciais para uma aplicação Java.\n\n");

        prompt.append("Contexto da aplicação:\n");
        prompt.append("- A aplicação usa um LLM local através da API do Ollama.\n");
        prompt.append("- A aplicação executa algoritmos com a framework JMetal.\n");
        prompt.append("- A resposta do LLM será processada automaticamente, por isso deves responder apenas com JSON válido.\n\n");

        prompt.append("Catálogo de algoritmos executáveis nesta versão:\n");
        prompt.append("- NSGA-II: implementado e executável na aplicação.\n\n");

        prompt.append("Algoritmos que podem ser mencionados apenas como alternativas futuras, mas não devem ser escolhidos como principal:\n");
        prompt.append("- NSGA-III: não implementado nesta versão.\n");
        prompt.append("- MOEA/D: não implementado nesta versão.\n\n");

        prompt.append("Formulação atual do problema:\n");
        prompt.append("- Tipo: timetabling simplificado, formulado como realocação de salas com horários fixos.\n");
        prompt.append("- A aplicação NÃO altera dias nem horas das aulas.\n");
        prompt.append("- A aplicação NÃO otimiza professores, porque o dataset não contém informação de docentes.\n");
        prompt.append("- A decisão é escolher uma sala para cada aula problemática.\n");
        prompt.append("- O objetivo atual é único: minimizar uma penalização ponderada total.\n");
        prompt.append("- A penalização agrega: salas inválidas, capacidade insuficiente, lugares em falta, conflitos de sala, incompatibilidade de características e capacidade desperdiçada.\n\n");

        prompt.append("Dados globais do dataset:\n");
        prompt.append("- Número de salas: ").append(datasetQualityReport.getNumberOfRooms()).append("\n");
        prompt.append("- Número de entradas de horário: ").append(datasetQualityReport.getNumberOfScheduleEntries()).append("\n");
        prompt.append("- Entradas com problema de capacidade: ").append(datasetQualityReport.getEntriesWithCapacityProblem()).append("\n");
        prompt.append("- Entradas sem sala atribuída: ").append(datasetQualityReport.getEntriesWithoutRoom()).append("\n");
        prompt.append("- Entradas com sala desconhecida: ").append(datasetQualityReport.getEntriesWithUnknownRoom()).append("\n");
        prompt.append("- Entradas que não necessitam de sala: ").append(datasetQualityReport.getEntriesNotRequiringRoom()).append("\n\n");

        prompt.append("Instância de otimização usada nesta execução:\n");
        prompt.append("- Número de aulas a otimizar: ").append(optimizationInstance.getEntriesToOptimize().size()).append("\n");
        prompt.append("- Número total de salas candidatas: ").append(optimizationInstance.getCandidateRooms().size()).append("\n");
        prompt.append("- Cada aula usa uma lista reduzida de salas candidatas, com até 30 salas por aula.\n");
        prompt.append("- Logo, o problema tem ").append(optimizationInstance.getEntriesToOptimize().size()).append(" variáveis de decisão.\n");
        prompt.append("- Cada variável representa a escolha de uma sala para uma aula.\n\n");

        prompt.append("Baseline heurística:\n");
        prompt.append("- Existe uma heurística greedy simples usada como comparação.\n");
        prompt.append("- O algoritmo recomendado deve tentar superar essa baseline através de busca evolutiva.\n\n");

        prompt.append("Regras para recomendação dos parâmetros:\n");
        prompt.append("- Não uses sempre valores genéricos baixos.\n");
        prompt.append("- Ajusta os parâmetros ao tamanho da instância.\n");
        prompt.append("- Para NSGA-II com cerca de 50 variáveis, evita max_evaluations inferior a 5000, salvo justificação forte.\n");
        prompt.append("- Para NSGA-II com cerca de 50 variáveis, recomenda population_size entre 50 e 100.\n");
        prompt.append("- A mutation_probability deve ser próxima de 1 / número_de_variáveis. Para 50 variáveis, um valor razoável é cerca de 0.02.\n");
        prompt.append("- A crossover_probability deve estar normalmente entre 0.8 e 0.95.\n");
        prompt.append("- Os parâmetros devem equilibrar qualidade da solução e tempo de execução local.\n\n");

        prompt.append("Responde apenas com JSON válido, sem texto antes ou depois.\n");
        prompt.append("A estrutura obrigatória é:\n");
        prompt.append("{\n");
        prompt.append("  \"task\": \"algorithm_recommendation\",\n");
        prompt.append("  \"problem_type\": \"timetabling_room_assignment\",\n");
        prompt.append("  \"recommended_algorithm\": \"NSGA-II\",\n");
        prompt.append("  \"justification\": \"...\",\n");
        prompt.append("  \"parameters\": {\n");
        prompt.append("    \"population_size\": 50,\n");
        prompt.append("    \"max_evaluations\": 5000,\n");
        prompt.append("    \"crossover_probability\": 0.9,\n");
        prompt.append("    \"mutation_probability\": 0.02\n");
        prompt.append("  },\n");
        prompt.append("  \"alternatives\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"algorithm\": \"...\",\n");
        prompt.append("      \"reason_not_selected\": \"...\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("Importante:\n");
        prompt.append("- O recommended_algorithm deve ser um algoritmo executável nesta aplicação.\n");
        prompt.append("- Não recomendes NSGA-III ou MOEA/D como algoritmo principal porque ainda não estão implementados.\n");
        prompt.append("- Não menciones conflitos de professores na justificação, porque o dataset não contém professores.\n");
        prompt.append("- A justificação deve referir que esta versão resolve realocação de salas com horários fixos.\n");

        return prompt.toString();
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
