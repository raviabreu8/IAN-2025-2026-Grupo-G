package pt.iscte.ian;

public class App {
    public static void main(String[] args) {
        System.out.println("Aplicação IAN iniciada.");
        System.out.println("A pedir recomendação de algoritmo ao LLM...");

        try {
            OllamaClient ollamaClient = new OllamaClient(
                    "http://localhost:11434/api/generate",
                    "llama3.2:3b"
            );

            AlgorithmCatalog algorithmCatalog = new AlgorithmCatalog();

            PromptBuilder promptBuilder = new PromptBuilder(algorithmCatalog);

            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildAlgorithmRecommendationPrompt();

            String llmResponse = ollamaClient.generateResponse(systemPrompt, userPrompt);

            System.out.println("Resposta JSON do LLM:");
            System.out.println(llmResponse);

            AlgorithmRecommendationValidator validator = new AlgorithmRecommendationValidator(algorithmCatalog);

            AlgorithmConfiguration config;

            try {
                config = validator.validateAndCreateConfiguration(llmResponse);
            } catch (Exception validationException) {
                System.out.println("Resposta inicial inválida.");
                System.out.println("Erro encontrado: " + validationException.getMessage());
                System.out.println("A pedir correção ao LLM...");

                String correctionPrompt = promptBuilder.buildCorrectionPrompt(
                        llmResponse,
                        validationException.getMessage()
                );

                String correctedResponse = ollamaClient.generateResponse(systemPrompt, correctionPrompt);

                System.out.println("Resposta corrigida do LLM:");
                System.out.println(correctedResponse);

                config = validator.validateAndCreateConfiguration(correctedResponse);
            }

            System.out.println("Resposta validada com sucesso.");
            System.out.println("Configuração criada:");
            System.out.println(config);

            OptimizationRunner runner = new OptimizationRunner();
            OptimizationResult result = runner.execute(config);

            System.out.println("Resultado final da execução:");
            System.out.println(result);

            ExecutionReportWriter reportWriter = new ExecutionReportWriter();
            reportWriter.write(config, result);

        } catch (Exception e) {
            System.out.println("Erro na aplicação:");
            System.out.println(e.getMessage());
        }
    }
}