# QA 07 — Auditoria Final e Preparação para Banca

**Data:** 2026-08-23  
**Fase:** 7 de 7 (FINAL)  
**Base:** Estado Consolidado V6  
**Objetivo:** Consolidar a auditoria, avaliar aptidão para React e preparar para banca.

---

## 1. Fluxo Completo: Usuário → API → Banco → Resposta

### 1.1 O que o sistema já implementa

```
[Cliente HTTP]
     │
     ├── POST /pedidos ──→ PedidoController ──→ PedidoService ──→ PedidoRepository ──→ MySQL
     │                     (recebe JSON)        (valida itens)     (save)               (INSERT)
     │                     ← ResponseEntity 201 ← Pedido salvo  ← Pedido gerenciado
     │
     ├── GET /pedidos ───→ PedidoController ──→ PedidoService ──→ PedidoRepository ──→ MySQL
     │                     ← ResponseEntity 200 ← List<Pedido>  ← findAll()            (SELECT)
     │
     ├── GET /pedidos/{id} → PedidoController → PedidoService ──→ PedidoRepository ──→ MySQL
     │                     ← ResponseEntity 200 ← Pedido        ← findById()           (SELECT)
     │
     └── DELETE /pedidos/{id} → PedidoController → PedidoService → PedidoRepository ──→ MySQL
                              ← ResponseEntity 204              ← deleteById()         (DELETE)
```

### 1.2 O que o sistema NÃO implementa

- ❌ Autenticação / Login
- ❌ CRUD de Usuário via API
- ❌ CRUD de Produto via API
- ❌ Atualização de Pedido (PUT/PATCH)
- ❌ Atualização de Status do Pedido
- ❌ Cálculo automático de valor total
- ❌ Vinculação Item → Produto
- ❌ Paginação
- ❌ Busca filtrada (por status, data, usuário)
- ❌ Configuração de segurança
- ❌ Configuração de CORS
- ❌ Tratamento global de exceções
- ❌ DTOs

---

## 2. Consolidação de Bugs por Severidade (Sem Duplicação)

### 2.1 CRÍTICO (4 achados)

| ID | Tipo | Resumo | Local | Impacto |
|---|---|---|---|---|
| QA-020 | BUG CONFIRMADO | Spring Security sem configuração bloqueia todas as rotas | Projeto (ausência de SecurityFilterChain) | API completamente inacessível |
| QA-001 | BUG CONFIRMADO | Serialização circular Pedido ↔ ItemPedido | Pedido.java ↔ ItemPedido.java | StackOverflowError em qualquer GET com itens |
| QA-013 | BUG CONFIRMADO | criarPedido() não seta backreference (item.setPedido) | PedidoService.java:29-31 | Impossível criar pedido com itens |
| QA-002 | BUG CONFIRMADO | Datas/horários como String | Pedido.java:25-31 | Sem validação temporal, sem ordenação, sem comparação |

### 2.2 ALTO (9 achados)

| ID | Tipo | Resumo | Local | Impacto |
|---|---|---|---|---|
| QA-021 | BUG CONFIRMADO | Sem configuração CORS | Projeto | React não acessa a API |
| QA-022 | BUG CONFIRMADO | @Valid ausente no PedidoController | PedidoController.java:24 | Validações de Bean Validation ignoradas |
| QA-023 | BUG CONFIRMADO | Sem @ControllerAdvice | Projeto | HTTP 500 + stack trace para qualquer erro |
| QA-005 | BUG CONFIRMADO | Entidade JPA exposta na API (senha do usuário) | PedidoController + Pedido.usuario | Dados sensíveis expostos |
| QA-014 | BUG CONFIRMADO | criarPedido não valida se o usuário existe | PedidoService.java:29-31 | FK violation → HTTP 500 |
| QA-015 | BUG CONFIRMADO | valorTotal não calculado pelo servidor | PedidoService.java:29-31 | Valor financeiro incorreto |
| QA-003 | BUG CONFIRMADO | valorTotal como double (imprecisão monetária) | Pedido.java:33-35 | Erros de arredondamento em valores |
| QA-004 | BUG CONFIRMADO | Senha em texto plano | Usuario.java:32-35 | Violação de segurança |
| QA-006 | BUG CONFIRMADO | Produto sem campo de preço | Produto.java | Impossível saber preço do produto |

### 2.3 MÉDIO (7 achados)

| ID | Tipo | Resumo | Local |
|---|---|---|---|
| QA-025 | RISCO POTENCIAL | Flyway sem migrações + ddl-auto=update | pom.xml + application-dev.properties |
| QA-026 | RISCO POTENCIAL | POST pode sobrescrever registro (id no body) | PedidoController.java:24-27 |
| QA-007 | RISCO POTENCIAL | ItemPedido sem FK para Produto | ItemPedido.java |
| QA-008 | RISCO POTENCIAL | findByAtivo retorna singular (deveria ser List) | UsuarioRepository.java:16-17 |
| QA-009 | RISCO POTENCIAL | findBySenha — anti-pattern de segurança | UsuarioRepository.java:19-20 |
| QA-010 | BUG CONFIRMADO | findByPreco recebe Double, campo é BigDecimal | ItemPedidoRepository.java:17-18 |
| QA-016 | RISCO POTENCIAL | @Transactional inconsistente (jakarta vs spring) | PedidoService vs ItemPedidoService |
| QA-017 | BUG CONFIRMADO | excluirItemPedido sem verificar existência | ItemPedidoService.java:55-58 |
| QA-018 | MELHORIA | Exceções genéricas nos services | Services |
| QA-024 | MELHORIA | Dois paradigmas de controller (REST vs MVC) | Controllers |

