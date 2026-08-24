# QA 02 — Entities, JPA/Hibernate e Banco

**Data:** 2026-08-23  
**Fase:** 2 de 7  
**Base:** Estado Consolidado V1  
**Objetivo:** Auditar domínio, persistência e integridade dos dados.

---

## 1. Análise das Entidades

### 1.1 Pedido

**Arquivo:** [Pedido.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Pedido.java)

| Campo | Tipo Java | Tipo JPA | Nullable | Unique | Validação | Observação |
|---|---|---|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | auto | PK | — | OK |
| `status` | `StatusPedido` | `@Enumerated(STRING) @Column(nullable=false)` | não | não | `@NotNull` | OK |
| `dataHoraPedido` | `String` | `@Column(nullable=false, length=50)` | não | não | `@NotBlank` | **PROBLEMA: deveria ser `LocalDateTime`** |
| `horarioEstimadoRetirada` | `String` | `@Column(nullable=false, length=50)` | não | não | `@NotBlank` | **PROBLEMA: deveria ser `LocalTime` ou `LocalDateTime`** |
| `valorTotal` | `double` (primitivo) | `@Column(nullable=false)` | não (primitivo = 0.0) | não | `@Min(0)` | **PROBLEMA: deveria ser `BigDecimal`** |
| `usuario` | `Usuario` | `@ManyToOne @JoinColumn(name="usuario_id", nullable=false)` | não | não | `@NotNull` | FK OK |
| `itens` | `List<ItemPedido>` | `@OneToMany(mappedBy="pedido", cascade=ALL, orphanRemoval=true)` | — | — | — | Bidirecional |

**Problemas graves:**

1. **`dataHoraPedido` como `String`** — impede comparações, ordenações e validações temporais no banco e no Java. Qualquer formato é aceito: "abc", "ontem", etc.
2. **`horarioEstimadoRetirada` como `String`** — mesmo problema.
3. **`valorTotal` como `double`** — imprecisão de ponto flutuante. Exemplo: `0.1 + 0.2 = 0.30000000000000004` em `double`. Para dinheiro, deve ser `BigDecimal`.
4. **`valorTotal` é primitivo `double`** — nunca é `null`, sempre inicia em `0.0`. A anotação `@Min(0)` nunca falha para um primitivo com valor padrão 0.
5. **Faltam setters** para `dataHoraPedido`, `horarioEstimadoRetirada`, `valorTotal` e `id`. O Jackson (desserialização JSON) pode falhar ao tentar popular esses campos no `@RequestBody`, dependendo da configuração. No caso padrão do Spring Boot (que usa field access via reflection), pode funcionar, mas é frágil e não segue boas práticas.

### 1.2 ItemPedido

**Arquivo:** [ItemPedido.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/ItemPedido.java)

| Campo | Tipo Java | Tipo JPA | Nullable | Unique | Validação | Observação |
|---|---|---|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | auto | PK | — | OK |
| `nome` | `String` | `@Column(nullable=false, length=100)` | não | não | `@NotBlank`, `@Size(3,100)` | OK |
| `preco` | `BigDecimal` | `@Column(nullable=false, precision=10, scale=2)` | não | não | `@NotNull`, `@DecimalMin("0.00")` | OK — usa BigDecimal ✓ |
| `pedido` | `Pedido` | `@ManyToOne @JoinColumn(name="pedido_id", nullable=false)` | não | não | — | FK OK, mas sem `@NotNull` na validação |

**Problemas:**

1. **Sem validação `@NotNull` no campo `pedido`** — a constraint do banco (`nullable=false`) garante integridade no banco, mas não há validação na camada Java antes de persistir.
2. **Sem referência a `Produto`** — o item é apenas nome/preço, sem FK para o catálogo. Alterações no produto (nome, preço) não se refletem nos itens já criados, mas também não é possível rastrear qual produto originou o item.
3. **Mensagem de validação incorreta** — `@NotBlank(message = "O nome do pedido é obrigatório")` diz "do pedido", mas deveria dizer "do item do pedido".

### 1.3 Produto

**Arquivo:** [Produto.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Produto.java)

| Campo | Tipo Java | Tipo JPA | Nullable | Unique | Validação | Observação |
|---|---|---|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | auto | PK | — | OK |
| `nome` | `String` | `@Column(nullable=false, length=100)` | não | **não** | `@NotBlank`, `@Size(3,100)` | **Sem unique — permite duplicatas** |
| `descricao` | `String` | `@Column(nullable=false, length=500)` | não | não | `@NotBlank`, `@Size(10,500)` | OK |
| `categoriaProduto` | `CategoriaProduto` | `@Enumerated(STRING) @Column(nullable=false)` | não | não | `@NotNull` | OK |
| `tempoPreparoMinutos` | `String` | `@Column(nullable=false, length=10)` | não | não | `@NotBlank`, `@Size(max=10)` | **PROBLEMA: deveria ser `int` ou `Duration`** |
| `quantidadeDisponivelHoje` | `int` (primitivo) | `@Column(nullable=false)` | não (primitivo) | não | `@Min(0)` | OK, mas primitivo nunca é null |
| `ativo` | `boolean` (primitivo) | `@Column(nullable=false)` | não (primitivo) | não | — | OK |

