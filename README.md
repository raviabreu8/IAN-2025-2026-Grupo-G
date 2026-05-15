# IAN-2025-26-Grupo-G

## Projeto

**Uso de LLMs para escolha e configuração de algoritmos de IA na resolução de problemas de otimização**

Este projeto foi desenvolvido no âmbito da unidade curricular **Inteligência Artificial Aplicada ao Negócio (IAN)**.

## Membros do grupo

- Ravi Abreu — 134640

## Objetivo

O objetivo deste projeto é desenvolver uma aplicação que utiliza um **LLM local** para apoiar a escolha e configuração de algoritmos de otimização disponíveis na framework **JMetal 6.1**.

A aplicação envia ao LLM uma descrição estruturada de um problema de otimização, neste caso um problema de **timetabling universitário**, e recebe como resposta uma recomendação sobre:

- algoritmo mais adequado;
- parâmetros iniciais;
- justificação da escolha;
- alternativas consideradas.

Depois de receber a resposta, a aplicação valida automaticamente o JSON devolvido pelo LLM e, se a recomendação for válida, executa dinamicamente o algoritmo correspondente através da framework JMetal.

## Descrição geral do funcionamento

O funcionamento atual da aplicação segue este fluxo:

```text
Descrição do problema em JSON
↓
Catálogo de algoritmos em JSON
↓
Construção do prompt
↓
Envio do pedido ao LLM local via Ollama
↓
Resposta JSON do LLM
↓
Validação da resposta
↓
Criação da configuração do algoritmo
↓
Execução do algoritmo através do JMetal
↓
Geração de relatório JSON com os resultados
```

## Tecnologias utilizadas

- Java 17
- Maven
- Ollama
- Modelo LLM local: `llama3.2:3b`
- JMetal 6.1
- Jackson Databind
- GitHub

## Estrutura do projeto

```text
docs/
  protocolo_llm.md

src/
  main/
    java/
      pt/
        iscte/
          ian/
            App.java
            OllamaClient.java
            PromptBuilder.java
            ProblemDescriptionLoader.java
            AlgorithmCatalog.java
            AlgorithmRecommendationValidator.java
            AlgorithmConfiguration.java
            OptimizationRunner.java
            OptimizationResult.java
            JMetalNsgaIIRunner.java
            ExecutionReportWriter.java

    resources/
      problem_description.json
      algorithm_catalog.json

outputs/
  last_execution.json
```

A pasta `outputs/` é gerada automaticamente pela aplicação e está ignorada pelo Git.

## Ficheiros principais

### `problem_description.json`

Contém a descrição estruturada do problema de otimização enviado ao LLM.

Inclui:

- tipo de problema;
- domínio;
- objetivos;
- restrições;
- framework usada.

Localização:

```text
src/main/resources/problem_description.json
```

### `algorithm_catalog.json`

Contém o catálogo de algoritmos conhecidos pela aplicação.

Inclui:

- nome do algoritmo;
- framework;
- indicação se está implementado;
- descrição;
- tipo de problemas para os quais é adequado.

Localização:

```text
src/main/resources/algorithm_catalog.json
```

### `protocolo_llm.md`

Documenta o protocolo de interação entre a aplicação e o LLM, incluindo o formato JSON dos pedidos e respostas.

Localização:

```text
docs/protocolo_llm.md
```

## Algoritmos considerados

O catálogo inicial inclui:

| Algoritmo | Framework | Implementado na aplicação |
|---|---|---|
| NSGA-II | JMetal 6.1 | Sim |
| NSGA-III | JMetal 6.1 | Não |
| MOEA/D | JMetal 6.1 | Não |

Nesta versão do protótipo, o algoritmo executado de forma real através do JMetal é o **NSGA-II**.

Os algoritmos **NSGA-III** e **MOEA/D** estão representados no catálogo, mas ainda não estão implementados no runner da aplicação.

## Validação da resposta do LLM

A aplicação não confia cegamente na resposta do LLM.

A resposta só é aceite se cumprir as seguintes condições:

- estar em JSON válido;
- conter o campo `recommended_algorithm`;
- conter o campo `parameters`;
- recomendar um algoritmo conhecido no catálogo;
- recomendar um algoritmo implementado na aplicação;
- incluir `population_size` positivo;
- incluir `max_evaluations` positivo;
- incluir `crossover_probability` entre 0 e 1;
- incluir `mutation_probability` entre 0 e 1.

Se a resposta inicial do LLM for inválida, a aplicação envia um novo pedido de correção ao LLM, indicando o erro detetado.

