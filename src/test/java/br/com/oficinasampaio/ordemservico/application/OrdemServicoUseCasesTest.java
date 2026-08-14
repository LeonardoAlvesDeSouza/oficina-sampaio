package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.cliente.application.ClienteQueries;
import br.com.oficinasampaio.ordemservico.domain.ItemOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.veiculo.application.VeiculoParaOrdem;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var ordem = abrirOrdem(repositorio, "Revisão preventiva");

        var detalhe = new AdicionarItemOrdemServico(repositorio).executar(
                new AdicionarItemOrdemServicoCommand(
                        ordem.id(), TipoItemOrdemServicoView.SERVICO,
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
    void removeItemDaOrdemAbertaEDevolveOsTotaisAtualizados() {
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var ordem = abrirOrdem(repositorio, "Revisão preventiva");
        var adicionarItem = new AdicionarItemOrdemServico(repositorio);
        adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO,
                "Alinhamento", BigDecimal.ONE, new BigDecimal("120.00")
        ));
        var comPeca = adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.PECA,
                "Amortecedor", BigDecimal.ONE, new BigDecimal("350.00")
        ));
        var pecaId = comPeca.itens().getLast().id();

        var semPeca = new RemoverItemOrdemServico(repositorio).executar(
                new RemoverItemOrdemServicoCommand(ordem.id(), pecaId)
        );

        assertAll(
                () -> assertEquals(1, semPeca.itens().size()),
                () -> assertEquals("Alinhamento", semPeca.itens().getFirst().descricao()),
                () -> assertEquals(new BigDecimal("0.00"), semPeca.totalPecas()),
                () -> assertEquals(new BigDecimal("120.00"), semPeca.total())
        );
    }

    @Test
    void bloqueiaRemocaoDeItemAposFinalizar() {
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var ordem = abrirOrdem(repositorio, "Revisão");
        var adicionarItem = new AdicionarItemOrdemServico(repositorio);
        adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO,
                "Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00")
        ));
        var comPeca = adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.PECA,
                "Filtro de óleo", BigDecimal.ONE, new BigDecimal("35.00")
        ));
        var pecaId = comPeca.itens().getLast().id();
        var alterarStatus = new AlterarStatusOrdemServico(repositorio);
        alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.INICIAR_EXECUCAO
        ));
        alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.FINALIZAR
        ));

        var removerItem = new RemoverItemOrdemServico(repositorio);
        var erro = assertThrows(RegraNegocioException.class, () ->
                removerItem.executar(new RemoverItemOrdemServicoCommand(ordem.id(), pecaId))
        );

        assertEquals(
                "Itens só podem ser alterados enquanto a ordem não está finalizada",
                erro.getMessage()
        );
        assertEquals(2, new BuscarOrdemServico(repositorio).executar(ordem.id()).itens().size());
    }

    @Test
    void naoEncontraOrdemDeOutroIdentificador() {
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        abrirOrdem(repositorio, "Revisão preventiva");

        var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
                new BuscarOrdemServico(repositorio).executar(UUID.randomUUID())
        );

        assertEquals("Ordem de serviço não encontrada", erro.getMessage());
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

    @Test
    void alteraStatusEInformaAsProximasAcoesDisponiveis() {
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var ordem = abrirOrdem(repositorio, "Revisão");

        var semItens = new BuscarOrdemServico(repositorio).executar(ordem.id());
        assertAll(
                () -> assertEquals(List.of(AcaoOrdemServicoView.CANCELAR), semItens.acoesDisponiveis()),
                () -> assertEquals(true, semItens.permiteAlterarItens())
        );

        var comItem = new AdicionarItemOrdemServico(repositorio).executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO,
                "Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00")
        ));
        assertEquals(
                List.of(AcaoOrdemServicoView.INICIAR_EXECUCAO, AcaoOrdemServicoView.CANCELAR),
                comItem.acoesDisponiveis()
        );
        var alterarStatus = new AlterarStatusOrdemServico(repositorio);

        var emExecucao = alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.INICIAR_EXECUCAO
        ));
        assertAll(
                () -> assertEquals(StatusOrdemServicoView.EM_EXECUCAO, emExecucao.status()),
                () -> assertEquals(true, emExecucao.permiteAlterarItens()),
                () -> assertEquals(
                        List.of(
                                AcaoOrdemServicoView.AGUARDAR_PECA,
                                AcaoOrdemServicoView.FINALIZAR,
                                AcaoOrdemServicoView.CANCELAR
                        ),
                        emExecucao.acoesDisponiveis()
                )
        );

        var aguardando = alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.AGUARDAR_PECA
        ));
        assertEquals(StatusOrdemServicoView.AGUARDANDO_PECA, aguardando.status());
        assertEquals(
                List.of(AcaoOrdemServicoView.RETOMAR_EXECUCAO, AcaoOrdemServicoView.CANCELAR),
                aguardando.acoesDisponiveis()
        );
    }

    @Test
    void lancaPecaRecebidaDuranteAEsperaEBloqueiaAposFinalizar() {
        var repositorio = new OrdemServicoRepositoryEmMemoria();
        var ordem = abrirOrdem(repositorio, "Revisão");
        var adicionarItem = new AdicionarItemOrdemServico(repositorio);
        var alterarStatus = new AlterarStatusOrdemServico(repositorio);
        adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO,
                "Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00")
        ));
        alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.INICIAR_EXECUCAO
        ));
        alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.AGUARDAR_PECA
        ));

        var comPeca = adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.PECA,
                "Sensor de rotação", BigDecimal.ONE, new BigDecimal("210.00")
        ));
        assertEquals(new BigDecimal("300.00"), comPeca.total());

        alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.RETOMAR_EXECUCAO
        ));
        var finalizada = alterarStatus.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.FINALIZAR
        ));
        assertEquals(false, finalizada.permiteAlterarItens());

        var erro = assertThrows(RegraNegocioException.class, () ->
                adicionarItem.executar(new AdicionarItemOrdemServicoCommand(
                        ordem.id(), TipoItemOrdemServicoView.PECA,
                        "Filtro de óleo", BigDecimal.ONE, new BigDecimal("35.00")
                ))
        );
        assertEquals(
                "Itens só podem ser alterados enquanto a ordem não está finalizada",
                erro.getMessage()
        );
    }

    private static OrdemServicoView abrirOrdem(
            OrdemServicoRepository repositorio,
            String relatoProblema
    ) {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        VeiculoQueries veiculos = id -> Optional.of(new VeiculoParaOrdem(
                veiculoId, clienteId, "ABC1D23", "Volkswagen", "Gol", true
        ));
        return new AbrirOrdemServico(
                id -> true,
                veiculos,
                repositorio,
                Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC)
        ).executar(new AbrirOrdemServicoCommand(veiculoId, relatoProblema));
    }

    /**
     * Simula a geração de identificadores do banco para que a busca por id seja
     * exercitada de verdade pelos casos de uso.
     */
    private static final class OrdemServicoRepositoryEmMemoria implements OrdemServicoRepository {

        private static final Field CAMPO_ID = campoId();
        private static final Field CAMPO_ID_ITEM = campoIdItem();

        private final Map<UUID, OrdemServico> ordens = new LinkedHashMap<>();

        @Override
        public OrdemServico salvar(OrdemServico ordemServico) {
            var id = ordemServico.getId() != null ? ordemServico.getId() : gerarId(ordemServico);
            identificarItens(ordemServico);
            ordens.put(id, ordemServico);
            return ordemServico;
        }

        @Override
        public Optional<OrdemServico> buscarPorId(UUID ordemServicoId) {
            return Optional.ofNullable(ordens.get(ordemServicoId));
        }

        @Override
        public List<OrdemServico> listar() {
            return List.copyOf(ordens.values());
        }

        private static UUID gerarId(OrdemServico ordemServico) {
            var id = UUID.randomUUID();
            try {
                CAMPO_ID.set(ordemServico, id);
            } catch (IllegalAccessException erro) {
                throw new IllegalStateException("Não foi possível atribuir o id da ordem", erro);
            }
            return id;
        }

        /**
         * O item também só ganha identificador ao ser gravado. Sem isso a
         * remoção por id não teria como ser exercitada fora do banco.
         */
        private static void identificarItens(OrdemServico ordemServico) {
            ordemServico.getItens().stream()
                    .filter(item -> item.getId() == null)
                    .forEach(item -> {
                        try {
                            CAMPO_ID_ITEM.set(item, UUID.randomUUID());
                        } catch (IllegalAccessException erro) {
                            throw new IllegalStateException("Não foi possível atribuir o id do item", erro);
                        }
                    });
        }

        private static Field campoId() {
            return campo(OrdemServico.class, "OrdemServico");
        }

        private static Field campoIdItem() {
            return campo(ItemOrdemServico.class, "ItemOrdemServico");
        }

        private static Field campo(Class<?> tipo, String nome) {
            try {
                var campo = tipo.getDeclaredField("id");
                campo.setAccessible(true);
                return campo;
            } catch (NoSuchFieldException erro) {
                throw new IllegalStateException("Campo id não encontrado em " + nome, erro);
            }
        }
    }
}
