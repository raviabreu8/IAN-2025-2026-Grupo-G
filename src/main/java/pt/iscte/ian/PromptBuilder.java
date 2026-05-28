package pt.iscte.ian;

/**
 * Constroi os prompts usados na comunicacao entre a aplicacao e o LLM.
 *
 * <p>Esta classe centraliza as instrucoes enviadas ao modelo: o papel do LLM,
 * a descricao do problema, o catalogo de algoritmos disponiveis, os dados da
 * instancia de otimizacao e o formato JSON esperado na resposta.</p>
 */
public class PromptBuilder {

    private final AlgorithmCatalog algorithmCatalog;

    /**
     * Cria um construtor de prompts com acesso ao catalogo de algoritmos da aplicacao.
     *
     * @param algorithmCatalog catalogo com os algoritmos conhecidos e o respetivo estado de implementacao
     */
    public PromptBuilder(AlgorithmCatalog algorithmCatalog) {
        this.algorithmCatalog = algorithmCatalog;
    }

    /**
     * Constroi o prompt de sistema enviado ao LLM.
     *
     * <p>Este prompt define o papel geral do modelo e reforca que a resposta deve
     * ser sempre JSON valido, sem texto adicional fora do JSON.</p>
     *
     * @return prompt de sistema para a chamada ao LLM
     */
    public String buildSystemPrompt() {
        return "És um assistente especializado em algoritmos de otimização disponíveis na framework JMetal 6.1. " +
                "Responde sempre em JSON válido. Não escrevas texto fora do JSON.";
    }

    /**
     * Constroi o prompt principal de recomendacao de algoritmo.
     *
     * <p>O prompt combina a descricao formal do problema, o catalogo de algoritmos,
     * estatisticas do dataset e informacao sobre a instancia de otimizacao. O objetivo
     * e obter do LLM uma recomendacao tratavel automaticamente pela aplicacao.</p>
     *
     * @param problemDescriptionJson descricao do problema carregada de {@code problem_description.json}
     * @param datasetQualityReport resumo de qualidade dos dados usados na execucao
     * @param optimizationInstance instancia simplificada que sera usada pelo algoritmo de otimizacao
     * @return prompt principal enviado ao LLM
     */
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

        prompt.append("Descrição detalhada do problema em JSON:\n");
        prompt.append(problemDescriptionJson).append("\n\n");

        prompt.append("Catálogo de algoritmos disponíveis:\n");
        for(AlgorithmCatalog.AlgorithmInfo algorithm : algorithmCatalog.getAlgorithms()) {
            prompt.append("- ").append(algorithm.name())
                    .append(": implementado nesta aplicação? ").append(algorithm.implemented() ? "sim" : "não")
                    .append(". Descrição: ").append(algorithm.description()).append("\n");
        }
        prompt.append("\n");

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

        prompt.append("Regras para recomendação dos parâmetros:\n");
        prompt.append("- Ajusta os parâmetros ao tamanho da instância e o contexto do problema.\n");
        prompt.append("- Os parâmetros devem equilibrar qualidade da solução e tempo de execução local.\n\n");

        prompt.append("Responde apenas com JSON válido, sem texto antes ou depois.\n");
        prompt.append("Campos obrigatórios da resposta JSON:\n");
        prompt.append("- task: deve ser \"algorithm_recommendation\".\n");
        prompt.append("- problem_type: deve ser \"timetabling_room_assignment\".\n");
        prompt.append("- recommended_algorithm: algoritmo implementado nesta aplicação.\n");
        prompt.append("- justification: texto justificando algoritmo e parâmetros.\n");
        prompt.append("- parameters.population_size: inteiro escolhido pelo LLM.\n");
        prompt.append("- parameters.max_evaluations: inteiro escolhido pelo LLM.\n");
        prompt.append("- parameters.crossover_probability: decimal escolhido pelo LLM.\n");
        prompt.append("- parameters.mutation_probability: decimal escolhido pelo LLM.\n");
        prompt.append("- alternatives: lista de alternativas.\n\n");

        prompt.append("Importante:\n");
        prompt.append("- O recommended_algorithm deve ser um algoritmo implementado nesta aplicação.\n");

        return prompt.toString();
    }
    
    /**
     * Constroi um prompt de correcao quando a resposta anterior do LLM e invalida.
     *
     * <p>Este prompt inclui a resposta anterior, o erro encontrado pela validacao e
     * o catalogo de algoritmos, pedindo ao LLM uma nova resposta que cumpra o contrato
     * JSON esperado pela aplicacao.</p>
     *
     * @param previousResponse resposta anterior produzida pelo LLM
     * @param validationError erro detetado durante a validacao da resposta
     * @return prompt de correcao enviado ao LLM
     */
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