### 2.4 BAIXO (3 achados)

| ID | Tipo | Resumo | Local |
|---|---|---|---|
| QA-011 | RISCO POTENCIAL | Sem @Version (controle de concorrência) | Todas as entidades |
| QA-012 | MELHORIA | tempoPreparoMinutos como String | Produto.java:34-37 |
| QA-019 | MELHORIA | 21 métodos de repository não utilizados | Todos os repositories |

### 2.5 Resumo Quantitativo

| Severidade | Bugs Confirmados | Riscos Potenciais | Melhorias | Total |
|---|---|---|---|---|
| CRÍTICO | 4 | 0 | 0 | **4** |
| ALTO | 9 | 0 | 0 | **9** |
| MÉDIO | 3 | 4 | 2 | **9** |
| BAIXO | 0 | 1 | 2 | **3** |
| **Total** | **16** | **5** | **4** | **25** |

---

## 3. Separação: Bugs, Riscos, Regras Não Definidas, Melhorias

### 3.1 Bugs Confirmados (16)

QA-001, QA-002, QA-003, QA-004, QA-005, QA-006, QA-010, QA-013, QA-014, QA-015, QA-017, QA-020, QA-021, QA-022, QA-023, QA-026

### 3.2 Riscos Potenciais (5)

QA-007, QA-008, QA-009, QA-011, QA-016, QA-025

### 3.3 Regras de Negócio Não Definidas

| Dúvida | Descrição |
|---|---|
| D-01 | MVC (Thymeleaf) ou REST (React)? Dois paradigmas coexistem |
| D-02 | valorTotal calculado pelo servidor ou informado pelo cliente? |
| D-03 | ItemPedido deveria referenciar Produto? |
| D-04 | Existe máquina de estados para StatusPedido? |
| D-05 | FormaPagamento será usada? Onde? |
| D-06 | Templates Thymeleaf existem? |
| D-07 | Quem pode cancelar um pedido? |
| D-08 | Produto deveria ter preço? |

### 3.4 Melhorias (4)

QA-012, QA-018, QA-019, QA-024

---

## 4. Avaliação: Backend Pronto para React?

### 4.1 Análise dos Critérios

| Critério | Status | Bloqueador? |
|---|---|---|
| Bugs críticos ou altos | 13 achados (4 críticos, 9 altos) | **SIM** |
| Falhas funcionais relevantes | Fluxo de criação de pedido não funciona | **SIM** |
| Problemas de contrato da API | Sem DTO, sem padronização de erro, sem CORS | **SIM** |
| Problemas de persistência | Backreference, serialização circular | **SIM** |
| Problemas de validação | @Valid ausente na API REST | **SIM** |
| Problemas de segurança | Spring Security bloqueia tudo; senha em texto plano | **SIM** |
| Inconsistências de regras de negócio | valorTotal não calculado; datas como String | **SIM** |
| Problemas arquiteturais bloqueadores | Dois paradigmas; sem CORS; sem exception handler | **SIM** |
| Riscos de integração prematura | Alta probabilidade de retrabalho extenso | **SIM** |

### 4.2 Classificação

## **NÃO APTO**

O backend apresenta **4 bugs críticos** e **9 bugs altos** que impedem seu funcionamento básico. O fluxo principal de criação de pedido não funciona de ponta a ponta. A API é inacessível (Spring Security bloqueia tudo), e mesmo que fosse acessível, a serialização circular impede o retorno de JSON válido. Iniciar o React agora resultaria em retrabalho extenso de ambos os lados.

### 4.3 Fatores Determinantes

1. **Spring Security sem configuração** — a API é completamente inacessível. Nenhuma requisição chega aos controllers.
2. **Serialização circular** — qualquer pedido com itens causa StackOverflowError. O React não receberia JSON válido.
3. **Backreference não setada** — impossível criar pedido com itens. O endpoint mais importante não funciona.
4. **Sem CORS** — mesmo resolvendo os problemas acima, o browser bloquearia todas as requisições do React.
5. **Sem tratamento de exceções** — o React receberia HTTP 500 + stack trace Java para qualquer erro.
6. **Sem DTO** — senha do usuário exposta na API; contrato acoplado à entidade.
7. **Entidades incompletas** — Produto sem preço, ItemPedido sem FK para Produto, datas como String.
8. **Metade dos endpoints é MVC** — ItemPedidoController retorna HTML, não JSON.
9. **Sem CRUD de Produto e Usuário** — React não consegue listar produtos ou gerenciar usuários.

### 4.4 Correções Mínimas para Atingir "APTO COM RESSALVAS"

Em ordem de prioridade:

