# QA 05 — Tentar Quebrar o Sistema

**Data:** 2026-08-23  
**Fase:** 5 de 7  
**Base:** Estado Consolidado V4  
**Objetivo:** Agir como QA adversarial para encontrar falhas de comportamento.

---

## 1. Simulação do Fluxo Normal de Criação de Pedido

### 1.1 Fluxo ideal esperado

```
1. Usuário autentica → obtém token/sessão
2. Usuário consulta catálogo → GET /produtos
3. Usuário monta carrinho → seleciona produtos e quantidades
4. Usuário cria pedido → POST /pedidos com itens selecionados
5. Sistema valida entrada, calcula valorTotal, registra data/hora, persiste
6. Sistema retorna pedido criado com status CRIADO
7. Funcionário atualiza status → PATCH /pedidos/{id}/status → EM_PREPARO → PRONTO
8. Usuário consulta status → GET /pedidos/{id}
```

### 1.2 O que realmente acontece

```
1. ❌ Sem autenticação → Spring Security bloqueia (QA-020)
   (BLOQUEADOR: nada funciona a partir daqui)

ASSUMINDO que Spring Security estivesse desabilitado:

2. ❌ GET /produtos → 404 (controller não existe)
3. ❌ Impossível montar carrinho baseado no catálogo
4. POST /pedidos com JSON:
   {
     "status": "CRIADO",
     "dataHoraPedido": "2024-01-01",
     "horarioEstimadoRetirada": "12:00",
     "valorTotal": 15.00,
     "usuario": { "id": 1 },
     "itens": [
       { "nome": "Coxinha", "preco": 5.00 },
       { "nome": "Coca-Cola", "preco": 10.00 }
     ]
   }
5. ❌ Sem @Valid → validações ignoradas (QA-022)
   ❌ Usuário não validado no banco (QA-014)
   ❌ valorTotal não calculado (QA-015)
   ❌ itens sem backreference → pedido_id null (QA-013)
   → ConstraintViolationException → HTTP 500
6. ❌ Se por milagre salvasse → serialização circular (QA-001) → StackOverflowError
7. ❌ Sem endpoint de atualização de status
8. ❌ Se consultasse → serialização circular (QA-001)
```

**Resultado: O fluxo normal de criação de pedido NÃO funciona de ponta a ponta.**

---

## 2. Dados Nulos, Vazios, Negativos, Zero

### 2.1 Todos os campos nulos

```json
POST /pedidos
null
```
→ `HttpMessageNotReadableException` → HTTP 400 (Spring trata automaticamente) ✓

### 2.2 Objeto vazio

```json
POST /pedidos
{}
```
→ `verificarPedido()` → `IllegalArgumentException("Pedido precisa conter ao menos um item")` → HTTP 500 (sem handler)

### 2.3 Status null

```json
POST /pedidos
{ "status": null, "itens": [{"nome": "X", "preco": 1}], ... }
```
→ Sem `@Valid` → status null aceito → `@Column(nullable=false)` → `DataIntegrityViolationException` → HTTP 500

### 2.4 Preço negativo

```json
POST /pedidos
{ ..., "itens": [{"nome": "X", "preco": -10.00}] }
```
→ Sem `@Valid` → preço negativo aceito → salvo no banco (se a constraint do banco não impedir)

### 2.5 Preço zero

```json
POST /pedidos
{ ..., "itens": [{"nome": "X", "preco": 0.00}] }
```
→ `@DecimalMin("0.00")` aceitaria (valor = mínimo), mas sem `@Valid`, nem isso é verificado → aceito

### 2.6 Nome vazio no item

```json
POST /pedidos
{ ..., "itens": [{"nome": "", "preco": 5.00}] }
```
→ Sem `@Valid` → nome vazio aceito → `@Column(nullable=false)` com `@NotBlank` não verificado → depende da constraint do banco → pode ser salvo como string vazia ou falhar

### 2.7 ID negativo no path

```
GET /pedidos/-1
```
→ `findById(-1L)` → `Optional.empty()` → `RuntimeException("Pedido não encontrado")` → HTTP 500

### 2.8 ID zero no path

```
GET /pedidos/0
```
→ `findById(0L)` → `Optional.empty()` → `RuntimeException("Pedido não encontrado")` → HTTP 500

### 2.9 ID como string

