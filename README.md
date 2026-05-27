# IAN-2025-2026-Grupo-G

## Projeto

**Projeto 2: Uso de LLMs para escolha e configuração de algoritmos de IA na resolução de problemas de otimização**

Projeto desenvolvido no âmbito da unidade curricular **Inteligência Artificial Aplicada ao Negócio (IAN)**.

## Membro do grupo

- Ravi Abreu - 134640

## Objetivo

Esta aplicação demonstra como um LLM local pode apoiar a seleção e configuração de algoritmos de otimização.

O caso de estudo implementado é um problema de **timetabling simplificado** com dados reais de salas e horários. A versão atual não altera dias nem horas das aulas; o problema tratado é a **realocação de salas para aulas problemáticas**, procurando minimizar uma penalização ponderada associada a capacidade, conflitos de sala, característica pedida para a sala vs. sala designada  e capacidade desperdiçada.

O LLM recomenda o algoritmo e os parâmetros iniciais. A aplicação valida a resposta e executa o algoritmo suportado através da framework **JMetal 6.1**.

## Fluxo da aplicação

```text
Carregamento dos datasets de salas e horários
↓
Análise de qualidade do dataset
↓
Construção da instância de otimização
↓
Construção do pedido ao LLM
↓
Envio ao Ollama local via API
↓
Resposta JSON com algoritmo e parâmetros
↓
Validação automática da resposta
↓
Execução do algoritmo com JMetal
↓
Avaliação e exportação dos resultados
```

## Tecnologias

- Java 17
- Maven
- Ollama
- Modelo local configurável, por defeito `llama3.2:3b`
- JMetal 6.1
- Jackson
- Apache Commons CSV

## Protocolo com o LLM

O protocolo de interação está documentado em:

```text
docs/protocolo_llm.md
```

Em resumo:

- a aplicação envia um `system_prompt` e um `algorithm_recommendation_prompt`;
- o pedido inclui a descrição do problema em JSON, dados do dataset e dados da instância a otimizar;
- o LLM deve responder apenas com JSON válido;
- a resposta é validada antes de ser usada;
- o pedido e a resposta ficam guardados no relatório de execução.

## Algoritmos

O catálogo de algoritmos está em:

```text
src/main/resources/algorithm_catalog.json
```

Estado atual:

| Algoritmo | Estado |
|---|---|
| NSGA-II | Implementado e executável |
| NSGA-III | Conhecido, mas não implementado |
| MOEA/D | Conhecido, mas não implementado |

O LLM pode mencionar NSGA-III e MOEA/D como alternativas futuras, mas o algoritmo principal tem de estar implementado. Atualmente, a execução real é feita com **NSGA-II**.

## Ficheiros principais

```text
src/main/java/pt/iscte/ian/App.java
```

Ponto de entrada da aplicação.

```text
src/main/java/pt/iscte/ian/PromptBuilder.java
```

Constrói os prompts enviados ao LLM.

```text
src/main/java/pt/iscte/ian/AlgorithmRecommendationValidator.java
```

Valida a resposta JSON do LLM.

```text
src/main/java/pt/iscte/ian/JMetalTimetablingNsgaIIRunner.java
```

Executa o NSGA-II com JMetal.

```text
src/main/resources/problem_description.json
```

Descrição formal do problema enviada ao LLM.

```text
src/main/resources/application_config.json
```

Configuração do Ollama, modelo local e tamanho da instância de otimização.

## Pré-requisitos

- Java 17
- Maven
- Ollama instalado e em execução
- Modelo definido em `application_config.json`

Para instalar o modelo usado por defeito:

```powershell
ollama pull llama3.2:3b
```

Para confirmar os modelos disponíveis:

```powershell
ollama list
```

## Executar

Na raiz do projeto:

```powershell
mvn clean compile
```

Depois:

```powershell
mvn exec:java
```

## Saídas geradas

A aplicação gera ficheiros na pasta `outputs/`:

```text
outputs/last_execution.json
outputs/history/
outputs/optimized_assignment.csv
outputs/optimized_schedule_simplified.csv
```

O relatório JSON inclui:

- pedido enviado ao LLM (`llm_request`);
- resposta validada do LLM (`llm_recommendation`);
- qualidade do dataset;
- avaliação do horário original;
- avaliação do horário após aplicação da solução;
- baseline original;
- baseline greedy;
- configuração escolhida;
- resultado da execução do NSGA-II.

## Configuração

O ficheiro principal de configuração é:

```text
src/main/resources/application_config.json
```

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

## Estado atual

Funcionalidades implementadas:

- carregamento dos datasets reais de salas e horários;
- análise de qualidade do dataset;
- construção de instância de otimização;
- comunicação com LLM local através da API do Ollama;
- protocolo de pedido/resposta documentado;
- resposta JSON validada automaticamente;
- mecanismo de correção de resposta inválida;
- execução real do NSGA-II com JMetal;
- comparação com baseline original e greedy;
- geração de relatório JSON;
- exportação de soluções otimizadas para CSV.

## Limitações

- Apenas o NSGA-II está implementado para execução real.
- O problema atual é uma versão simplificada: realocação de salas com horários fixos.
- O dataset não contém docentes, por isso conflitos de professores não são otimizados.
- A aplicação funciona por consola.

## Documentação

```text
docs/protocolo_llm.md
```

O ficheiro `protocolo_llm.md` é o documento principal para explicar ao professor o modelo de comunicação entre a aplicação e o LLM.
