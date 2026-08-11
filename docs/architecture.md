# Arquitetura do Oficina Sampaio

## Visão geral

O Oficina Sampaio será construído como um monólito modular em Spring Boot. Cada
área de negócio mantém seu domínio, seus casos de uso, sua persistência e sua
interface web, compartilhando inicialmente uma única aplicação e um banco
PostgreSQL.

```mermaid
flowchart TB
    actor["Administrador / Funcionário"] --> browser["Navegador"]
    browser --> security["Spring Security<br/>Autenticação e autorização"]

    subgraph system["Oficina Sampaio — Monólito modular Spring Boot"]
        security --> web["Spring MVC<br/>Thymeleaf + Bootstrap"]

        subgraph modules["Módulos de negócio"]
            usuario["Usuário<br/>Contas e perfis"]
            cliente["Cliente<br/>Cadastro e inativação"]
            veiculo["Veículo<br/>Cadastro e proprietário"]
            ordem["Ordem de Serviço<br/>Itens, totais e ciclo operacional"]
            financeiro["Financeiro<br/>Pagamentos e movimentações"]
            relatorio["Consultas e Relatórios<br/>Dashboard, histórico e documentos"]
        end

        web --> usuario
        web --> cliente
        web --> veiculo
        web --> ordem
        web --> financeiro
        web --> relatorio

        security --> usuario
        veiculo -->|"Consulta cliente"| cliente
        ordem -->|"Consulta cliente"| cliente
        ordem -->|"Consulta veículo"| veiculo
        ordem -.->|"Evento PagamentoRegistrado"| financeiro

        relatorio -->|"Consultas de leitura"| cliente
        relatorio -->|"Consultas de leitura"| veiculo
        relatorio -->|"Consultas de leitura"| ordem
        relatorio -->|"Consultas de leitura"| financeiro

        persistence["Persistência por módulo<br/>Spring Data JPA"]
        migrations["Migrações<br/>Flyway"]
        jasper["Motor de relatórios<br/>JasperReports + JRXML"]

        usuario --> persistence
        cliente --> persistence
        veiculo --> persistence
        ordem --> persistence
        financeiro --> persistence
        relatorio --> jasper
        ordem --> jasper
    end

    persistence --> database[("PostgreSQL")]
    migrations --> database
    jasper --> document["Relatórios e OS<br/>PDF"]
```

## Estrutura interna dos módulos

```mermaid
flowchart LR
    request["Requisição HTTP"] --> presentation["presentation<br/>Controllers, Forms e Views"]
    presentation --> application["application<br/>Casos de uso e DTOs"]
    application --> domain["domain<br/>Entidades, regras, eventos e portas"]
    application --> repository["Porta de repositório"]
    infrastructure["infrastructure<br/>JPA e integrações"] -.->|"Implementa"| repository
    infrastructure --> database[("PostgreSQL")]
    application --> response["Resultado do caso de uso"]
    response --> presentation
```

A direção permitida das dependências é:

```text
presentation -> application -> domain
infrastructure -> portas do domain/application
```

O domínio não conhece controllers, HTML, Thymeleaf ou detalhes do PostgreSQL.
Nenhum módulo acessa diretamente o repositório interno de outro módulo. A
integração acontece por casos de uso públicos, consultas públicas ou eventos.

## Agregados iniciais

```mermaid
flowchart LR
    cliente["Cliente<br/>Aggregate Root"] -->|"possui"| veiculo["Veículo<br/>Aggregate Root"]
    veiculo -->|"recebe"| os["OrdemServico<br/>Aggregate Root"]
    os --> item["ItemOrdemServico<br/>Entidade interna<br/>SERVICO ou PECA"]
    os -.->|"Pagamento registrado"| pagamento["Pagamento<br/>Aggregate Root"]
    pagamento --> movimento["MovimentacaoFinanceira<br/>ENTRADA"]
    usuario["Usuário<br/>Aggregate Root"] -.->|"Responsável pela operação"| os
    usuario -.-> movimento
```

- `ItemOrdemServico` pertence à `OrdemServico`, diferencia serviço e peça pelo
  tipo e não possui repositório próprio.
- Pagamento é separado do estado operacional da ordem.
- Movimentações financeiras formam o histórico do caixa.
- O saldo é calculado como entradas menos saídas; não existe um saldo mutável
  armazenado em uma entidade `Caixa`.
- Registros com histórico são inativados ou cancelados, não removidos
  fisicamente.

## Ciclo da ordem de serviço

Estados operacionais:

```mermaid
stateDiagram-v2
    [*] --> ABERTA
    ABERTA --> EM_EXECUCAO: iniciar execução
    EM_EXECUCAO --> AGUARDANDO_PECA: aguardar peça
    AGUARDANDO_PECA --> EM_EXECUCAO: retomar execução
    EM_EXECUCAO --> FINALIZADA: finalizar
    FINALIZADA --> ENTREGUE: registrar entrega
    ABERTA --> CANCELADA: cancelar
    EM_EXECUCAO --> CANCELADA: cancelar
    AGUARDANDO_PECA --> CANCELADA: cancelar
    FINALIZADA --> CANCELADA: cancelar
    ENTREGUE --> [*]
    CANCELADA --> [*]
```

