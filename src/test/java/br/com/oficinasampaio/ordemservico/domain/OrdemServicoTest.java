package br.com.oficinasampaio.ordemservico.domain;

import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoTest {

    private static final String ITENS_BLOQUEADOS =
            "Itens só podem ser alterados enquanto a ordem não está finalizada";

    @Test
    void abreOrdemSemItensComStatusAberta() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var abertaEm = Instant.parse("2026-08-11T12:00:00Z");

        var ordem = OrdemServico.abrir(
                clienteId, veiculoId, "  Ruído na suspensão dianteira ", abertaEm
        );

        assertAll(
                () -> assertEquals(clienteId, ordem.getClienteId()),
                () -> assertEquals(veiculoId, ordem.getVeiculoId()),
                () -> assertEquals("Ruído na suspensão dianteira", ordem.getRelatoProblema()),
                () -> assertEquals(abertaEm, ordem.getAbertaEm()),
                () -> assertEquals(StatusOrdemServico.ABERTA, ordem.getStatus()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotalServicos()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotalPecas()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotal())
        );
    }

    @Test
    void adicionaServicosEPecasECalculaTotais() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Revisão da suspensão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        ordem.adicionarServico("Alinhamento", new BigDecimal("1.5"), new BigDecimal("120.00"));
        ordem.adicionarPeca("Amortecedor dianteiro", new BigDecimal("2"), new BigDecimal("350.50"));

        assertAll(
                () -> assertEquals(2, ordem.getItens().size()),
                () -> assertEquals(TipoItemOrdemServico.SERVICO, ordem.getItens().get(0).getTipo()),
                () -> assertEquals(TipoItemOrdemServico.PECA, ordem.getItens().get(1).getTipo()),
                () -> assertEquals(new BigDecimal("180.00"), ordem.getTotalServicos()),
                () -> assertEquals(new BigDecimal("701.00"), ordem.getTotalPecas()),
                () -> assertEquals(new BigDecimal("881.00"), ordem.getTotal())
        );
    }

    @Test
    void impedeItemComQuantidadeNaoPositiva() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        var erro = assertThrows(RegraNegocioException.class, () ->
                ordem.adicionarServico("Alinhamento", BigDecimal.ZERO, new BigDecimal("120.00"))
        );

        assertEquals("Quantidade deve ser positiva", erro.getMessage());
        assertEquals(0, ordem.getItens().size());
    }

    @Test
    void impedeValorUnitarioQueArredondariaParaZero() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        var erro = assertThrows(RegraNegocioException.class, () ->
                ordem.adicionarPeca("Arruela", BigDecimal.ONE, new BigDecimal("0.001"))
        );

        assertEquals("Valor unitário deve ser positivo", erro.getMessage());
        assertEquals(0, ordem.getItens().size());
    }

    @Test
    void iniciaExecucaoComItensEMantemItensEditaveis() {
        var ordem = ordemComServico();

        ordem.iniciarExecucao();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        ordem.adicionarPeca("Filtro de óleo", BigDecimal.ONE, new BigDecimal("35.00"));
        assertEquals(2, ordem.getItens().size());
    }

    @Test
    void lancaPecaRecebidaDuranteAEsperaECompoeOTotal() {
        var ordem = ordemComServico();
        ordem.iniciarExecucao();
        ordem.aguardarPeca();

        ordem.adicionarPeca("Sensor de rotação", BigDecimal.ONE, new BigDecimal("210.00"));

        assertAll(
                () -> assertEquals(StatusOrdemServico.AGUARDANDO_PECA, ordem.getStatus()),
                () -> assertEquals(new BigDecimal("90.00"), ordem.getTotalServicos()),
                () -> assertEquals(new BigDecimal("210.00"), ordem.getTotalPecas()),
                () -> assertEquals(new BigDecimal("300.00"), ordem.getTotal())
        );
    }

    @Test
    void bloqueiaAlteracaoDeItensAposFinalizar() {
        var ordem = ordemComServico();
        ordem.iniciarExecucao();
        ordem.finalizar();

        var erro = assertThrows(RegraNegocioException.class, () ->
                ordem.adicionarPeca("Filtro de óleo", BigDecimal.ONE, new BigDecimal("35.00"))
        );

        assertEquals(ITENS_BLOQUEADOS, erro.getMessage());
        assertEquals(1, ordem.getItens().size());
    }

    @Test
    void bloqueiaAlteracaoDeItensNaOrdemCancelada() {
        var ordem = ordemComServico();
        ordem.cancelar();

        var erro = assertThrows(RegraNegocioException.class, () ->
                ordem.adicionarServico("Troca de óleo", BigDecimal.ONE, new BigDecimal("80.00"))
        );

        assertEquals(ITENS_BLOQUEADOS, erro.getMessage());
        assertEquals(1, ordem.getItens().size());
    }

    @Test
    void removeItemLancadoPorEngano_eRecalculaOsTotais() {
        var ordem = ordemComServico();
        ordem.adicionarPeca("Amortecedor", new BigDecimal("2"), new BigDecimal("350.50"));
        var pecaId = idDoItem(ordem, 1);

        ordem.removerItem(pecaId);

        assertAll(
                () -> assertEquals(1, ordem.getItens().size()),
                () -> assertEquals("Diagnóstico", ordem.getItens().getFirst().getDescricao()),
                () -> assertEquals(new BigDecimal("90.00"), ordem.getTotalServicos()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotalPecas()),
                () -> assertEquals(new BigDecimal("90.00"), ordem.getTotal())
        );
    }

    @Test
    void removeUltimoItemEnquantoAOrdemEstaAberta() {
        var ordem = ordemComServico();

        ordem.removerItem(idDoItem(ordem, 0));

        assertAll(
                () -> assertEquals(0, ordem.getItens().size()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotal())
        );
    }

    @Test
    void removeItemDaOrdemQueAguardaPeca() {
        var ordem = ordemComServico();
        ordem.adicionarPeca("Sensor de rotação", BigDecimal.ONE, new BigDecimal("210.00"));
        ordem.iniciarExecucao();
        ordem.aguardarPeca();

        ordem.removerItem(idDoItem(ordem, 1));

        assertEquals(1, ordem.getItens().size());
        assertEquals(new BigDecimal("90.00"), ordem.getTotal());
    }

    @Test
    void impedeEsvaziarOrdemJaEmAndamento() {
        var ordem = ordemComServico();
        ordem.iniciarExecucao();
        var servicoId = idDoItem(ordem, 0);

        var erro = assertThrows(RegraNegocioException.class, () -> ordem.removerItem(servicoId));

        assertEquals("A ordem já em andamento precisa manter ao menos um item", erro.getMessage());
        assertEquals(1, ordem.getItens().size());
    }

    @Test
    void bloqueiaRemocaoDeItemAposFinalizar() {
        var ordem = ordemComServico();
        ordem.adicionarPeca("Filtro de óleo", BigDecimal.ONE, new BigDecimal("35.00"));
        ordem.iniciarExecucao();
        ordem.finalizar();
        var pecaId = idDoItem(ordem, 1);

        var erro = assertThrows(RegraNegocioException.class, () -> ordem.removerItem(pecaId));

        assertEquals(ITENS_BLOQUEADOS, erro.getMessage());
        assertEquals(2, ordem.getItens().size());
    }

    @Test
    void bloqueiaRemocaoDeItemNaOrdemEntregue() {
        var ordem = ordemComServico();
        ordem.adicionarPeca("Filtro de óleo", BigDecimal.ONE, new BigDecimal("35.00"));
        ordem.iniciarExecucao();
        ordem.finalizar();
        ordem.entregar();
        var pecaId = idDoItem(ordem, 1);

        var erro = assertThrows(RegraNegocioException.class, () -> ordem.removerItem(pecaId));

        assertEquals(ITENS_BLOQUEADOS, erro.getMessage());
        assertEquals(2, ordem.getItens().size());
    }

    @Test
    void bloqueiaRemocaoDeItemNaOrdemCancelada() {
        var ordem = ordemComServico();
        ordem.cancelar();
        var servicoId = idDoItem(ordem, 0);

        var erro = assertThrows(RegraNegocioException.class, () -> ordem.removerItem(servicoId));

        assertEquals(ITENS_BLOQUEADOS, erro.getMessage());
        assertEquals(1, ordem.getItens().size());
    }

    @Test
    void recusaRemocaoDeItemDeOutraOrdem() {
        var ordem = ordemComServico();
        var idDesconhecido = UUID.randomUUID();

        var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
                ordem.removerItem(idDesconhecido)
        );

        assertEquals("Item não encontrado nesta ordem de serviço", erro.getMessage());
        assertEquals(1, ordem.getItens().size());
    }

    @Test
    void impedeIniciarExecucaoSemItens() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        var erro = assertThrows(RegraNegocioException.class, ordem::iniciarExecucao);

        assertEquals("Inclua ao menos um item antes de iniciar a execução", erro.getMessage());
        assertEquals(StatusOrdemServico.ABERTA, ordem.getStatus());
    }

    @Test
    void percorreFluxoDeEsperaRetomadaFinalizacaoEEntrega() {
        var ordem = ordemComServico();
        ordem.iniciarExecucao();

        ordem.aguardarPeca();
        assertEquals(StatusOrdemServico.AGUARDANDO_PECA, ordem.getStatus());

        ordem.retomarExecucao();
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());

        ordem.finalizar();
        assertEquals(StatusOrdemServico.FINALIZADA, ordem.getStatus());

        ordem.entregar();
        assertEquals(StatusOrdemServico.ENTREGUE, ordem.getStatus());
    }

    @Test
    void cancelaOrdemAntesDaEntregaETornaEstadoTerminal() {
        var ordemAberta = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );
        ordemAberta.cancelar();
        assertEquals(StatusOrdemServico.CANCELADA, ordemAberta.getStatus());

        var ordemEmExecucao = ordemComServico();
        ordemEmExecucao.iniciarExecucao();
        ordemEmExecucao.cancelar();
        assertEquals(StatusOrdemServico.CANCELADA, ordemEmExecucao.getStatus());

        var erro = assertThrows(RegraNegocioException.class, ordemEmExecucao::cancelar);
        assertEquals("A ordem entregue ou cancelada não pode ser cancelada", erro.getMessage());
    }

    private static OrdemServico ordemComServico() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );
        ordem.adicionarServico("Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00"));
        return ordem;
    }

    /**
     * Fora do banco o item nasce sem id. Como a remoção é feita por
     * identificador, o teste simula aqui a geração que o JPA faria ao gravar.
     */
    private static UUID idDoItem(OrdemServico ordem, int posicao) {
        var item = ordem.getItens().get(posicao);
        if (item.getId() != null) {
            return item.getId();
        }
        var id = UUID.randomUUID();
        try {
            var campo = ItemOrdemServico.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(item, id);
        } catch (NoSuchFieldException | IllegalAccessException erro) {
            throw new IllegalStateException("Não foi possível atribuir o id do item", erro);
        }
        return id;
    }
}
