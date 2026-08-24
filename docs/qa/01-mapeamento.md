# QA 01 — Mapeamento Geral

**Data:** 2026-08-23  
**Fase:** 1 de 7  
**Objetivo:** Entender a estrutura, arquitetura, fluxos e componentes do projeto antes da auditoria profunda.

---

## 1. Visão Geral do Projeto

O sistema "Cantina" é uma aplicação acadêmica de agendamento de pedidos para a cantina do SENAI. Desenvolvido em **Java 21** com **Spring Boot 4.1.0**, persistência em **MySQL** via **JPA/Hibernate**, migrações planejadas com **Flyway** (dependência presente, mas sem arquivos de migração), segurança via **Spring Security** (dependência presente, sem configuração), e interface via **Thymeleaf** (parcialmente usada).

**Autores:** Erick, Joyce, Marcus e Thiago.

---

## 2. Stack Tecnológica

| Componente | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 4.1.0 |
| Persistência | Spring Data JPA / Hibernate | (gerenciada pelo parent) |
| Banco de dados | MySQL | Runtime |
| Migrações | Flyway | (dependência presente, sem migrações) |
| Segurança | Spring Security | (dependência presente, sem configuração) |
| Interface | Thymeleaf | (parcialmente usado) |
| WebSocket | Spring WebSocket | (dependência presente, sem uso) |
| Validação | Jakarta Validation (Bean Validation) | (parcialmente usado) |
| Build | Maven | (wrapper incluso) |
| Utilitário | Lombok | (dependência presente, sem uso no código) |
| Testes | Spring Boot Test starters | (apenas contextLoads) |

---

## 3. Estrutura do Projeto

```
src/main/java/br/com/cantina/Cantina/
├── CantinaApplication.java            — Classe principal
├── controller/
│   ├── PedidoController.java          — REST API (@RestController)
│   ├── ItemPedidoController.java      — MVC/Thymeleaf (@Controller)
│   └── classe.java                    — Placeholder vazio
├── database/
│   ├── enums/
│   │   ├── CategoriaProduto.java      — BEBIDAS, SALGADOS, DOCES, PRATOS_PRONTOS, DESCARTAVEIS
│   │   ├── FormaPagamento.java        — PIX, DEBITO, CREDITO, DINHEIRO
│   │   ├── Perfil.java               — ALUNO, SECRETARIA, VISITANTE, PROFESSOR, FUNCIONARIO_CANTINA
│   │   └── StatusPedido.java         — CRIADO, EM_PREPARO, PRONTO
│   └── model/
│       ├── Pedido.java                — Entidade principal de pedido
│       ├── ItemPedido.java            — Item dentro de um pedido
│       ├── Produto.java               — Produto cadastrado na cantina
│       └── Usuario.java              — Usuário do sistema
├── exception/
│   ├── admin/classe.java              — Placeholder vazio
│   ├── carrinho/classe.java           — Placeholder vazio
│   ├── pagamento/classe.java          — Placeholder vazio
│   ├── reserva/classe.java            — Placeholder vazio
│   └── usuario/classe.java           — Placeholder vazio
├── repository/
│   ├── ItemPedidoRepository.java      — JpaRepository com queries customizadas
│   ├── PedidoRepository.java          — JpaRepository com queries customizadas
│   ├── ProdutoRepository.java         — JpaRepository com queries customizadas
│   ├── UsuarioRepository.java         — JpaRepository com queries customizadas
│   ├── entity/classe.java             — Placeholder vazio
│   ├── mapper/classe.java             — Placeholder vazio
│   └── persistence/classe.java        — Placeholder vazio
└── service/
    ├── PedidoService.java             — Lógica de negócio de pedidos
    ├── ItemPedidoService.java         — Lógica de negócio de itens de pedido
    └── Class.java                     — Placeholder vazio

src/main/resources/
├── application.properties             — Ativa perfil "dev"
└── application-dev.properties         — Conexão MySQL, ddl-auto=update

src/test/java/br/com/cantina/Cantina/
└── CantinaApplicationTests.java       — Apenas contextLoads()
```

---

## 4. Arquitetura Identificada

O projeto segue uma **arquitetura em camadas** parcialmente implementada:

```
[Cliente] → Controller → Service → Repository → [MySQL]
```

### Camadas presentes:

