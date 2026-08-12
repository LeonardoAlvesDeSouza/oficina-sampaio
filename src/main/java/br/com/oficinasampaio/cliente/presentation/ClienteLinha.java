package br.com.oficinasampaio.cliente.presentation;

import br.com.oficinasampaio.cliente.application.ClienteView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.util.List;
import java.util.UUID;

/**
 * Uma linha da lista de clientes, com documento e telefone já no formato que o
 * balcão lê — o cadastro guarda só dígitos, e ler "11988771200" em voz alta
 * para conferir com o cliente é pedir erro.
 */
public record ClienteLinha(
        UUID id,
        String nome,
        String documento,
        String telefone,
        String email
) {

    public static List<ClienteLinha> de(List<ClienteView> clientes) {
        return clientes.stream().map(ClienteLinha::de).toList();
    }

    private static ClienteLinha de(ClienteView cliente) {
        return new ClienteLinha(
                cliente.id(),
                cliente.nome(),
                FormatoOficina.documento(cliente.cpfCnpj()),
                FormatoOficina.telefone(cliente.telefone()),
                cliente.email()
        );
    }

    /** Cliente sem telefone nem e-mail: a célula de contato mostra um traço. */
    public boolean semContato() {
        return telefone == null && email == null;
    }
}