# QA 04 — Controllers e API REST

**Data:** 2026-08-23  
**Fase:** 4 de 7  
**Base:** Estado Consolidado V3  
**Objetivo:** Auditar a API que futuramente será consumida pelo React.

---

## 1. Mapeamento Completo dos Endpoints

### 1.1 PedidoController (REST API)

**Arquivo:** [PedidoController.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/PedidoController.java)

**Tipo:** `@RestController` — retorna JSON diretamente.

| Método HTTP | URL | Método Java | Request Body | Response Body | Status HTTP | @Valid |
|---|---|---|---|---|---|---|
| POST | `/pedidos` | `criarPedido` | `Pedido` (entidade) | `Pedido` (entidade) | 201 Created | ❌ |
| GET | `/pedidos/{id}` | `buscarPorId` | — | `Pedido` (entidade) | 200 OK | — |
| GET | `/pedidos` | `listarTodos` | — | `List<Pedido>` (entidades) | 200 OK | — |
| DELETE | `/pedidos/{id}` | `excluirPedido` | — | void | 204 No Content | — |

**Ausências críticas:**
- ❌ PUT/PATCH para atualizar pedido
- ❌ PATCH para atualizar status do pedido
- ❌ Busca por status, usuário, data
- ❌ Paginação
- ❌ `@Valid` no `@RequestBody`

### 1.2 ItemPedidoController (MVC / Thymeleaf)

**Arquivo:** [ItemPedidoController.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/ItemPedidoController.java)

**Tipo:** `@Controller` — retorna nomes de views Thymeleaf.

| Método HTTP | URL | Método Java | Retorno |
|---|---|---|---|
| GET | `/item-pedido/listar` | `listarItemPedido` | View `item-pedido/listar` |
| GET | `/item-pedido/criar` | `mostrarFormularioCriarItemPedido` | View `item-pedido/criar` |
| POST | `/item-pedido/criar` | `criarItemPedido` | Redirect `/item-pedido/listar` |
| GET | `/item-pedido/editar/{id}` | `mostrarFormularioEditarItemPedido` | View `item-pedido/editar` |
| PUT | `/item-pedido/{id}` | `atualizarItemPedido` | Redirect `/item-pedido/listar` |
| POST | `/item-pedido/deletar/{id}` | `deletarItemPedido` | Redirect `/item-pedido/listar` |

**Problema arquitetural:** Este controller usa `@Controller` (MVC) e retorna views Thymeleaf. **Não é uma API REST**. Para a integração com React, seria necessário um `@RestController` equivalente que retorne JSON.

### 1.3 Controllers Ausentes

| Entidade | Controller | Status |
|---|---|---|
| Pedido | PedidoController | ✓ REST (parcial) |
| ItemPedido | ItemPedidoController | ✗ MVC (não REST) |
| Produto | — | ❌ Ausente |
| Usuario | — | ❌ Ausente |

---

## 2. Análise de DTOs

**Status:** DTOs completamente ausentes.

Todas as entradas e saídas usam diretamente as entidades JPA:

```java
// PedidoController — recebe e retorna Pedido (entidade)
public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) { ... }
public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) { ... }
public ResponseEntity<List<Pedido>> listarTodos() { ... }
```

**Consequências da ausência de DTOs:**

1. **Exposição de dados sensíveis** — `Usuario.senha` aparece na resposta JSON (QA-005)
2. **Acoplamento** — qualquer mudança na entidade JPA muda automaticamente o contrato da API
3. **Serialização circular** — `Pedido ↔ ItemPedido` (QA-001)
4. **Entrada insegura** — o cliente pode enviar qualquer campo da entidade, incluindo `id`, `ativo`, etc.
5. **Impossibilidade de versionamento** — sem DTO, não é possível manter duas versões da API

---

## 3. Análise de Validações

### 3.1 PedidoController — SEM validação

```java
@PostMapping
public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
    // Sem @Valid → anotações de validação na entidade Pedido são ignoradas
    Pedido novoPedido = pedidoService.criarPedido(pedido);
    return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
}
```

