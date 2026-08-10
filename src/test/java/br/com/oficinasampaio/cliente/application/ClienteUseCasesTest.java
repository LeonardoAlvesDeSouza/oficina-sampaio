package br.com.oficinasampaio.cliente.application;

import br.com.oficinasampaio.cliente.domain.Cliente;
import br.com.oficinasampaio.cliente.domain.ClienteRepository;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteUseCasesTest {

    @Test
    void clienteCadastradoPodeSerConsultado() {
        var repositorio = new ClienteRepositoryEmMemoria();
        var cadastrarCliente = new CadastrarCliente(repositorio);
        var listarClientes = new ListarClientes(repositorio);

        cadastrarCliente.executar(new CadastrarClienteCommand(
                "  Maria da Silva  ",
                "529.982.247-25",
                "(11) 99999-8888",
                "MARIA@EXAMPLE.COM"
        ));

        var clientes = listarClientes.executar();

        assertEquals(1, clientes.size());
        var cliente = clientes.getFirst();
        assertAll(
                () -> assertEquals("Maria da Silva", cliente.nome()),
                () -> assertEquals("52998224725", cliente.cpfCnpj()),
                () -> assertEquals("11999998888", cliente.telefone()),
                () -> assertEquals("maria@example.com", cliente.email()),
                () -> assertTrue(cliente.ativo())
        );
    }

    @Test
    void impedeCadastroDuplicadoDoMesmoCpfCnpj() {
        var repositorio = new ClienteRepositoryEmMemoria();
        var cadastrarCliente = new CadastrarCliente(repositorio);

        cadastrarCliente.executar(new CadastrarClienteCommand(
                "Maria da Silva", "529.982.247-25", null, null
        ));

        var erro = assertThrows(RegraNegocioException.class, () ->
                cadastrarCliente.executar(new CadastrarClienteCommand(
                        "Outra Pessoa", "52998224725", null, null
                ))
        );

        assertEquals("CPF/CNPJ já cadastrado", erro.getMessage());
        assertEquals(1, new ListarClientes(repositorio).executar().size());
    }

    private static final class ClienteRepositoryEmMemoria implements ClienteRepository {

        private final List<Cliente> clientes = new ArrayList<>();

        @Override
        public Cliente salvar(Cliente cliente) {
            clientes.add(cliente);
            return cliente;
        }

        @Override
        public boolean existePorCpfCnpj(String cpfCnpj) {
            return clientes.stream().anyMatch(cliente -> cpfCnpj.equals(cliente.getCpfCnpj()));
        }

        @Override
        public Optional<Cliente> buscarPorId(UUID id) {
            return clientes.stream().filter(cliente -> id.equals(cliente.getId())).findFirst();
        }

        @Override
        public List<Cliente> listarTodos() {
            return List.copyOf(clientes);
        }
    }
}
