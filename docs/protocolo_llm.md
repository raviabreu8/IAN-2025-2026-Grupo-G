# Protocolo de interação entre a aplicação e o LLM

## Objetivo

Este documento resume o protocolo atualmente implementado entre a aplicação Java e o LLM local.

O projeto usa o LLM como apoio à escolha e configuração de algoritmos de otimização para um problema de timetabling simplificado. A aplicação envia ao LLM a descrição do problema, dados da instância e regras de resposta; o LLM devolve uma recomendação em JSON; a aplicação valida essa resposta e executa o algoritmo suportado através da framework JMetal 6.1.

## Tecnologia usada

- LLM local: Ollama.
- Modelo configurável em `src/main/resources/application_config.json`.
- API usada: endpoint local configurado em `ollama.api_url`.
- Framework de otimização: JMetal 6.1.
- Algoritmo executável nesta versão: NSGA-II.

## Problema de otimização

O problema resolvido nesta versão é uma realocação de salas com horários fixos:

- a aplicação não altera dias nem horas das aulas;
- a aplicação não otimiza professores, porque o dataset não contém docentes;
- cada variável representa a escolha de uma sala para uma aula problemática;
- o objetivo atual é minimizar uma penalização ponderada total;
- a penalização considera capacidade insuficiente, lugares em falta, conflitos de sala, incompatibilidade de características e capacidade desperdiçada.

A descrição formal e estável do problema está em:

```text
src/main/resources/problem_description.json
```

## Pedido enviado ao LLM

A aplicação envia dois prompts através da API do Ollama:

- `system_prompt`: define o papel do LLM e obriga a resposta em JSON válido.
- `algorithm_recommendation_prompt`: contém o pedido principal de recomendação.

O pedido principal combina instruções textuais com uma descrição estruturada em JSON. Inclui:

- descrição do problema carregada de `problem_description.json`;
- catálogo de algoritmos executáveis e alternativas futuras;
- resumo do dataset real;
- tamanho da instância de otimização;
- regras para escolha de parâmetros;
- campos obrigatórios da resposta JSON.

Para efeitos de auditoria, cada execução guarda o pedido completo no relatório:

```json
"llm_request": {
  "problem_description": { },
  "system_prompt": "...",
  "algorithm_recommendation_prompt": "..."
}
```

## Catálogo de algoritmos

O catálogo está em:

```text
src/main/resources/algorithm_catalog.json
```

Neste momento contém:

- `NSGA-II`: conhecido e implementado;
- `NSGA-III`: conhecido, mas não implementado;
- `MOEA/D`: conhecido, mas não implementado.

O LLM pode mencionar NSGA-III e MOEA/D como alternativas futuras, mas o algoritmo principal recomendado tem de estar implementado. Atualmente, só `NSGA-II` pode ser executado.

## Resposta esperada do LLM

O LLM deve responder apenas com JSON válido, sem texto antes ou depois. A estrutura esperada é:

```json
{
  "task": "algorithm_recommendation",
  "problem_type": "timetabling_room_assignment",
  "recommended_algorithm": "NSGA-II",
  "justification": "Justificação curta da escolha do algoritmo e dos parâmetros.",
  "parameters": {
    "population_size": 100,
    "max_evaluations": 5000,
    "crossover_probability": 0.9,
    "mutation_probability": 0.02
  },
  "alternatives": []
}
```

## Validação da resposta

A resposta é validada por `AlgorithmRecommendationValidator` antes de ser usada. A aplicação verifica:

1. se a resposta é JSON válido;
2. se existe `recommended_algorithm`;
3. se existe `parameters`;
4. se o algoritmo existe no catálogo;
5. se o algoritmo está implementado;
6. se existem `population_size`, `max_evaluations`, `crossover_probability` e `mutation_probability`;
7. se os valores numéricos são válidos.

Se a resposta for inválida, a aplicação pode enviar um prompt de correção ao LLM, limitado por `max_correction_attempts` em `application_config.json`.

## Execução do algoritmo

Depois da validação, a resposta do LLM é convertida numa `AlgorithmConfiguration`. O `OptimizationRunner` usa essa configuração para executar o algoritmo correspondente.

Na versão atual:

- `NSGA-II` é executado com JMetal;
- `NSGA-III` e `MOEA/D` são reconhecidos, mas não são executáveis nesta implementação.

## Registo dos resultados

Cada execução gera relatórios em:

```text
outputs/last_execution.json
outputs/history/
```

O relatório inclui:

- pedido enviado ao LLM (`llm_request`);
- resposta validada do LLM (`llm_recommendation`);
- qualidade do dataset;
- avaliação do horário original;
- avaliação do horário após aplicação da solução;
- baseline original;
- baseline greedy;
- configuração escolhida;
- resultado da execução do NSGA-II.

Assim, a execução fica rastreável: é possível ver o que foi enviado ao LLM, o que foi recomendado, como a resposta foi validada e que resultado foi obtido.
