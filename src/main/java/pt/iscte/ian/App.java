package pt.iscte.ian;

public class App {
    public static void main(String[] args) {
        System.out.println("Aplicação IAN iniciada.");
        System.out.println("A pedir recomendação de algoritmo ao LLM...");

        try {
            ApplicationConfigLoader configLoader = new ApplicationConfigLoader();
            ApplicationConfig applicationConfig = configLoader.load();

            System.out.println("Configuração da aplicação carregada:");
            System.out.println(applicationConfig);

            DatasetLoader datasetLoader = new DatasetLoader();
            TimetablingDataset dataset = datasetLoader.loadTimetablingDataset();

            System.out.println("Datasets carregados com sucesso.");
            System.out.println("Número de salas: " + dataset.getRooms().size());
            System.out.println("Número de entradas de horário: " + dataset.getScheduleEntries().size());

            System.out.println("Primeira sala carregada:");
            System.out.println(dataset.getRooms().get(0));

            System.out.println("Primeira entrada de horário carregada:");
            System.out.println(dataset.getScheduleEntries().get(0));

            DatasetQualityAnalyzer qualityAnalyzer = new DatasetQualityAnalyzer();
            DatasetQualityReport qualityReport = qualityAnalyzer.analyze(dataset);

            System.out.println("Resumo de qualidade dos datasets:");
            System.out.println(qualityReport);

            OllamaClient ollamaClient = new OllamaClient(
                    applicationConfig.getOllamaApiUrl(),
                    applicationConfig.getOllamaModel()
            );

            AlgorithmCatalog algorithmCatalog = new AlgorithmCatalog();

            PromptBuilder promptBuilder = new PromptBuilder(algorithmCatalog);

            ProblemDescriptionLoader problemLoader = new ProblemDescriptionLoader();
            String problemDescriptionJson = problemLoader.loadProblemDescription();

            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildAlgorithmRecommendationPrompt(problemDescriptionJson, qualityReport);

            AlgorithmRecommendationValidator validator = new AlgorithmRecommendationValidator(algorithmCatalog);

            String llmResponse = ollamaClient.generateResponse(systemPrompt, userPrompt);
            String finalLlmResponse = llmResponse;

            System.out.println("Resposta JSON do LLM:");
            System.out.println(llmResponse);

            AlgorithmConfiguration config = null;

            int correctionAttempt = 0;
            int maxCorrectionAttempts = applicationConfig.getMaxCorrectionAttempts();

            while (config == null) {
                try {
                    config = validator.validateAndCreateConfiguration(finalLlmResponse);
                } catch (Exception validationException) {
                    if (correctionAttempt >= maxCorrectionAttempts) {
                        throw new RuntimeException(
                                "Não foi possível obter uma resposta válida do LLM após "
                                        + maxCorrectionAttempts
                                        + " tentativa(s) de correção. Último erro: "
                                        + validationException.getMessage(),
                                validationException
                        );
                    }

                    correctionAttempt++;

                    System.out.println("Resposta inválida recebida do LLM.");
                    System.out.println("Erro encontrado: " + validationException.getMessage());
                    System.out.println("A pedir correção ao LLM. Tentativa "
                            + correctionAttempt
                            + " de "
                            + maxCorrectionAttempts
                            + "...");

                    String correctionPrompt = promptBuilder.buildCorrectionPrompt(
                            finalLlmResponse,
                            validationException.getMessage()
                    );

                    String correctedResponse = ollamaClient.generateResponse(systemPrompt, correctionPrompt);

                    System.out.println("Resposta corrigida do LLM:");
                    System.out.println(correctedResponse);

                    finalLlmResponse = correctedResponse;
                }
            }

            System.out.println("Resposta validada com sucesso.");
            System.out.println("Configuração criada:");
            System.out.println(config);

            OptimizationRunner runner = new OptimizationRunner();
            OptimizationResult result = runner.execute(config);

            System.out.println("Resultado final da execução:");
            System.out.println(result);

            ExecutionReportWriter reportWriter = new ExecutionReportWriter();
            reportWriter.write(config, result, finalLlmResponse, qualityReport);

        } catch (Exception e) {
            System.out.println("Erro na aplicação:");
            System.out.println(e.getMessage());
        }
    }
}