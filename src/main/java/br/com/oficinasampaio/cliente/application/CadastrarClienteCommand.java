package br.com.oficinasampaio.cliente.application;

public record CadastrarClienteCommand(
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {
}