1. Configurar Spring Security (permitir rotas públicas ou desabilitar para desenvolvimento)
2. Resolver serialização circular (`@JsonBackReference` ou DTOs)
3. Setar backreference nos itens do pedido (`item.setPedido(pedido)`)
4. Configurar CORS
5. Criar `@RestControllerAdvice` para tratamento de exceções
6. Adicionar `@Valid` no `PedidoController`
7. Criar DTOs básicos (pelo menos sem senha)
8. Converter `ItemPedidoController` para `@RestController`

---

## 5. Conceitos Presentes no Projeto

### 5.1 Java e Orientação a Objetos

| Conceito | Presente? | Onde |
|---|---|---|
| Classes e Objetos | ✓ | Todas as entidades e serviços |
| Encapsulamento | ✓ | Campos private + getters/setters |
| Abstração | ✓ | JpaRepository, Service Layer |
| Herança | Indireta | Repositories extends JpaRepository |
| Polimorfismo | Indireta | Implementação dinâmica de Repository |
| Composição | ✓ | Pedido HAS-A List<ItemPedido> |
| Enums | ✓ | StatusPedido, CategoriaProduto, Perfil, FormaPagamento |
| Generics | ✓ | List<>, JpaRepository<Pedido, Long> |
| BigDecimal | Parcial | ItemPedido.preco (✓), Pedido.valorTotal (✗ usa double) |

### 5.2 Spring Boot

| Conceito | Presente? | Onde |
|---|---|---|
| Auto-configuração | ✓ | @SpringBootApplication |
| Dependency Injection | ✓ | @Autowired por construtor |
| IoC Container | ✓ | Spring gerencia todos os beans |
| Profiles | ✓ | dev profile |
| Bean Validation | Parcial | Anotações declaradas, @Valid só no MVC |
| Spring Security | Dependência | Sem configuração |
| WebSocket | Dependência | Sem uso |

### 5.3 JPA / Hibernate / Banco

| Conceito | Presente? | Onde |
|---|---|---|
| ORM | ✓ | @Entity mapeada para tabela |
| Chave Primária | ✓ | @Id + @GeneratedValue(IDENTITY) |
| Chave Estrangeira | ✓ | @JoinColumn (usuario_id, pedido_id) |
| Relacionamento 1:N | ✓ | Pedido → ItemPedido, Usuario → Pedido |
| Cascade | ✓ | CascadeType.ALL em Pedido.itens |
| orphanRemoval | ✓ | Em Pedido.itens |
| EAGER/LAZY | Padrão | ManyToOne EAGER, OneToMany LAZY |
| @Transactional | ✓ | Nos Services |
| JPQL | ✓ | @Query nos Repositories |
| ddl-auto | ✓ | update (dev) |

### 5.4 REST / HTTP / API

| Conceito | Presente? | Onde |
|---|---|---|
| @RestController | ✓ | PedidoController |
| Verbos HTTP | Parcial | GET, POST, DELETE (falta PUT/PATCH) |
| Status HTTP | ✓ | 200, 201, 204 |
| ResponseEntity | ✓ | PedidoController |
| JSON | ✓ | Jackson (auto-configurado) |
| DTO | ❌ | Ausente |
| CORS | ❌ | Ausente |
| Paginação | ❌ | Ausente |

### 5.5 Engenharia de Software

| Conceito | Presente? | Onde |
|---|---|---|
| Arquitetura em Camadas | ✓ | Controller → Service → Repository |
| Repository Pattern | ✓ | JpaRepository |
| Service Layer | ✓ | PedidoService, ItemPedidoService |
| MVC | ✓ | ItemPedidoController |
| SOLID (DIP) | ✓ | Services dependem de interfaces |
| Separation of Concerns | Parcial | Sem DTO = acoplamento API ↔ Entity |

---

## 6. Para Cada Conceito Importante (com Código Real)

### 6.1 Injeção de Dependência

```java
// PedidoController.java
@Autowired
public PedidoController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
}
```

O Spring cria `PedidoRepository` → injeta no `PedidoService` → injeta no `PedidoController`. O desenvolvedor nunca escreve `new PedidoService(new PedidoRepositoryImpl())`.

### 6.2 Repository Pattern com Spring Data

```java
// PedidoRepository.java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @Query("SELECT p FROM Pedido p WHERE p.status = :status")
    List<Pedido> findByStatus(@Param("status") StatusPedido status);
}
```

O Spring Data cria a implementação automaticamente. O `save()`, `findById()`, `findAll()`, `deleteById()` são herdados. Queries customizadas podem ser declaradas via `@Query` ou derived query methods.

### 6.3 Transações

```java
// PedidoService.java
@Transactional
public Pedido criarPedido(Pedido pedido) {
    verificarPedido(pedido.getItens());
    return pedidoRepository.save(pedido);
}
```

Se `save()` falhar, a transação faz rollback automaticamente. Nada é persistido parcialmente.

### 6.4 Validação com Bean Validation

```java
// ItemPedidoController.java
@PostMapping("/criar")
public String criarItemPedido(@Valid @ModelAttribute ItemPedido itemPedido, 
                               BindingResult resultado, ...) {
    if (resultado.hasErrors()) {
        return "item-pedido/criar";  // volta ao formulário com erros
    }
}
```

`@Valid` aciona as anotações (`@NotBlank`, `@Size`, `@DecimalMin`) antes de entrar no método.

### 6.5 Mapeamento JPA

