# QA 06 — Engenharia de Software e Aprendizado

**Data:** 2026-08-23  
**Fase:** 6 de 7  
**Base:** Estado Consolidado V5  
**Objetivo:** Transformar a auditoria técnica em material de estudo e preparação para banca.

---

## 1. Análise Arquitetural

### 1.1 Arquitetura em Camadas

O projeto implementa uma **arquitetura em camadas** (Layered Architecture):

```
┌─────────────────────────────────────────────┐
│           CONTROLLER (Apresentação)          │
│  PedidoController    ItemPedidoController    │
│  @RestController     @Controller             │
├─────────────────────────────────────────────┤
│             SERVICE (Negócio)                │
│  PedidoService       ItemPedidoService       │
│  @Service            @Service                │
├─────────────────────────────────────────────┤
│            REPOSITORY (Persistência)         │
│  PedidoRepo   ItemPedidoRepo   ProdutoRepo  │
│  UsuarioRepo   extends JpaRepository         │
├─────────────────────────────────────────────┤
│             MODEL (Domínio)                  │
│  Pedido   ItemPedido   Produto   Usuario     │
│  @Entity  @Entity      @Entity   @Entity     │
├─────────────────────────────────────────────┤
│              BANCO DE DADOS                  │
│                 MySQL                        │
└─────────────────────────────────────────────┘
```

**Separação de responsabilidades:**
- **Controller:** Recebe HTTP, delega para Service, retorna resposta → ✓ Correto
- **Service:** Contém regras de negócio, usa Repository para persistir → ✓ Parcial (regras incompletas)
- **Repository:** Abstrai acesso a dados, usa JPA → ✓ Correto
- **Model:** Define estrutura de dados e validações → ✓ Parcial (tipos incorretos)

**Problemas na separação:**
1. Controller expõe entidades JPA diretamente (sem DTO) → violação de isolamento entre camadas
2. Sem camada de DTO para mapear entrada/saída
3. Sem camada de exceções de domínio
4. Sem camada de configuração (segurança, CORS)

### 1.2 Quando NÃO usar arquitetura em camadas

A arquitetura em camadas é adequada para o projeto, mas tem limitações:
- **Projetos muito complexos:** Hexagonal/Ports & Adapters ou Clean Architecture oferecem melhor isolamento
- **Microserviços:** Cada serviço geralmente é pequeno demais para justificar camadas rígidas
- **CQRS:** Quando as operações de leitura e escrita são radicalmente diferentes

Para este projeto acadêmico, a arquitetura em camadas é a escolha correta. ✓

---

## 2. Análise de POO

### 2.1 Encapsulamento

**O que é:** Esconder os detalhes internos de um objeto, expondo apenas o que é necessário via métodos públicos.

**Onde aparece:**
- Campos `private` em todas as entidades ✓
- Getters e setters controlam o acesso ✓

**Problemas encontrados:**
- `Pedido`: falta setters para `dataHoraPedido`, `horarioEstimadoRetirada`, `valorTotal`, `id` → encapsulamento excessivo que impede operações legítimas
- `Usuario`: falta setters para a maioria dos campos → impossível atualizar usuário
- `Usuario.excluir()`: bom exemplo de encapsulamento — o comportamento de "exclusão lógica" é encapsulado no método, escondendo a implementação (`this.ativo = false`)

**Vantagens no projeto:** Impede acesso direto ao banco de dados ou manipulação arbitrária de campos.

**Como explicar ao professor:** "Encapsulamento é como um carro: o motorista usa o volante e os pedais (interface pública), sem precisar acessar o motor diretamente (campos privados). No nosso projeto, cada entidade expõe apenas os métodos necessários."

### 2.2 Abstração

**O que é:** Simplificar a complexidade, mostrando apenas o essencial e escondendo detalhes de implementação.

**Onde aparece:**
- `JpaRepository<Pedido, Long>` — abstrai todo o acesso a dados: o Service chama `save()` e `findById()` sem saber se é MySQL, PostgreSQL ou MongoDB
- `PedidoService` — abstrai a lógica de negócio: o Controller chama `criarPedido()` sem saber como a validação e persistência funcionam

**Como explicar ao professor:** "Abstração é como usar um caixa eletrônico: você só vê 'sacar' e 'depositar', sem saber como o cofre funciona por dentro. No nosso código, `pedidoRepository.save(pedido)` abstrai toda a complexidade do SQL, JPA e JDBC."

### 2.3 Herança

