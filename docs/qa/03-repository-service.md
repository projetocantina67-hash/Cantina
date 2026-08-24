# QA 03 — Repository e Service

**Data:** 2026-08-23  
**Fase:** 3 de 7  
**Base:** Estado Consolidado V2  
**Objetivo:** Auditar persistência, regras de negócio e separação de responsabilidades.

---

## 1. Auditoria dos Repositories

### 1.1 ItemPedidoRepository

**Arquivo:** [ItemPedidoRepository.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/ItemPedidoRepository.java)

```java
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    List<ItemPedido> findByPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT i FROM ItemPedido i WHERE i.nome = :nome")
    List<ItemPedido> findByNome(@Param("nome") String nome);

    @Query("SELECT i FROM ItemPedido i WHERE i.preco = :preco")
    List<ItemPedido> findByPreco(@Param("preco") Double preco);
}
```

**Análise:**

| Método | Usado? | Problema |
|---|---|---|
| `findByPedidoId` | Não | Redundante — `Pedido.itens` (OneToMany) já fornece a lista via navegação JPA |
| `findByNome` | Não | Funcional, mas não usado |
| `findByPreco` | Não | **BUG:** parâmetro `Double`, campo é `BigDecimal` (QA-010) |

**Queries redundantes:** Todas as três queries poderiam ser substituídas por Spring Data derived query methods (nomenclatura do método), eliminando a necessidade de `@Query`:
- `findByPedidoId` → Spring Data geraria automaticamente por convenção de nomes
- `findByNome` → idem
- `findByPreco` → idem (mas precisa corrigir o tipo)

**N+1:** `findByPedidoId` é redundante porque ao carregar um `Pedido`, os itens podem ser obtidos via `pedido.getItens()`. Se a coleção for LAZY, o JPA faz uma segunda query (mas não é N+1 propriamente, pois é uma query explícita para um pedido específico).

### 1.2 PedidoRepository

**Arquivo:** [PedidoRepository.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/PedidoRepository.java)

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByDataHoraPedido(String dataHoraPedido);
    List<Pedido> findByHorarioEstimadoRetirada(String horarioEstimadoRetirada);
    List<Pedido> findByValorTotal(double valorTotal);
    List<Pedido> findByUsuarioIdAndStatus(Long usuarioId, StatusPedido status);
}
```

**Análise:**

| Método | Usado? | Problema |
|---|---|---|
| `findByStatus` | Não | Funcional |
| `findByUsuarioId` | Não | Funcional |
| `findByDataHoraPedido` | Não | **Problema:** busca String exata; se o formato variar (ex: "2024-01-01" vs "01/01/2024"), não encontra |
| `findByHorarioEstimadoRetirada` | Não | Mesmo problema de String |
| `findByValorTotal` | Não | **Problema:** busca `double` por igualdade — imprecisão de ponto flutuante torna igualdade não confiável |
| `findByUsuarioIdAndStatus` | Não | Funcional |

**Nenhum método é usado** pelos Services existentes. Todos os Services usam apenas os métodos herdados de `JpaRepository` (`save`, `findById`, `findAll`, `deleteById`, `existsById`).

**Problema com `findByValorTotal`:** Comparar `double` por igualdade é perigoso: `findByValorTotal(10.10)` pode não encontrar um registro salvo como `10.100000000000001`. Isso é consequência do QA-003.

### 1.3 ProdutoRepository

**Arquivo:** [ProdutoRepository.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/ProdutoRepository.java)

```java
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByCategoria(CategoriaProduto categoria);
    List<Produto> findByNome(String nome);
    List<Produto> findByQuantidadeDisponivelHoje(int quantidade);
    List<Produto> findByAtivo(boolean ativo);
    List<Produto> findByDescricao(String descricao);
    List<Produto> findByTempoPreparoMinutos(String tempoPreparoMinutos);
}
```

**Análise:**

| Método | Usado? | Problema |
|---|---|---|
| `findByCategoria` | Não | Funcional |
| `findByNome` | Não | Funcional, mas busca por igualdade exata (sem LIKE) |
| `findByQuantidadeDisponivelHoje` | Não | Pouco útil — busca quantidade exata, não ">=1" (disponíveis) |
| `findByAtivo` | Não | Funcional |
| `findByDescricao` | Não | Pouco útil — busca descrição exata, não parcial |
| `findByTempoPreparoMinutos` | Não | Tipo String, sem utilidade prática |

**Sem Service nem Controller.** Nenhum destes métodos é utilizado por nenhuma camada do sistema.

### 1.4 UsuarioRepository

**Arquivo:** [UsuarioRepository.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/repository/UsuarioRepository.java)

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByNome(String nome);
    Usuario findByCpf(String cpf);
    Usuario findByAtivo(boolean ativo);     // QA-008: retorna singular para múltiplos resultados
    Usuario findBySenha(String senha);      // QA-009: anti-pattern de segurança
    Usuario findByTelefone(String telefone);
    Usuario findByEmail(String email);
}
```