```java
// Pedido.java
@Entity(name = "Pedido")
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
}
```

`@Entity` + `@Table` → mapeia para tabela `pedido`. `@JoinColumn` → cria FK `usuario_id`. `@OneToMany(cascade=ALL)` → operações propagam para os itens.

---

## 7. Perguntas de Banca (30+)

### FÁCIL (10 perguntas)

**P1. O que é Spring Boot e qual sua vantagem sobre o Spring tradicional?**
> **Resposta curta:** Spring Boot é um framework que auto-configura o Spring, eliminando configuração manual de XML/Java.
> **Aprofundamento:** O Spring tradicional requer dezenas de arquivos de configuração para definir beans, datasource, transaction manager, etc. O Spring Boot usa `@SpringBootApplication` que habilita auto-configuração, component scan e configuração baseada em convenções. No nosso projeto, basta declarar `spring.datasource.url` e o Spring Boot configura automaticamente o JPA, o Hibernate e o pool de conexões.

**P2. O que é uma entidade JPA?**
> **Resposta curta:** Uma classe Java anotada com `@Entity` que representa uma tabela no banco de dados.
> **Aprofundamento:** Cada instância da classe corresponde a uma linha na tabela. Os campos mapeiam para colunas. No nosso projeto, `Pedido`, `ItemPedido`, `Produto` e `Usuario` são entidades. O Hibernate cria/atualiza as tabelas automaticamente (`ddl-auto=update`) baseado nessas anotações.

**P3. O que é `@GeneratedValue(strategy = GenerationType.IDENTITY)`?**
> **Resposta curta:** Diz ao JPA que o ID é gerado automaticamente pelo banco (auto-incremento no MySQL).
> **Aprofundamento:** `IDENTITY` delega a geração do ID ao banco de dados (cláusula `AUTO_INCREMENT` no MySQL). Outras estratégias: `SEQUENCE` (usa sequência do banco, comum em PostgreSQL/Oracle), `TABLE` (tabela auxiliar de IDs) e `AUTO` (o JPA decide). No MySQL, `IDENTITY` é a mais eficiente.

**P4. Qual a diferença entre `@Controller` e `@RestController`?**
> **Resposta curta:** `@Controller` retorna views (HTML). `@RestController` retorna dados (JSON/XML) diretamente.
> **Aprofundamento:** `@RestController` = `@Controller` + `@ResponseBody`. O `@ResponseBody` diz ao Spring para serializar o retorno do método diretamente como corpo da resposta HTTP (geralmente JSON), em vez de procurar uma view com aquele nome. No nosso projeto, `PedidoController` usa `@RestController` (retorna JSON) e `ItemPedidoController` usa `@Controller` (retorna nomes de views Thymeleaf).

**P5. O que é `ResponseEntity` e por que usamos?**
> **Resposta curta:** É um wrapper que permite controlar o status HTTP, headers e corpo da resposta.
> **Aprofundamento:** Sem `ResponseEntity`, o Spring retorna sempre HTTP 200. Com ele, podemos retornar `ResponseEntity.status(201).body(pedido)` para criação ou `ResponseEntity.noContent().build()` (204) para deleção. Isso é importante para o REST semântico.

**P6. O que é `@Autowired`?**
> **Resposta curta:** Diz ao Spring para injetar automaticamente a dependência.
> **Aprofundamento:** Quando o Spring cria o `PedidoController`, vê que o construtor precisa de `PedidoService`. Ele busca no seu container um bean do tipo `PedidoService` e injeta automaticamente. É o mecanismo de Dependency Injection.

**P7. O que são enums e onde são usados no projeto?**
> **Resposta curta:** Enums são tipos com valores fixos e pré-definidos. No projeto: `StatusPedido` (CRIADO, EM_PREPARO, PRONTO), `CategoriaProduto`, `Perfil` e `FormaPagamento`.
> **Aprofundamento:** `@Enumerated(EnumType.STRING)` armazena o nome do enum como texto no banco (ex: "CRIADO"). A alternativa `EnumType.ORDINAL` armazenaria o índice numérico (0, 1, 2), que é frágil — se alguém reordenar os valores, os dados existentes ficam incorretos.

**P8. O que é `nullable = false` no `@Column`?**
> **Resposta curta:** Gera uma constraint `NOT NULL` na coluna do banco, impedindo valores nulos.
> **Aprofundamento:** É a última linha de defesa. Mesmo que a aplicação falhe em validar, o banco rejeita o `INSERT`. No projeto, campos obrigatórios como `nome`, `status` e `usuario_id` são `nullable = false`.

**P9. O que é `cascade = CascadeType.ALL`?**
> **Resposta curta:** Propaga todas as operações JPA (persist, merge, remove, etc.) do pai para os filhos.
> **Aprofundamento:** Quando salvamos um `Pedido`, os `ItemPedido` da lista são salvos automaticamente. Quando deletamos o `Pedido`, os itens são deletados. Sem cascade, teríamos que salvar cada item separadamente.

**P10. O que é `orphanRemoval = true`?**
> **Resposta curta:** Quando um item é removido da coleção do pai (`pedido.getItens().remove(item)`), ele é automaticamente deletado do banco.
> **Aprofundamento:** Sem `orphanRemoval`, o item seria desassociado do pedido mas continuaria existindo no banco — um "órfão". Com `orphanRemoval=true`, o JPA garante que itens sem pai são removidos automaticamente.

