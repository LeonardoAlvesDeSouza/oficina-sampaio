-- Estado financeiro da ordem, separado do operacional. As ordens que já existem
-- nascem com a conta em aberto; o default sai depois de preenchê-las para que a
-- coluna não aceite insert sem estado declarado.
alter table ordens_servico
    add column status_pagamento varchar(20) not null default 'PENDENTE';

alter table ordens_servico
    alter column status_pagamento drop default;

alter table ordens_servico
    add constraint ck_ordens_servico_status_pagamento check (
        status_pagamento in ('PENDENTE', 'PAGA')
    );

create table pagamentos (
    id uuid primary key,
    ordem_servico_id uuid not null,
    cliente_id uuid not null,
    forma varchar(20) not null,
    valor numeric(12, 2) not null,
    registrado_em timestamptz not null,
    versao bigint not null default 0,
    constraint fk_pagamentos_ordem foreign key (ordem_servico_id) references ordens_servico (id),
    constraint fk_pagamentos_cliente foreign key (cliente_id) references clientes (id),
    -- Uma ordem, um pagamento. É esta restrição que sobrevive a dois cliques
    -- simultâneos no mesmo botão, quando a checagem do caso de uso não basta.
    constraint uk_pagamentos_ordem unique (ordem_servico_id),
    constraint ck_pagamentos_forma check (
        forma in ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'TRANSFERENCIA')
    ),
    constraint ck_pagamentos_valor check (valor > 0)
);

create table movimentacoes_financeiras (
    id uuid primary key,
    tipo varchar(10) not null,
    descricao varchar(200) not null,
    valor numeric(12, 2) not null,
    ocorrida_em timestamptz not null,
    pagamento_id uuid,
    versao bigint not null default 0,
    constraint fk_movimentacoes_pagamento foreign key (pagamento_id) references pagamentos (id),
    -- O mesmo pagamento não pode gerar duas entradas no caixa. Nulo repetido é
    -- permitido pelo Postgres, então as saídas, que não têm pagamento, convivem.
    constraint uk_movimentacoes_pagamento unique (pagamento_id),
    constraint ck_movimentacoes_tipo check (tipo in ('ENTRADA', 'SAIDA')),
    -- O valor é sempre positivo: quem diz se soma ou subtrai é o tipo.
    constraint ck_movimentacoes_valor check (valor > 0),
    -- Entrada vem de pagamento; saída é lançada à mão e não tem pagamento.
    constraint ck_movimentacoes_origem check (
        (tipo = 'ENTRADA' and pagamento_id is not null)
            or (tipo = 'SAIDA' and pagamento_id is null)
    )
);

create index idx_ordens_servico_status_pagamento on ordens_servico (status_pagamento);
create index idx_pagamentos_registrado_em on pagamentos (registrado_em desc);
create index idx_movimentacoes_ocorrida_em on movimentacoes_financeiras (ocorrida_em desc);
create index idx_movimentacoes_tipo on movimentacoes_financeiras (tipo);
