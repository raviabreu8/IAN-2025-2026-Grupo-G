# Arquitetura da aplicação

## Visão geral

A aplicação desenvolvida tem como objetivo usar um LLM local para apoiar a escolha e configuração de algoritmos de otimização, executando depois o algoritmo recomendado sobre um problema de timetabling simplificado baseado em datasets reais do Iscte.

O sistema segue o seguinte fluxo principal:

```text
Datasets reais CSV
↓
Carregamento dos dados
↓
Análise da qualidade dos dados
↓
Criação de uma instância simplificada de timetabling
↓
Construção do prompt
↓
Envio do pedido ao LLM local via Ollama
↓
Resposta JSON com recomendação de algoritmo
↓
Validação da resposta
↓
Execução do algoritmo recomendado com JMetal
↓
Geração de relatório JSON
```

## Componentes principais

### App

Classe principal da aplicação.

Responsabilidades:

- carregar a configuração da aplicação;
- carregar os datasets reais;
- analisar a qualidade dos dados;
- criar a instância simplificada de timetabling;
- pedir ao LLM uma recomendação de algoritmo;
- validar a resposta do LLM;
- executar o algoritmo recomendado;
- gerar o relatório final.

### ApplicationConfig

Representa a configuração global da aplicação.

Inclui:

- URL da API local do Ollama;
- modelo LLM usado;
- número máximo de tentativas de correção;
- número máximo de entradas de horário a otimizar.

### ApplicationConfigLoader

Carrega o ficheiro de configuração da aplicação:

```text
src/main/resources/application_config.json
```

Este ficheiro permite alterar parâmetros da aplicação sem modificar diretamente o código Java.

Exemplo:

```json
{
  "ollama": {
    "api_url": "http://localhost:11434/api/generate",
    "model": "llama3.2:3b"
  },
  "execution": {
    "max_correction_attempts": 1
  },
  "timetabling": {
    "max_entries_for_optimization": 50
  }
}
```

### DatasetLoader

Carrega os datasets reais fornecidos pelo docente:

```text
data/input/Caracterizacao das salas.csv
data/input/Horários 1º sem 2022-23.csv
```

Transforma os dados CSV em objetos Java:

```text
Room
ScheduleEntry
TimetablingDataset
```

### Room

Representa uma sala do Iscte.

Contém informação como:

- edifício;
- nome da sala;
- capacidade normal;
- capacidade de exame.

### ScheduleEntry

Representa uma entrada do horário.

Contém informação como:

- curso;
- unidade de execução;
- turno;
- turma;
- número de inscritos;
- dia da semana;
- hora de início;
- hora de fim;
- data;
- sala atribuída;
- lotação;
- características da sala.

### TimetablingDataset

Agrupa os dados carregados dos CSV.

Contém:

```text
List<Room>
List<ScheduleEntry>
```

É usado como estrutura central para passar os dados reais entre os vários componentes da aplicação.

### DatasetQualityAnalyzer

Analisa a qualidade geral dos datasets carregados.

Calcula métricas como:

- número total de salas;
- número total de entradas de horário;
- número de salas distintas usadas no horário;
- entradas com problema de capacidade;
- entradas sem sala atribuída;
- entradas com sala desconhecida.

### DatasetQualityReport

Representa o resultado da análise de qualidade dos datasets.

Exemplo de saída:

```text
DatasetQualityReport{
  numberOfRooms=131,
  numberOfScheduleEntries=26020,
  numberOfDistinctRoomsUsed=99,
  entriesWithCapacityProblem=2940,
  entriesWithoutRoom=2255,
  entriesWithUnknownRoom=3209
}
```

### TimetablingEvaluator

Avalia o horário atual com base em métricas ligadas ao problema de timetabling.

Calcula:

- violações de capacidade;
- aulas sem sala;
- salas desconhecidas;
- conflitos de sala;
- conflitos de turma;
- penalização total;
- percentagens de violação.

Esta avaliação permite transformar os dados reais em métricas úteis para a futura otimização.

### TimetablingEvaluation

Representa o resultado da avaliação do horário atual.

Exemplo:

```text
TimetablingEvaluation{
  totalEntries=26020,
  capacityViolations=2940,
  missingRoomAssignments=2255,
  unknownRoomAssignments=3209,
  roomTimeConflicts=0,
  classGroupTimeConflicts=733,
  totalPenalty=38795
}
```

### TimetablingOptimizationInstanceBuilder

Cria uma instância simplificada do problema de otimização.

Nesta versão, a aplicação considera apenas um subconjunto das entradas problemáticas do horário.

Cada entrada problemática passa a representar uma futura variável de decisão:

```text
variável i = sala escolhida para a aula problemática i
```

### TimetablingOptimizationInstance

Representa a instância simplificada do problema de otimização.

Contém:

- entradas de horário que serão otimizadas;
- salas candidatas;
- número de variáveis;
- número de salas candidatas.

Exemplo:

```text
TimetablingOptimizationInstance{
  entriesToOptimize=50,
  candidateRooms=131
}
```

### GreedyTimetablingAssignmentBuilder

Cria uma solução inicial simples, usando uma estratégia greedy.

A estratégia usada é:

```text
para cada aula problemática:
  escolher a menor sala que tenha capacidade suficiente
```

Esta solução serve como referência inicial para comparação com o resultado do algoritmo de otimização.

### TimetablingAssignment

Representa uma atribuição de salas às entradas problemáticas.

Internamente, guarda um vetor de inteiros:

```text
roomIndexes[i] = índice da sala escolhida para a entrada i
```

Cada valor inteiro aponta para uma sala dentro da lista de salas candidatas.

### TimetablingAssignmentEvaluator

Avalia uma atribuição candidata de salas.

Calcula:

- número de entradas atribuídas;
- atribuições inválidas;
- violações de capacidade;
- conflitos de sala/horário;
- capacidade desperdiçada;
- penalização total.

Esta classe é usada tanto para avaliar a solução greedy como para avaliar as soluções criadas pelo JMetal.

### TimetablingAssignmentEvaluation

Representa o resultado da avaliação de uma atribuição candidata.

Exemplo:

```text
TimetablingAssignmentEvaluation{
  assignedEntries=50,
  invalidRoomAssignments=0,
  capacityViolations=0,
  roomTimeConflicts=4,
  totalUnusedCapacity=444,
  totalPenalty=476
}
```

### TimetablingRoomAssignmentProblem

Representa o problema simplificado de timetabling no formato esperado pelo JMetal.

Tipo de solução:

```text
IntegerSolution
```

Interpretação das variáveis:

```text
cada variável inteira representa o índice de uma sala candidata
```

Objetivos:

```text
objetivo 1: minimizar penalização total
objetivo 2: minimizar capacidade desperdiçada
```

Nesta fase, o problema considera uma versão simplificada do timetabling real, focada na realocação de salas para entradas problemáticas, mantendo os horários fixos.

### AlgorithmCatalog

Carrega o catálogo de algoritmos a partir de:

```text
src/main/resources/algorithm_catalog.json
```

O catálogo contém:

- algoritmos conhecidos;
- framework em que existem;
- se estão ou não implementados na aplicação;
- descrição;
- tipos de problema para que são adequados.

Exemplo simplificado:

```json
{
  "algorithms": [
    {
      "name": "NSGA-II",
      "framework": "JMetal 6.1",
      "implemented": true,
      "description": "Algoritmo multiobjetivo baseado em ordenação não-dominada.",
      "suitable_for": [
        "multiobjective_optimization",
        "constrained_optimization",
        "timetabling"
      ]
    }
  ]
}
```

### PromptBuilder

Constrói os prompts enviados ao LLM.

Inclui:

- descrição estruturada do problema;
- resumo dos datasets reais;
- catálogo de algoritmos;
- regras para a resposta;
- formato JSON esperado.

Esta classe ajuda a garantir que o LLM responde de forma tratável computacionalmente.

### ProblemDescriptionLoader

Carrega a descrição estruturada do problema a partir de:

```text
src/main/resources/problem_description.json
```

Este ficheiro contém:

- tipo de problema;
- descrição;
- domínio;
- objetivos;
- restrições;
- framework usada.

### OllamaClient

Responsável pela comunicação com o LLM local através da API do Ollama.