---

### INTERMEDIÁRIO (10 perguntas)

**P11. Explique o fluxo completo de uma requisição `POST /pedidos`: do browser até o banco e de volta.**
> **Resposta curta:** Request HTTP → DispatcherServlet → PedidoController → PedidoService → PedidoRepository → Hibernate → JDBC → MySQL → Response JSON.
> **Aprofundamento:** (1) O browser envia POST com JSON. (2) O DispatcherServlet do Spring MVC recebe e roteia para `PedidoController.criarPedido()` baseado na URL e método HTTP. (3) O Jackson desserializa o JSON para objeto `Pedido`. (4) O controller delega para `PedidoService.criarPedido()`. (5) O service valida e chama `pedidoRepository.save()`. (6) O Hibernate gera `INSERT INTO pedido (...)`. (7) O JDBC envia o SQL para o MySQL. (8) O MySQL executa e retorna o ID gerado. (9) O Hibernate popula o ID no objeto. (10) O Jackson serializa o `Pedido` para JSON. (11) O Spring retorna `ResponseEntity` com status 201 e o JSON.

**P12. Qual a diferença entre `@ManyToOne` e `@OneToMany` e como eles se relacionam?**
> **Resposta curta:** `@ManyToOne` fica no lado "muitos" (onde está a FK). `@OneToMany` fica no lado "um" (a coleção). Juntos formam um relacionamento bidirecional.
> **Aprofundamento:** No nosso projeto, `ItemPedido` tem `@ManyToOne Pedido pedido` (cada item pertence a um pedido) e `Pedido` tem `@OneToMany List<ItemPedido> itens` (cada pedido tem vários itens). O `mappedBy = "pedido"` em `@OneToMany` diz ao JPA que a FK está no lado do `ItemPedido`. O "dono" da relação é quem tem a `@JoinColumn` — neste caso, `ItemPedido`.

**P13. O que é `@Transactional` e o que acontece se não usarmos?**
> **Resposta curta:** Garante atomicidade: ou tudo é salvo ou nada é. Sem ela, operações parciais podem ser persistidas.
> **Aprofundamento:** No `criarPedido()`, se salvarmos o pedido mas a inserção de um item falhar, sem `@Transactional` o pedido ficaria salvo sem aquele item — dados inconsistentes. Com `@Transactional`, o Spring abre uma transação antes do método e faz commit no final. Se houver exceção, faz rollback de tudo.

**P14. Por que `double` é problemático para valores monetários?**
> **Resposta curta:** `double` usa ponto flutuante (IEEE 754). `0.1 + 0.2 ≠ 0.3` em double. Para dinheiro, usamos `BigDecimal`.
> **Aprofundamento:** O computador armazena `double` em base 2. O número `0.1` em binário é `0.000110011001100...` (dízima periódica infinita, que é truncada). Em uma cantina com 200 vendas por dia, erros de centavos se acumulam. `BigDecimal` armazena os dígitos decimais exatos, garantindo que `new BigDecimal("0.1").add(new BigDecimal("0.2")).equals(new BigDecimal("0.3"))` é `true`.

**P15. O que é CORS e por que o React não funciona sem configurá-lo?**
> **Resposta curta:** CORS é um mecanismo de segurança do browser que bloqueia requisições entre origens diferentes (ex: localhost:3000 → localhost:8080).
> **Aprofundamento:** A Same-Origin Policy do browser impede que um site acesse recursos de outro domínio/porta. O React roda em `localhost:3000` e a API em `localhost:8080` — são origens diferentes. O browser envia um "preflight" (OPTIONS) para perguntar se o servidor aceita a origem. Se o servidor não responder com headers CORS (`Access-Control-Allow-Origin`), o browser bloqueia a requisição real.

**P16. O que é o Repository Pattern e qual vantagem ele traz?**
> **Resposta curta:** Abstrai o acesso a dados como uma "coleção em memória". O Service não sabe se os dados vêm de MySQL, MongoDB ou arquivo.
> **Aprofundamento:** No nosso projeto, `PedidoRepository extends JpaRepository<Pedido, Long>` expõe métodos como `save()`, `findById()`, `findAll()`. O `PedidoService` usa esses métodos sem saber que por trás há Hibernate gerando SQL para MySQL. Se trocarmos para PostgreSQL, nenhuma linha do Service muda.

**P17. O que é `@Valid` e por que é importante?**
> **Resposta curta:** Aciona a validação das anotações (`@NotNull`, `@Size`, etc.) antes de processar a requisição.
> **Aprofundamento:** Sem `@Valid`, as anotações de validação na entidade são ignoradas. O `PedidoController` não usa `@Valid`, então é possível criar pedido com status `null`, data vazia e valor negativo. A validação só falha quando o JPA tenta inserir no banco (`@Column nullable=false`), resultando em erro técnico (HTTP 500) em vez de mensagem amigável (HTTP 400).

