package br.com.oficinasampaio.cliente.application;

import br.com.oficinasampaio.cliente.domain.Cliente;

import java.util.UUID;

public record ClienteView(
        UUID id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        boolean ativo
) {

    static ClienteView de(Cliente cliente) {
        return new ClienteView(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.isAtivo()
        );
    }
}
