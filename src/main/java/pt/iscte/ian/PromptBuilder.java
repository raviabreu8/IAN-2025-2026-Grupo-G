package pt.iscte.ian;

public class PromptBuilder {

    public String buildSystemPrompt() {
        return "És um assistente especializado em algoritmos de otimização multiobjetivo disponíveis na framework JMetal 6.1. " +
                "Responde sempre em JSON válido. Não escrevas texto fora do JSON.";
    }

    public String buildAlgorithmRecommendationPrompt() {
        return """
                Analisa o seguinte problema de otimização:

                Tipo de problema: timetabling universitário
                Descrição: alocação de aulas a salas e horários.

                Objetivos:
                - minimizar conflitos de horários
                - minimizar conflitos de professores
                - maximizar uso adequado da capacidade das salas
                - minimizar penalizações por violações de restrições

                Restrições:
                - um professor não pode estar em duas aulas ao mesmo tempo
                - uma sala não pode ter duas aulas ao mesmo tempo
                - a capacidade da sala deve ser suficiente para a turma
                - aulas da mesma turma não devem sobrepor-se

                Algoritmos disponíveis na framework JMetal 6.1:
                - NSGA-II
                - NSGA-III
                - MOEA/D

                Escolhe exatamente UM algoritmo principal.

                Responde obrigatoriamente neste formato JSON:
                {
                  "task": "algorithm_recommendation",
                  "problem_type": "timetabling",
                  "recommended_algorithm": "NSGA-II | NSGA-III | MOEA/D",
                  "justification": "explicação curta",
                  "parameters": {
                    "population_size": 100,
                    "max_evaluations": 25000,
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
                """;
    }
}