**P18. Qual a diferença entre EAGER e LAZY loading e como isso afeta o projeto?**
> **Resposta curta:** EAGER carrega o relacionamento imediatamente. LAZY carrega apenas quando acessado.
> **Aprofundamento:** No nosso projeto, `Pedido.usuario` é `@ManyToOne` (EAGER por padrão) — ao carregar um pedido, o usuário vem junto, incluindo a senha. `Pedido.itens` é `@OneToMany` (LAZY por padrão) — os itens só são carregados quando `pedido.getItens()` é chamado. EAGER pode causar problemas de performance (carregar dados desnecessários) e segurança (carregar dados sensíveis).

**P19. O que é um DTO e por que o projeto deveria ter?**
> **Resposta curta:** DTO é um objeto simples para transferir dados entre camadas, controlando exatamente o que é exposto.
> **Aprofundamento:** Sem DTO, a entidade JPA vai direto para a API. Isso expõe a senha do usuário, causa serialização circular, e acopla o contrato da API à estrutura do banco. Com DTO: `PedidoResponseDTO` teria `id`, `status`, `valorTotal`, `nomeUsuario` (sem senha). `PedidoRequestDTO` teria `statusId`, `usuarioId`, `itens[]` (sem `id`). DTOs desacoplam as camadas.

**P20. O que é Dependency Inversion Principle (DIP) e como ele aparece no projeto?**
> **Resposta curta:** Módulos de alto nível dependem de abstrações (interfaces), não de implementações concretas.
> **Aprofundamento:** O `PedidoService` depende da interface `PedidoRepository`, não da implementação concreta que o Spring Data cria em runtime. Se trocarmos o Spring Data JPA por outra implementação que respeite a mesma interface, o Service não muda. Isso é o DIP em ação.

---

### DIFÍCIL (10 perguntas)

**P21. O que acontece internamente quando vocês chamam `pedidoRepository.save(pedido)`?**
> **Resposta curta:** O Spring Data verifica se a entidade tem ID. Se não tem, faz `persist` (INSERT). Se tem, faz `merge` (UPDATE). O Hibernate gera o SQL, abre uma PreparedStatement via JDBC e executa no MySQL.
> **Aprofundamento:** (1) `save()` delega para `SimpleJpaRepository.save()`. (2) Verifica `entityInformation.isNew(entity)` — se o ID é null, é nova. (3) Se nova: `entityManager.persist(entity)` → Hibernate adiciona ao Persistence Context (cache L1) → no flush (ou commit), gera `INSERT INTO pedido (...) VALUES (?)` → JDBC executa → MySQL retorna ID gerado → Hibernate seta no objeto. (4) Se existente: `entityManager.merge(entity)` → Hibernate faz `SELECT` para carregar o estado atual → compara → gera `UPDATE`. (5) O cascade propaga para os itens.

**P22. Explique o problema de serialização circular do projeto e como resolvê-lo.**
> **Resposta curta:** `Pedido` serializa `itens`, cada `ItemPedido` serializa `pedido`, que serializa `itens` de novo → loop infinito → StackOverflowError.
> **Aprofundamento:** O Jackson percorre o grafo de objetos para gerar JSON. `Pedido` tem campo `itens` → entra no array → cada `ItemPedido` tem campo `pedido` → volta para o `Pedido` → entra em `itens` de novo → recursão infinita. Soluções: (1) `@JsonBackReference` no `ItemPedido.pedido` — o Jackson ignora esse lado. (2) `@JsonIgnore` no campo — mais simples mas menos flexível. (3) DTOs — a solução arquitetural correta, que elimina o problema por construção.

**P23. Se vocês recebem um POST com um ID que já existe no banco, o que acontece? Por que isso é um problema?**
> **Resposta curta:** `save()` faz `merge` em vez de `persist` — atualiza o registro existente em vez de criar um novo. POST deveria sempre criar, não atualizar.
> **Aprofundamento:** No JPA, `save()` usa `isNew()` para decidir entre `persist` e `merge`. Se o JSON traz `"id": 1` e já existe pedido com id=1, o JPA faz merge (UPDATE). Isso viola a semântica REST: POST = criar, PUT = atualizar. A solução é usar DTO sem campo `id` para criação, ou ignorar o `id` explicitamente no Service.

**P24. O que acontece se dois usuários tentarem atualizar o mesmo pedido ao mesmo tempo? Como prevenir?**
> **Resposta curta:** Lost Update — o segundo sobrescreve o primeiro sem aviso. Prevenção: `@Version` (optimistic locking).
> **Aprofundamento:** (1) User A lê pedido (version=1). (2) User B lê pedido (version=1). (3) User A atualiza → `UPDATE ... WHERE id=1 AND version=1` → sucesso → version=2. (4) User B tenta atualizar → `UPDATE ... WHERE id=1 AND version=1` → 0 rows affected → `OptimisticLockException`. O `@Version` adiciona uma coluna de versão que o Hibernate verifica automaticamente em cada UPDATE.

**P25. Por que `spring.jpa.hibernate.ddl-auto=update` é perigoso em produção?**
> **Resposta curta:** O Hibernate pode alterar o schema do banco automaticamente e irreversivelmente. Ele adiciona colunas/tabelas mas nunca remove.
> **Aprofundamento:** `update` compara o modelo JPA com o banco e aplica `ALTER TABLE` automaticamente. Problemas: (1) Nunca faz DROP COLUMN — colunas removidas do código continuam no banco. (2) Pode criar índices ou constraints inesperados. (3) Não é idempotente — falhas podem deixar o schema em estado inconsistente. (4) Não é versionado — ninguém sabe quais alterações foram feitas. Em produção, usar Flyway com migrações versionadas.

