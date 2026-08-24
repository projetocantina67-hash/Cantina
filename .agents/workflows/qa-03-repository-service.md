---
description: 
---

# QA 03 — Repository e Service

Descrição: Auditar persistência, regras de negócio e separação de responsabilidades.

## Passos
1. Use os estados consolidados anteriores.
2. Audite Repositories/DAOs: queries, métodos derivados, Optional, null, resultados inesperados, N+1 e consultas desnecessárias.
3. Audite Services: regras de negócio, validações, transações, ordem de execução, lógica duplicada e responsabilidades.
4. Simule falhas de persistência e operações parcialmente concluídas. Analise transações, atomicidade, rollback e operações parcialmente concluídas.
5. Explique Repository Pattern, Service Layer, Dependency Injection, Inversion of Control, coesão, acoplamento e SOLID usando o código real.
6. Identifique bugs confirmados, riscos, regras indefinidas e melhorias.
7. Não altere código.
8. Gere `docs/qa/03-repository-service.md`.
9. Finalize com `ESTADO CONSOLIDADO — V3`.
