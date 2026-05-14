# IAN-2025-26-Grupo-G

## Projeto

Uso de LLMs para escolha e configuração de algoritmos de IA na resolução de problemas de otimização.

## Membros do grupo

- Ravi Abreu — 134640

## Objetivo

Este projeto tem como objetivo desenvolver uma aplicação que utiliza um LLM local para apoiar a escolha e configuração de algoritmos de otimização disponíveis na framework JMetal 6.1.

A aplicação envia ao LLM uma descrição estruturada de um problema de otimização, neste caso timetabling universitário, e recebe como resposta uma recomendação de algoritmo, parâmetros iniciais e justificação.

## Tecnologias previstas

- Ollama
- Modelo LLM local: llama3.2:3b
- JMetal 6.1
- Java
- Maven
- GitHub

## Estado atual

- Ollama instalado
- Modelo llama3.2:3b testado localmente
- API local do Ollama testada com sucesso
- Primeira resposta JSON obtida a partir do LLM

## Estrutura do projeto

```text
docs/              documentação do projeto
docs/evidencias/   testes e evidências
src/               código da aplicação
data/              datasets ou exemplos
scripts/           scripts auxiliares