## Pré-requisitos

Antes de executar a aplicação, é necessário ter instalado:

- Java 17
- Maven
- Ollama
- Modelo local `llama3.2:3b`

## Instalar o modelo LLM local

Caso o modelo ainda não esteja instalado no Ollama, executar:

```powershell
ollama pull llama3.2:3b
```

Para confirmar se o modelo está disponível:

```powershell
ollama list
```

Deve aparecer algo semelhante a:

```text
llama3.2:3b
```

## Executar a aplicação

Na raiz do projeto, executar:

```powershell
mvn clean compile
```

Depois:

```powershell
mvn exec:java
```

## Resultado esperado

Durante a execução, a aplicação deverá:

1. iniciar a aplicação Java;
2. carregar a descrição do problema;
3. carregar o catálogo de algoritmos;
4. enviar o pedido ao LLM local;
5. receber uma recomendação em JSON;
6. validar a resposta;
7. criar a configuração do algoritmo;
8. executar o algoritmo recomendado, caso esteja implementado;
9. guardar um relatório final em JSON.

Exemplo de saída esperada:

```text
Aplicação IAN iniciada.
A pedir recomendação de algoritmo ao LLM...
Resposta JSON do LLM:
{
  "task": "algorithm_recommendation",
  "problem_type": "timetabling",
  "recommended_algorithm": "NSGA-II",
  ...
}

Resposta validada com sucesso.
Configuração criada:
AlgorithmConfiguration{algorithm='NSGA-II', populationSize=100, maxEvaluations=25000, crossoverProbability=0.9, mutationProbability=0.01}

A preparar execução do algoritmo...
A executar o algoritmo NSGA-II com JMetal.
NSGA-II terminou a execução.

Resultado final da execução:
OptimizationResult{algorithm='NSGA-II', executionTimeMs=..., numberOfSolutions=...}
```

## Relatório gerado

Após a execução, é criado automaticamente o ficheiro:

```text
outputs/last_execution.json
```

Este ficheiro contém:

- data/hora da execução;
- resposta final do LLM;
- algoritmo recomendado;
- parâmetros utilizados;
- tempo de execução;
- número de soluções obtidas.

Exemplo da estrutura:

```json
{
  "generated_at": "2026-05-15T00:00:00",
  "llm_recommendation": {
    "task": "algorithm_recommendation",
    "problem_type": "timetabling",
    "recommended_algorithm": "NSGA-II",
    "justification": "..."
  },
  "configuration": {
    "algorithm": "NSGA-II",
    "population_size": 100,
    "max_evaluations": 25000,
    "crossover_probability": 0.9,
    "mutation_probability": 0.01
  },
  "result": {
    "algorithm": "NSGA-II",
    "execution_time_ms": 878,
    "number_of_solutions": 98
  }
}
```

## Estado atual do projeto

Funcionalidades já implementadas:

- instalação e teste do Ollama;
- uso de LLM local;
- comunicação com o Ollama através da API local;
- construção de prompts;
- descrição do problema em JSON;
- catálogo de algoritmos em JSON;
- resposta do LLM em JSON;
- validação automática da resposta do LLM;
- mecanismo de correção caso a resposta seja inválida;
- criação de configuração Java a partir da resposta validada;
- execução real do NSGA-II através do JMetal;
- geração de relatório JSON com o resultado final.

## Limitações atuais

Nesta versão do protótipo:

- apenas o algoritmo NSGA-II está implementado para execução real;
- o problema executado no JMetal ainda é um problema de teste, baseado em ZDT1;
- a integração com o dataset real de horários/salas do ISCTE ainda não está concluída;
- ainda não existe interface gráfica;
- o projeto ainda funciona como aplicação de consola.

## Trabalho futuro

Possíveis melhorias futuras:

- implementar runners para NSGA-III e MOEA/D;
- integrar o dataset real de salas e horários do ISCTE;
- criar uma representação computacional específica para o problema de timetabling;
- adicionar uma interface gráfica ou API REST;
- guardar histórico de execuções;
- comparar resultados entre algoritmos diferentes;
- permitir que o utilizador escolha o problema ou os objetivos a considerar.

## Comandos úteis

Compilar o projeto:

```powershell
mvn clean compile
```

Executar o projeto:

```powershell
mvn exec:java
```

Verificar modelos instalados no Ollama:

```powershell
ollama list
```

Instalar o modelo usado no projeto:

```powershell
ollama pull llama3.2:3b
```