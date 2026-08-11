create table usuarios (
    id uuid primary key,
    nome varchar(120) not null,
    login varchar(80) not null,
    senha_hash varchar(100) not null,
    perfil varchar(20) not null,
    ativo boolean not null default true,
    versao bigint not null default 0,
    constraint uk_usuarios_login unique (login),
    constraint ck_usuarios_perfil check (perfil in ('ADMIN', 'FUNCIONARIO'))
);

create index idx_usuarios_ativo on usuarios (ativo);