**Análise:**

- **QA-008** (já registrado): `findByAtivo` retorna `Usuario` singular — vai lançar `IncorrectResultSizeDataAccessException` se houver mais de um usuário ativo.
- **QA-009** (já registrado): `findBySenha` é anti-pattern de segurança.
- `findByNome` retorna `Usuario` singular — pode falhar se houver homônimos.
- Nenhum método retorna `Optional<Usuario>` — se o resultado for nulo, o chamador recebe `null` sem aviso.

**Sem Service nem Controller.** Nenhum destes métodos é utilizado.

### 1.5 Resumo: Métodos declarados vs utilizados

| Repository | Métodos customizados | Métodos usados | % utilização |
|---|---|---|---|
| ItemPedidoRepository | 3 | 0 | 0% |
| PedidoRepository | 6 | 0 | 0% |
| ProdutoRepository | 6 | 0 | 0% |
| UsuarioRepository | 6 | 0 | 0% |
| **Total** | **21** | **0** | **0%** |

Dos 21 métodos customizados declarados, **nenhum é utilizado** pelo código atual.

---

## 2. Auditoria dos Services

### 2.1 PedidoService

**Arquivo:** [PedidoService.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/PedidoService.java)

```java
@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository) { ... }

    public void verificarPedido(List<ItemPedido> itemPedido) {
        if (itemPedido == null || itemPedido.isEmpty()) {
            throw new IllegalArgumentException("Pedido precisa conter ao menos um item");
        }
    }

    @Transactional
    public Pedido criarPedido(Pedido pedido) {
        verificarPedido(pedido.getItens());
        return pedidoRepository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional
    public void excluirPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido não encontrado");
        }
        pedidoRepository.deleteById(id);
    }
}
```

**Análise detalhada:**

#### `verificarPedido()`
- ✓ Valida que a lista de itens não é nula/vazia.
- ✗ Não valida o conteúdo dos itens (nome, preço).
- ✗ Não valida o usuário do pedido.
- ✗ Não valida status, datas ou valorTotal.

#### `criarPedido()`
- ✓ `@Transactional` — garante atomicidade.
- ✗ Não define o status inicial (o cliente envia qualquer status).
- ✗ Não calcula `valorTotal` a partir dos itens.
- ✗ Não registra `dataHoraPedido` automaticamente (o cliente envia).
- ✗ Não valida se o `Usuario` existe no banco.
- ✗ Não seta a referência `pedido` em cada `ItemPedido` (itens vêm do JSON sem backreference → o campo `pedido_id` fica null → `ConstraintViolationException`).
- ✗ Usa `jakarta.transaction.Transactional` em vez de `org.springframework.transaction.annotation.Transactional` — funciona, mas não tem acesso a atributos como `readOnly`, `rollbackFor`, etc.

#### `buscarPorId()`
- ✗ Não é `@Transactional(readOnly = true)`.
- ✗ Lança `RuntimeException` genérica — deveria ser exceção de domínio mapeada para HTTP 404.

#### `listarTodos()`
- ✗ Não é `@Transactional(readOnly = true)`.
- ✗ Retorna todas as entidades sem paginação — pode ser problema de performance com muitos registros.

#### `excluirPedido()`
- ✓ Verifica existência antes de excluir.
- ✗ Lança `RuntimeException` genérica.
- ✗ Não verifica se o pedido está em um estado que permite exclusão (ex: pedido "PRONTO" pode ser excluído?).

#### Funcionalidades ausentes no PedidoService:
- Sem atualização de pedido (PUT/PATCH)
- Sem atualização de status (máquina de estados)
- Sem cálculo de valorTotal
- Sem validação do usuário
- Sem busca por status, usuário, data
- Sem paginação

### 2.2 ItemPedidoService

**Arquivo:** [ItemPedidoService.java](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/ItemPedidoService.java)

