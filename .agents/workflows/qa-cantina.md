---
description: 
---

# QA Cantina — Auditoria Completa

Descrição: Auditoria somente-leitura, em sete fases, do backend Java/Spring Boot da cantina, com foco em bugs, qualidade, Engenharia de Software e preparação para banca.

---

## Regra global

Antes de começar, siga integralmente:

- `AGENTS.md`;
- `.agents/rules/qa-readonly.md`;
- as demais regras aplicáveis ao projeto.

Não altere o código-fonte.

Não:

- refatore;
- aplique correções;
- instale dependências;
- faça commits;
- altere o banco de dados;
- modifique configurações funcionais;
- interrompa a auditoria para pedir confirmação.

Registre dúvidas e hipóteses e continue a análise.

---

## Execução

Execute as fases rigorosamente na seguinte ordem:

1. `/qa-01-mapeamento`
2. `/qa-02-modelo-banco`
3. `/qa-03-repository-service`
4. `/qa-04-controller-api`
5. `/qa-05-break-system`
6. `/qa-06-engenharia`
7. `/qa-07-final`

Não pule fases.

Não execute uma fase fora da ordem.

Não considere a auditoria concluída antes da execução das sete fases.

---

## Contexto acumulativo

Cada fase deve considerar:

- o estado consolidado produzido pelas fases anteriores;
- os relatórios anteriores em `docs/qa/`;
- os achados já registrados;
- as dúvidas pendentes;
- as hipóteses adotadas;
- os componentes ainda não auditados.

Não descarte conclusões anteriores.

Se uma análise posterior confirmar, alterar ou invalidar uma conclusão anterior, registre explicitamente essa mudança.

Ao finalizar cada fase, produza ou atualize o relatório correspondente em `docs/qa/`, quando solicitado pelo workflow.

O estado consolidado deve permanecer disponível para as fases seguintes.

---

## Dúvidas e regras de negócio

Se uma fase encontrar uma regra de negócio não definida:

1. registre a dúvida;
2. explique por que ela é relevante;
3. declare uma hipótese temporária quando possível;
4. continue a auditoria.

Nunca interrompa toda a auditoria por causa de uma dúvida isolada.

Nunca classifique como BUG CONFIRMADO algo que dependa exclusivamente de uma regra de negócio desconhecida.

---

## Correções

Durante esta auditoria:

**NÃO IMPLEMENTE CORREÇÕES.**

Se encontrar um problema:

**ANALISAR → VALIDAR → CLASSIFICAR → EXPLICAR → RECOMENDAR**

As correções devem ser apresentadas apenas conceitualmente.

A implementação de correções pertence a uma rodada posterior e separada.

---

## Critério para início do React

Ao final da auditoria, avalie se o backend está em condição adequada para iniciar a integração com React.

Considere:

- bugs críticos ou altos;
- falhas funcionais relevantes;
- problemas de contrato da API;
- problemas de persistência;
- problemas de validação;
- problemas de segurança;
- inconsistências de regras de negócio;
- problemas arquiteturais que possam bloquear a integração;
- riscos que possam tornar a integração prematura.

Classifique o resultado como:

**APTO** — não existem bloqueadores relevantes identificados.

**APTO COM RESSALVAS** — existem problemas ou riscos que não impedem imediatamente o início do React, mas devem ser acompanhados.

**NÃO APTO** — existem problemas relevantes que devem ser tratados antes da integração.

Explique brevemente os fatores que determinaram a classificação.

---

## Encerramento

Ao terminar todas as sete fases, apresente uma breve mensagem final contendo somente:

- quantidade de bugs por severidade;
- principais 5 riscos;
- status do backend para iniciar o React: APTO, APTO COM RESSALVAS ou NÃO APTO;
- indicação de que os relatórios completos estão em `docs/qa/`.

Os detalhes completos devem permanecer nos relatórios da auditoria.