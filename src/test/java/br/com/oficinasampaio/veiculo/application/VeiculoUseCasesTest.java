package br.com.oficinasampaio.veiculo.application;

import br.com.oficinasampaio.cliente.application.ClienteQueries;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.veiculo.domain.Veiculo;
import br.com.oficinasampaio.veiculo.domain.VeiculoRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VeiculoUseCasesTest {

    @Test
    void veiculoCadastradoParaClienteAtivoPodeSerConsultado() {
        var clienteId = UUID.randomUUID();
        ClienteQueries clientes = id -> clienteId.equals(id);
        var repositorio = new VeiculoRepositoryEmMemoria();
        var cadastrarVeiculo = new CadastrarVeiculo(clientes, repositorio);
        var listarVeiculos = new ListarVeiculosDoCliente(repositorio);

        cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                clienteId, "abc-1d23", "Volkswagen", "Gol",
                2022, "Prata", 35_000L
        ));

        var veiculos = listarVeiculos.executar(clienteId);

        assertEquals(1, veiculos.size());
        var veiculo = veiculos.getFirst();
        assertAll(
                () -> assertEquals(clienteId, veiculo.clienteId()),
                () -> assertEquals("ABC1D23", veiculo.placa()),
                () -> assertEquals("Volkswagen", veiculo.marca()),
                () -> assertEquals("Gol", veiculo.modelo())
        );
    }

    @Test
    void impedeCadastroDuplicadoDaMesmaPlaca() {
        var clienteId = UUID.randomUUID();
        ClienteQueries clientes = id -> true;
        var repositorio = new VeiculoRepositoryEmMemoria();
        var cadastrarVeiculo = new CadastrarVeiculo(clientes, repositorio);

        cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                clienteId, "ABC-1D23", "Volkswagen", "Gol",
                2022, null, 35_000L
        ));

        var erro = assertThrows(RegraNegocioException.class, () ->
                cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                        clienteId, "abc1d23", "Volkswagen", "Polo",
                        2023, null, 10_000L
                ))
        );

        assertEquals("Placa já cadastrada", erro.getMessage());
        assertEquals(1, new ListarVeiculosDoCliente(repositorio).executar(clienteId).size());
    }

    @Test
    void impedeCadastroParaClienteInativoOuInexistente() {
        var clienteId = UUID.randomUUID();
        ClienteQueries clientes = id -> false;
        var cadastrarVeiculo = new CadastrarVeiculo(clientes, new VeiculoRepositoryEmMemoria());

        var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
                cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                        clienteId, "ABC-1D23", "Volkswagen", "Gol",
                        2022, null, 35_000L
                ))
        );

        assertEquals("Cliente ativo não encontrado", erro.getMessage());
    }

    private static final class VeiculoRepositoryEmMemoria implements VeiculoRepository {

        private final List<Veiculo> veiculos = new ArrayList<>();

        @Override
        public Veiculo salvar(Veiculo veiculo) {
            veiculos.add(veiculo);
            return veiculo;
        }

        @Override
        public boolean existePorPlaca(String placa) {
            return veiculos.stream().anyMatch(veiculo -> placa.equals(veiculo.getPlaca()));
        }

        @Override
        public List<Veiculo> listarPorCliente(UUID clienteId) {
            return veiculos.stream()
                    .filter(veiculo -> clienteId.equals(veiculo.getClienteId()))
                    .toList();
        }
    }
}
