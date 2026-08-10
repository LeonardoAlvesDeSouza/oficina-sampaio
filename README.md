# Oficina Sampaio

Sistema web para gestão de oficina mecânica, construído como monólito modular
com Java, Spring Boot, Thymeleaf e PostgreSQL.

## Estado atual

A primeira fatia vertical está implementada:

- cadastro e listagem de clientes;
- normalização e unicidade de CPF/CNPJ;
- cadastro e listagem de veículos por cliente;
- normalização e unicidade de placa;
- regras de domínio para inativação e quilometragem;
- persistência PostgreSQL com migrações Flyway;
- telas server-side com Thymeleaf e Bootstrap;
- testes de domínio, aplicação, HTTP e integração com PostgreSQL real.

Os módulos de ordem de serviço, financeiro, usuários, segurança e relatórios
ainda serão implementados.

## Arquitetura

O desenho completo, as fronteiras entre módulos, os agregados e o fluxo de
pagamento estão em [docs/architecture.md](docs/architecture.md).

## Requisitos

- JDK 21 ou superior;
- Maven 3.6.3 ou superior;
- Docker Desktop com Docker Compose.

O projeto usa Java 21 como versão de compilação. JDKs mais novos compatíveis
também podem ser usados para executar o Maven.

## Executar localmente

Inicie o PostgreSQL:

```shell
docker compose up -d
```

Execute a aplicação:

```shell
mvn spring-boot:run
```

Abra <http://localhost:8080>. A página inicial redireciona para o cadastro de
clientes.

As credenciais padrão do banco são apenas para desenvolvimento local e podem
ser substituídas pelas variáveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

## Testes

```shell
mvn test
```

Os testes de integração usam Testcontainers e precisam de acesso ao Docker.