**Anotações de validação declaradas em `Pedido` mas NUNCA acionadas:**
- `@NotNull` em `status`
- `@NotBlank` em `dataHoraPedido`, `horarioEstimadoRetirada`
- `@Min(0)` em `valorTotal`
- `@NotNull` em `usuario`

**Resultado:** É possível criar um pedido com `status: null`, `dataHoraPedido: null`, `valorTotal: -100`, `usuario: null`. O service só valida se a lista de itens não é vazia.

### 3.2 ItemPedidoController — COM validação

```java
@PostMapping("/criar")
public String criarItemPedido(@Valid @ModelAttribute ItemPedido itemPedido, 
                               BindingResult resultado, ...) {
    if (resultado.hasErrors()) {
        model.addAttribute("itemPedido", itemPedido);
        return "item-pedido/criar";
    }
    // ...
}
```

**`@Valid` é usado** ✓ — mas apenas no controller MVC (Thymeleaf), não no REST.

**Anotações acionadas:**
- `@NotBlank` em `nome`
- `@Size(3, 100)` em `nome`
- `@NotNull` em `preco`
- `@DecimalMin("0.00")` em `preco`

**Mas:** O `BindingResult` retorna à view Thymeleaf com os erros. Em uma API REST, os erros deveriam ser retornados como JSON.

---

## 4. Análise de Tratamento de Exceções

**Status:** Sem tratamento global.

Não existe `@ControllerAdvice`, `@RestControllerAdvice` ou `@ExceptionHandler` em nenhum lugar do projeto.

**Exceções que podem ocorrer e seu comportamento:**

| Exceção | Origem | HTTP Status | Corpo da Resposta |
|---|---|---|---|
| `IllegalArgumentException` | `PedidoService.verificarPedido` | 500 | Stack trace |
| `RuntimeException` | `PedidoService.buscarPorId` | 500 | Stack trace |
| `RuntimeException` | `PedidoService.excluirPedido` | 500 | Stack trace |
| `IllegalStateException` | `ItemPedidoService` (vários) | 500 | Stack trace |
| `DataIntegrityViolationException` | JPA/Hibernate (unique, FK) | 500 | Stack trace |
| `ConstraintViolationException` | Bean Validation (quando @Valid presente) | 400 | Genérico Spring |
| `HttpMessageNotReadableException` | Jackson (JSON inválido) | 400 | Genérico Spring |
| `MethodArgumentNotValidException` | @Valid falha | 400 | Genérico Spring |
| `StackOverflowError` | Serialização circular | 500 | Sem corpo |

**Para o React:** Sem tratamento de exceções, o frontend recebe:
- HTTP 500 com stack trace Java para qualquer erro de negócio
- Sem JSON padronizado de erro
- Sem mensagens amigáveis
- Sem campos de erro para validação de formulário

---

## 5. Análise Semântica HTTP

### 5.1 PedidoController

| Endpoint | Semântica HTTP | Status |
|---|---|---|
| `POST /pedidos` | Criação → 201 Created | ✓ Correto |
| `GET /pedidos/{id}` | Leitura → 200 OK | ✓ Correto |
| `GET /pedidos` | Listagem → 200 OK | ✓ Correto |
| `DELETE /pedidos/{id}` | Exclusão → 204 No Content | ✓ Correto |

**Ausências:**
- `PUT /pedidos/{id}` — atualização completa
- `PATCH /pedidos/{id}` — atualização parcial (ex: status)

### 5.2 ItemPedidoController

| Endpoint | Semântica | Problema |
|---|---|---|
| `GET /item-pedido/listar` | Listagem | ❌ URL deveria ser `GET /itens-pedido` |
| `GET /item-pedido/criar` | Formulário | N/A (MVC) |
| `POST /item-pedido/criar` | Criação | ❌ URL deveria ser `POST /itens-pedido` |
| `GET /item-pedido/editar/{id}` | Formulário | N/A (MVC) |
| `PUT /item-pedido/{id}` | Atualização | ⚠️ PUT via formulário HTML requer `HiddenHttpMethodFilter` |
| `POST /item-pedido/deletar/{id}` | Exclusão | ❌ Deveria ser `DELETE /itens-pedido/{id}` |