A execução só começa quando existe ao menos um item. Serviços e peças podem ser
lançados enquanto a ordem não está finalizada, ou seja, em `ABERTA`,
`EM_EXECUCAO` e `AGUARDANDO_PECA` — assim a peça recebida durante a espera entra
no total antes do fechamento. `FINALIZADA` encerra o valor da ordem, e
`ENTREGUE` e `CANCELADA` são estados terminais.

O cancelamento é restrito ao perfil `ADMIN`; as demais transições estão
disponíveis para qualquer usuário autenticado. A restrição é aplicada no
servidor e também esconde o botão na tela de detalhe.

Violações dessas regras são sinalizadas pelo domínio com `RegraNegocioException`
e chegam ao usuário como mensagem na própria tela. Como o agregado usa lock
otimista, uma alteração concorrente é reportada como conflito, sem perder a
versão já gravada.

O pagamento possui estado financeiro próprio (`PENDENTE` ou `PAGA`). Uma ordem
cancelada não pode receber pagamento.

## Registro de pagamento

```mermaid
sequenceDiagram
    actor U as Usuário
    participant W as Controller
    participant A as RegistrarPagamentoUseCase
    participant O as OrdemServico
    participant F as Financeiro
    participant DB as PostgreSQL

    U->>W: Registrar pagamento da OS
    W->>A: registrar(osId, forma, valor)
    A->>O: validar e registrar pagamento
    O-->>A: PagamentoRegistrado
    A->>F: processar evento
    F->>F: Criar Pagamento
    F->>F: Criar entrada financeira
    A->>DB: Salvar tudo na mesma transação

    alt Operação válida
        DB-->>A: Commit
        A-->>W: Pagamento registrado
        W-->>U: OS paga e caixa atualizado
    else Pagamento duplicado ou OS inválida
        DB-->>A: Rollback
        A-->>W: Regra de negócio violada
        W-->>U: Exibir erro
    end
```

Pagamento, atualização da ordem e entrada financeira devem ser persistidos na
mesma transação. Uma restrição única no banco deve impedir que o mesmo pagamento
gere mais de uma movimentação.

## Relatórios e documentos

Relatórios gerenciais e a impressão da ordem de serviço serão gerados com
JasperReports. Os layouts ficam versionados como templates `JRXML` no módulo de
relatórios e são compilados para execução pela aplicação.

```mermaid
flowchart LR
    consulta["Caso de uso de consulta"] --> dados["DTO / JRBeanCollectionDataSource"]
    template["Template JRXML versionado"] --> jasper["JasperReports"]
    dados --> jasper
    jasper --> pdf["PDF da OS ou relatório"]
```

O domínio não conhece JasperReports. Casos de uso preparam os dados de leitura,
enquanto a infraestrutura de relatórios seleciona o template, preenche os
parâmetros e exporta o documento. Essa fronteira permite testar as consultas sem
depender da renderização e validar os templates separadamente.

## Pacotes

```text
br.com.oficinasampaio
├── shared
│   ├── domain
│   ├── infrastructure
│   └── presentation
├── usuario
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
├── cliente
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
├── veiculo
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
├── ordemservico
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
├── financeiro
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
├── relatorio
│   ├── application
│   ├── infrastructure
│   └── presentation
└── security
```

Como compromisso de DDD Lite, as entidades de domínio podem receber anotações
JPA inicialmente. Os repositórios continuam expostos como portas, enquanto a
implementação Spring Data permanece na infraestrutura. DTOs são usados nas
fronteiras da aplicação e não substituem as entidades dentro do domínio.

## Estado da implementação

Implementado nas quatro primeiras fatias verticais:

- fundação Spring Boot 4.1 e Java 21;
- módulos `cliente` e `veiculo` nas quatro camadas;
- contratos públicos entre Veículo e Cliente;
- PostgreSQL 17, Flyway e Docker Compose;
- interfaces MVC/Thymeleaf de cadastro e listagem para os dois módulos;
- módulo `usuario` com perfis `ADMIN` e `FUNCIONARIO`;
- autenticação por formulário, senhas BCrypt e autorização de rotas;
- gestão de usuários restrita a administradores e bootstrap do primeiro acesso;
- módulo `ordemservico` com abertura para cliente e veículo ativos;
- itens de serviço e peça internos ao agregado, com quantidades e valores positivos;
- totais monetários derivados no domínio e consulta detalhada pela interface web;
- persistência das ordens e seus itens pela migration Flyway `V3`;
- ciclo operacional completo da ordem, com ações válidas expostas pela aplicação;
- itens lançáveis até a finalização e cancelamento restrito ao administrador;
- persistência otimista das mudanças de estado e controles correspondentes na interface web;
- navegação e telas de stand-by para Pagamentos, Financeiro e Relatórios, servidas
  temporariamente por `shared.presentation` até que cada módulo assuma sua rota;
- testes de domínio, casos de uso, HTTP e persistência real com Testcontainers.

Ainda planejado no desenho, mas não implementado: pagamento, `financeiro`,
relatórios JasperReports e seus templates `JRXML`. Até essas fatias serem
entregues, as respectivas rotas exibem “Módulo em construção”.
