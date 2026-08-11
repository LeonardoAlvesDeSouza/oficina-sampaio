create table ordens_servico (
    id uuid primary key,
    cliente_id uuid not null,
    veiculo_id uuid not null,
    relato_problema varchar(1000) not null,
    aberta_em timestamptz not null,
    status varchar(30) not null,
    versao bigint not null default 0,
    constraint fk_ordens_servico_cliente foreign key (cliente_id) references clientes (id),
    constraint fk_ordens_servico_veiculo foreign key (veiculo_id) references veiculos (id),
    constraint ck_ordens_servico_status check (
        status in ('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECA', 'FINALIZADA', 'ENTREGUE', 'CANCELADA')
    )
);

create table itens_ordem_servico (
    id uuid primary key,
    ordem_servico_id uuid not null,
    tipo varchar(20) not null,
    descricao varchar(200) not null,
    quantidade numeric(10, 3) not null,
    valor_unitario numeric(12, 2) not null,
    constraint fk_itens_ordem_servico_ordem foreign key (ordem_servico_id)
        references ordens_servico (id) on delete cascade,
    constraint ck_itens_ordem_servico_tipo check (tipo in ('SERVICO', 'PECA')),
    constraint ck_itens_ordem_servico_quantidade check (quantidade > 0),
    constraint ck_itens_ordem_servico_valor_unitario check (valor_unitario > 0)
);

create index idx_ordens_servico_aberta_em on ordens_servico (aberta_em desc);
create index idx_ordens_servico_status on ordens_servico (status);
create index idx_itens_ordem_servico_ordem_id on itens_ordem_servico (ordem_servico_id);
