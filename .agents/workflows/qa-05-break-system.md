---
description: 
---

# QA 05 — Tentar Quebrar o Sistema

Descrição: Agir como QA adversarial para encontrar falhas de comportamento.

## Passos
1. Utilize todo o conhecimento acumulado.
2. Simule o fluxo normal de criação de pedido. 
3. Tente dados nulos, vazios, negativos, zero, IDs inexistentes, produtos inexistentes e payloads incompletos.
4. Teste datas passadas, futuras, horários inválidos, conflitos e timezone.
5. Teste requisições duplicadas e double-click.
6. Teste concorrência: dois usuários para a mesma operação/recurso.
7. Teste banco vazio, registro inexistente, falha de persistência e banco indisponível.
8. Teste riscos básicos de segurança e manipulação de IDs.
9. Teste mentalmente a futura integração React → API.
10. Não classifique como BUG CONFIRMADO um comportamento cuja correção dependa de uma regra de negócio não definida.
11. Diferencie falha efetivamente demonstrada de cenário hipotético.
12. Quando for seguro e não destrutivo, execute testes existentes ou verificações somente leitura; não altere o projeto.
13. Gere um ranking dos 10 maiores riscos.
14. Gere `docs/qa/05-break-system.md`.
15. Finalize com `ESTADO CONSOLIDADO — V5`.