**Uso no projeto:** Não há herança entre as entidades do projeto. As entidades não estendem nenhuma superclasse (exceto `Object`, implicitamente).

**Herança indireta:** Os repositories estendem `JpaRepository`, que estende `PagingAndSortingRepository`, que estende `CrudRepository`, que estende `Repository`. É uma hierarquia de interfaces do Spring Data.

```
Repository
  └── CrudRepository
       └── PagingAndSortingRepository
            └── JpaRepository
                 ├── PedidoRepository
                 ├── ItemPedidoRepository
                 ├── ProdutoRepository
                 └── UsuarioRepository
```

### 2.4 Polimorfismo

**Uso no projeto:** Polimorfismo está presente via interfaces do Spring:
- `JpaRepository` é a interface → o Spring Data cria a implementação em runtime (proxy dinâmico)
- O Service depende da interface `PedidoRepository`, não da implementação concreta → polimorfismo de subtipo

**Como explicar ao professor:** "Polimorfismo significa 'muitas formas'. O `PedidoService` depende da interface `PedidoRepository`, e o Spring cria automaticamente a implementação. Se amanhã trocarmos o MySQL por MongoDB, o Service não muda — só a implementação do Repository."

### 2.5 Composição

**Onde aparece:**
- `Pedido` **tem** uma lista de `ItemPedido` → composição (o item não existe sem o pedido, `orphanRemoval=true`)
- `Pedido` **tem** um `Usuario` → associação (o usuário existe independentemente do pedido)
- `PedidoService` **tem** um `PedidoRepository` → composição/injeção

**Composição vs Herança:** O projeto usa composição corretamente:
- "Pedido TEM itens" (composição) ✓ — em vez de "ItemPedido HERDA de Pedido" (herança errada)
- "Pedido TEM usuário" (associação) ✓ — em vez de "Pedido HERDA de Usuário" (herança errada)

**Como explicar ao professor:** "Usamos composição em vez de herança: 'Pedido TEM itens' é mais correto que 'ItemPedido É UM tipo de Pedido'. A composição é mais flexível e desacoplada."

---

## 3. Análise SOLID

### 3.1 S — Single Responsibility Principle (SRP)

**O que é:** Cada classe deve ter uma única responsabilidade (uma razão para mudar).

**Análise:**

| Classe | Responsabilidade | SRP |
|---|---|---|
| `PedidoController` | Receber requisições HTTP de pedidos | ✓ |
| `PedidoService` | Lógica de negócio de pedidos | ✓ |
| `PedidoRepository` | Acesso a dados de pedidos | ✓ |
| `Pedido` | Representar a estrutura de um pedido + validação + persistência | ⚠️ Múltiplas |
| `ItemPedidoController` | Receber requisições HTTP + renderizar views | ⚠️ Mistura REST com MVC |

**Problema:** A entidade `Pedido` tem múltiplas responsabilidades:
1. Estrutura de dados (modelo)
2. Regras de validação (`@NotNull`, `@NotBlank`)
3. Mapeamento JPA (`@Entity`, `@Table`, `@Column`)
4. Serialização JSON (Jackson)

Com DTOs, a entidade teria apenas as responsabilidades 1-3, e o DTO cuidaria da 4.

### 3.2 O — Open/Closed Principle (OCP)

**O que é:** Classes devem estar abertas para extensão, fechadas para modificação.

**No projeto:** Não há uso explícito deste princípio. Não há interfaces de serviço ou estratégias que permitam extensão sem modificação.

**Quando seria útil:** Se o cálculo do `valorTotal` pudesse variar (ex: com desconto, taxa de entrega, cupom), uma interface `CalculoValorStrategy` permitiria adicionar novos cálculos sem modificar o `PedidoService`.

### 3.3 L — Liskov Substitution Principle (LSP)

**O que é:** Subtipos devem ser substituíveis por seus tipos base sem quebrar o programa.

**No projeto:** Os repositories estendem `JpaRepository` corretamente. Qualquer `PedidoRepository` pode ser usado onde `JpaRepository<Pedido, Long>` é esperado. ✓

### 3.4 I — Interface Segregation Principle (ISP)

**O que é:** Clientes não devem depender de interfaces que não usam.

**No projeto:** Os services usam apenas uma fração dos métodos de `JpaRepository` (save, findById, findAll, deleteById). A interface `JpaRepository` é ampla, mas isso é aceitável no contexto do Spring Data — é uma framework interface, não uma interface de domínio.

### 3.5 D — Dependency Inversion Principle (DIP)

