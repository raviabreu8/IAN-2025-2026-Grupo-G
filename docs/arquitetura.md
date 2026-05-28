# Arquitetura da aplicacao

Este documento resume o fluxo interno da aplicacao, de acordo com o que e executado em `App.java`.

## Visao geral

A aplicacao carrega dados reais de salas e horarios, constroi uma instancia simplificada de otimizacao, pede a um LLM local uma recomendacao de algoritmo e parametros, valida a resposta recebida e executa o algoritmo escolhido com JMetal.

O LLM nao executa codigo. O seu papel e recomendar uma configuracao. A aplicacao Java mantem o controlo da validacao, da execucao e da escrita dos resultados.

## Fluxo de execucao

1. A aplicacao carrega `application_config.json`, que define o URL do Ollama, o modelo local, o numero maximo de tentativas de correcao e o numero maximo de entradas a otimizar.

2. O `DatasetLoader` carrega os ficheiros CSV de salas e horarios e cria um `TimetablingDataset`.

3. O `DatasetQualityAnalyzer` analisa a qualidade geral dos dados, contando salas, entradas de horario, problemas de capacidade, entradas sem sala e outros indicadores.

4. O `TimetablingSolutionEvaluator` avalia o horario original completo e calcula a penalizacao total antes da otimizacao.

5. O `RoomFeatureAnalyzer` analisa a compatibilidade entre as caracteristicas pedidas para as aulas e as caracteristicas reais das salas.

6. O `TimetablingOptimizationInstanceBuilder` cria uma instancia reduzida do problema, escolhendo as aulas problematicas a otimizar e as salas candidatas.

7. A aplicacao avalia duas baselines para comparacao: a atribuicao original das entradas otimizadas e uma solucao greedy simples.

8. O `PromptBuilder` constroi o `system_prompt` e o `algorithm_recommendation_prompt`, combinando a descricao do problema, o catalogo de algoritmos, dados do dataset e dados da instancia.

9. O `OllamaClient` envia o pedido ao LLM local atraves da API do Ollama e recebe uma resposta em JSON.

10. O `AlgorithmRecommendationValidator` valida a resposta do LLM. Se a resposta for invalida, a aplicacao pode pedir uma correcao ao LLM, respeitando o limite definido na configuracao.

11. Depois da validacao, a resposta e convertida numa `AlgorithmConfiguration`, contendo o algoritmo recomendado e os parametros escolhidos.

12. O `OptimizationRunner` delega a execucao para o runner correto, por exemplo `JMetalTimetablingGeneticAlgorithmRunner` ou `JMetalTimetablingNsgaIIRunner`.

13. O algoritmo e executado com JMetal sobre o problema `TimetablingRoomAssignmentProblem`, onde cada variavel representa a escolha de uma sala para uma aula problematica.

14. A melhor solucao encontrada e aplicada ao dataset atraves do `OptimizedTimetablingDatasetBuilder`.

15. O horario otimizado e avaliado novamente para comparar o resultado com o horario original e com as baselines.

16. O `ExecutionReportWriter` grava o relatorio da execucao em `outputs/last_execution.json` e cria uma copia historica em `outputs/history/`.

17. Por fim, a aplicacao exporta os CSVs com as atribuicoes otimizadas e com o horario simplificado apos aplicacao da solucao.

## Componentes principais

- `App`: ponto de entrada e orquestrador do fluxo completo.
- `ApplicationConfigLoader`: carrega a configuracao da aplicacao.
- `DatasetLoader`: carrega os dados de salas e horarios.
- `DatasetQualityAnalyzer`: produz indicadores de qualidade do dataset.
- `TimetablingOptimizationInstanceBuilder`: seleciona as entradas problematicas e salas candidatas.
- `PromptBuilder`: constroi os prompts enviados ao LLM.
- `OllamaClient`: comunica com o LLM local atraves da API do Ollama.
- `AlgorithmRecommendationValidator`: valida a resposta JSON do LLM.
- `OptimizationRunner`: escolhe o runner do algoritmo recomendado.
- `JMetalTimetablingGeneticAlgorithmRunner`: executa o Genetic Algorithm com JMetal.
- `JMetalTimetablingNsgaIIRunner`: executa o NSGA-II com JMetal.
- `ExecutionReportWriter`: escreve o relatorio JSON da execucao.
- `OptimizedAssignmentExporter` e `OptimizedScheduleExporter`: exportam resultados em CSV.

## Decisoes de desenho

- A versao atual resolve realocacao de salas com horarios fixos.
- O dataset nao contem docentes, por isso conflitos de professores nao sao considerados.
- A formulacao atual e de objetivo unico: minimizar a penalizacao total ponderada.
- O catalogo de algoritmos fica em `algorithm_catalog.json`.
- Apenas algoritmos marcados como implementados podem ser executados.
- A resposta do LLM e sempre validada antes de ser usada.
- O relatorio final guarda o pedido enviado ao LLM, a resposta recebida, a configuracao executada e os resultados obtidos.

## Outputs principais

```text
outputs/last_execution.json
outputs/history/
outputs/optimized_assignments.csv
outputs/optimized_schedule_simplified.csv
```

