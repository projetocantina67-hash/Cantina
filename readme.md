# 🍽️ Agenda — Sistema de Agendamento de Produtos da Cantina SENAI

Sistema desenvolvido em **Java**, com persistência de dados em **MySQL** via JDBC, para o agendamento de produtos da cantina do SENAI. Permite que os alunos façam pedidos antecipados dos produtos disponíveis, otimizando o atendimento e reduzindo filas nos horários de pico.

Projeto acadêmico desenvolvido por Erick, Joyce, Marcus e Thiago.

## 🚀 Funcionalidades

**Usuários**
- Cadastro de usuários (nome, gênero, data de nascimento, etnia, endereço, bairro, cidade, telefone, celular e e-mail)
- Atualização de dados cadastrados
- Exclusão de registros
- Listagem de todos os usuários (ordenados por nome)
- Busca de usuário por nome ou por ID

**Produtos e agendamentos**
- Cadastro de produtos da cantina
- Agendamento de pedidos por usuário
- Consulta de agendamentos

## 🛠️ Tecnologias utilizadas

- React
- Java (JDK 21)
- Springboot
- MySQL
- IntelliJ IDEA

## 📁 Estrutura do projeto

```
Agenda/
├── src/
│   ├── banco/        # Classe de conexão com o banco de dados (BD)
│   ├── DAO/           # Classes de acesso a dados (UsuarioDAO, ...)
│   ├── model/         # Classes de modelo (Usuario, ...)
│   └── ...
└── lib/                # Bibliotecas externas (driver JDBC do MySQL)
```
---

## Autores

| Nome | GitHub |
|---|---|
| Erick Soares | [@Erick](https://github.com/Erickhitman) |
| Joyce Lyderis | [@Joyce](https://github.com/mynamu) |
| Marcus Navarro | [@Marcus](https://github.com/MarcusNavarroSilva) |
| Thiago Andrade | [@Thiago](https://github.com/Thiagodevfull) |

## 📄 Licença

Projeto de uso acadêmico.
