# Oficina Sampaio

Sistema web para gestão de oficina mecânica, construído como monólito modular
com Java, Spring Boot, Thymeleaf e PostgreSQL.

## Estado atual

As quatro primeiras fatias verticais estão implementadas:

- cadastro e listagem de clientes;
- normalização e unicidade de CPF/CNPJ;
- cadastro e listagem de veículos por cliente;
- normalização e unicidade de placa;
- regras de domínio para inativação e quilometragem;
- persistência PostgreSQL com migrações Flyway;
- telas server-side com Thymeleaf e Bootstrap;
- autenticação por formulário com senhas BCrypt;
- perfis `ADMIN` e `FUNCIONARIO` com autorização por rota;
- cadastro e listagem de usuários restritos ao administrador;
- criação idempotente do administrador inicial;
- abertura e listagem de ordens de serviço para clientes e veículos ativos;
- inclusão de serviços e peças enquanto a ordem está aberta;
- cálculo separado de serviços, peças e total da ordem;
- consulta detalhada da ordem pela interface web;
- ciclo operacional com início, espera por peça, retomada, finalização, entrega e cancelamento;
- ações disponíveis por estado e bloqueio de itens após o início da execução;
- testes de domínio, aplicação, HTTP e integração com PostgreSQL real.

Pagamentos, financeiro e relatórios ainda serão implementados. Os relatórios e
a impressão da OS usarão JasperReports com templates `JRXML` versionados.

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

Abra <http://localhost:8080>. A página inicial redireciona para o login e, após
autenticação, para o cadastro de clientes.

No primeiro início é criado um administrador de desenvolvimento:

```text
login: admin
senha: Oficina@123
```

Defina `APP_ADMIN_PASSWORD` antes do primeiro início fora do ambiente local. O
bootstrap não altera um usuário que já existe.

As credenciais padrão do banco são apenas para desenvolvimento local e podem
ser substituídas pelas variáveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
APP_BOOTSTRAP_ADMIN_ENABLED
APP_ADMIN_NAME
APP_ADMIN_LOGIN
APP_ADMIN_PASSWORD
```

## Testes

```shell
mvn test
```

Os testes de integração usam Testcontainers e precisam de acesso ao Docker.