```
GET /pedidos/abc
```
→ Spring tenta converter "abc" para `Long` → `MethodArgumentTypeMismatchException` → HTTP 400 (Spring trata automaticamente) ✓

---

## 3. Datas Passadas, Futuras, Inválidas

Como `dataHoraPedido` e `horarioEstimadoRetirada` são `String` (QA-002), todos os cenários abaixo são aceitos sem erro:

| Cenário | Valor | Resultado |
|---|---|---|
| Data passada | `"2020-01-01"` | ✓ Aceito |
| Data muito no futuro | `"2099-12-31"` | ✓ Aceito |
| Data inválida | `"2024-13-32"` | ✓ Aceito (é String!) |
| Texto qualquer | `"ontem"` | ✓ Aceito |
| Número | `"12345"` | ✓ Aceito |
| Emoji | `"🕐"` | ✓ Aceito |
| SQL injection | `"'; DROP TABLE pedido; --"` | ✓ Aceito (mas JPA parametriza, sem risco de SQL injection) |

**Nenhuma validação temporal existe.** Qualquer texto é aceito e persistido.

---

## 4. Requisições Duplicadas e Double-Click

### 4.1 POST duplicado

```json
POST /pedidos  (enviado 2x rapidamente)
{ "status": "CRIADO", ... }
```

**Resultado:** Dois pedidos idênticos criados com IDs diferentes. Sem proteção.

**Impacto:** O usuário pode ser cobrado duas vezes; o funcionário prepara dois pedidos iguais.

**Defesa possível:** Token de idempotência (header `Idempotency-Key`), ou verificação de duplicidade no service.

### 4.2 DELETE duplicado

```
DELETE /pedidos/1  (enviado 2x rapidamente)
```

**1ª requisição:** `existsById(1)` → true → `deleteById(1)` → sucesso → HTTP 204
**2ª requisição:** `existsById(1)` → false → `RuntimeException("Pedido não encontrado")` → HTTP 500

**Impacto:** Erro na segunda requisição. DELETE deveria ser idempotente (RFC 7231).

---

## 5. Concorrência

### 5.1 Dois usuários criando pedidos simultâneos

```
Thread A: POST /pedidos (usuário 1)
Thread B: POST /pedidos (usuário 2)
```

**Resultado:** Ambos os pedidos são criados sem conflito. ✓ (desde que os pedidos independam entre si)

### 5.2 Dois usuários editando o mesmo pedido

Não é possível testar — não existe endpoint de atualização de pedido.

**Mas conceitualmente:** Sem `@Version`, o último `save()` sobrescreve silenciosamente (Lost Update — QA-011).

### 5.3 Dois processos para o mesmo recurso

```
Thread A: DELETE /pedidos/1
Thread B: DELETE /pedidos/1
```

**Cenário race condition:**
1. Thread A: `existsById(1)` → true
2. Thread B: `existsById(1)` → true
3. Thread A: `deleteById(1)` → sucesso
4. Thread B: `deleteById(1)` → `EmptyResultDataAccessException` ou silêncio → HTTP 500 ou OK

**Sem lock ou sincronização.** O `existsById` + `deleteById` não é atômico.

---

## 6. Banco Vazio, Registro Inexistente, Falha de Persistência

### 6.1 GET /pedidos com banco vazio

```
GET /pedidos
```
→ `findAll()` → `[]` → HTTP 200 com `[]` ✓

### 6.2 GET /pedidos/{id} com registro inexistente

```
GET /pedidos/999
```
→ `RuntimeException("Pedido não encontrado")` → HTTP 500 (deveria ser 404)

### 6.3 POST /pedidos com banco indisponível

```
POST /pedidos (MySQL parado)
```
→ `DataAccessResourceFailureException` → HTTP 500 com stack trace do JDBC

**Sem tratamento:** O cliente recebe informações sobre a infraestrutura (hostname, porta, driver) no stack trace.

### 6.4 Falha de persistência por FK violation

```json
POST /pedidos
{ "usuario": {"id": 999}, ... }
```
→ `DataIntegrityViolationException` → HTTP 500 com stack trace incluindo nome de tabela e coluna do banco

---

## 7. Riscos Básicos de Segurança

### 7.1 Manipulação de IDs

```json
POST /pedidos
{ "id": 1, ... }  // Tentando forçar ID específico
```
→ Se `id=1` já existe: `EntityExistsException` ou merge (atualiza o registro 1!)
→ Se não existe: pode ser inserido com `id=1` ou ignorado (depende do JPA/Hibernate)