| Camada | Implementação | Status |
|---|---|---|
| **Controller** | `PedidoController` (REST), `ItemPedidoController` (MVC) | Parcial — dois paradigmas diferentes |
| **Service** | `PedidoService`, `ItemPedidoService` | Parcial — só Pedido e ItemPedido |
| **Repository** | 4 interfaces JpaRepository | Funcional |
| **Model/Entity** | `Pedido`, `ItemPedido`, `Produto`, `Usuario` | Funcional |
| **DTO** | Nenhum | Ausente |
| **Exception Handler** | Nenhum (pacotes vazios) | Ausente |

### Camadas ausentes ou incompletas:

- **Sem DTOs**: as entidades JPA são expostas diretamente nos controllers (tanto na API REST quanto no MVC).
- **Sem tratamento global de exceções**: não existe `@ControllerAdvice` / `@RestControllerAdvice`. Exceções como `RuntimeException` e `IllegalStateException` propagam sem tratamento.
- **Sem configuração de segurança**: Spring Security está no classpath, o que **bloqueia todas as requisições por padrão** (retorna 401/302 para login), mas não há nenhuma classe `SecurityFilterChain` ou `WebSecurityConfigurerAdapter`.
- **Sem configuração CORS**: nenhuma configuração encontrada.
- **Sem migrações Flyway**: dependência presente, mas nenhum arquivo em `db/migration`.
- **Sem Services para Produto e Usuário**: os repositórios existem, mas não há camada de negócio.
- **Sem Controllers para Produto e Usuário**: nenhuma forma de acessar essas entidades via HTTP.

---

## 5. Entidades e Relacionamentos

### Diagrama de Relacionamentos

```
┌──────────┐       ┌──────────┐       ┌─────────────┐
│ Usuario  │ 1───N │  Pedido  │ 1───N │ ItemPedido  │
└──────────┘       └──────────┘       └─────────────┘

┌──────────┐
│ Produto  │ (isolado — sem relacionamento com ItemPedido)
└──────────┘
```

### 5.1 Usuario
- **Tabela:** `usuario`
- **Campos:** id (PK, auto), nome, cpf (unique), ativo (boolean), senha, telefone, email (unique), perfil (enum)
- **Relacionamento declarado:** Nenhum do lado do Usuário; referenciado por `Pedido.usuario` (ManyToOne)
- **Observação:** Não tem `@OneToMany` para pedidos (relação unidirecional)

### 5.2 Pedido
- **Tabela:** `pedido`
- **Campos:** id (PK, auto), status (enum), dataHoraPedido (String!), horarioEstimadoRetirada (String!), valorTotal (double!), usuario_id (FK)
- **Relacionamentos:**
  - `@ManyToOne` → Usuario (obrigatório, FK `usuario_id`)
  - `@OneToMany` → List<ItemPedido> (cascade ALL, orphanRemoval true)

### 5.3 ItemPedido
- **Tabela:** `item_pedido`
- **Campos:** id (PK, auto), nome (String), preco (BigDecimal), pedido_id (FK)
- **Relacionamento:** `@ManyToOne` → Pedido (FK `pedido_id`, nullable false)
- **Observação crítica:** Não referencia `Produto`. O item tem apenas `nome` e `preco` — não está ligado ao catálogo de produtos.

### 5.4 Produto
- **Tabela:** `produto`
- **Campos:** id (PK, auto), nome, descricao, categoriaProduto (enum), tempoPreparoMinutos (String!), quantidadeDisponivelHoje (int), ativo (boolean)
- **Relacionamento:** Nenhum — entidade isolada, sem FK para/de ItemPedido ou Pedido.

---

## 6. Fluxo Principal de Dados

### 6.1 Criação de Pedido (REST API)

```
POST /pedidos  →  PedidoController.criarPedido()
                   →  PedidoService.criarPedido()
                        →  verificarPedido(itens) — valida lista não vazia
                        →  pedidoRepository.save(pedido)
                   →  ResponseEntity 201 CREATED
```

**Observação:** O pedido é recebido como entidade JPA completa (sem DTO), incluindo o `usuario` inteiro no JSON. Não há validação do usuário, não há `@Valid`, não há cálculo de `valorTotal`.

### 6.2 CRUD de ItemPedido (MVC/Thymeleaf)

