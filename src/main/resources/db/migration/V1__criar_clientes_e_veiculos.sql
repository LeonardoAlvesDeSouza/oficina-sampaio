create table clientes (
    id uuid primary key,
    nome varchar(150) not null,
    cpf_cnpj varchar(14),
    telefone varchar(20),
    email varchar(150),
    ativo boolean not null default true,
    versao bigint not null default 0,
    constraint uk_clientes_cpf_cnpj unique (cpf_cnpj)
);

create table veiculos (
    id uuid primary key,
    cliente_id uuid not null,
    placa varchar(7) not null,
    marca varchar(80) not null,
    modelo varchar(100) not null,
    ano integer,
    cor varchar(50),
    quilometragem bigint,
    ativo boolean not null default true,
    versao bigint not null default 0,
    constraint uk_veiculos_placa unique (placa),
    constraint fk_veiculos_cliente foreign key (cliente_id) references clientes (id),
    constraint ck_veiculos_quilometragem check (quilometragem is null or quilometragem >= 0)
);

create index idx_veiculos_cliente_id on veiculos (cliente_id);
