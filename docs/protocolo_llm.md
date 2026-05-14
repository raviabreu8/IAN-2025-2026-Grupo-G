# Protocolo de interação entre a aplicação e o LLM

## Objetivo

Este documento define a forma como a aplicação comunica com o LLM local.

A aplicação envia ao LLM uma descrição estruturada de um problema de otimização e recebe como resposta uma recomendação de algoritmo, juntamente com parâmetros iniciais para a sua execução na framework JMetal 6.1.

## Pedido enviado pela aplicação ao LLM

A aplicação envia um pedido em formato JSON com a descrição do problema, os objetivos, as restrições e os algoritmos disponíveis.

```json
{
  "task": "recommend_optimization_algorithm",
  "problem": {
    "type": "timetabling",
    "description": "Alocação de aulas a salas e horários num contexto universitário.",
    "objectives": [
      "minimize_schedule_conflicts",
      "minimize_teacher_conflicts",
      "maximize_room_capacity_usage",
      "minimize_constraint_violations"
    ],
    "constraints": [
      "no_teacher_overlap",
      "no_room_overlap",
      "room_capacity_must_be_sufficient",
      "no_class_overlap_for_same_group"
    ]
  },
  "available_algorithms": [
    "NSGA-II",
    "NSGA-III",
    "MOEA/D"
  ],
  "framework": "JMetal 6.1"
}
```

## Resposta esperada do LLM

O LLM deve responder apenas em JSON válido, escolhendo exatamente um algoritmo principal.

```json
{
  "task": "algorithm_recommendation",
  "problem_type": "timetabling",
  "recommended_algorithm": "NSGA-II",
  "justification": "O NSGA-II é adequado para problemas de otimização multiobjetivo com restrições.",
  "parameters": {
    "population_size": 100,
    "max_evaluations": 25000,
    "crossover_probability": 0.9,
    "mutation_probability": 0.01
  },
  "alternatives": [
    {
      "algorithm": "NSGA-III",
      "reason_not_selected": "Pode ser mais adequado para problemas com muitos objetivos."
    },
    {
      "algorithm": "MOEA/D",
      "reason_not_selected": "Pode exigir uma configuração mais cuidadosa de decomposição do problema."
    }
  ]
}
```

## Regras de validação da resposta

A aplicação só deve aceitar a resposta do LLM se:

1. A resposta estiver em JSON válido.
2. O campo `recommended_algorithm` existir.
3. O algoritmo recomendado for um dos algoritmos permitidos:
   - NSGA-II
   - NSGA-III
   - MOEA/D
4. O campo `parameters` existir.
5. `population_size` for um número inteiro positivo.
6. `max_evaluations` for um número inteiro positivo.
7. `crossover_probability` estiver entre 0 e 1.
8. `mutation_probability` estiver entre 0 e 1.

## Prompt base enviado ao LLM

Antes de enviar o pedido principal, a aplicação deve definir o seguinte contexto para o LLM:

```text
És um assistente especializado em algoritmos de otimização multiobjetivo disponíveis na framework JMetal 6.1.

A tua tarefa é analisar problemas de otimização e recomendar exatamente um algoritmo disponível.

Deves responder sempre em JSON válido, sem texto adicional fora do JSON.

Só podes recomendar algoritmos presentes na lista fornecida pela aplicação.
```

## Observação

O LLM é usado como apoio à decisão. A aplicação não deve confiar cegamente na resposta recebida. A resposta deve ser sempre validada antes de ser usada para configurar e executar qualquer algoritmo.