```
GET  /item-pedido/listar           → lista todos
GET  /item-pedido/criar            → formulário de criação
POST /item-pedido/criar            → cria item (com @Valid)
GET  /item-pedido/editar/{id}      → formulário de edição
PUT  /item-pedido/{id}             → atualiza item (com @Valid)
POST /item-pedido/deletar/{id}     → exclui item
```

**Observação:** Este controller usa `@Controller` (MVC) em vez de `@RestController`, retorna nomes de views Thymeleaf. Não é uma API REST.

---

## 7. Endpoints Mapeados

| Método | URL | Controller | Tipo | Retorno |
|---|---|---|---|---|
| POST | `/pedidos` | PedidoController | REST | JSON (Pedido) |
| GET | `/pedidos/{id}` | PedidoController | REST | JSON (Pedido) |
| GET | `/pedidos` | PedidoController | REST | JSON (List<Pedido>) |
| DELETE | `/pedidos/{id}` | PedidoController | REST | 204 No Content |
| GET | `/item-pedido/listar` | ItemPedidoController | MVC | View Thymeleaf |
| GET | `/item-pedido/criar` | ItemPedidoController | MVC | View Thymeleaf |
| POST | `/item-pedido/criar` | ItemPedidoController | MVC | Redirect |
| GET | `/item-pedido/editar/{id}` | ItemPedidoController | MVC | View Thymeleaf |
| PUT | `/item-pedido/{id}` | ItemPedidoController | MVC | Redirect |
| POST | `/item-pedido/deletar/{id}` | ItemPedidoController | MVC | Redirect |

**Endpoints ausentes:** Nenhum CRUD para `Produto` ou `Usuario` é exposto.

---

## 8. Regras de Negócio Comprovadas no Código

| # | Regra | Local | Evidência |
|---|---|---|---|
| RN-01 | Pedido precisa conter ao menos 1 item | `PedidoService.verificarPedido()` | `if (itemPedido == null \|\| itemPedido.isEmpty())` |
| RN-02 | Status do pedido é obrigatório | `Pedido.status` | `@NotNull` |
| RN-03 | Valor total não pode ser negativo | `Pedido.valorTotal` | `@Min(0)` |
| RN-04 | Usuário é obrigatório no pedido | `Pedido.usuario` | `@NotNull` |
| RN-05 | Data/hora do pedido obrigatória | `Pedido.dataHoraPedido` | `@NotBlank` |
| RN-06 | Horário de retirada obrigatório | `Pedido.horarioEstimadoRetirada` | `@NotBlank` |
| RN-07 | Item precisa de nome (3-100 chars) | `ItemPedido.nome` | `@NotBlank`, `@Size` |
| RN-08 | Item precisa de preço ≥ 0 | `ItemPedido.preco` | `@NotNull`, `@DecimalMin("0.00")` |
| RN-09 | Item deve pertencer a um pedido | `ItemPedidoService.criarItemPedido()` | Validação programática |
| RN-10 | CPF do usuário é único | `Usuario.cpf` | `@Column(unique = true)` |
| RN-11 | Email do usuário é único | `Usuario.email` | `@Column(unique = true)` |
| RN-12 | Senha do usuário: 6-255 chars | `Usuario.senha` | `@Size(min=6, max=255)` |
| RN-13 | Exclusão lógica de usuário | `Usuario.excluir()` | `this.ativo = false` |

---

## 9. Conceitos de Java, Spring Boot, JPA e Engenharia de Software Identificados

### Java / OOP
- Classes, interfaces, enums com atributos
- Encapsulamento (getters/setters)
- Construtores com e sem argumentos
- Generics (`List<ItemPedido>`, `JpaRepository<Pedido, Long>`)
- `BigDecimal` para valores monetários (parcialmente — `Pedido.valorTotal` usa `double`)