**P26. Explique como o Spring Security protege a aplicação por padrão e por que a API não funciona sem configuração.**
> **Resposta curta:** O Spring Security adiciona um filtro HTTP que exige autenticação para todas as rotas. Sem configuração, gera uma senha aleatória e redireciona para `/login`.
> **Aprofundamento:** O Spring Boot auto-configura `SecurityAutoConfiguration` que cria um `SecurityFilterChain` padrão: (1) Todas as rotas exigem autenticação. (2) Gera um usuário `user` com senha aleatória no console. (3) Habilita HTTP Basic e form login. Para resolver, criamos uma classe `@Configuration` com `@Bean SecurityFilterChain` que define quais rotas são públicas (`.permitAll()`), quais exigem autenticação, e como a autenticação funciona (JWT, sessão, etc.).

**P27. O que é o Persistence Context (cache L1) do JPA e como ele afeta o código?**
> **Resposta curta:** É um cache dentro da transação que garante que cada entidade é representada por uma única instância Java. Se buscarmos o mesmo pedido duas vezes na mesma transação, recebemos o mesmo objeto.
> **Aprofundamento:** O EntityManager mantém o Persistence Context (cache L1) durante a transação. Quando fazemos `findById(1)`, a entidade entra no cache. Se chamarmos `findById(1)` novamente, o JPA retorna o mesmo objeto (sem nova query). No flush, o Hibernate compara o estado atual dos objetos no cache com o estado original e gera UPDATEs automaticamente (dirty checking). Isso é transparente para o desenvolvedor.

**P28. Vocês usam Bean Validation no projeto. Explique a diferença entre validação no controller, no service e no banco.**
> **Resposta curta:** São três camadas de defesa: controller valida a entrada HTTP (rapidez, mensagem amigável), service valida regras de negócio (lógica), banco valida integridade (última barreira).
> **Aprofundamento:** No nosso projeto: (1) Controller com `@Valid` — valida `@NotNull`, `@Size`, etc. antes de processar. Retorna HTTP 400 com lista de erros. (2) Service com validação programática — `verificarPedido()` valida que o pedido tem itens. Retorna exceção de negócio. (3) Banco com `@Column(nullable=false)` — rejeita INSERT/UPDATE inválido. Retorna `ConstraintViolationException`. As três camadas são complementares; no nosso projeto, a primeira (controller) está ausente no `PedidoController`.

**P29. Vocês usam `@Query` nos repositories. Quando usar `@Query` e quando usar derived query methods?**
> **Resposta curta:** Derived queries (`findByNome`) são geradas pelo Spring Data a partir do nome do método. `@Query` é necessário para queries complexas (joins, agregações, subqueries).
> **Aprofundamento:** No nosso projeto, todas as `@Query` poderiam ser substituídas por derived queries: `findByPedidoId(Long pedidoId)` geraria `SELECT i FROM ItemPedido i WHERE i.pedido.id = ?1` automaticamente. Derived queries são mais simples e menos propensas a erros de JPQL. `@Query` é útil quando a query é complexa: `@Query("SELECT p FROM Pedido p JOIN p.itens i WHERE i.nome LIKE %:nome% GROUP BY p HAVING COUNT(i) > 3")`.

**P30. Se o professor perguntar: 'o código deste projeto está pronto para produção?', como vocês responderiam?**
> **Resposta curta:** "Não. O projeto demonstra os conceitos de Java, Spring Boot, JPA e arquitetura em camadas, mas tem problemas que precisam ser resolvidos antes de produção."
> **Aprofundamento:** Os principais problemas para produção são: (1) Spring Security sem configuração. (2) Senha em texto plano. (3) Serialização circular. (4) Sem DTO — dados sensíveis expostos. (5) Sem tratamento de exceções. (6) Sem CORS. (7) ddl-auto=update. (8) Senha do banco hardcoded. (9) Sem testes. No entanto, o projeto demonstra corretamente: arquitetura em camadas, DI, JPA, enums, validação, transações e REST API.

---

### PERGUNTAS QUE TESTAM COMPREENSÃO REAL (5 perguntas)

**P31. Se eu mudar o `@GeneratedValue(IDENTITY)` para `@GeneratedValue(SEQUENCE)`, o que muda?**
> O MySQL não suporta SEQUENCE nativamente (a partir do MySQL 8.0.17, suporta parcialmente). O Hibernate criaria uma tabela auxiliar para simular a sequência, ou falharia na inicialização. No PostgreSQL/Oracle, SEQUENCE é mais performático que IDENTITY porque permite batching de INSERTs.

**P32. Se eu remover o `cascade = CascadeType.ALL` do `Pedido.itens`, o que acontece ao salvar um pedido com itens?**
> Os itens NÃO seriam salvos junto com o pedido. Teríamos que salvar cada item separadamente via `itemPedidoRepository.save(item)` APÓS salvar o pedido e setar `item.setPedido(pedido)`.