**Problemas:**

1. **Sem campo de preço** — a entidade `Produto` não tem preço! O preço só existe no `ItemPedido`. Isso é uma lacuna grave: como o cliente sabe quanto custa um produto?
2. **`tempoPreparoMinutos` como `String`** — deveria ser `int` ou `Integer`. Aceita qualquer texto: "rápido", "abc", etc.
3. **Nome do produto não é unique** — permite cadastrar dois produtos com o mesmo nome.
4. **Entidade isolada** — nenhum relacionamento com `ItemPedido` ou `Pedido`.

### 1.4 Usuario

**Arquivo:** [Usuario.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Usuario.java)

| Campo | Tipo Java | Tipo JPA | Nullable | Unique | Validação | Observação |
|---|---|---|---|---|---|---|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | auto | PK | — | OK |
| `nome` | `String` | `@Column(nullable=false, length=100)` | não | não | `@NotBlank`, `@Size(3,100)` | OK |
| `cpf` | `String` | `@Column(nullable=false, unique=true, length=11)` | não | **sim** | `@NotBlank`, `@Size(11,11)` | Unique ✓, mas sem validação de formato |
| `ativo` | `boolean` | `@Column(nullable=false)` | não (primitivo) | não | — | default `true` |
| `senha` | `String` | `@Column(nullable=false)` | não | não | `@NotBlank`, `@Size(6,255)` | **Texto plano — sem hashing** |
| `telefone` | `String` | `@Column(nullable=false, length=11)` | não | não | `@NotBlank`, `@Size(10,11)` | Sem unique — dois usuários com mesmo telefone |
| `email` | `String` | `@Column(nullable=false, unique=true, length=150)` | não | **sim** | `@NotBlank`, `@Email` | Unique ✓, validação de formato ✓ |
| `perfil` | `Perfil` | `@Enumerated(STRING) @Column(nullable=false)` | não | não | `@NotNull` | OK |

**Problemas:**

1. **Senha em texto plano** — a senha é armazenada como `String` sem hashing (`BCryptPasswordEncoder` não é usado em nenhum lugar). Grave problema de segurança.
2. **Sem setter para campos imutáveis** — `nome`, `cpf`, `senha`, `telefone`, `email`, `perfil`, `id` não têm setters. Isso impede a atualização do usuário e pode impedir a desserialização.
3. **CPF sem validação de formato** — aceita qualquer string de 11 caracteres: "aaaaaaaaaaa", "12345678901" (inválido como CPF).
4. **Método `excluir()` faz soft delete** — boa prática, mas sem correspondência no Service/Controller.
5. **Sem `@OneToMany` para pedidos** — relação unidirecional; não é possível navegar do usuário aos seus pedidos via JPA.

---

## 2. Análise dos Relacionamentos

### 2.1 Usuario → Pedido (1:N — Unidirecional do lado Pedido)

```java
// Em Pedido.java
@ManyToOne
@JoinColumn(name = "usuario_id", nullable = false)
private Usuario usuario;
```

- **Fetch:** padrão `EAGER` (ManyToOne é EAGER por padrão no JPA)
- **Cascade:** nenhum (correto — excluir pedido não deve excluir usuário)
- **Bidirecionalidade:** apenas do Pedido → Usuário. Não é possível fazer `usuario.getPedidos()`.
- **Problema potencial:** Ao serializar `Pedido` para JSON, o `Usuario` inteiro é incluído, **inclusive a senha**.

### 2.2 Pedido → ItemPedido (1:N — Bidirecional)

```java
// Em Pedido.java (lado "um")
@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ItemPedido> itens = new ArrayList<>();

// Em ItemPedido.java (lado "muitos")
@ManyToOne
@JoinColumn(name = "pedido_id", nullable = false)
private Pedido pedido;
```

- **Fetch:** `OneToMany` → padrão `LAZY` ✓; `ManyToOne` → padrão `EAGER`
- **Cascade:** `ALL` no Pedido → itens (persist, merge, remove, refresh, detach)
- **orphanRemoval:** `true` — itens removidos da lista são deletados do banco ✓
- **mappedBy:** `"pedido"` — lado inverso correto ✓

**Problema crítico — Serialização circular:**
- `Pedido.itens` → serializa cada `ItemPedido` → cada `ItemPedido.pedido` → serializa `Pedido` novamente → loop infinito
- Resultado: `StackOverflowError` ou `HttpMessageNotWritableException` ao retornar `Pedido` como JSON
- **Sem `@JsonManagedReference`/`@JsonBackReference` ou `@JsonIgnore`** para quebrar o ciclo