```java
@Service
public class ItemPedidoService {
    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoRepository pedidoRepository;

    @Autowired
    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository, PedidoRepository pedidoRepository) { ... }

    @Transactional
    public ItemPedido criarItemPedido(ItemPedido itemPedido) { ... }

    @Transactional
    public ItemPedido atualizarItemPedido(Long id, ItemPedido itemPedido) { ... }

    @Transactional
    public void excluirItemPedido(Long id) { itemPedidoRepository.deleteById(id); }

    @Transactional(readOnly = true)
    public List<ItemPedido> listAll() { return itemPedidoRepository.findAll(); }

    public ItemPedido buscarPorId(Long id) { ... }
}
```

**Análise detalhada:**

#### `criarItemPedido()`
- ✓ Valida que o item não é nulo.
- ✓ Valida que o pedido não é nulo e tem ID.
- ✓ Valida que o preço não é nulo e não é negativo.
- ✓ Busca o pedido no banco e seta a referência.
- ✗ Não atualiza o `valorTotal` do pedido após adicionar o item.
- ✗ Usa `IllegalStateException` — exceção genérica.

#### `atualizarItemPedido()`
- ✓ Busca o item existente e atualiza campos.
- ✗ Não atualiza o `valorTotal` do pedido.
- ✗ Não valida os novos valores (nome pode ficar null, preço pode ficar null).

#### `excluirItemPedido()`
- ✗ **Não verifica existência** antes de excluir. `deleteById()` com ID inexistente lança `EmptyResultDataAccessException` (nas versões mais antigas do Spring Data) ou não faz nada (versões mais recentes). Comportamento depende da versão.
- ✗ Não atualiza o `valorTotal` do pedido.
- ✗ Não verifica se o pedido permite alteração (ex: pedido "PRONTO" pode ter itens removidos?).

#### `listAll()`
- ✓ `@Transactional(readOnly = true)` — boa prática.
- ✗ Lista **todos** os itens de **todos** os pedidos — pouco útil sem filtro por pedido.

#### `buscarPorId()`
- ✗ Não é `@Transactional(readOnly = true)`.
- ✗ Lança `RuntimeException` genérica.

#### Inconsistência de `@Transactional`:
- `ItemPedidoService` usa `org.springframework.transaction.annotation.Transactional`
- `PedidoService` usa `jakarta.transaction.Transactional`
- Ambas funcionam, mas a do Spring oferece mais recursos (`readOnly`, `rollbackFor`, `propagation`). Misturar as duas no mesmo projeto é confuso.

### 2.3 Services ausentes

| Entidade | Service | Status |
|---|---|---|
| Pedido | PedidoService | Parcial |
| ItemPedido | ItemPedidoService | Parcial |
| Produto | — | **Ausente** |
| Usuario | — | **Ausente** |

Sem Services para `Produto` e `Usuario`, não há como gerenciar o catálogo de produtos ou os usuários via aplicação.

---

## 3. Simulação de Falhas de Persistência

### 3.1 Criação de pedido com usuário inexistente

```json
POST /pedidos
{
  "status": "CRIADO",
  "dataHoraPedido": "2024-01-01",
  "horarioEstimadoRetirada": "12:00",
  "valorTotal": 10.00,
  "usuario": { "id": 999 },
  "itens": [ { "nome": "Coxinha", "preco": 5.00 } ]
}
```

**Fluxo:**
1. Jackson desserializa o JSON → cria `Pedido` com `Usuario(id=999)` não gerenciado
2. `PedidoService.verificarPedido()` → itens não vazio ✓
3. `pedidoRepository.save(pedido)` → Hibernate tenta `INSERT INTO pedido (..., usuario_id) VALUES (..., 999)`
4. Se `usuario_id=999` não existe → `DataIntegrityViolationException` (FK violation)
5. Sem tratamento → HTTP 500

**Hipótese alternativa:** Se o Jackson criar o `Usuario` como entidade transient (sem id=999 na sessão), o Hibernate pode tentar `INSERT` no `Usuario` → falha se houver constraint unique ou se nenhum campo obrigatório for preenchido.

### 3.2 Operação parcialmente concluída

**Cenário:** Criar pedido com 3 itens, onde o 2º item tem preço `null`.