**Problemas de convenção REST:**
- URLs com verbos (`/listar`, `/criar`, `/editar`, `/deletar`) — REST usa substantivos
- POST para deletar — REST usa DELETE
- URL no singular (`/item-pedido`) — REST geralmente usa plural (`/itens-pedido`)

---

## 6. Teste Mental de Payloads

### 6.1 Payload vazio

```json
POST /pedidos
{}
```

**Fluxo:**
1. Jackson desserializa → `Pedido` com todos os campos nulos/default
2. Sem `@Valid` → validações ignoradas
3. `PedidoService.criarPedido()` → `pedido.getItens()` retorna `new ArrayList<>()` (inicializado na entidade)
4. `verificarPedido()` → lista vazia → `IllegalArgumentException`
5. Sem handler → **HTTP 500 com stack trace**

### 6.2 Payload com campos incompletos

```json
POST /pedidos
{
  "status": "CRIADO",
  "itens": [{ "nome": "Coxinha", "preco": 5.00 }]
}
```

**Fluxo:**
1. `dataHoraPedido` = null, `horarioEstimadoRetirada` = null, `valorTotal` = 0.0, `usuario` = null
2. Sem `@Valid` → todas as validações ignoradas
3. `verificarPedido()` → 1 item ✓
4. `pedidoRepository.save()` → `@Column(nullable=false)` em `dataHoraPedido` → **`ConstraintViolationException` do banco** → HTTP 500

### 6.3 ID inexistente no GET

```
GET /pedidos/999
```

**Fluxo:**
1. `PedidoService.buscarPorId(999L)` → `findById(999L)` → `Optional.empty()`
2. `orElseThrow(() -> new RuntimeException("Pedido não encontrado"))`
3. **HTTP 500** em vez de **HTTP 404**

### 6.4 ID inexistente no DELETE

```
DELETE /pedidos/999
```

**Fluxo:**
1. `existsById(999L)` → false
2. `throw new RuntimeException("Pedido não encontrado")`
3. **HTTP 500** em vez de **HTTP 404**

### 6.5 Valor inválido de enum

```json
POST /pedidos
{ "status": "INVALIDO" }
```

**Fluxo:**
1. Jackson tenta desserializar "INVALIDO" como `StatusPedido`
2. `InvalidFormatException` → `HttpMessageNotReadableException`
3. Spring retorna **HTTP 400** com mensagem genérica ✓ (tratamento padrão do Spring)

### 6.6 Payload duplicado (double-click)

```json
POST /pedidos
{ "status": "CRIADO", "itens": [...], "usuario": {"id": 1}, ... }
// Enviado duas vezes rapidamente
```

**Resultado:** Dois pedidos idênticos criados. Sem proteção contra idempotência ou deduplicação.

---

## 7. Análise CORS

**Status:** Sem configuração CORS.

Não existe:
- `@CrossOrigin` em nenhum controller
- `WebMvcConfigurer` com `addCorsMappings()`
- `CorsFilter` ou `CorsConfigurationSource`

**Consequência para React:** O frontend React, que roda em `localhost:3000` (ou porta similar), não conseguirá fazer requisições para a API em `localhost:8080`. O browser bloqueará as requisições com erro de CORS.

**Agravante:** Com Spring Security no classpath (sem configuração), todas as requisições já são bloqueadas por autenticação antes mesmo de chegar à verificação CORS.

---

## 8. Análise de Datas, Horários e JSON

### 8.1 Formato de datas

Como `dataHoraPedido` e `horarioEstimadoRetirada` são `String`, não há formato definido. O cliente pode enviar qualquer texto:

```json
{ "dataHoraPedido": "2024-01-01T12:00:00" }  // OK
{ "dataHoraPedido": "ontem" }                   // Aceito sem erro
{ "dataHoraPedido": "" }                         // Rejeitado por @NotBlank (se @Valid)
```

**Para React:** O frontend teria que decidir sozinho o formato e torcer para que o backend aceite. Sem contrato definido.

### 8.2 Timezone

Nenhuma configuração de timezone encontrada. Se fosse `LocalDateTime`, o Spring Boot usa UTC por padrão (configurável com `spring.jackson.time-zone`). Como é `String`, timezone é irrelevante — e isso é um problema.

### 8.3 Serialização JSON de enums

Enums são serializados como `STRING` (`@Enumerated(EnumType.STRING)`):

```json
{ "status": "CRIADO" }      // ✓ Funciona
{ "status": "EM_PREPARO" }  // ✓ Funciona
```

Isso é correto e compatível com React. ✓

---

## 9. Consistência dos Contratos para React

### 9.1 Resumo da Avaliação do Contrato

| Aspecto | Status | Problema |
|---|---|---|
| Endpoints REST para Pedido | Parcial | Falta PUT/PATCH, busca filtrada, paginação |
| Endpoints REST para ItemPedido | ❌ | Controller é MVC, não REST |
| Endpoints REST para Produto | ❌ | Não existe |
| Endpoints REST para Usuário | ❌ | Não existe |
| DTOs | ❌ | Entidades expostas |
| Validação de entrada | ❌ | Sem @Valid na API REST |
| Tratamento de erros | ❌ | Sem @ControllerAdvice |
| CORS | ❌ | Sem configuração |
| Segurança | ❌ | Spring Security bloqueia tudo |
| Formato de datas | ❌ | Strings, sem contrato |
| Paginação | ❌ | Não implementada |
| Autenticação | ❌ | Sem endpoint de login |

### 9.2 O que o React precisaria para funcionar

1. **CORS configurado** para `localhost:3000`
2. **Spring Security** configurado (ou desabilitado) para permitir requisições
3. **Endpoints REST** para todas as entidades (Pedido, ItemPedido, Produto, Usuário)
4. **DTOs** para controlar a estrutura das respostas
5. **Tratamento de erros** padronizado em JSON
6. **Formato de datas** definido e consistente
7. **Paginação** para listagens
8. **Endpoint de login** (POST /auth/login → retorna token/sessão)

---

## 10. Achados Formais

---

### QA-020

**Severidade:** CRÍTICO  
**Tipo:** BUG CONFIRMADO  
**Local:** Projeto inteiro — ausência de `SecurityFilterChain`

**Problema:** Spring Security está no classpath (`spring-boot-starter-security` no `pom.xml`) sem nenhuma configuração. O comportamento padrão do Spring Security é: **todas as rotas exigem autenticação**, gera uma senha aleatória no console, e redireciona para `/login`.

**Como reproduzir:** Iniciar a aplicação → acessar qualquer endpoint → redirecionado para `/login` ou recebe HTTP 401/403.

**Comportamento atual/provável:** Nenhuma requisição funciona sem autenticação. A aplicação é inacessível.

**Comportamento esperado:** Configurar `SecurityFilterChain` para definir quais rotas são públicas e quais exigem autenticação.

**Risco/impacto:** API completamente inacessível. Bloqueador total para desenvolvimento e integração.

**Conceito envolvido:** Spring Security, auto-configuração, SecurityFilterChain, filtros de segurança.

**Sugestão de correção conceitual:** Criar uma classe `@Configuration` com `SecurityFilterChain` que permita acesso aos endpoints públicos e configure autenticação para os protegidos.

**Por que a solução resolve:** Define explicitamente as regras de acesso em vez de depender do comportamento padrão restritivo.

