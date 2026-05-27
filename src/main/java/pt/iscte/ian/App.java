package pt.iscte.ian;

public class App {
    public static void main(String[] args) {
        System.out.println("Aplicação IAN iniciada.");

        try {
            ApplicationConfigLoader configLoader = new ApplicationConfigLoader();
            ApplicationConfig applicationConfig = configLoader.load();

            System.out.println("Configuração da aplicação carregada:");
            System.out.println(applicationConfig);
            System.out.println();

            DatasetLoader datasetLoader = new DatasetLoader();
            TimetablingDataset dataset = datasetLoader.loadTimetablingDataset();

            System.out.println("Datasets carregados com sucesso.");
            System.out.println("Número de salas: " + dataset.getRooms().size());
            System.out.println("Número de entradas de horário: " + dataset.getScheduleEntries().size());
            System.out.println();

            
            System.out.println("Primeira sala carregada:");
            System.out.println(dataset.getRooms().get(0));
            System.out.println();


            System.out.println("Primeira entrada de horário carregada:");
            System.out.println(dataset.getScheduleEntries().get(0));
            System.out.println();

            DatasetQualityAnalyzer qualityAnalyzer = new DatasetQualityAnalyzer();
            DatasetQualityReport qualityReport = qualityAnalyzer.analyze(dataset);

            System.out.println("Resumo de qualidade dos datasets:");
            System.out.println(qualityReport);
            System.out.println();


            TimetablingSolutionEvaluator timetablingSolutionEvaluator = new TimetablingSolutionEvaluator();
            TimetablingSolutionEvaluation timetablingEvaluation = timetablingSolutionEvaluator.evaluate(dataset);

            System.out.println("Avaliação do horário atual:");
            System.out.println(timetablingEvaluation);
            System.out.println();


            RoomFeatureAnalyzer roomFeatureAnalyzer = new RoomFeatureAnalyzer();
            RoomFeatureAnalysisReport roomFeatureReport = roomFeatureAnalyzer.analyze(dataset);

            System.out.println("Análise de compatibilidade das características das salas:");
            System.out.println(roomFeatureReport);
            System.out.println();


            TimetablingOptimizationInstanceBuilder instanceBuilder = new TimetablingOptimizationInstanceBuilder();

            TimetablingOptimizationInstance optimizationInstance = instanceBuilder.build(
                    dataset,
                    applicationConfig.getMaxEntriesForOptimization()
            );

            System.out.println("Instância simplificada de otimização criada:");
            System.out.println(optimizationInstance);
            System.out.println();


            System.out.println("Número de variáveis futuras no problema JMetal: "
                    + optimizationInstance.getNumberOfVariables());

            System.out.println("Número de salas candidatas: "
                    + optimizationInstance.getNumberOfCandidateRooms());
            System.out.println();
            TimetablingAssignmentEvaluator assignmentEvaluator =
                    new TimetablingAssignmentEvaluator(dataset, optimizationInstance);

            OriginalTimetablingAssignmentBuilder originalAssignmentBuilder =
                    new OriginalTimetablingAssignmentBuilder();

            TimetablingAssignment originalAssignment =
                    originalAssignmentBuilder.build(optimizationInstance);

            TimetablingAssignmentEvaluation originalAssignmentEvaluation =
                    assignmentEvaluator.evaluate(originalAssignment);

            System.out.println("Avaliação da atribuição original nas entradas otimizadas:");
            System.out.println(originalAssignmentEvaluation);
            System.out.println();

            GreedyTimetablingAssignmentBuilder greedyAssignmentBuilder = new GreedyTimetablingAssignmentBuilder();
            TimetablingAssignment greedyAssignment = greedyAssignmentBuilder.build(optimizationInstance);

            TimetablingAssignmentEvaluation assignmentEvaluation =
                    assignmentEvaluator.evaluate(greedyAssignment);

            System.out.println("Avaliação da atribuição greedy inicial:");
            System.out.println(assignmentEvaluation);
            System.out.println();

            OllamaClient ollamaClient = new OllamaClient(
                    applicationConfig.getOllamaApiUrl(),
                    applicationConfig.getOllamaModel()
            );

            AlgorithmCatalog algorithmCatalog = new AlgorithmCatalog();

            PromptBuilder promptBuilder = new PromptBuilder(algorithmCatalog);

            ProblemDescriptionLoader problemLoader = new ProblemDescriptionLoader();
            String problemDescriptionJson = problemLoader.loadProblemDescription();

            String systemPrompt = promptBuilder.buildSystemPrompt();
            String algorithmRecommendationPrompt = promptBuilder.buildAlgorithmRecommendationPrompt(
                    problemDescriptionJson,
                    qualityReport,
                    optimizationInstance
            );

            AlgorithmRecommendationValidator validator = new AlgorithmRecommendationValidator(algorithmCatalog);

            System.out.println("A pedir recomendação de algoritmo ao LLM...");
            System.out.println();

            String llmResponse = ollamaClient.generateResponse(systemPrompt, algorithmRecommendationPrompt);
            String finalLlmResponse = llmResponse;

            System.out.println("Resposta JSON do LLM:");
            System.out.println(llmResponse);
            System.out.println();

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
            System.out.println();

            OptimizationRunner runner = new OptimizationRunner();
            OptimizationResult result = runner.execute(
                    config,
                    dataset,
                    optimizationInstance
            );

            OptimizedTimetablingDatasetBuilder optimizedDatasetBuilder =
                    new OptimizedTimetablingDatasetBuilder();

            TimetablingDataset optimizedDataset =
                    optimizedDatasetBuilder.build(dataset, optimizationInstance, result);

            TimetablingSolutionEvaluation optimizedTimetablingEvaluation =
                    timetablingSolutionEvaluator.evaluate(optimizedDataset);

            System.out.println("Avaliação do horário completo após aplicação da solução NSGA-II:");
            System.out.println(optimizedTimetablingEvaluation);
            System.out.println();


            System.out.println("Resultado final da execução:");
            System.out.println(result);
            System.out.println();


            ExecutionReportWriter reportWriter = new ExecutionReportWriter();
            reportWriter.write(
                                config,
                                result,
                                finalLlmResponse,
                                systemPrompt,
                                algorithmRecommendationPrompt,
                                qualityReport,
                                timetablingEvaluation,
                                optimizedTimetablingEvaluation,
                                optimizationInstance,
                                originalAssignmentEvaluation,
                                assignmentEvaluation
            );

            OptimizedAssignmentExporter optimizedAssignmentExporter = new OptimizedAssignmentExporter();
            optimizedAssignmentExporter.export(optimizationInstance, result);

            OptimizedScheduleExporter optimizedScheduleExporter = new OptimizedScheduleExporter();
            optimizedScheduleExporter.export(dataset, optimizationInstance, result);

        } catch (Exception e) {
            System.out.println("Erro na aplicação:");
            System.out.println(e.getMessage());
        }
    }
}
