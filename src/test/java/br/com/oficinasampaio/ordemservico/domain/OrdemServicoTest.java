package br.com.oficinasampaio.ordemservico.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoTest {

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

        var erro = assertThrows(IllegalArgumentException.class, () ->
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

        var erro = assertThrows(IllegalArgumentException.class, () ->
                ordem.adicionarPeca("Arruela", BigDecimal.ONE, new BigDecimal("0.001"))
        );

        assertEquals("Valor unitário deve ser positivo", erro.getMessage());
        assertEquals(0, ordem.getItens().size());
    }

    @Test
    void iniciaExecucaoComItensEBloqueiaNovasAlteracoes() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );
        ordem.adicionarServico("Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00"));

        ordem.iniciarExecucao();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        var erro = assertThrows(IllegalStateException.class, () ->
                ordem.adicionarPeca("Filtro", BigDecimal.ONE, new BigDecimal("35.00"))
        );
        assertEquals("Itens só podem ser alterados enquanto a ordem está aberta", erro.getMessage());
    }

    @Test
    void impedeIniciarExecucaoSemItens() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        var erro = assertThrows(IllegalStateException.class, ordem::iniciarExecucao);

        assertEquals("Inclua ao menos um item antes de iniciar a execução", erro.getMessage());
        assertEquals(StatusOrdemServico.ABERTA, ordem.getStatus());
    }

    @Test
    void percorreFluxoDeEsperaRetomadaFinalizacaoEEntrega() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );
        ordem.adicionarServico("Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00"));
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

        var ordemEmExecucao = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );
        ordemEmExecucao.adicionarServico("Diagnóstico", BigDecimal.ONE, new BigDecimal("90.00"));
        ordemEmExecucao.iniciarExecucao();
        ordemEmExecucao.cancelar();
        assertEquals(StatusOrdemServico.CANCELADA, ordemEmExecucao.getStatus());

        var erro = assertThrows(IllegalStateException.class, ordemEmExecucao::cancelar);
        assertEquals("A ordem entregue ou cancelada não pode ser cancelada", erro.getMessage());
    }
}