```json
{
  "status": "CRIADO",
  "dataHoraPedido": "2024-01-01",
  "valorTotal": 15.00,
  "usuario": { "id": 1 },
  "itens": [
    { "nome": "Coxinha", "preco": 5.00 },
    { "nome": "Coca-Cola", "preco": null },
    { "nome": "Pão de queijo", "preco": 3.00 }
  ]
}
```

**Fluxo com `@Transactional`:**
1. `verificarPedido()` → itens não vazio ✓ (3 itens)
2. `pedidoRepository.save(pedido)` → Hibernate tenta persistir pedido + 3 itens (cascade ALL)
3. Item 2 tem `preco = null` → `@Column(nullable=false)` → `ConstraintViolationException`
4. `@Transactional` faz rollback → nenhum registro é salvo ✓

**Resultado:** A atomicidade é preservada pelo `@Transactional`. ✓

**Mas:** Como não há `@Valid` no controller e a validação do service é insuficiente, a exceção só ocorre na camada de persistência (banco), não na camada de aplicação. O erro retornado é uma stack trace técnica, não uma mensagem amigável.

### 3.3 Exclusão de item que pertence a pedido com cascade

**Cenário:** Excluir item via `ItemPedidoService.excluirItemPedido(id)` → `itemPedidoRepository.deleteById(id)`.

O item é excluído diretamente pelo repository, sem passar pelo pedido pai. Isso funciona, mas:
- O `valorTotal` do pedido não é atualizado.
- O `orphanRemoval` em `Pedido.itens` não é acionado (esse mecanismo só funciona quando o item é removido da coleção `pedido.getItens().remove(item)`).

---

## 4. Achados Formais

---

### QA-013