Endpoint usado:

```text
http://localhost:11434/api/generate
```

O cliente envia:

- modelo;
- prompt de sistema;
- prompt de utilizador;
- instrução para resposta em JSON.

Recebe a resposta do Ollama e extrai o campo `response`.

### AlgorithmRecommendationValidator

Valida a resposta devolvida pelo LLM.

A resposta só é aceite se:

- estiver em JSON válido;
- indicar um algoritmo conhecido no catálogo;
- indicar um algoritmo implementado;
- possuir os parâmetros obrigatórios;
- os parâmetros estiverem em intervalos válidos.

Parâmetros validados:

- `population_size`;
- `max_evaluations`;
- `crossover_probability`;
- `mutation_probability`.

### AlgorithmConfiguration

Representa a configuração recomendada pelo LLM depois de validada.

Inclui:

- algoritmo escolhido;
- tamanho da população;
- número máximo de avaliações;
- probabilidade de crossover;
- probabilidade de mutação.

### OptimizationRunner

Recebe a configuração validada e decide que runner deve executar.

Nesta versão, o algoritmo implementado é:

```text
NSGA-II
```

Se o LLM recomendar um algoritmo conhecido mas ainda não implementado, a aplicação rejeita ou pede uma correção ao LLM.

### JMetalTimetablingNsgaIIRunner

Executa o algoritmo NSGA-II com JMetal sobre o problema simplificado de timetabling.

Usa:

- `NSGAIIBuilder`;
- `IntegerSBXCrossover`;
- `IntegerPolynomialMutation`;
- `TimetablingRoomAssignmentProblem`.

A execução devolve uma lista de soluções não-dominadas encontradas pelo algoritmo.

### OptimizationResult

Representa o resultado final da execução do algoritmo.

Inclui:

- algoritmo executado;
- tempo de execução;
- número de soluções;
- melhor penalização;
- capacidade desperdiçada associada à melhor penalização;
- melhor capacidade desperdiçada;
- penalização associada à melhor capacidade desperdiçada.

### ExecutionReportWriter

Gera o relatório final da execução em JSON.

Ficheiros gerados:

```text
outputs/last_execution.json
outputs/history/execution_DATA_HORA.json
```

O relatório inclui:

- resposta final do LLM;
- resumo da qualidade dos datasets;
- avaliação do horário atual;
- configuração recomendada;
- resultado da execução do algoritmo;
- melhores objetivos encontrados.

## Modelo de interação com o LLM

A aplicação não permite que o utilizador fale diretamente com o LLM.

O utilizador interage apenas com a aplicação.

A aplicação é responsável por:

1. construir o prompt;
2. enviar o pedido ao LLM;
3. receber JSON;
4. validar a resposta;
5. pedir correção automática se necessário;
6. executar o algoritmo recomendado.

Este modelo evita uma interação livre e não controlada com o LLM, tornando a resposta tratável computacionalmente.

## Pedido enviado ao LLM

A aplicação envia ao LLM informação sobre:

- descrição do problema de otimização;
- resumo dos datasets reais;
- catálogo de algoritmos conhecidos;
- indicação dos algoritmos implementados;
- regras de seleção;
- formato JSON obrigatório para resposta.

A descrição base do problema está em:

```text
src/main/resources/problem_description.json
```

O catálogo de algoritmos está em:

```text
src/main/resources/algorithm_catalog.json
```

## Resposta esperada do LLM

Exemplo de resposta esperada:

```json
{
  "task": "algorithm_recommendation",
  "problem_type": "timetabling",
  "recommended_algorithm": "NSGA-II",
  "justification": "O NSGA-II é adequado para problemas multiobjetivo com restrições.",
  "parameters": {
    "population_size": 30,
    "max_evaluations": 1000,
    "crossover_probability": 0.9,
    "mutation_probability": 0.01
  },
  "alternatives": [
    {
      "algorithm": "MOEA/D",
      "reason_not_selected": "Não está implementado nesta aplicação."
    }
  ]
}
```

## Validação da resposta do LLM

A aplicação valida a resposta antes de a usar.

A resposta é rejeitada se:

- não for JSON válido;
- não contiver `recommended_algorithm`;
- não contiver `parameters`;
- recomendar um algoritmo desconhecido;
- recomendar um algoritmo não implementado;
- usar parâmetros inválidos.

Se a resposta for inválida e ainda existirem tentativas de correção disponíveis, a aplicação envia um novo prompt ao LLM, explicando o erro e pedindo uma resposta corrigida.

## Execução com JMetal

Depois de validada a resposta do LLM, a aplicação cria uma configuração de algoritmo.

Se o algoritmo recomendado for `NSGA-II`, a aplicação executa:

```text
JMetalTimetablingNsgaIIRunner
```

Esse runner cria o problema:

```text
TimetablingRoomAssignmentProblem
```

e executa o NSGA-II com os parâmetros recomendados pelo LLM.

## Problema simplificado de timetabling

Nesta versão, o problema otimizado é uma simplificação do problema real.

A simplificação adotada é:

```text
realocar salas para entradas problemáticas do horário,
mantendo dias e horas fixos.
```

Cada solução representa uma possível atribuição de salas.

Cada variável inteira indica a sala escolhida para uma determinada entrada problemática.

### Objetivos

O problema tem dois objetivos:

```text
1. minimizar penalização total
2. minimizar capacidade desperdiçada
```

### Penalização total

A penalização considera:

- atribuições inválidas;
- violações de capacidade;
- conflitos de sala/horário;
- capacidade desperdiçada.

## Relatório final

Após a execução, a aplicação gera um relatório em:

```text
outputs/last_execution.json
```

Também é guardada uma cópia histórica em:

```text
outputs/history/
```

Exemplo de estrutura do relatório:

```json
{
  "generated_at": "2026-05-16T01:32:28",
  "llm_recommendation": {
    "task": "algorithm_recommendation",
    "problem_type": "timetabling",
    "recommended_algorithm": "NSGA-II"
  },
  "dataset_quality_report": {
    "number_of_rooms": 131,
    "number_of_schedule_entries": 26020,
    "entries_with_capacity_problem": 2940
  },
  "timetabling_evaluation": {
    "total_entries": 26020,
    "total_penalty": 38795
  },
  "configuration": {
    "algorithm": "NSGA-II",
    "population_size": 30,
    "max_evaluations": 1000
  },
  "result": {
    "algorithm": "NSGA-II",
    "execution_time_ms": 684,
    "number_of_solutions": 1,
    "best_penalty": 240.0,
    "unused_capacity_of_best_penalty_solution": 0.0,
    "best_unused_capacity": 0.0,
    "penalty_of_best_unused_capacity_solution": 240.0
  }
}
```

## Estado atual da implementação

Nesta fase, a aplicação já consegue:

- carregar os datasets reais do Iscte;
- analisar a qualidade dos dados;
- avaliar o horário atual;
- criar uma instância simplificada de timetabling;
- criar uma solução greedy inicial;
- criar um problema JMetal baseado no timetabling simplificado;
- pedir ao LLM local uma recomendação de algoritmo;
- validar a resposta do LLM;
- executar NSGA-II através do JMetal;
- otimizar uma atribuição simplificada de salas;
- gerar um relatório JSON com os resultados.

## Limitações atuais

Nesta versão:

- apenas o NSGA-II está implementado para execução real;
- a otimização considera apenas realocação de salas;
- os horários são mantidos fixos;
- professores ainda não são modelados diretamente;
- não existe ainda interface gráfica;
- a aplicação corre em modo consola;
- o número de entradas otimizadas é limitado por configuração;
- o LLM pode justificar incorretamente algumas escolhas, por isso a aplicação valida a resposta;
- os datasets reais são usados numa versão simplificada do problema.

## Próximos passos

Possíveis evoluções:

- aumentar gradualmente o número de entradas otimizadas;
- melhorar a função de avaliação;
- incluir restrições adicionais;
- modelar professores e conflitos de docentes;
- implementar NSGA-III ou MOEA/D;
- comparar resultados entre algoritmos;
- melhorar o relatório final;
- criar documentação JavaDoc;
- criar uma interface simples ou API REST;
- permitir execução parametrizada por linha de comandos;
- permitir comparação entre solução greedy e solução NSGA-II no relatório final.