**O que é:** Módulos de alto nível não devem depender de módulos de baixo nível. Ambos devem depender de abstrações.

**No projeto:**

```java
@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;  // Interface!
    
    @Autowired
    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }
}
```

✓ O Service depende da **interface** `PedidoRepository`, não de uma implementação concreta.
✓ A implementação é injetada pelo Spring (IoC Container).

**Como explicar ao professor:** "O `PedidoService` não sabe como os dados são salvos. Ele depende da interface `PedidoRepository`, e o Spring fornece a implementação. Se trocarmos MySQL por PostgreSQL, o Service não muda."

---

## 4. Padrões de Projeto Presentes

### 4.1 MVC (Model-View-Controller)

**O que é:** Separa a aplicação em três componentes: Model (dados), View (apresentação) e Controller (lógica de controle).

**No projeto:**
- **Model:** Entidades JPA (`Pedido`, `ItemPedido`, etc.)
- **View:** Templates Thymeleaf (referenciados, mas não encontrados)
- **Controller:** `@Controller` / `@RestController`

**Onde aparece:** `ItemPedidoController` segue o padrão MVC clássico (recebe request → processa → retorna view). `PedidoController` segue uma variação API-first (sem view, retorna JSON).

### 4.2 Repository Pattern

**O que é:** Abstrai o acesso a dados como se fosse uma coleção de objetos em memória.

**No projeto:** As 4 interfaces `extends JpaRepository` implementam o Repository Pattern.

**Vantagem:** O Service não sabe se está acessando MySQL, PostgreSQL ou memória. Apenas chama `save()`, `findById()`, etc.

### 4.3 Service Layer Pattern

**O que é:** Camada que define os limites da aplicação, coordenando operações de negócio.

**No projeto:** `PedidoService` e `ItemPedidoService` coordenam validação + persistência.

### 4.4 Dependency Injection / IoC Container

**O que é:** O framework cria e gerencia os objetos (beans), injetando as dependências automaticamente.

**No projeto:** Toda a injeção é feita por construtor com `@Autowired`:

```java
@Autowired
public PedidoController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
}
```

O Spring cria o `PedidoRepository` → injeta no `PedidoService` → injeta no `PedidoController`.

**Alternativas:**
- Injeção por campo (`@Autowired` direto no campo) — não recomendada (dificulta testes)
- Injeção por setter — menos comum
- Injeção por construtor — **recomendada** e usada no projeto ✓

---

## 5. Análise de Coesão, Acoplamento e Modularização

### 5.1 Coesão (Alta = Bom)

| Componente | Coesão | Justificativa |
|---|---|---|
| `PedidoService` | Alta | Trata apenas de lógica de Pedido |
| `ItemPedidoService` | Alta | Trata apenas de lógica de ItemPedido |
| `PedidoController` | Alta | Trata apenas de endpoints de Pedido |
| `Pedido` (entidade) | Média | Mistura modelo, validação, JPA e serialização |
| Pacote `repository` | Baixa | Contém interfaces de Repository + subpacotes vazios (entity, mapper, persistence) |

### 5.2 Acoplamento (Baixo = Bom)

| Relação | Acoplamento | Justificativa |
|---|---|---|
| Controller → Service | Baixo ✓ | Depende da classe concreta, mas a interface é simples |
| Service → Repository | Baixo ✓ | Depende de interface (DIP) |
| Controller → Entity | **Alto** ❌ | Controller manipula entidade JPA diretamente (sem DTO) |
| API → Entity | **Alto** ❌ | Contrato da API acoplado à estrutura do banco |

### 5.3 Modularização

O projeto tem uma estrutura de pacotes que sugere modularização planejada, mas com muitos pacotes vazios:

```
controller/       → 2 controllers + 1 placeholder
database/model/   → 4 entidades
database/enums/   → 4 enums
repository/       → 4 repos + 3 subpacotes vazios (entity, mapper, persistence)
service/          → 2 services + 1 placeholder
exception/        → 5 subpacotes vazios (admin, carrinho, pagamento, reserva, usuario)
```

**8 classes placeholder vazias** sugerem que o projeto foi estruturado pensando em expansão futura (boa intenção), mas nenhuma foi implementada.

---

## 6. Perguntas do Professor com Respostas

### Pergunta 1: "O que é arquitetura em camadas e por que vocês escolheram essa arquitetura?"

**Resposta curta:** "É a separação do código em camadas com responsabilidades distintas: apresentação (Controller), negócio (Service), persistência (Repository) e domínio (Model). Escolhemos porque é o padrão recomendado pelo Spring Boot para aplicações web de médio porte."