**Risco:** O cliente pode sobrescrever pedidos existentes enviando um ID específico.

### 7.2 Mass Assignment

```json
POST /item-pedido (se fosse REST)
{
  "nome": "Coxinha",
  "preco": 0.01,
  "pedido": { "id": 1, "status": "PRONTO", "valorTotal": 0 }
}
```

**Sem DTO:** O Jackson desserializa tudo, incluindo campos que o cliente não deveria controlar (status, valorTotal).

### 7.3 Acesso sem autenticação

Sem Spring Security configurado (se desabilitado), qualquer pessoa pode:
- Ver todos os pedidos de todos os usuários (`GET /pedidos`)
- Excluir pedidos de outros usuários (`DELETE /pedidos/{id}`)
- Criar pedidos em nome de qualquer usuário (`POST /pedidos` com qualquer `usuario.id`)

### 7.4 Exposição de dados sensíveis

`GET /pedidos/{id}` retorna:
```json
{
  "usuario": {
    "nome": "João",
    "cpf": "12345678901",
    "senha": "minhasenha123",     // ❌ EXPOSTO
    "email": "joao@email.com",
    "telefone": "11999999999"
  }
}
```

**Impacto:** Senha, CPF, telefone e email expostos na API.

### 7.5 SQL Injection

**Risco: BAIXO.** O JPA/Hibernate usa PreparedStatement com parâmetros, prevenindo SQL injection em todas as queries JPQL. ✓

As `@Query` usam `:param` (named parameters), que são parametrizados automaticamente. ✓

### 7.6 Senha do banco exposta

**Arquivo:** `application-dev.properties`
```
spring.datasource.password=131417
```

Se o repositório for público (ex: GitHub), a senha do banco fica exposta.

---

## 8. Integração React → API (Teste Mental)

### 8.1 Cenário: React tenta listar pedidos

```javascript
// React Component
useEffect(() => {
  fetch('http://localhost:8080/pedidos')
    .then(res => res.json())
    .then(data => setPedidos(data))
    .catch(err => console.error(err));
}, []);
```

**Resultado esperado pelo React:**
```json
[
  { "id": 1, "status": "CRIADO", "valorTotal": 15.00, "itens": [...] }
]
```

**Resultado real (sequência de falhas):**

1. **Spring Security** → HTTP 302 (redirect para /login) ou HTTP 401
2. **Se Security desabilitado + CORS ausente** → Erro de CORS no browser
3. **Se CORS configurado + Security desabilitado** → `fetch()` retorna JSON
4. **Se pedido tem itens** → Serialização circular → HTTP 500 (StackOverflowError)
5. **Se pedido sem itens** → JSON retorna com `usuario.senha` exposta

**Camadas de falha: 4 problemas bloqueadores antes de funcionar minimamente.**

### 8.2 Cenário: React tenta criar pedido

```javascript
fetch('http://localhost:8080/pedidos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    status: 'CRIADO',
    dataHoraPedido: new Date().toISOString(),
    horarioEstimadoRetirada: '12:00',
    valorTotal: 0,
    usuario: { id: 1 },
    itens: [{ nome: 'Coxinha', preco: 5.00 }]
  })
})
```

**Falhas:**
1. Spring Security bloqueia
2. CORS bloqueia (se frontend em porta diferente)
3. Itens sem backreference → `ConstraintViolationException`
4. Sem `@Valid` → validações ignoradas
5. `valorTotal: 0` aceito sem recalcular
6. Se salvasse → serialização circular no retorno

---

## 9. Ranking dos 10 Maiores Riscos

| # | ID | Severidade | Risco | Impacto |
|---|---|---|---|---|
| 1 | QA-020 | CRÍTICO | Spring Security sem configuração | **API completamente inacessível** |
| 2 | QA-001 | CRÍTICO | Serialização circular | **Qualquer GET com itens causa StackOverflow** |
| 3 | QA-013 | CRÍTICO | Backreference não setada | **Impossível criar pedido com itens** |
| 4 | QA-005 | ALTO | Senha exposta na API | **Dados sensíveis vazam** |
| 5 | QA-021 | ALTO | Sem CORS | **React não consegue acessar a API** |
| 6 | QA-023 | ALTO | Sem tratamento de exceções | **HTTP 500 com stack trace para qualquer erro** |
| 7 | QA-022 | ALTO | Sem @Valid | **Dados inválidos aceitos** |
| 8 | QA-014 | ALTO | Usuário não validado | **Pedido com usuário inexistente** |
| 9 | QA-002 | CRÍTICO | Datas como String | **Impossível trabalhar com tempo** |
| 10 | QA-004 | ALTO | Senha em texto plano | **Violação de segurança fundamental** |