### 2.3 Produto (Isolado)

`Produto` não tem nenhum relacionamento JPA. É uma entidade independente sem FK.

**Consequência:** Não existe vínculo entre o catálogo de produtos e os itens dos pedidos. O `ItemPedido` duplica informações (nome, preço) sem rastreabilidade.

---

## 3. Análise de IDs, Tipos e Constraints

### 3.1 Estratégia de ID

Todas as entidades usam `@GeneratedValue(strategy = GenerationType.IDENTITY)` — auto-incremento no MySQL. Adequado para MySQL/InnoDB.

### 3.2 Inconsistência de tipos monetários

| Entidade | Campo | Tipo Java | Problema |
|---|---|---|---|
| `Pedido` | `valorTotal` | `double` | Imprecisão de ponto flutuante |
| `ItemPedido` | `preco` | `BigDecimal` | Correto ✓ |
| `Produto` | — | — | Sem campo de preço |

A mistura de `double` e `BigDecimal` para valores monetários é uma inconsistência grave. Se o `valorTotal` fosse calculado somando os `preco` dos itens, haveria conversão `BigDecimal` → `double`, perdendo precisão.

### 3.3 Tipos temporais como String

| Entidade | Campo | Tipo | Deveria ser |
|---|---|---|---|
| `Pedido` | `dataHoraPedido` | `String` | `LocalDateTime` |
| `Pedido` | `horarioEstimadoRetirada` | `String` | `LocalTime` ou `LocalDateTime` |
| `Produto` | `tempoPreparoMinutos` | `String` | `int` ou `Integer` |

---

## 4. Simulações Conceituais

### 4.1 Exclusão de um Pedido

```
DELETE /pedidos/{id}
  → PedidoService.excluirPedido(id)
    → pedidoRepository.existsById(id)     // verifica existência
    → pedidoRepository.deleteById(id)     // exclui pedido
      → cascade=ALL + orphanRemoval=true  // itens são excluídos automaticamente
```

**Resultado:** Funciona como esperado. Os itens do pedido são removidos em cascata. ✓

**Mas:** O usuário referenciado pelo pedido NÃO é afetado (correto, não há cascade no ManyToOne).

### 4.2 Exclusão de um Usuário que tem Pedidos

**Cenário:** O `Usuario` não tem controller nem service, mas se fosse excluído diretamente no banco:

```sql
DELETE FROM usuario WHERE id = 1;
```

**Resultado:** `ConstraintViolationException` — a FK `pedido.usuario_id` impede a exclusão do usuário enquanto existirem pedidos associados. O banco protege a integridade referencial.

**Mas:** O método `Usuario.excluir()` faz soft delete (`ativo = false`), o que evita esse problema. A questão é que esse método não é chamado por nenhum Service ou Controller.

### 4.3 Criação de Pedido sem Itens

```java
// PedidoService.criarPedido()
verificarPedido(pedido.getItens());
// getItens() retorna new ArrayList<>() (inicializado na entidade)
// ArrayList vazio → isEmpty() == true → lança IllegalArgumentException
```

**Resultado:** A validação funciona ✓. Mas a exceção `IllegalArgumentException` propaga sem tratamento (não há `@ExceptionHandler`), resultando em HTTP 500 Internal Server Error com stack trace exposto.

### 4.4 Criação de Pedido com Itens (via REST API)

**JSON enviado:**
```json
{
  "status": "CRIADO",
  "dataHoraPedido": "qualquer texto",
  "horarioEstimadoRetirada": "qualquer texto",
  "valorTotal": 99.99,
  "usuario": { "id": 1 },
  "itens": [
    { "nome": "Coxinha", "preco": 5.50 }
  ]
}
```

**Problemas no fluxo:**

1. **Sem `@Valid`** no `PedidoController.criarPedido()` — as anotações de validação em `Pedido` nunca são verificadas.
2. **`dataHoraPedido` e `horarioEstimadoRetirada`** aceitam qualquer texto.
3. **`valorTotal` é informado pelo cliente** — o servidor não calcula. O cliente pode enviar `0.0` para um pedido de R$ 100.
4. **`usuario: { "id": 1 }`** — o JPA pode tentar persistir um novo usuário ou falhar, dependendo do estado de gerenciamento da entidade. Como o `usuario` vem por desserialização e não é uma entidade gerenciada, o `save()` do Pedido pode tentar fazer `persist` no usuário (que já existe), causando `EntityExistsException` ou `PersistenceException`.
5. **Os itens não têm `pedido` setado** — no JSON, os itens não referenciam o pedido pai. O `cascade` faz o JPA persistir os itens junto com o pedido, mas o campo `pedido` de cada `ItemPedido` fica `null`. Como `pedido_id` é `nullable=false` no banco, isso causa `ConstraintViolationException`.