**Explicação aprofundada:** A arquitetura em camadas isola as preocupações: o Controller não sabe como os dados são salvos; o Service não sabe se o cliente é um browser ou um app mobile; o Repository não sabe quais regras de negócio existem. Isso permite que uma camada mude sem afetar as outras. Por exemplo, se trocarmos MySQL por PostgreSQL, só o Repository (e a configuração) muda — o Service e o Controller permanecem intactos.

### Pergunta 2: "O que é JPA e qual a diferença entre JPA e Hibernate?"

**Resposta curta:** "JPA é a especificação (a 'interface'). Hibernate é a implementação (a 'classe concreta'). JPA define as anotações e o contrato. Hibernate faz o trabalho pesado de gerar SQL e executar no banco."

**Explicação aprofundada:** JPA define `@Entity`, `@Table`, `@Column`, `EntityManager`, etc. O Hibernate implementa tudo isso: quando chamamos `save()`, o Hibernate gera o `INSERT INTO` correto para o banco configurado, gerencia o cache de primeiro nível, implementa lazy loading, etc. Poderíamos trocar o Hibernate por EclipseLink (outro provider JPA) sem mudar o código — porque dependemos da especificação, não da implementação.

### Pergunta 3: "O que é `@Transactional` e por que usamos?"

**Resposta curta:** "Garante que um conjunto de operações no banco aconteça como uma unidade atômica: ou todas são concluídas com sucesso (commit) ou nenhuma é (rollback)."

**Explicação aprofundada:** No `criarPedido()`, precisamos salvar o pedido e seus itens. Se o pedido é salvo mas um item falha, ficamos com dados inconsistentes: pedido sem itens. Com `@Transactional`, se qualquer operação falhar, todas são revertidas. O Spring cria um proxy ao redor do método que abre uma transação antes de executar e faz commit ou rollback depois.

### Pergunta 4: "Por que vocês usam `BigDecimal` em vez de `double` para preço?"

**Resposta curta:** "`double` tem imprecisão de ponto flutuante. `0.1 + 0.2 = 0.30000000000000004` em `double`. Para dinheiro, precisamos de precisão exata, e `BigDecimal` oferece isso."

**Explicação aprofundada:** `double` armazena números em base 2 (binário). O número `0.1` em binário é uma dízima periódica infinita: `0.000110011001100...`. O computador trunca essa representação, causando erros de arredondamento. Para uma cantina que processa centenas de transações por dia, esses erros se acumulam. `BigDecimal` armazena os dígitos decimais exatamente como são, sem conversão para binário.

### Pergunta 5: "O que é CORS e por que precisamos configurar?"

**Resposta curta:** "CORS é um mecanismo de segurança do browser que impede um site de acessar recursos de outro servidor. Se o React roda em `localhost:3000` e a API em `localhost:8080`, são origens diferentes, e o browser bloqueia a comunicação."

**Explicação aprofundada:** A Same-Origin Policy é uma proteção do browser contra ataques CSRF e XSS. Ela impede que `sitemalicioso.com` faça requisições para `meubancodigital.com` usando os cookies do usuário. CORS (Cross-Origin Resource Sharing) é a exceção controlada: o servidor diz ao browser "aceito requisições da origem X". Sem isso, o React não consegue nem fazer um `GET` para a API.

### Pergunta 6: "O que é o Spring Security e por que a API não funciona sem configurá-lo?"

**Resposta curta:** "O Spring Security protege a aplicação por padrão. Ao adicionar a dependência, todas as rotas exigem autenticação. Precisamos configurar quais rotas são públicas e como a autenticação funciona."

**Explicação aprofundada:** O Spring Security opera com filtros HTTP (Security Filter Chain). Quando a dependência está no classpath, o Spring Boot auto-configura uma cadeia de filtros que exige autenticação para TODAS as rotas. A senha é gerada aleatoriamente no console. Para que a API funcione, precisamos criar um `SecurityFilterChain` que defina: quais endpoints são públicos, como o login funciona, se usamos JWT ou sessão, etc.

---

## 7. Mapa de Conhecimento

