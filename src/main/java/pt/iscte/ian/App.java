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

            PromptBuilder promptBuilder = new PromptBuilder();

            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildAlgorithmRecommendationPrompt();

            String llmResponse = ollamaClient.generateResponse(systemPrompt, userPrompt);

            System.out.println("Resposta JSON do LLM:");
            System.out.println(llmResponse);

            AlgorithmRecommendationValidator validator = new AlgorithmRecommendationValidator();
            AlgorithmConfiguration config = validator.validateAndCreateConfiguration(llmResponse);

            System.out.println("Resposta validada com sucesso.");
            System.out.println("Configuração criada:");
            System.out.println(config);

            OptimizationRunner runner = new OptimizationRunner();
            runner.execute(config);

        } catch (Exception e) {
            System.out.println("Erro na aplicação:");
            System.out.println(e.getMessage());
        }
    }
}