### Spring Boot
- `@SpringBootApplication` — auto-configuração, component scan
- `@RestController` / `@Controller` — camada web
- `@Service` — camada de negócio
- `@Autowired` com injeção por construtor — Dependency Injection / IoC
- `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- `@PathVariable`, `@RequestBody`, `@ModelAttribute`
- `@Valid` — integração com Bean Validation (usado apenas no ItemPedidoController)
- `ResponseEntity` — controle de status HTTP
- Profiles (`spring.profiles.active=dev`)
- Thymeleaf (templates MVC)
- WebSocket (dependência, sem uso)

### JPA / Hibernate
- `@Entity`, `@Table` — mapeamento objeto-relacional
- `@Id`, `@GeneratedValue(IDENTITY)` — chave primária auto-incremento
- `@Column` — mapeamento de colunas (nullable, unique, length, precision, scale)
- `@ManyToOne`, `@OneToMany` — relacionamentos
- `@JoinColumn` — FK
- `@Enumerated(EnumType.STRING)` — enums como texto no banco
- `cascade = CascadeType.ALL` — operações em cascata
- `orphanRemoval = true` — remoção de órfãos
- `mappedBy` — lado inverso do relacionamento
- `JpaRepository` — padrão Repository do Spring Data
- `@Query` — JPQL customizado
- `@Transactional` — controle transacional

### Bean Validation
- `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@DecimalMin`, `@Email`

### Engenharia de Software
- Arquitetura em camadas (Controller → Service → Repository)
- Repository Pattern
- Service Layer
- Dependency Injection / Inversion of Control
- Separation of Concerns (parcial)
- MVC Pattern (parcial — ItemPedidoController)
- REST (parcial — PedidoController)

---

## 10. Suspeitas Iniciais (a investigar nas fases seguintes)

> ⚠️ As suspeitas abaixo são observações preliminares e **não foram auditadas em profundidade**. Serão verificadas nas fases 2 a 6.

### Severidade provável: CRÍTICO / ALTO

| # | Suspeita | Local |
|---|---|---|
| S-01 | **Spring Security bloqueia tudo** — dependência no classpath sem configuração; por padrão, todas as rotas exigem autenticação | `pom.xml` + ausência de `SecurityFilterChain` |
| S-02 | **Flyway falha na inicialização** — dependência presente sem diretório `db/migration`; pode impedir o startup | `pom.xml` + `application-dev.properties` |
| S-03 | **Entidade exposta na API REST sem DTO** — `PedidoController` recebe/retorna `Pedido` (entidade JPA), expondo estrutura interna e senha do usuário na serialização | `PedidoController.java` |
| S-04 | **Serialização circular** — `Pedido ↔ ItemPedido` (bidirecional) causa `StackOverflowError` ou loop infinito no Jackson | `Pedido.itens` ↔ `ItemPedido.pedido` |
| S-05 | **`valorTotal` usa `double`** — imprecisão de ponto flutuante para valores monetários; `ItemPedido.preco` usa `BigDecimal` corretamente, mas `Pedido.valorTotal` não | `Pedido.java` |
| S-06 | **Datas como `String`** — `dataHoraPedido` e `horarioEstimadoRetirada` são `String`, impossibilitando validações, comparações e ordenações temporais no banco | `Pedido.java` |
| S-07 | **`@Valid` não usado no PedidoController** — as anotações de validação da entidade `Pedido` nunca são acionadas na API REST | `PedidoController.criarPedido()` |
| S-08 | **Sem `@Valid` no `@RequestBody`** — o pedido pode ser criado com campos nulos/inválidos | `PedidoController.java:24` |
| S-09 | **`ItemPedido` não referencia `Produto`** — item do pedido é apenas nome/preço, sem FK para o catálogo de produtos | `ItemPedido.java` |
| S-10 | **`valorTotal` não é calculado** — é recebido do cliente, nunca calculado pelo servidor a partir dos itens | `PedidoService.criarPedido()` |

### Severidade provável: MÉDIO / BAIXO

| # | Suspeita | Local |
|---|---|---|
| S-11 | **Dois paradigmas de controller** — `PedidoController` é REST; `ItemPedidoController` é MVC/Thymeleaf; inconsistência arquitetural | Controllers |
| S-12 | **Classes placeholder vazias** — `classe.java`, `Class.java` em vários pacotes; lixo no projeto | Múltiplos pacotes |
| S-13 | **Exceções genéricas** — `RuntimeException` e `IllegalStateException` usadas em vez de exceções de domínio | Services |
| S-14 | **Senha armazenada em texto plano** — `Usuario.senha` é `String` sem hashing | `Usuario.java` |
| S-15 | **`ddl-auto=update`** — perigoso em produção; pode alterar schema automaticamente | `application-dev.properties` |
| S-16 | **Senha do banco hardcoded** — `spring.datasource.password=131417` | `application-dev.properties` |
| S-17 | **Queries redundantes nos Repositories** — queries como `findByNome`, `findByCpf` poderiam usar Spring Data derived queries | Repositories |
| S-18 | **`UsuarioRepository.findByAtivo()` retorna `Usuario` (singular)** — deveria retornar `List<Usuario>`, pois múltiplos usuários podem ter `ativo=true` | `UsuarioRepository.java` |
| S-19 | **`UsuarioRepository.findBySenha()` existe** — busca por senha é um risco de segurança e anti-pattern | `UsuarioRepository.java` |
| S-20 | **`ItemPedidoRepository.findByPreco()` recebe `Double`** — mas `ItemPedido.preco` é `BigDecimal`; incompatibilidade de tipos | `ItemPedidoRepository.java` |
| S-21 | **Produto sem preço** — entidade `Produto` não tem campo de preço | `Produto.java` |
| S-22 | **Enum `FormaPagamento` declarada mas não usada** — nenhuma entidade referencia forma de pagamento | `FormaPagamento.java` |
| S-23 | **`Pedido` sem método `setValorTotal()`, `setDataHoraPedido()`, `setHorarioEstimadoRetirada()`** — campos sem setter, dificultam desserialização do JSON | `Pedido.java` |
| S-24 | **`excluirItemPedido()` não verifica existência** — chama `deleteById()` direto, que lança `EmptyResultDataAccessException` se não existir | `ItemPedidoService.java:57` |
| S-25 | **Lombok declarado mas não usado** — nenhuma classe usa `@Data`, `@Getter`, `@Setter`, etc. | `pom.xml` vs código |

---

## ESTADO CONSOLIDADO — V1

### Arquitetura conhecida
- Spring Boot 4.1.0 com Java 21
- Arquitetura em camadas parcialmente implementada (Controller → Service → Repository → Entity)
- MySQL como SGBD, conexão configurada no perfil `dev`
- Flyway e Spring Security como dependências, sem configuração
- Dois paradigmas de controller: REST (Pedido) e MVC/Thymeleaf (ItemPedido)

### Componentes analisados
- 4 entidades JPA: Pedido, ItemPedido, Produto, Usuario
- 4 enums: StatusPedido, CategoriaProduto, FormaPagamento, Perfil
- 4 repositories (JpaRepository)
- 2 services (PedidoService, ItemPedidoService)
- 2 controllers (PedidoController, ItemPedidoController)
- 8 classes placeholder vazias
- 1 teste (contextLoads)
- Configuração: application.properties + application-dev.properties

### Regras de negócio identificadas
- RN-01 a RN-13 (ver seção 8)

### Bugs / Riscos (não auditados ainda)
- S-01 a S-25 (ver seção 10) — suspeitas a investigar

### Dúvidas pendentes
- D-01: O sistema vai operar com Thymeleaf, REST API, ou ambos? (há dois paradigmas)
- D-02: Como o `valorTotal` do pedido deve ser determinado? Calculado pelo servidor ou informado pelo cliente?
- D-03: ItemPedido deveria referenciar Produto? Se sim, como?
- D-04: Existe alguma regra de negócio para status do pedido (máquina de estados)?
- D-05: FormaPagamento será usada? Se sim, onde?
- D-06: Há templates Thymeleaf correspondentes aos controllers MVC?

### Hipóteses adotadas
- H-01: O projeto está em transição de uma arquitetura MVC (Thymeleaf) para uma API REST que será consumida pelo React.
- H-02: As classes placeholder (`classe.java`, `Class.java`) foram criadas para reservar pacotes para implementação futura.
- H-03: O `ddl-auto=update` está sendo usado propositalmente durante o desenvolvimento, em vez de Flyway migrations.

### Itens ainda não auditados
- [ ] Análise profunda de persistência, JPA e integridade (Fase 2)
- [ ] Análise profunda de Repository e Service (Fase 3)
- [ ] Análise profunda de Controller e API REST (Fase 4)
- [ ] Testes adversariais (Fase 5)
- [ ] Engenharia de Software e aprendizado (Fase 6)
- [ ] Consolidação final e preparação para banca (Fase 7)
