package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.cliente.application.ClienteQueries;
import br.com.oficinasampaio.ordemservico.domain.OrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.veiculo.application.VeiculoParaOrdem;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoUseCasesTest {

    @Test
    void ordemAbertaParaVeiculoEClienteAtivosPodeSerConsultada() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        VeiculoQueries veiculos = id -> Optional.of(new VeiculoParaOrdem(
                veiculoId, clienteId, "ABC1D23", "Volkswagen", "Gol", true
        ));
        ClienteQueries clientes = id -> clienteId.equals(id);
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var relogio = Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC);
        var abrir = new AbrirOrdemServico(clientes, veiculos, repositorio, relogio);

        abrir.executar(new AbrirOrdemServicoCommand(
                veiculoId, "Ruído na suspensão dianteira"
        ));

        var ordens = new ListarOrdensServico(repositorio).executar();

        assertEquals(1, ordens.size());
        var ordem = ordens.getFirst();
        assertAll(
                () -> assertEquals(clienteId, ordem.clienteId()),
                () -> assertEquals(veiculoId, ordem.veiculoId()),
                () -> assertEquals("Ruído na suspensão dianteira", ordem.relatoProblema()),
                () -> assertEquals(Instant.parse("2026-08-11T12:00:00Z"), ordem.abertaEm()),
                () -> assertEquals(StatusOrdemServicoView.ABERTA, ordem.status()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.total())
        );
    }

    @Test
    void servicoEAdicionadoAOrdemAbertaECompõeOTotalDoDetalhe() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        VeiculoQueries veiculos = id -> Optional.of(new VeiculoParaOrdem(
                veiculoId, clienteId, "ABC1D23", "Volkswagen", "Gol", true
        ));
        ClienteQueries clientes = id -> true;
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var relogio = Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC);
        new AbrirOrdemServico(clientes, veiculos, repositorio, relogio).executar(
                new AbrirOrdemServicoCommand(veiculoId, "Revisão preventiva")
        );

        var detalhe = new AdicionarItemOrdemServico(repositorio).executar(
                new AdicionarItemOrdemServicoCommand(
                        UUID.randomUUID(), TipoItemOrdemServicoView.SERVICO,
                        "Alinhamento", new BigDecimal("1"), new BigDecimal("120.00")
                )
        );

        assertAll(
                () -> assertEquals(1, detalhe.itens().size()),
                () -> assertEquals(TipoItemOrdemServicoView.SERVICO, detalhe.itens().getFirst().tipo()),
                () -> assertEquals("Alinhamento", detalhe.itens().getFirst().descricao()),
                () -> assertEquals(new BigDecimal("120.00"), detalhe.totalServicos()),
                () -> assertEquals(new BigDecimal("120.00"), detalhe.total())
        );
    }

    @Test
    void impedeAberturaParaVeiculoInativo() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        VeiculoQueries veiculos = id -> Optional.of(new VeiculoParaOrdem(
                veiculoId, clienteId, "ABC1D23", "Volkswagen", "Gol", false
        ));
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var abrir = new AbrirOrdemServico(
                id -> true,
                veiculos,
                repositorio,
                Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC)
        );

        var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
                abrir.executar(new AbrirOrdemServicoCommand(veiculoId, "Revisão"))
        );

        assertEquals("Veículo ativo não encontrado", erro.getMessage());
        assertEquals(0, repositorio.listar().size());
    }

    @Test
    void impedeAberturaParaClienteInativo() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        VeiculoQueries veiculos = id -> Optional.of(new VeiculoParaOrdem(
                veiculoId, clienteId, "ABC1D23", "Volkswagen", "Gol", true
        ));
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var abrir = new AbrirOrdemServico(
                id -> false,
                veiculos,
                repositorio,
                Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC)
        );

        var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
                abrir.executar(new AbrirOrdemServicoCommand(veiculoId, "Revisão"))
        );

        assertEquals("Cliente ativo não encontrado", erro.getMessage());
        assertEquals(0, repositorio.listar().size());
    }

    private static final class OrdemServicoRepositoryEmMemoria implements OrdemServicoRepository {

        private final List<OrdemServico> ordens = new ArrayList<>();

        @Override
        public OrdemServico salvar(OrdemServico ordemServico) {
            if (!ordens.contains(ordemServico)) {
                ordens.add(ordemServico);
            }
            return ordemServico;
        }

        @Override
        public Optional<OrdemServico> buscarPorId(UUID ordemServicoId) {
            return ordens.stream().findFirst();
        }

        @Override
        public List<OrdemServico> listar() {
            return List.copyOf(ordens);
        }
    }
}
