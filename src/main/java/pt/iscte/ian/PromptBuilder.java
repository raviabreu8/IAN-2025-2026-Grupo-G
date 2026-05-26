package pt.iscte.ian;

public class PromptBuilder {

    private final AlgorithmCatalog algorithmCatalog;

    public PromptBuilder(AlgorithmCatalog algorithmCatalog) {
        this.algorithmCatalog = algorithmCatalog;
    }

    public String buildSystemPrompt() {
        return "És um assistente especializado em algoritmos de otimização disponíveis na framework JMetal 6.1. " +
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
        prompt.append("- A penalização agrega: salas inválidas, capacidade insuficiente, lugares em falta, conflitos de sala, incompatibilidade de características na sala pedida vs. a sala atribuída e capacidade desperdiçada.\n\n");

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
        prompt.append("- Ajusta os parâmetros ao tamanho da instância e o contexto do problema.\n");
        prompt.append("- Os parâmetros devem equilibrar qualidade da solução e tempo de execução local.\n\n");

        prompt.append("Responde apenas com JSON válido, sem texto antes ou depois.\n");
        prompt.append("Campos obrigatórios da resposta JSON:\n");
        prompt.append("- task: deve ser \"algorithm_recommendation\".\n");
        prompt.append("- problem_type: deve ser \"timetabling_room_assignment\".\n");
        prompt.append("- recommended_algorithm: algoritmo executável nesta aplicação.\n");
        prompt.append("- justification: texto curto justificando algoritmo e parâmetros.\n");
        prompt.append("- parameters.population_size: inteiro escolhido pelo LLM.\n");
        prompt.append("- parameters.max_evaluations: inteiro escolhido pelo LLM.\n");
        prompt.append("- parameters.crossover_probability: decimal escolhido pelo LLM.\n");
        prompt.append("- parameters.mutation_probability: decimal escolhido pelo LLM.\n");
        prompt.append("- alternatives: lista de alternativas, ou [] se não houver alternativa relevante.\n\n");

        prompt.append("Importante:\n");
        prompt.append("- O recommended_algorithm deve ser um algoritmo executável nesta aplicação.\n");
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

                Campos obrigatórios da resposta JSON:
                - task: deve ser exatamente "algorithm_recommendation".
                - problem_type: deve ser exatamente "timetabling_room_assignment".
                - recommended_algorithm: deve conter exatamente o nome de um algoritmo implementado nesta aplicação.
                - justification: explicação curta.
                - parameters.population_size: número inteiro escolhido pelo LLM.
                - parameters.max_evaluations: número inteiro escolhido pelo LLM.
                - parameters.crossover_probability: número decimal escolhido pelo LLM.
                - parameters.mutation_probability: número decimal escolhido pelo LLM.
                - alternatives: lista de alternativas, ou [] se não houver alternativa relevante.
                """.formatted(validationError, previousResponse, algorithmsDescription.toString());
    }
}