### 4.5 Registro Inexistente

```java
// PedidoService.buscarPorId(999L)
pedidoRepository.findById(999L).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
```

**Resultado:** `RuntimeException` propagada sem tratamento → HTTP 500 com stack trace. Deveria retornar HTTP 404.

### 4.6 Banco Vazio

```java
// PedidoService.listarTodos()
pedidoRepository.findAll(); // retorna lista vazia
```

**Resultado:** HTTP 200 com `[]`. Correto ✓.

### 4.7 Duplicidade

- **Pedido:** Sem controle de duplicidade. O mesmo pedido pode ser criado múltiplas vezes.
- **Produto:** Sem `unique` no nome. Duplicatas permitidas.
- **Usuário:** `cpf` e `email` são `unique`. Tentativa de duplicar causa `DataIntegrityViolationException` → HTTP 500 (sem tratamento).

### 4.8 Concorrência

Não há mecanismo de controle de concorrência (`@Version` / Optimistic Locking). Dois usuários podem atualizar o mesmo registro simultaneamente, e o último `save()` sobrescreve as alterações do primeiro (Lost Update).

---

## 5. Achados Formais

---

### QA-001

**Severidade:** CRÍTICO  
**Tipo:** BUG CONFIRMADO  
**Local:** [Pedido.java:42-43](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Pedido.java#L42-L43) ↔ [ItemPedido.java:26-28](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/ItemPedido.java#L26-L28)

**Problema:** Serialização circular entre `Pedido` e `ItemPedido`. Jackson tenta serializar `Pedido.itens` → cada `ItemPedido.pedido` → `Pedido` novamente → loop infinito.

**Como reproduzir:** `GET /pedidos/{id}` ou `GET /pedidos` quando existem pedidos com itens.

**Comportamento atual/provável:** `StackOverflowError` ou `HttpMessageNotWritableException` → HTTP 500.

**Comportamento esperado:** JSON do pedido com seus itens, sem loop.

**Risco/impacto:** API REST completamente quebrada para qualquer pedido que tenha itens.

**Conceito envolvido:** Serialização JSON (Jackson), referências circulares em grafos de objetos bidirecionais.

**Sugestão de correção conceitual:** Adicionar `@JsonManagedReference` em `Pedido.itens` e `@JsonBackReference` em `ItemPedido.pedido`. Ou melhor: usar DTOs que controlem exatamente o que é serializado.

**Por que a solução resolve:** Ao anotar o lado "de volta" com `@JsonBackReference`, o Jackson não tenta serializar a referência circular, quebrando o loop.

**Como explicar ao professor:** "O JPA precisa de referência bidirecional para manter a integridade, mas o Jackson (que converte para JSON) não sabe parar quando encontra essa referência circular. É como dois espelhos frente a frente. Precisamos dizer ao Jackson qual lado ignorar."

---

### QA-002

**Severidade:** CRÍTICO  
**Tipo:** BUG CONFIRMADO  
**Local:** [Pedido.java:25-31](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Pedido.java#L25-L31)

**Problema:** `dataHoraPedido` e `horarioEstimadoRetirada` são `String` em vez de tipos temporais Java (`LocalDateTime`, `LocalTime`).

**Como reproduzir:** Enviar `"dataHoraPedido": "não é uma data"` no JSON → aceito sem erro.

**Comportamento atual/provável:** Qualquer string é aceita e persistida. Não é possível ordenar, filtrar ou comparar datas no banco.

**Comportamento esperado:** Validação de formato de data/hora; armazenamento como `DATETIME` ou `TIME` no MySQL; capacidade de ordenação e comparação.

**Risco/impacto:** Impossível implementar funcionalidades que dependam de datas: "pedidos do dia", "pedidos atrasados", "horário de retirada mais próximo", etc.

**Conceito envolvido:** Tipos temporais do Java (`java.time.*`), mapeamento JPA de datas, `@Column(columnDefinition)`.

**Sugestão de correção conceitual:** Usar `LocalDateTime` para `dataHoraPedido` e `LocalTime` ou `LocalDateTime` para `horarioEstimadoRetirada`. JPA/Hibernate mapeia automaticamente para `DATETIME`/`TIME` no MySQL.

**Por que a solução resolve:** Tipos temporais nativos garantem validação automática, ordenação correta e comparação no banco e no Java.

**Como explicar ao professor:** "Usar String para datas é como guardar um número como texto — funciona para exibir, mas não funciona para calcular, ordenar ou comparar. O Java tem a API `java.time` justamente para isso."

---

### QA-003

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [Pedido.java:33-35](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Pedido.java#L33-L35)

**Problema:** `valorTotal` é `double` (tipo primitivo de ponto flutuante) para representar valor monetário.

**Como reproduzir:** Operações aritméticas com `double`: `0.1 + 0.2 = 0.30000000000000004`.

**Comportamento atual/provável:** Erros de arredondamento em cálculos financeiros. Valores como R$ 10.10 podem ser armazenados como `10.100000000000001`.

**Comportamento esperado:** Precisão decimal exata para valores monetários.

**Risco/impacto:** Valores financeiros incorretos; incoerência entre soma dos itens e valor total.

**Conceito envolvido:** Representação de ponto flutuante (IEEE 754), `BigDecimal` em Java.

**Sugestão de correção conceitual:** Usar `BigDecimal` com `@Column(precision=10, scale=2)`, exatamente como já é feito em `ItemPedido.preco`.

**Por que a solução resolve:** `BigDecimal` armazena valores decimais exatos, sem erros de representação binária.

**Como explicar ao professor:** "O `double` armazena números em binário (base 2), e alguns decimais (base 10) não têm representação exata em binário. Por isso `0.1 + 0.2 ≠ 0.3` em double. Para dinheiro, usamos `BigDecimal` que trabalha em base 10."

---

### QA-004

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [Usuario.java:32-35](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Usuario.java#L32-L35)

**Problema:** Senha armazenada em texto plano. Não há hashing com `BCryptPasswordEncoder` ou equivalente.

**Como reproduzir:** Inserir um usuário → consultar o banco → senha visível em texto claro.

**Comportamento atual/provável:** Qualquer pessoa com acesso ao banco (ou à API, quando existir) pode ler as senhas.

**Comportamento esperado:** Senha armazenada como hash irreversível (bcrypt, argon2, etc.).

**Risco/impacto:** Violação de segurança grave. Em um contexto real, violaria a LGPD. Em contexto acadêmico, demonstra desconhecimento de segurança básica.

**Conceito envolvido:** Hashing de senhas, Spring Security `PasswordEncoder`, LGPD, princípio de defesa em profundidade.

**Sugestão de correção conceitual:** Configurar `BCryptPasswordEncoder` como `@Bean` e usá-lo no Service de criação de usuário para codificar a senha antes de salvar.

**Por que a solução resolve:** BCrypt é uma função hash unidirecional (não pode ser revertida) e incorpora salt automático, prevenindo ataques de rainbow table.

**Como explicar ao professor:** "Nunca armazenamos senhas em texto plano. Usamos uma função hash (como bcrypt) que transforma a senha em um código irreversível. Na hora do login, aplicamos o hash na senha digitada e comparamos com o hash armazenado."

---

### QA-005

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [PedidoController.java:24](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/controller/PedidoController.java#L24) ↔ [Pedido.java:37-40](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Pedido.java#L37-L40)

**Problema:** Ao criar pedido via REST API, o JSON inclui o objeto `usuario` completo. Se o `usuario` é desserializado como nova entidade (sem estar no contexto de persistência), o `cascade` pode tentar persistir o usuário novamente ou falhar. Além disso, ao retornar pedidos no JSON, o objeto `Usuario` é serializado completamente, **incluindo a senha em texto plano**.

**Como reproduzir:** `GET /pedidos/{id}` → resposta JSON inclui `usuario.senha`.

**Comportamento atual/provável:** Senha do usuário exposta na API.

**Comportamento esperado:** Senha nunca deve aparecer em respostas da API.

**Risco/impacto:** Exposição de dados sensíveis.

**Conceito envolvido:** Exposição de dados, DTO vs Entidade, serialização Jackson.

**Sugestão de correção conceitual:** Usar DTOs para entrada e saída da API. Alternativamente, anotar `senha` com `@JsonIgnore`, mas DTOs são a solução arquitetural correta.

**Por que a solução resolve:** DTOs permitem controlar exatamente quais campos são expostos na API, desacoplando a representação externa da estrutura interna do banco.

**Como explicar ao professor:** "A entidade JPA representa a estrutura do banco. O DTO representa a estrutura da API. São coisas diferentes. Se expormos a entidade diretamente, qualquer campo interno (como senha) vai parar na resposta da API."

---

### QA-006

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [Produto.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Produto.java)

**Problema:** Entidade `Produto` não possui campo de preço.

**Como reproduzir:** Análise estática — não existe `preco` nem `valor` na classe `Produto`.

**Comportamento atual/provável:** Não é possível saber o preço de um produto cadastrado. O preço só existe no `ItemPedido`.

**Comportamento esperado:** `Produto` deveria ter um campo `preco` (`BigDecimal`), e o `ItemPedido` deveria referenciar o `Produto` e copiar o preço no momento da criação do pedido.

**Risco/impacto:** Impossível listar produtos com preço para o cliente. Impossível validar se o preço do item confere com o preço do produto.

**Conceito envolvido:** Modelagem de domínio, normalização vs desnormalização, integridade de dados.

**Sugestão de correção conceitual:** Adicionar `BigDecimal preco` em `Produto`. Adicionar `@ManyToOne Produto produto` em `ItemPedido`. O `ItemPedido.preco` passa a ser o "preço no momento da compra" (snapshot), enquanto `Produto.preco` é o preço atual.

**Por que a solução resolve:** Permite listar produtos com preço e rastrear a origem do preço de cada item.

**Como explicar ao professor:** "É como um supermercado: o produto na prateleira tem preço (Produto.preco), e a nota fiscal registra o preço que você pagou naquele dia (ItemPedido.preco). Ambos precisam existir."

---

### QA-007

**Severidade:** MÉDIO  
**Tipo:** RISCO POTENCIAL  
**Local:** [ItemPedido.java:26-28](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/ItemPedido.java#L26-L28)

**Problema:** `ItemPedido` não referencia `Produto` via FK. O item armazena apenas `nome` e `preco`, sem rastreabilidade ao catálogo.

**Como reproduzir:** Criar um item com `nome: "Coxinha"` → alterar o nome do produto para "Coxinha de Frango" → o item antigo continua com "Coxinha".

**Comportamento atual/provável:** Sem rastreabilidade. Relatórios de vendas por produto são impossíveis.

**Comportamento esperado:** FK para `Produto`, com `nome` e `preco` como snapshot.

**Risco/impacto:** Impossível gerar relatórios de vendas por produto, controlar estoque, ou validar preços.

**Conceito envolvido:** Chave estrangeira, integridade referencial, normalização.

**Sugestão de correção conceitual:** Adicionar `@ManyToOne Produto produto` em `ItemPedido` e `@OneToMany List<ItemPedido> itens` em `Produto` (se necessário bidirecional).

**Por que a solução resolve:** A FK garante que cada item está ligado a um produto real, permitindo rastreabilidade e consultas.

**Como explicar ao professor:** "Sem a FK, o banco não garante que o 'Coxinha' do item de pedido é a mesma 'Coxinha' do catálogo. A chave estrangeira é a forma do banco garantir essa relação."

---

### QA-008

**Severidade:** MÉDIO  
**Tipo:** RISCO POTENCIAL  
**Local:** [UsuarioRepository.java:16-17](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/UsuarioRepository.java#L16-L17)

**Problema:** `findByAtivo(boolean ativo)` retorna `Usuario` (singular), mas múltiplos usuários podem ter o mesmo valor de `ativo`.

**Como reproduzir:** Ter mais de um usuário ativo → chamar `findByAtivo(true)`.

**Comportamento atual/provável:** Se houver mais de um resultado, o Spring Data lança `IncorrectResultSizeDataAccessException` em tempo de execução.

**Comportamento esperado:** Retornar `List<Usuario>`.

**Risco/impacto:** Exceção em runtime quando houver múltiplos usuários ativos (que é o cenário normal).

**Conceito envolvido:** Spring Data query return types, cardinalidade, `IncorrectResultSizeDataAccessException`.

**Sugestão de correção conceitual:** Mudar o retorno para `List<Usuario>`.

**Por que a solução resolve:** `List` suporta zero, um ou muitos resultados, sem exceção.

**Como explicar ao professor:** "Quando uma query pode retornar múltiplos resultados, o método deve retornar `List`. Se declaramos retorno singular e vem mais de um resultado, o Spring lança exceção."

---

### QA-009

**Severidade:** MÉDIO  
**Tipo:** RISCO POTENCIAL  
**Local:** [UsuarioRepository.java:19-20](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/UsuarioRepository.java#L19-L20)

**Problema:** `findBySenha(String senha)` — método que busca usuário por senha. Anti-pattern de segurança.

**Como reproduzir:** Análise estática — o método existe.

**Comportamento atual/provável:** Permite buscar usuários pela senha. Se exposto em uma API, qualquer pessoa pode tentar senhas até encontrar uma que retorne usuário.

**Comportamento esperado:** Busca por senha não deveria existir.

**Risco/impacto:** Vetor de ataque por força bruta; senha em texto plano torna isso trivial.

**Conceito envolvido:** Segurança, autenticação, hashing de senhas.

**Sugestão de correção conceitual:** Remover o método. A autenticação deve ser feita pelo Spring Security, comparando o hash.

**Por que a solução resolve:** Elimina o vetor de ataque e segue o padrão de autenticação correto.

**Como explicar ao professor:** "Nunca buscamos usuário pela senha. A autenticação correta é: buscar o usuário pelo email/cpf, pegar o hash armazenado e comparar com o hash da senha digitada."

---

### QA-010

**Severidade:** MÉDIO  
**Tipo:** BUG CONFIRMADO  
**Local:** [ItemPedidoRepository.java:17-18](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/ItemPedidoRepository.java#L17-L18)

**Problema:** `findByPreco(@Param("preco") Double preco)` — o parâmetro é `Double` (wrapper de double), mas `ItemPedido.preco` é `BigDecimal`.

**Como reproduzir:** Chamar o método passando um `Double`.

**Comportamento atual/provável:** O Hibernate pode realizar conversão implícita, mas a comparação de `double` com `BigDecimal` pode gerar resultados incorretos por imprecisão de ponto flutuante, ou falhar silenciosamente (nenhum resultado retornado para um preço que existe).

**Comportamento esperado:** Parâmetro deveria ser `BigDecimal`.

**Risco/impacto:** Busca por preço retorna resultados incorretos ou vazios.

**Conceito envolvido:** Compatibilidade de tipos, `BigDecimal` vs `double`, conversão implícita.

**Sugestão de correção conceitual:** Mudar o parâmetro para `BigDecimal`.

**Por que a solução resolve:** Eliminando a conversão implícita, a comparação é feita com o tipo correto.

**Como explicar ao professor:** "Quando comparamos `Double` com `BigDecimal`, o banco pode fazer conversão implícita e perder precisão. Devemos usar o mesmo tipo no parâmetro e no campo."

---

### QA-011

**Severidade:** BAIXO  
**Tipo:** MELHORIA  
**Local:** Todas as entidades

**Problema:** Ausência de `@Version` para controle de concorrência otimista.

**Como reproduzir:** Dois usuários editam o mesmo pedido simultaneamente → ambos fazem `save()` → segundo sobrescreve o primeiro sem aviso.

**Comportamento atual/provável:** Lost Update — a última gravação "ganha" sem detectar conflito.

**Comportamento esperado:** Segunda gravação deveria detectar o conflito e lançar `OptimisticLockException`.

**Risco/impacto:** Perda silenciosa de dados em cenários de edição concorrente.

**Conceito envolvido:** Controle de concorrência otimista, `@Version`, Lost Update Problem.

**Sugestão de correção conceitual:** Adicionar `@Version private Long version;` nas entidades.

**Por que a solução resolve:** O JPA verifica automaticamente se a versão mudou entre o `SELECT` e o `UPDATE`. Se mudou, lança `OptimisticLockException`.

**Como explicar ao professor:** "Imagine dois caixas editando o mesmo pedido. Sem `@Version`, o segundo caixa sobrescreve as mudanças do primeiro sem perceber. Com `@Version`, o sistema detecta que o pedido mudou e avisa que há conflito."

---

### QA-012

**Severidade:** BAIXO  
**Tipo:** MELHORIA  
**Local:** [Produto.java:34-37](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/database/model/Produto.java#L34-L37)

**Problema:** `tempoPreparoMinutos` é `String` em vez de `int` ou `Integer`.

**Como reproduzir:** Inserir `tempoPreparoMinutos: "rápido"` → aceito sem erro.

**Comportamento atual/provável:** Qualquer texto é aceito. Não é possível ordenar produtos por tempo de preparo ou filtrar "produtos que ficam prontos em até 10 minutos".

**Comportamento esperado:** Campo numérico que represente minutos.

**Risco/impacto:** Impossível utilizar o campo em cálculos, comparações ou filtros.

**Conceito envolvido:** Modelagem de dados, tipos adequados, tipagem forte.

**Sugestão de correção conceitual:** Mudar para `int tempoPreparoMinutos` com `@Min(1)`.

**Por que a solução resolve:** Tipo numérico garante que apenas valores válidos são aceitos e permite operações matemáticas e comparações.

**Como explicar ao professor:** "Usar String para um valor numérico impede o compilador e o banco de dados de validarem o valor. É como guardar idade como texto — funciona para exibir, mas não para calcular."

---

## 6. Explicações Conceituais

### 6.1 JPA (Java Persistence API)

**O que é:** Especificação Java que define como mapear objetos Java para tabelas de banco de dados.

**Para que serve:** Evitar escrever SQL manualmente para operações CRUD. O desenvolvedor trabalha com objetos Java e o JPA traduz para SQL.

**Onde aparece no projeto:** Todas as classes em `database/model/` usam anotações JPA (`@Entity`, `@Table`, `@Column`, etc.).

**Como funciona:** O desenvolvedor define a entidade com anotações → O Hibernate (implementação do JPA) gera o SQL correspondente → O Spring Boot auto-configura a conexão e o `EntityManager`.

### 6.2 Hibernate (ORM)

**O que é:** Implementação mais popular do JPA. É o ORM (Object-Relational Mapping) padrão do Spring Boot.

**Problema que resolve:** A "impedance mismatch" — a diferença entre o modelo orientado a objetos (Java) e o modelo relacional (SQL).

**No projeto:** Quando `PedidoRepository.save(pedido)` é chamado, o Hibernate gera `INSERT INTO pedido (status, ...) VALUES (?, ...)` automaticamente.

### 6.3 Cardinalidade (1:1, 1:N, N:N)

**O que é:** Descreve quantos registros de uma tabela se relacionam com quantos registros de outra.

**No projeto:**
- `Usuario` 1 → N `Pedido` (um usuário faz vários pedidos)
- `Pedido` 1 → N `ItemPedido` (um pedido tem vários itens)

**Anotações JPA:**
- `@ManyToOne` no lado "muitos" (FK fica aqui)
- `@OneToMany` no lado "um" (lista; usa `mappedBy`)

### 6.4 Chave Primária e Estrangeira

**Chave Primária (PK):** Identifica unicamente cada registro. No projeto: `@Id @GeneratedValue(IDENTITY)`.

**Chave Estrangeira (FK):** Referência a uma PK de outra tabela. No projeto: `@JoinColumn(name = "usuario_id")` em `Pedido` cria a FK para `Usuario`.

**Integridade referencial:** O banco garante que o `usuario_id` em `pedido` sempre aponta para um `id` válido em `usuario`.

### 6.5 Cascade e orphanRemoval

**Cascade:** Propaga operações do pai para os filhos. `CascadeType.ALL` em `Pedido.itens` significa que ao salvar/deletar um pedido, os itens são salvos/deletados automaticamente.

**orphanRemoval:** Quando um item é removido da lista `pedido.getItens().remove(item)`, ele é automaticamente deletado do banco. Sem isso, o item ficaria órfão (sem pai, mas existindo no banco).

**No projeto:** `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` em `Pedido.itens`.

### 6.6 Fetch (EAGER vs LAZY)

**EAGER:** Carrega o relacionamento imediatamente junto com a entidade principal. Padrão para `@ManyToOne` e `@OneToOne`.

**LAZY:** Carrega o relacionamento apenas quando acessado. Padrão para `@OneToMany` e `@ManyToMany`.

**No projeto:**
- `Pedido.usuario` (ManyToOne) → EAGER por padrão → ao carregar um pedido, o usuário já vem junto
- `Pedido.itens` (OneToMany) → LAZY por padrão → os itens só são carregados quando `pedido.getItens()` é chamado

**Risco no projeto:** Como `Pedido.usuario` é EAGER e o `PedidoController` retorna a entidade diretamente, o JSON sempre inclui o usuário completo (com senha).

---

## ESTADO CONSOLIDADO — V2

### Arquitetura conhecida
(Mantida da V1, sem alterações)

### Componentes analisados
- V1: todos os componentes mapeados
- V2: análise profunda das 4 entidades, 4 repositories, integridade referencial, simulações de persistência

### Regras de negócio identificadas
(Mantidas RN-01 a RN-13 da V1)

### Bugs confirmados
| ID | Severidade | Resumo |
|---|---|---|
| QA-001 | CRÍTICO | Serialização circular Pedido ↔ ItemPedido |
| QA-002 | CRÍTICO | Datas como String (dataHoraPedido, horarioEstimadoRetirada) |
| QA-003 | ALTO | valorTotal como double (imprecisão monetária) |
| QA-004 | ALTO | Senha em texto plano |
| QA-005 | ALTO | Entidade JPA exposta na API (inclui senha do usuário) |
| QA-006 | ALTO | Produto sem campo de preço |
| QA-010 | MÉDIO | findByPreco com Double vs BigDecimal |

### Riscos potenciais
| ID | Severidade | Resumo |
|---|---|---|
| QA-007 | MÉDIO | ItemPedido sem FK para Produto |
| QA-008 | MÉDIO | findByAtivo retorna singular, deveria retornar List |
| QA-009 | MÉDIO | findBySenha — anti-pattern de segurança |
| QA-011 | BAIXO | Sem @Version (controle de concorrência) |
| QA-012 | BAIXO | tempoPreparoMinutos como String |

### Dúvidas pendentes
- D-01 a D-06 (mantidas da V1)
- D-07: O valorTotal deveria ser calculado server-side? (hipótese: sim)
- D-08: O Produto deveria ter preço? (hipótese: sim)

### Hipóteses adotadas
- H-01 a H-03 (mantidas da V1)
- H-04: `valorTotal` deveria ser calculado pelo servidor somando os preços dos itens.
- H-05: `Produto` deveria ter campo `preco` e `ItemPedido` deveria referenciar `Produto`.

### Itens ainda não auditados
- [ ] Análise profunda de Repository e Service (Fase 3)
- [ ] Análise profunda de Controller e API REST (Fase 4)
- [ ] Testes adversariais (Fase 5)
- [ ] Engenharia de Software e aprendizado (Fase 6)
- [ ] Consolidação final e preparação para banca (Fase 7)
