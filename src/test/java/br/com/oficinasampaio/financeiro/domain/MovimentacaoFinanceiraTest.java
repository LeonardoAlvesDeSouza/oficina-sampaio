package br.com.oficinasampaio.financeiro.domain;

import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimentacaoFinanceiraTest {

    private static final Instant OCORRIDA_EM = Instant.parse("2026-08-12T18:30:00Z");

    @Test
    void entradaDePagamentoCitaONumeroCurtoDaOrdem() {
        var pagamentoId = UUID.randomUUID();
        var ordemServicoId = UUID.fromString("1a2b3c4d-0000-0000-0000-000000000000");

        var entrada = MovimentacaoFinanceira.entradaDePagamento(
                pagamentoId, ordemServicoId, new BigDecimal("300.00"), OCORRIDA_EM
        );

        assertAll(
                () -> assertEquals(TipoMovimentacao.ENTRADA, entrada.getTipo()),
                () -> assertEquals("Pagamento da OS 1a2b3c4d", entrada.getDescricao()),
                () -> assertEquals(new BigDecimal("300.00"), entrada.getValor()),
                () -> assertEquals(OCORRIDA_EM, entrada.getOcorridaEm()),
                () -> assertEquals(pagamentoId, entrada.getPagamentoId())
        );
    }

    @Test
    void saidaNaoTemPagamentoAtrasDela() {
        var saida = MovimentacaoFinanceira.saida(
                "  Jogo de pastilhas no fornecedor ", new BigDecimal("180.5"), OCORRIDA_EM
        );

        assertAll(
                () -> assertEquals(TipoMovimentacao.SAIDA, saida.getTipo()),
                () -> assertEquals("Jogo de pastilhas no fornecedor", saida.getDescricao()),
                // O valor é positivo mesmo saindo: o sinal é o tipo.
                () -> assertEquals(new BigDecimal("180.50"), saida.getValor()),
                () -> assertNull(saida.getPagamentoId())
        );
    }

    @Test
    void recusaMovimentacaoSemDescricao() {
        var erro = assertThrows(RegraNegocioException.class, () ->
                MovimentacaoFinanceira.saida("   ", new BigDecimal("10.00"), OCORRIDA_EM)
        );

        assertEquals("Descrição da movimentação é obrigatória", erro.getMessage());
    }

    @Test
    void recusaValorNaoPositivo() {
        var erro = assertThrows(RegraNegocioException.class, () ->
                MovimentacaoFinanceira.saida("Conta de luz", BigDecimal.ZERO, OCORRIDA_EM)
        );

        assertEquals("Valor da movimentação deve ser positivo", erro.getMessage());
    }

    @Test
    void recusaValorQueArredondariaParaZero() {
        var erro = assertThrows(RegraNegocioException.class, () ->
                MovimentacaoFinanceira.saida("Arruela", new BigDecimal("0.001"), OCORRIDA_EM)
        );

        assertEquals("Valor da movimentação deve ser positivo", erro.getMessage());
    }

    @Test
    void entradaExigePagamentoEOrdem() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        MovimentacaoFinanceira.entradaDePagamento(
                                null, UUID.randomUUID(), BigDecimal.TEN, OCORRIDA_EM
                        )),
                () -> assertThrows(NullPointerException.class, () ->
                        MovimentacaoFinanceira.entradaDePagamento(
                                UUID.randomUUID(), null, BigDecimal.TEN, OCORRIDA_EM
                        ))
        );
    }
}