**Severidade:** CRÍTICO  
**Tipo:** BUG CONFIRMADO  
**Local:** [PedidoService.java:29-31](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/PedidoService.java#L29-L31)

**Problema:** `criarPedido()` não seta a referência `pedido` em cada `ItemPedido` antes de salvar. Quando o pedido é criado via REST API, os itens do JSON não têm a referência ao pedido pai. O `cascade` faz o Hibernate tentar inserir os itens, mas `item_pedido.pedido_id` é `nullable=false` → `ConstraintViolationException`.

**Como reproduzir:** `POST /pedidos` com corpo que inclua itens.

**Comportamento atual/provável:** `ConstraintViolationException` → HTTP 500.

**Comportamento esperado:** O service deveria iterar os itens e setar `item.setPedido(pedido)` antes do `save()`.

**Risco/impacto:** Impossível criar pedido com itens via API REST.

**Conceito envolvido:** Relacionamento bidirecional JPA, gerenciamento de referências inversas, cascade.

**Sugestão de correção conceitual:**
```java
public Pedido criarPedido(Pedido pedido) {
    verificarPedido(pedido.getItens());
    for (ItemPedido item : pedido.getItens()) {
        item.setPedido(pedido);  // setar referência inversa
    }
    return pedidoRepository.save(pedido);
}
```

**Por que a solução resolve:** O JPA precisa que ambos os lados do relacionamento bidirecional estejam consistentes. Sem `item.setPedido(pedido)`, o Hibernate não consegue gerar o `INSERT` com o `pedido_id` correto.

**Como explicar ao professor:** "Em um relacionamento bidirecional, o JPA precisa que ambos os lados apontem um para o outro. O `mappedBy` diz ao JPA que o 'dono' da relação é o `ItemPedido.pedido`. Se não setamos esse campo, o Hibernate não sabe qual `pedido_id` inserir."

---

### QA-014

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [PedidoService.java:29-31](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/PedidoService.java#L29-L31)

**Problema:** `criarPedido()` não valida se o `usuario` do pedido existe no banco. O usuário vem do JSON como objeto desserializado, não gerenciado pelo JPA. Pode causar `EntityExistsException`, `PersistenceException` ou FK violation.

**Como reproduzir:** `POST /pedidos` com `"usuario": { "id": 999 }` (ID inexistente).

**Comportamento atual/provável:** `DataIntegrityViolationException` → HTTP 500.

**Comportamento esperado:** Validar existência do usuário e retornar HTTP 400/404 com mensagem amigável.

**Risco/impacto:** Criação de pedido com usuário inexistente causa erro técnico sem explicação.

**Conceito envolvido:** Validação de integridade referencial na camada de Service, gerenciamento de entidades JPA (detached vs managed).

**Sugestão de correção conceitual:** No `criarPedido()`, buscar o usuário pelo ID com `usuarioRepository.findById()` antes de salvar. Se não encontrar, lançar exceção de domínio.

**Por que a solução resolve:** Garante que o usuário existe antes de tentar persistir o pedido, com mensagem de erro adequada.

**Como explicar ao professor:** "O JSON traz apenas o ID do usuário. Precisamos verificar no banco se esse ID existe antes de criar o pedido. Caso contrário, o banco rejeita o registro (FK violation) e o erro chega ao cliente como stack trace técnica."

---

### QA-015

**Severidade:** ALTO  
**Tipo:** BUG CONFIRMADO  
**Local:** [PedidoService.java:29-31](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/PedidoService.java#L29-L31)

**Problema:** `valorTotal` não é calculado pelo servidor. O cliente pode informar qualquer valor (inclusive zero ou negativo, caso a validação falhe). Não há lógica para calcular `valorTotal = Σ(item.preco)`.

**Como reproduzir:** `POST /pedidos` com `valorTotal: 0` e itens que somam R$ 50.

**Comportamento atual/provável:** Pedido salvo com `valorTotal = 0`.

**Comportamento esperado:** `valorTotal` calculado automaticamente pelo servidor.

**Risco/impacto:** Inconsistência financeira; pedidos com valor incorreto.

**Conceito envolvido:** Regra de negócio, integridade de dados, Single Source of Truth.

**Sugestão de correção conceitual:** No service, calcular `pedido.setValorTotal(itens.stream().map(ItemPedido::getPreco).reduce(BigDecimal.ZERO, BigDecimal::add))`.

**Por que a solução resolve:** O servidor é a fonte da verdade para cálculos financeiros. Nunca confiar no cliente.

**Como explicar ao professor:** "É como um restaurante: o cliente escolhe os pratos, mas quem calcula a conta é o caixa. Nunca confiamos no valor que o cliente diz que deve pagar."

---

### QA-016

**Severidade:** MÉDIO  
**Tipo:** RISCO POTENCIAL  
**Local:** [PedidoService.java:6](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/PedidoService.java#L6) vs [ItemPedidoService.java:10](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/ItemPedidoService.java#L10)

**Problema:** Uso inconsistente de `@Transactional` — `PedidoService` importa `jakarta.transaction.Transactional`, `ItemPedidoService` importa `org.springframework.transaction.annotation.Transactional`.

**Como reproduzir:** Análise estática.

**Comportamento atual/provável:** Ambas funcionam no Spring, mas `jakarta.transaction.Transactional` não suporta `readOnly`, `rollbackFor`, `propagation` e outros atributos do Spring.

**Comportamento esperado:** Usar consistentemente `org.springframework.transaction.annotation.Transactional`.

**Risco/impacto:** Confusão no código; impossibilidade de usar `readOnly=true` ou `rollbackFor` no PedidoService.

**Conceito envolvido:** Gerenciamento de transações, Spring Transaction Management, Jakarta EE vs Spring.

**Sugestão de correção conceitual:** Padronizar todas as classes para usar `org.springframework.transaction.annotation.Transactional`.

**Por que a solução resolve:** Unifica o gerenciamento transacional e habilita todos os recursos do Spring.

**Como explicar ao professor:** "O Spring tem sua própria anotação `@Transactional` que oferece mais recursos que a do Jakarta. Devemos usar a do Spring para ter acesso a `readOnly` (otimização de leitura) e `rollbackFor` (controle de rollback)."

---

### QA-017

**Severidade:** MÉDIO  
**Tipo:** BUG CONFIRMADO  
**Local:** [ItemPedidoService.java:55-58](file:///Users/user/Desktop/Cantina-Thiago/Cantina/src/main/java/br/com/cantina/Cantina/service/ItemPedidoService.java#L55-L58)

**Problema:** `excluirItemPedido()` não verifica existência do item antes de chamar `deleteById()`. Não atualiza o `valorTotal` do pedido pai.

**Como reproduzir:** `POST /item-pedido/deletar/999` (ID inexistente).

**Comportamento atual/provável:** Dependendo da versão do Spring Data:
- Versões antigas: `EmptyResultDataAccessException` → HTTP 500
- Versões recentes (Spring Data 3+): silenciosamente não faz nada

**Comportamento esperado:** Verificar existência e retornar erro adequado. Atualizar `valorTotal` do pedido.

**Risco/impacto:** Erro silencioso ou exceção sem tratamento; inconsistência do `valorTotal`.

**Conceito envolvido:** Validação de existência, idempotência, integridade de dados.

**Sugestão de correção conceitual:**
```java
public void excluirItemPedido(Long id) {
    ItemPedido item = itemPedidoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item não encontrado"));
    // Atualizar valorTotal do pedido pai
    itemPedidoRepository.delete(item);
}
```

**Por que a solução resolve:** Garante que o item existe, permite atualizar o pedido pai e fornece erro adequado.

**Como explicar ao professor:** "Antes de deletar, precisamos verificar se o registro existe. Além disso, como o item faz parte de um pedido, o valor total do pedido precisa ser recalculado."

---

### QA-018

**Severidade:** MÉDIO  
**Tipo:** MELHORIA  
**Local:** Todos os Services

**Problema:** Exceções genéricas (`RuntimeException`, `IllegalArgumentException`, `IllegalStateException`) usadas em vez de exceções de domínio.

**Como reproduzir:** Qualquer cenário de erro nos services.

**Comportamento atual/provável:** HTTP 500 com stack trace técnica para qualquer erro de negócio.

**Comportamento esperado:** Exceções de domínio (`PedidoNaoEncontradoException`, `ItemInvalidoException`) mapeadas para HTTP status corretos (404, 400, 422).

**Risco/impacto:** Experiência ruim do cliente da API; informação técnica exposta; impossibilidade de distinguir tipos de erro.

**Conceito envolvido:** Exception handling, `@ControllerAdvice`, `@ExceptionHandler`, exceções de domínio, HTTP status semânticos.

**Sugestão de correção conceitual:** Criar exceções como `PedidoNaoEncontradoException extends RuntimeException` e um `@RestControllerAdvice` que mapeie cada exceção para o status HTTP correto.

**Por que a solução resolve:** Separa a lógica de erro do domínio da apresentação HTTP, seguindo o princípio de responsabilidade única.

**Como explicar ao professor:** "As exceções genéricas do Java (`RuntimeException`) não carregam significado de negócio. Criamos exceções específicas (como `PedidoNaoEncontradoException`) e um handler global que traduz cada exceção para o HTTP status correto (404, 400, etc.)."

---

### QA-019

**Severidade:** BAIXO  
**Tipo:** MELHORIA  
**Local:** Todos os Repositories

**Problema:** 21 métodos customizados declarados nos repositories, nenhum utilizado pelo código.

**Como reproduzir:** Análise estática — grep por chamadas aos métodos.

**Comportamento atual/provável:** Código morto. Nenhum impacto funcional.

**Comportamento esperado:** Declarar apenas os métodos necessários, ou utilizá-los nos Services.

**Risco/impacto:** Manutenção desnecessária; falsa impressão de completude; se as entidades mudarem, esses métodos podem quebrar silenciosamente.

**Conceito envolvido:** Dead code, YAGNI (You Aren't Gonna Need It), manutenibilidade.

**Sugestão de correção conceitual:** Remover métodos não utilizados ou criar Services que os utilizem.

**Por que a solução resolve:** Código morto aumenta a complexidade sem benefício. Removê-lo simplifica a manutenção.

**Como explicar ao professor:** "Código morto é como manter ferramentas enferrujadas na caixa — não servem para nada, mas ocupam espaço e confundem quem procura as ferramentas certas. O princípio YAGNI diz: não implemente algo que não precisa agora."

---

## 5. Explicações Conceituais

### 5.1 Repository Pattern

**O que é:** Padrão de projeto que abstrai o acesso a dados, criando uma "coleção virtual" de objetos de domínio.

**Para que serve:** Desacoplar a lógica de negócio (Service) do mecanismo de persistência (JPA, JDBC, MongoDB, etc.).

**Onde aparece:** As 4 interfaces `XxxRepository extends JpaRepository<Xxx, Long>`.

**Como funciona no Spring Data:** O Spring Data JPA cria implementações automaticamente em runtime. Não precisamos escrever SQL nem implementar a interface — o Spring faz isso por nós, baseado no nome dos métodos ou em `@Query`.

**Problema que resolve:** Sem o Repository Pattern, a lógica de negócio ficaria misturada com queries SQL, dificultando testes e manutenção.

### 5.2 Service Layer

**O que é:** Camada que contém a lógica de negócio da aplicação.

**Para que serve:** Isolar regras de negócio do controller (entrada HTTP) e do repository (persistência).

**Onde aparece:** `PedidoService`, `ItemPedidoService`.

**Problema que resolve:** Sem a camada de Service, a lógica de negócio ficaria no Controller, que deveria apenas receber a requisição e delegar. Se amanhã houver uma interface desktop ou mobile, a mesma lógica poderia ser reutilizada.

### 5.3 Dependency Injection (DI) e Inversion of Control (IoC)

**O que é:**
- **IoC:** O controle da criação e gerenciamento de objetos é invertido — em vez do programador criar objetos com `new`, o framework (Spring) os cria e injeta.
- **DI:** O mecanismo pelo qual o IoC funciona — as dependências são "injetadas" via construtor, setter ou field.

**Onde aparece:** Todo service e controller usa `@Autowired` com injeção por construtor:
```java
@Autowired
public PedidoService(PedidoRepository pedidoRepository) {
    this.pedidoRepository = pedidoRepository;
}
```

**Vantagem da injeção por construtor:** Garante que todas as dependências são fornecidas na criação do objeto (não podem ser `null`). É a forma recomendada pelo Spring.

### 5.4 SOLID — Aplicação no Projeto

| Princípio | Aplicação | Observação |
|---|---|---|
| **S** — Single Responsibility | Parcial | Services têm regras de negócio, mas controllers não usam DTOs |
| **O** — Open/Closed | Não aplicável diretamente | — |
| **L** — Liskov Substitution | OK | Repositories estendem JpaRepository corretamente |
| **I** — Interface Segregation | OK | JpaRepository fornece interface adequada |
| **D** — Dependency Inversion | ✓ | Services dependem de interfaces (Repository), não de implementações concretas |

### 5.5 Coesão e Acoplamento

**Coesão (alta = bom):** Cada service trata de uma entidade. `PedidoService` trata de pedidos, `ItemPedidoService` trata de itens. Coesão razoável.

**Acoplamento (baixo = bom):** 
- ✓ Services dependem de interfaces de Repository (baixo acoplamento)
- ✗ Controllers dependem diretamente de entidades JPA (alto acoplamento — sem DTO)
- ✗ `ItemPedidoService` depende de `PedidoRepository` diretamente (deveria usar `PedidoService`?)

---

## ESTADO CONSOLIDADO — V3

### Bugs confirmados (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-001 | CRÍTICO | Serialização circular Pedido ↔ ItemPedido | 2 |
| QA-002 | CRÍTICO | Datas como String | 2 |
| QA-013 | CRÍTICO | criarPedido não seta backreference nos itens | 3 |
| QA-003 | ALTO | valorTotal como double | 2 |
| QA-004 | ALTO | Senha em texto plano | 2 |
| QA-005 | ALTO | Entidade exposta na API (inclui senha) | 2 |
| QA-006 | ALTO | Produto sem preço | 2 |
| QA-014 | ALTO | criarPedido não valida usuário | 3 |
| QA-015 | ALTO | valorTotal não calculado pelo servidor | 3 |
| QA-010 | MÉDIO | findByPreco Double vs BigDecimal | 2 |
| QA-016 | MÉDIO | @Transactional inconsistente (jakarta vs spring) | 3 |
| QA-017 | MÉDIO | excluirItemPedido sem verificar existência | 3 |

### Riscos potenciais (acumulado)

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-007 | MÉDIO | ItemPedido sem FK para Produto | 2 |
| QA-008 | MÉDIO | findByAtivo retorna singular | 2 |
| QA-009 | MÉDIO | findBySenha anti-pattern | 2 |
| QA-011 | BAIXO | Sem @Version (concorrência) | 2 |
| QA-012 | BAIXO | tempoPreparoMinutos como String | 2 |

### Melhorias

| ID | Severidade | Resumo | Fase |
|---|---|---|---|
| QA-018 | MÉDIO | Exceções genéricas | 3 |
| QA-019 | BAIXO | 21 métodos de repository não utilizados | 3 |

### Dúvidas pendentes
- D-01 a D-08 (mantidas)

### Hipóteses adotadas
- H-01 a H-05 (mantidas)
- H-06: ItemPedidoService provavelmente não atualiza valorTotal porque o valorTotal não é calculado pelo servidor.

### Itens ainda não auditados
- [ ] Análise profunda de Controller e API REST (Fase 4)
- [ ] Testes adversariais (Fase 5)
- [ ] Engenharia de Software e aprendizado (Fase 6)
- [ ] Consolidação final e preparação para banca (Fase 7)
