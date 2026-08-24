---
trigger: always_on
---

# QA Read-Only — Regra de Auditoria

Esta regra vale para a auditoria inicial do projeto.

## Proibições

- Não altere código-fonte.
- Não aplique patches.
- Não refatore.
- Não reescreva classes.
- Não instale dependências.
- Não altere `pom.xml` ou configurações funcionais.
- Não faça commits.
- Não altere banco de dados.
- Não execute migrações.
- Não crie ou remova dados permanentes.
- Não execute comandos destrutivos.
- Não inicie processos que possam alterar o estado do sistema.

## Autonomia da análise

- Não peça autorização para continuar a análise.
- Registre dúvidas e hipóteses e continue.
- Nunca apresente uma hipótese como fato.
- Se o sistema propuser uma correção, apresente-a apenas como recomendação textual.
- Não implemente correções durante a auditoria inicial.

## Testes e verificações

- Podem ser executados testes e comandos de diagnóstico somente quando forem necessários para validar a auditoria.
- Prefira operações seguras e não destrutivas.
- Não execute uma operação se houver risco relevante de modificar o código, as configurações funcionais ou o banco de dados.
- Quando uma verificação não puder ser realizada com segurança, registre essa limitação no relatório.

## Relatórios

A única exceção às regras de somente leitura é a geração ou atualização de relatórios dentro de:

`docs/qa/`

Essa exceção só se aplica quando a fase/workflow solicitar explicitamente a geração ou atualização de um relatório.

Não crie ou altere arquivos fora de `docs/qa/` para registrar resultados da auditoria.

## Objetivo

A auditoria inicial deve seguir o princípio:

**ANALISAR → VALIDAR → CLASSIFICAR → EXPLICAR → RECOMENDAR**

Não:

**ANALISAR → CORRIGIR → REFATORAR**