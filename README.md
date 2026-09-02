
# 🍔 Cantina (CantiNAI)

Sistema completo de gerenciamento e pedidos para cantinas e restaurantes. O **CantiNAI** permite que os usuários naveguem pelo cardápio de forma interativa, façam pedidos, realizem pagamentos via PIX e acompanhem seus pedidos em tempo real. Para a equipe administrativa, o sistema oferece um painel Kanban visual para gestão eficiente dos pedidos da cozinha.

---

## Funcionalidades

### Para o Cliente (Frontend SPA)
- **Cardápio Interativo**: Navegação por categorias, pratos do dia, filtros e visualização detalhada dos produtos.
- **Carrinho de Compras**: Fluxo de checkout dinâmico e persistência de sessão (Local Storage).
- **Gerenciamento de Pedidos**:
  - **Pagamento via PIX**: Modal interativo com geração de QR Code, chave Copia/Cola e confirmação de pagamento.
  - **Cancelamento**: Opção para o cliente cancelar pedidos pendentes diretamente pela interface.
- **Autenticação**: Sistema de login e registro de usuários seguro utilizando JWT (JSON Web Tokens).

### Para o Administrador
- **Painel Kanban**: Gestão visual dos pedidos para os funcionários da cantina, separando as etapas de preparo e entrega.
- **Gerenciamento de Produtos**: Cadastro e atualização de itens do cardápio.
- **Documentação da API**: Acesso facilitado aos endpoints via Swagger UI (SpringDoc OpenAPI).

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java** (Spring Boot)
- **Spring Data JPA** (Integração ORM com banco de dados)
- **Spring Security** + **JWT** (Autenticação e Autorização)
- **Spring MVC** + **Thymeleaf** (Renderização de templates)
- **Flyway** (Versionamento e migração do banco de dados)
- **Spring Mail** (Envio de notificações por e-mail)
- **SpringDoc OpenAPI** (Documentação Swagger)
- **MySQL** (Banco de dados relacional)
- **JUnit 5, Mockito & AssertJ** (Testes unitários e de integração)

### Frontend
- **JavaScript (Vanilla)** (Arquitetura SPA modular: `main.js`, `products.js`, `cart.js`, `orders.js`, `api.js`)
- **HTML5 & CSS3**
- **Thymeleaf** (Templates dinâmicos integrados ao Spring)

---

## 📂 Estrutura do Projeto

O repositório contém a estrutura padrão Maven/Gradle, com a versão estável e refinada atual localizada na pasta `CantinaV4-corrigido`.

```text
Cantina/
├── src/                       # Estrutura de código e testes (Versão base/raiz)
├── CantinaV4-corrigido/       # Versão estável e refinada (Principal)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Lógica de negócio, Controllers, Entities, Security
│   │   │   └── resources/     # Templates Thymeleaf, static (JS/CSS/Images), application.properties
│   │   └── test/              # Testes unitários
│   ├── mvnw / mvnw.cmd        # Maven Wrapper (Permite rodar sem ter o Maven instalado na máquina)
│   └── pom.xml                # Gerenciador de dependências
└── .gitignore
```

---

## ⚙️ Pré-requisitos

Antes de começar, você vai precisar ter instalado em sua máquina:
- [Java JDK 17 ou superior](https://www.oracle.com/java/technologies/javase-downloads.html)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/)
- (Opcional) [Maven](https://maven.apache.org/), caso prefira não usar o Maven Wrapper (`mvnw`).

---

## 🏃 Como Executar o Projeto

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/projetocantina67-hash/Cantina.git
   cd Cantina/CantinaV4-corrigido/CantinaV4-corrigido
   ```

2. **Configure o Banco de Dados:**
   Crie um banco de dados MySQL (ex: `cantina_db`). Em seguida, acesse o arquivo `src/main/resources/application.properties` e atualize as credenciais de acesso (`spring.datasource.username` e `spring.datasource.password`).

3. **Execute as Migrações do Flyway:**
   O Flyway criará e atualizará automaticamente as tabelas do banco de dados na primeira vez que a aplicação for iniciada.

4. **Inicie a aplicação Spring Boot:**
   - **No Linux/Mac:**
     ```bash
     ./mvnw spring-boot:run
     ```
   - **No Windows:**
     ```cmd
     mvnw.cmd spring-boot:run
     ```

5. **Acesse a aplicação:**
   A aplicação estará disponível em `http://localhost:8080`.
   - **Documentação da API (Swagger):** `http://localhost:8080/swagger-ui.html`

---

## 🧪 Testes

O projeto inclui uma suíte de testes para garantir a integridade das regras de negócio. Para executar todos os testes, rode o seguinte comando:
```bash
./mvnw test
```

---

## 📝 Próximos Passos (Roadmap)

- [ ] **WebSockets / SSE**: Atualização em tempo real (Real-Time) do painel Kanban administrativo, substituindo o polling atual.
- [ ] **Testes E2E**: Implementar suíte de testes de ponta a ponta (Playwright ou Cypress) cobrindo os fluxos críticos de compra e PIX.

---

## 🤝 Contribuidores

Este projeto é desenvolvido por:

- **Erick Soares** ([@Erickhitman](https://github.com/Erickhitman))
- **Thiago Christopher** ([@Thiagodevfull](https://github.com/Thiagodevfull))
- **Marcus Vinicius** ([@MarcusNavarroSilva](https://github.com/MarcusNavarroSilva))
- **Joyce Lideris** ([@mynamu](https://github.com/mynamu)) 👑
- Equipe **projetocantina67-hash**

---
*Feito com ☕ e 💻 pela equipe Cantina.*
```
