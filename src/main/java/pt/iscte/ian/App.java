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

            TimetablingSolutionEvaluator timetablingSolutionEvaluator = new TimetablingSolutionEvaluator();
            TimetablingSolutionEvaluation timetablingEvaluation = timetablingSolutionEvaluator.evaluate(dataset);

            System.out.println("Avaliação do horário atual:");
            System.out.println(timetablingEvaluation);

            RoomFeatureAnalyzer roomFeatureAnalyzer = new RoomFeatureAnalyzer();
            RoomFeatureAnalysisReport roomFeatureReport = roomFeatureAnalyzer.analyze(dataset);

            System.out.println("Análise de compatibilidade das características das salas:");
            System.out.println(roomFeatureReport);

            TimetablingOptimizationInstanceBuilder instanceBuilder = new TimetablingOptimizationInstanceBuilder();

            TimetablingOptimizationInstance optimizationInstance = instanceBuilder.build(
                    dataset,
                    applicationConfig.getMaxEntriesForOptimization()
            );

            System.out.println("Instância simplificada de otimização criada:");
            System.out.println(optimizationInstance);

            System.out.println("Número de variáveis futuras no problema JMetal: "
                    + optimizationInstance.getNumberOfVariables());

            System.out.println("Número de salas candidatas: "
                    + optimizationInstance.getNumberOfCandidateRooms());
            
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

            GreedyTimetablingAssignmentBuilder greedyAssignmentBuilder = new GreedyTimetablingAssignmentBuilder();
            TimetablingAssignment greedyAssignment = greedyAssignmentBuilder.build(optimizationInstance);

            TimetablingAssignmentEvaluation assignmentEvaluation =
                    assignmentEvaluator.evaluate(greedyAssignment);

            System.out.println("Avaliação da atribuição greedy inicial:");
            System.out.println(assignmentEvaluation);

            TimetablingRoomAssignmentProblem timetablingProblem =
            new TimetablingRoomAssignmentProblem(dataset, optimizationInstance);

            var randomSolution = timetablingProblem.createSolution();
            timetablingProblem.evaluate(randomSolution);

            System.out.println("Problema JMetal simplificado criado:");
            System.out.println("Nome do problema: " + timetablingProblem.name());
            System.out.println("Número de variáveis: " + timetablingProblem.numberOfVariables());
            System.out.println("Número de objetivos: " + timetablingProblem.numberOfObjectives());
            System.out.println("Avaliação de uma solução aleatória:");
            System.out.println("Objetivo único - penalização total: " + randomSolution.objectives()[0]);

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
            OptimizationResult result = runner.execute(
                    config,
                    dataset,
                    optimizationInstance
            );

            System.out.println("Resultado final da execução:");
            System.out.println(result);

            ExecutionReportWriter reportWriter = new ExecutionReportWriter();
            reportWriter.write(
                                config,
                                result,
                                finalLlmResponse,
                                qualityReport,
                                timetablingEvaluation,
                                optimizationInstance,
                                originalAssignmentEvaluation,
                                assignmentEvaluation
            );

            OptimizedAssignmentExporter optimizedAssignmentExporter = new OptimizedAssignmentExporter();
            optimizedAssignmentExporter.export(optimizationInstance, result);

            OptimizedScheduleExporter optimizedScheduleExporter = new OptimizedScheduleExporter();
            optimizedScheduleExporter.export(dataset, optimizationInstance, result);

        } catch (Exception e) {
            System.out.println("Erro na aplicaçã:");
            System.out.println(e.getMessage());
        }
    }
}
