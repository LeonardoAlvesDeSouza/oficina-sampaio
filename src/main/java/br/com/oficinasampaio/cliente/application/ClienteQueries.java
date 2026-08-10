package br.com.oficinasampaio.cliente.application;

import java.util.UUID;

@FunctionalInterface
public interface ClienteQueries {

    boolean existeClienteAtivo(UUID clienteId);
}