**P33. Se eu trocar `@Enumerated(EnumType.STRING)` por `@Enumerated(EnumType.ORDINAL)` no StatusPedido, quais são os riscos?**
> Os valores seriam armazenados como números (0=CRIADO, 1=EM_PREPARO, 2=PRONTO). Se alguém adicionar um novo status antes de "PRONTO" (ex: CANCELADO entre EM_PREPARO e PRONTO), todos os pedidos "PRONTO" passariam a ser "CANCELADO" no banco — corrupção de dados silenciosa.

**P34. Se eu remover o `orphanRemoval = true` do `Pedido.itens` e fizer `pedido.getItens().clear()`, o que acontece?**
> Os itens seriam desassociados do pedido (campo `pedido_id` ficaria `null` se fosse nullable), mas permaneceriam no banco como registros "órfãos". Com `nullable=false`, o JPA tentaria setar `pedido_id = null` e o banco rejeitaria com `ConstraintViolationException`.

**P35. O que acontece se dois métodos `@Transactional` chamarem um ao outro? A transação é a mesma ou são duas?**
> Depende da `propagation`. O padrão é `REQUIRED`: se já existe uma transação, o segundo método participa da mesma. Se não existe, cria uma nova. Se o segundo método falhar, toda a transação (incluindo o primeiro) faz rollback. Com `REQUIRES_NEW`, o segundo método criaria uma transação independente.

---

## 8. Plano de Estudo por Prioridade

### 🔴 Prioridade 1 — Essencial para a Banca

1. **Arquitetura em camadas** — saber explicar o papel de cada camada
2. **JPA e Hibernate** — entender @Entity, @ManyToOne, @OneToMany, cascade, orphanRemoval
3. **Spring Boot DI** — explicar @Autowired, @Service, @Repository, IoC
4. **@Transactional** — atomicidade, commit, rollback
5. **REST API** — verbos HTTP, status codes, @RestController vs @Controller
6. **Bean Validation** — @Valid, @NotNull, @Size, e por que importa

### 🟡 Prioridade 2 — Importante para Perguntas Intermediárias

7. **CORS** — Same-Origin Policy, por que o React precisa
8. **DTO** — por que não expor entidades, como desacoplar
9. **SOLID** — principalmente SRP e DIP (que aparecem no código)
10. **Repository Pattern** — abstração de dados
11. **EAGER vs LAZY** — como afeta performance e o que é carregado
12. **BigDecimal vs double** — por que importa para dinheiro

### 🟢 Prioridade 3 — Diferencial para Nota Máxima

13. **Spring Security** — por que bloqueia tudo, como configurar
14. **Serialização circular** — @JsonBackReference/@JsonManagedReference
15. **Persistence Context** — cache L1, dirty checking
16. **Concorrência** — @Version, Optimistic Locking
17. **Flyway vs ddl-auto** — gerenciamento de schema
18. **Exception Handling** — @ControllerAdvice, @ExceptionHandler

---

## 9. Classificação Final do Backend

### **NÃO APTO** para iniciar o React

**Justificativa resumida:**
- 4 bugs CRÍTICOS que impedem o funcionamento básico
- 9 bugs ALTOS que comprometem segurança, validação e contrato da API
- O fluxo principal (criar pedido) não funciona de ponta a ponta
- API inacessível (Spring Security) e incompatível com React (sem CORS)
- Dados sensíveis expostos (senha em texto plano na API)
- Metade dos endpoints é MVC (Thymeleaf), não REST
- Sem CRUD de Produto e Usuário

**As 8 correções prioritárias para mudar a classificação estão detalhadas na seção 4.4.**

---

## 10. Mensagem Final

| Métrica | Quantidade |
|---|---|
| Bugs CRÍTICOS | 4 |
| Bugs ALTOS | 9 |
| Bugs MÉDIOS | 3 |
| Riscos potenciais | 6 |
| Melhorias | 4 |
| **Total de achados** | **25** (sem duplicação entre fases) |

### Principais 5 Riscos

1. **API completamente inacessível** — Spring Security bloqueia tudo
2. **API retorna erro fatal** — Serialização circular causa StackOverflow
3. **Endpoint principal não funciona** — Criar pedido falha por backreference
4. **Dados sensíveis expostos** — Senha em texto plano retornada na API
5. **Integração com React bloqueada** — Sem CORS, sem DTOs, sem tratamento de erros

### Status do Backend para React

## **NÃO APTO**

Existem problemas relevantes que devem ser tratados antes da integração com React.

### Relatórios Completos

Os detalhes completos da auditoria estão em:

- [`docs/qa/01-mapeamento.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/01-mapeamento.md) — Mapeamento Geral
- [`docs/qa/02-modelo-banco.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/02-modelo-banco.md) — Entities, JPA e Banco
- [`docs/qa/03-repository-service.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/03-repository-service.md) — Repository e Service
- [`docs/qa/04-controller-api.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/04-controller-api.md) — Controllers e API REST
- [`docs/qa/05-break-system.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/05-break-system.md) — Testes Adversariais
- [`docs/qa/06-engenharia-software.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/06-engenharia-software.md) — Engenharia de Software
- [`docs/qa/07-auditoria-final.md`](file:///Users/user/Desktop/Cantina-Thiago/Cantina/docs/qa/07-auditoria-final.md) — Auditoria Final (este documento)