---

## 10. Achado Formal Adicional

---

### QA-026

**Severidade:** ALTO  
**Tipo:** RISCO POTENCIAL  
**Local:** [PedidoController.java:24-27](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/PedidoController.java#L24-L27)

**Problema:** O endpoint `POST /pedidos` recebe `@RequestBody Pedido pedido`, que inclui o campo `id`. Se o cliente enviar um `id` já existente, o `save()` pode atualizar (merge) o registro existente em vez de criar um novo. Sem DTO, não há controle sobre quais campos o cliente pode enviar.

**Como reproduzir:**
```json
POST /pedidos
{ "id": 1, "status": "CRIADO", "valorTotal": 0, ... }
```
Se o pedido com `id=1` já existe, o `save()` do JPA faz `merge` (atualiza).

**Comportamento atual/provável:** Sobrescrita silenciosa de pedido existente.

**Comportamento esperado:** POST deveria sempre criar novo recurso. Para atualizar, usar PUT.

**Risco/impacto:** Dados sobrescritos acidentalmente ou maliciosamente.

**Conceito envolvido:** Diferença entre `persist` e `merge` no JPA, semântica de POST vs PUT.

**Sugestão de correção conceitual:** Usar DTO sem campo `id` para criação. Ou ignorar o `id` do JSON e deixar o banco gerar.

**Por que a solução resolve:** Sem o campo `id` na entrada, o JPA sempre faz `persist` (insert), nunca `merge` (update).

**Como explicar ao professor:** "No JPA, `save()` verifica se a entidade tem ID. Se tiver e já existir no banco, ele atualiza (merge). Se não tiver, insere (persist). Se o cliente enviar um ID no POST, ele pode acidentalmente atualizar um registro existente."

---

## ESTADO CONSOLIDADO — V5

### Bugs confirmados (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-001 | CRÍTICO | Serialização circular | 2 |
| QA-002 | CRÍTICO | Datas como String | 2 |
| QA-013 | CRÍTICO | Backreference não setada nos itens | 3 |
| QA-020 | CRÍTICO | Spring Security sem configuração | 4 |
| QA-003 | ALTO | valorTotal como double | 2 |
| QA-004 | ALTO | Senha em texto plano | 2 |
| QA-005 | ALTO | Entidade exposta (inclui senha) | 2 |
| QA-006 | ALTO | Produto sem preço | 2 |
| QA-014 | ALTO | Usuário não validado na criação | 3 |
| QA-015 | ALTO | valorTotal não calculado | 3 |
| QA-021 | ALTO | Sem CORS | 4 |
| QA-022 | ALTO | @Valid ausente no REST | 4 |
| QA-023 | ALTO | Sem @ControllerAdvice | 4 |
| QA-010 | MÉDIO | findByPreco tipo errado | 2 |
| QA-016 | MÉDIO | @Transactional inconsistente | 3 |
| QA-017 | MÉDIO | excluirItemPedido sem verificação | 3 |

### Riscos potenciais (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-025 | ALTO | Flyway sem migrações | 4 |
| QA-026 | ALTO | POST pode sobrescrever registro (id no body) | 5 |
| QA-007 | MÉDIO | ItemPedido sem FK para Produto | 2 |
| QA-008 | MÉDIO | findByAtivo retorna singular | 2 |
| QA-009 | MÉDIO | findBySenha anti-pattern | 2 |
| QA-011 | BAIXO | Sem @Version | 2 |
| QA-012 | BAIXO | tempoPreparoMinutos String | 2 |

### Melhorias (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-018 | MÉDIO | Exceções genéricas | 3 |
| QA-024 | MÉDIO | Dois paradigmas de controller | 4 |
| QA-019 | BAIXO | 21 métodos não utilizados | 3 |

### Itens ainda não auditados
- [ ] Engenharia de Software e aprendizado (Fase 6)
- [ ] Consolidação final e preparação para banca (Fase 7)