**Como explicar ao professor:** "O Spring Security, quando adicionado ao projeto, bloqueia tudo por segurança. É como instalar uma fechadura na porta e perder a chave. Precisamos configurar quais portas ficam abertas (endpoints públicos) e quais precisam de chave (endpoints autenticados)."

---

### QA-021

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** Projeto inteiro — ausência de configuração CORS

**Problema:** Sem configuração CORS, o browser bloqueia requisições de origens diferentes (ex: React em `localhost:3000` → API em `localhost:8080`).

**Como reproduzir:** Iniciar React em `localhost:3000` e tentar `fetch('http://localhost:8080/pedidos')`.

**Comportamento atual/provável:** Erro de CORS no browser: "Access to fetch at 'http://localhost:8080/pedidos' from origin 'http://localhost:3000' has been blocked by CORS policy".

**Comportamento esperado:** Configuração CORS permitindo a origem do React.

**Risco/impacto:** Integração com React impossível.

**Conceito envolvido:** Same-Origin Policy, CORS (Cross-Origin Resource Sharing), preflight requests.

**Sugestão de correção conceitual:** Implementar `WebMvcConfigurer.addCorsMappings()` ou adicionar `@CrossOrigin` nos controllers.

**Por que a solução resolve:** CORS diz ao browser quais origens externas podem acessar a API.

**Como explicar ao professor:** "O browser impede que um site (React) acesse recursos de outro servidor (API) por segurança. CORS é a forma de dizer ao browser: 'essa origem é confiável, pode acessar'. Sem isso, o React não consegue fazer nenhuma requisição."

---