```
Java (JDK 21)
├── OOP
│   ├── Encapsulamento → campos private, getters/setters
│   ├── Abstração → JpaRepository abstrai SQL
│   ├── Composição → Pedido HAS-A List<ItemPedido>
│   └── Polimorfismo → Repository interface, implementação dinâmica
│
├── Tipos
│   ├── BigDecimal → valores monetários (ItemPedido.preco)
│   ├── Enum → StatusPedido, CategoriaProduto, Perfil, FormaPagamento
│   ├── Generics → List<ItemPedido>, JpaRepository<Pedido, Long>
│   └── ⚠️ double vs BigDecimal → inconsistência (Pedido.valorTotal)
│
└── java.time (não utilizado → ⚠️ datas como String)

Spring Boot (4.1.0)
├── Auto-configuração → @SpringBootApplication
├── Dependency Injection → @Autowired, injeção por construtor
├── Inversion of Control → Spring Container gerencia beans
├── Profiles → spring.profiles.active=dev
│
├── Spring MVC
│   ├── @RestController → PedidoController (JSON)
│   ├── @Controller → ItemPedidoController (Thymeleaf)
│   ├── @RequestMapping, @GetMapping, @PostMapping, etc.
│   ├── @PathVariable, @RequestBody, @ModelAttribute
│   ├── ResponseEntity → controle de status HTTP
│   └── ⚠️ @Valid ausente na API REST
│
├── Spring Data JPA
│   ├── JpaRepository → CRUD automático
│   ├── @Query → JPQL customizado
│   ├── @Transactional → controle de transações
│   └── ⚠️ 21 métodos declarados, 0 utilizados
│
├── Spring Security
│   └── ⚠️ Dependência presente, sem configuração → bloqueia tudo
│
├── Thymeleaf
│   └── ⚠️ Templates referenciados, não encontrados
│
└── Bean Validation
    ├── @NotNull, @NotBlank, @Size, @Min, @DecimalMin, @Email
    └── ⚠️ Acionado apenas no ItemPedidoController

Persistência (JPA/Hibernate)
├── @Entity, @Table → mapeamento ORM
├── @Id, @GeneratedValue(IDENTITY) → PK auto-incremento
├── @Column → nullable, unique, length, precision, scale
├── @Enumerated(STRING) → enums como texto
├── @ManyToOne, @OneToMany → relacionamentos
├── @JoinColumn → chave estrangeira
├── cascade = ALL → operações em cascata
├── orphanRemoval = true → remoção de órfãos
├── mappedBy → lado inverso
├── EAGER vs LAZY → estratégias de carregamento
└── ⚠️ Serialização circular (sem @JsonBackReference)

API REST
├── Verbos HTTP → GET, POST, DELETE (parcial)
├── Status HTTP → 200, 201, 204 (corretos)
├── JSON → Jackson (serialização/desserialização)
├── ⚠️ Sem DTO
├── ⚠️ Sem CORS
├── ⚠️ Sem @ControllerAdvice
└── ⚠️ Entidades expostas diretamente

Banco de Dados (MySQL)
├── Tabelas → pedido, item_pedido, produto, usuario
├── Chaves → PK auto-incremento, FK (usuario_id, pedido_id)
├── Constraints → NOT NULL, UNIQUE (cpf, email)
├── ddl-auto=update → Hibernate gerencia schema
└── ⚠️ Flyway sem migrações

Engenharia de Software
├── Arquitetura em Camadas → Controller → Service → Repository
├── Repository Pattern → abstração de dados
├── Service Layer → lógica de negócio isolada
├── MVC → Model-View-Controller
├── SOLID → D (DIP) bem aplicado; S parcial
├── Coesão → Alta nos Services ✓
├── Acoplamento → Alto entre API e Entity ⚠️
└── ⚠️ 8 classes placeholder vazias

Integração React (futura)
├── ⚠️ Spring Security bloqueia
├── ⚠️ CORS não configurado
├── ⚠️ Metade dos endpoints é MVC (Thymeleaf), não REST
├── ⚠️ Sem endpoints para Produto e Usuário
└── ⚠️ Serialização circular impede JSON funcional
```

---

## ESTADO CONSOLIDADO — V6

(Mantido idêntico ao V5, acrescido apenas desta fase de estudo)

### Conceitos explicados nesta fase
- Arquitetura em Camadas (com alternativas)
- POO: Encapsulamento, Abstração, Herança (indireta), Polimorfismo, Composição
- SOLID: S, O, L, I, D — análise de cada princípio no projeto
- Padrões: MVC, Repository, Service Layer, DI/IoC
- Coesão, Acoplamento, Modularização
- 6 perguntas de professor com respostas
- Mapa de conhecimento completo

### Itens ainda não auditados
- [ ] Consolidação final e preparação para banca (Fase 7)