### QA-022

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [PedidoController.java:24](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/PedidoController.java#L24)

**Problema:** `@RequestBody Pedido pedido` sem `@Valid`. As anotações de validação (`@NotNull`, `@NotBlank`, `@Min`) declaradas na entidade `Pedido` nunca são acionadas.

**Como reproduzir:** `POST /pedidos` com corpo `{"itens": [{"nome": "x", "preco": 1}]}` — sem status, sem data, sem usuário.

**Comportamento atual/provável:** O pedido passa pela validação do service (apenas verifica itens), depois falha no banco (`@Column(nullable=false)`) com exceção técnica.

**Comportamento esperado:** Validação deveria ocorrer na camada de aplicação (antes de chegar ao banco) com mensagens claras.

**Risco/impacto:** Dados inválidos chegam até o banco; erros técnicos em vez de mensagens amigáveis.

**Conceito envolvido:** Bean Validation (JSR 380), `@Valid`, `BindingResult`, `MethodArgumentNotValidException`.

**Sugestão de correção conceitual:** Adicionar `@Valid` antes de `@RequestBody Pedido pedido`.

**Por que a solução resolve:** `@Valid` aciona o Bean Validation antes de entrar no método do controller. Se houver erros, o Spring lança `MethodArgumentNotValidException` (HTTP 400) antes de qualquer lógica de negócio.

**Como explicar ao professor:** "`@Valid` é o 'porteiro' que verifica os dados na entrada. Sem ele, as anotações de validação da entidade são apenas decoração — existem, mas não são verificadas."

---

### QA-023

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** Projeto inteiro — ausência de `@ControllerAdvice`

**Problema:** Sem tratamento global de exceções. Qualquer exceção não capturada resulta em HTTP 500 com stack trace Java completa.

**Como reproduzir:** `GET /pedidos/999` → HTTP 500 com `java.lang.RuntimeException: Pedido não encontrado` e centenas de linhas de stack trace.

**Comportamento atual/provável:** Stack trace Java exposta ao cliente.

**Comportamento esperado:** JSON padronizado: `{ "status": 404, "error": "Pedido não encontrado" }`.

**Risco/impacto:** Informações internas expostas; impossível para o React tratar erros programaticamente; experiência ruim do usuário.

**Conceito envolvido:** `@RestControllerAdvice`, `@ExceptionHandler`, ResponseEntity, tratamento global de exceções.

**Sugestão de correção conceitual:** Criar `@RestControllerAdvice` com handlers para `RuntimeException`, `IllegalArgumentException`, `DataIntegrityViolationException`, etc.

**Por que a solução resolve:** Centraliza o tratamento de erros em um único lugar, retornando JSON padronizado com status HTTP adequados.

**Como explicar ao professor:** "`@RestControllerAdvice` é como um 'filtro de saída' que intercepta exceções antes de chegarem ao cliente. Em vez de mostrar o erro técnico do Java, transformamos em uma mensagem JSON amigável com o código HTTP correto."

---

### QA-024

**Severidade:** MÉDIO  
**Tipo:** MELHORIA  
**Local:** [PedidoController.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/PedidoController.java) e [ItemPedidoController.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/ItemPedidoController.java)

**Problema:** Dois paradigmas de controller: `PedidoController` é `@RestController` (retorna JSON), `ItemPedidoController` é `@Controller` (retorna views Thymeleaf). Para a integração com React, ambos deveriam ser `@RestController`.

**Como reproduzir:** Análise estática.

**Comportamento atual/provável:** O React só consegue consumir a API de Pedido (JSON). Para ItemPedido, receberia HTML de views Thymeleaf.

**Comportamento esperado:** Ambos os controllers deveriam ser `@RestController` retornando JSON para o React.

**Risco/impacto:** Metade da API não está disponível como REST para o React.

**Conceito envolvido:** `@RestController` vs `@Controller`, SPA (Single Page Application), API-first design.

**Sugestão de correção conceitual:** Criar um `ItemPedidoRestController` (`@RestController`) separado, ou converter o existente.

**Por que a solução resolve:** React consome JSON, não views Thymeleaf.

**Como explicar ao professor:** "O React é um frontend que consome dados em JSON. O `@Controller` retorna HTML (views do Thymeleaf). Precisamos de `@RestController` que retorna JSON. São dois paradigmas diferentes: server-side rendering (Thymeleaf) vs client-side rendering (React)."

---

### QA-025

**Severidade:** ALTO  
**Tipo:** RISCO POTENCIAL  
**Local:** [pom.xml:31-40](file:///Users/user/Desktop/Cantina-Thiago/Cantina/pom.xml#L31-L40)

**Problema:** Flyway (`spring-boot-starter-flyway` + `flyway-mysql`) está no classpath sem nenhum arquivo de migração em `db/migration`. O comportamento depende da versão e configuração: pode falhar na inicialização ou funcionar silenciosamente.

**Como reproduzir:** Iniciar a aplicação sem o diretório `src/main/resources/db/migration`.

**Comportamento atual/provável:** O `ddl-auto=update` gerencia o schema. O Flyway pode criar a tabela `flyway_schema_history` vazia e não fazer nada, ou pode falhar se não encontrar o diretório.

**Comportamento esperado:** Usar Flyway com migrações versionadas OU remover a dependência.

**Risco/impacto:** Possível falha na inicialização; configuração contraditória (Flyway + ddl-auto=update).

**Conceito envolvido:** Database migration, Flyway, schema versioning, ddl-auto.

**Sugestão de correção conceitual:** Decidir entre Flyway (com migrações versionadas) ou ddl-auto=update (apenas para desenvolvimento). Em produção, usar apenas Flyway.

**Por que a solução resolve:** Elimina a contradição e define uma estratégia clara de gerenciamento de schema.

**Como explicar ao professor:** "Flyway e `ddl-auto=update` fazem coisas parecidas de formas diferentes. O Flyway controla o schema com scripts SQL versionados (como Git para o banco). O `ddl-auto=update` deixa o Hibernate alterar o schema automaticamente. Usar ambos pode causar conflitos."

---

## 11. Explicações Conceituais

### 11.1 REST (Representational State Transfer)

**O que é:** Estilo arquitetural para APIs web, baseado em recursos (substantivos) e operações HTTP (verbos).

**Princípios REST:**
1. **Recursos** — identificados por URLs (ex: `/pedidos`, `/pedidos/1`)
2. **Verbos HTTP** — GET (ler), POST (criar), PUT (atualizar tudo), PATCH (atualizar parcial), DELETE (excluir)
3. **Stateless** — cada requisição contém toda a informação necessária
4. **Status HTTP** — 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error)

**No projeto:** `PedidoController` segue parcialmente os princípios REST (substantivos em URLs, verbos corretos, status HTTP adequados). `ItemPedidoController` NÃO segue REST (verbos na URL, retorna HTML).

### 11.2 DTO (Data Transfer Object)

**O que é:** Objeto simples usado para transferir dados entre camadas, especialmente entre a API e o cliente.

**Para que serve:** Desacoplar a representação interna (entidade JPA) da representação externa (API).

**Por que é importante:** Sem DTO, a entidade JPA é exposta diretamente, o que:
1. Expõe campos sensíveis (senha)
2. Causa serialização circular
3. Acopla a API à estrutura do banco
4. Impede versionamento da API

**No projeto:** DTOs são completamente ausentes. Todas as entidades são expostas diretamente.

### 11.3 Controller no MVC

**O que é:** Camada que recebe requisições HTTP, delega para o Service e retorna a resposta.

**Responsabilidade do Controller (SRP):**
- Receber e validar a entrada (HTTP → objetos)
- Delegar para o Service (lógica de negócio)
- Formatar e retornar a resposta (objetos → HTTP/JSON)

**O Controller NÃO deve conter:**
- Lógica de negócio
- Acesso direto ao Repository
- Manipulação de dados de persistência

**No projeto:** Os controllers delegam corretamente para os services. ✓

---

## ESTADO CONSOLIDADO — V4

### Bugs confirmados (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-001 | CRÍTICO | Serialização circular Pedido ↔ ItemPedido | 2 |
| QA-002 | CRÍTICO | Datas como String | 2 |
| QA-013 | CRÍTICO | criarPedido não seta backreference nos itens | 3 |
| QA-020 | CRÍTICO | Spring Security bloqueia tudo (sem configuração) | 4 |
| QA-003 | ALTO | valorTotal como double | 2 |
| QA-004 | ALTO | Senha em texto plano | 2 |
| QA-005 | ALTO | Entidade exposta na API (inclui senha) | 2 |
| QA-006 | ALTO | Produto sem preço | 2 |
| QA-014 | ALTO | criarPedido não valida usuário | 3 |
| QA-015 | ALTO | valorTotal não calculado pelo servidor | 3 |
| QA-021 | ALTO | Sem configuração CORS | 4 |
| QA-022 | ALTO | @Valid ausente no PedidoController | 4 |
| QA-023 | ALTO | Sem @ControllerAdvice (tratamento de exceções) | 4 |
| QA-010 | MÉDIO | findByPreco Double vs BigDecimal | 2 |
| QA-016 | MÉDIO | @Transactional inconsistente | 3 |
| QA-017 | MÉDIO | excluirItemPedido sem verificar existência | 3 |

### Riscos potenciais (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-007 | MÉDIO | ItemPedido sem FK para Produto | 2 |
| QA-008 | MÉDIO | findByAtivo retorna singular | 2 |
| QA-009 | MÉDIO | findBySenha anti-pattern | 2 |
| QA-025 | ALTO | Flyway sem migrações + ddl-auto=update | 4 |
| QA-011 | BAIXO | Sem @Version (concorrência) | 2 |
| QA-012 | BAIXO | tempoPreparoMinutos como String | 2 |

### Melhorias (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-018 | MÉDIO | Exceções genéricas nos services | 3 |
| QA-024 | MÉDIO | Dois paradigmas de controller (REST vs MVC) | 4 |
| QA-019 | BAIXO | 21 métodos de repository não utilizados | 3 |

### Itens ainda não auditados
- [ ] Testes adversariais (Fase 5)
- [ ] Engenharia de Software e aprendizado (Fase 6)
- [ ] Consolidação final e preparação para banca (Fase 7)
