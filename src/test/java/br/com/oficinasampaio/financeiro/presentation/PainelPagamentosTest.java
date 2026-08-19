package br.com.oficinasampaio.financeiro.presentation;

import br.com.oficinasampaio.financeiro.application.FormaPagamentoView;
import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PainelPagamentosTest {

    @Test
    void somaOQueFaltaReceberEOQueJaEntrou() {
        var painel = PainelPagamentos.montar(
                List.of(
                        aReceber(new BigDecimal("310.00")),
                        aReceber(new BigDecimal("89.90"))
                ),
                List.of(
                        recebido(new BigDecimal("150.00")),
                        recebido(new BigDecimal("420.10"))
                )
        );

        assertAll(
                () -> assertEquals(new BigDecimal("399.90"), painel.totalAReceber()),
                () -> assertEquals(new BigDecimal("570.10"), painel.totalRecebido())
        );
    }

    /** Caixa do dia sem nada: os dois totais são zero, e não nulo na tela. */
    @Test
    void painelVazioSomaZero() {
        var painel = PainelPagamentos.montar(List.of(), List.of());

        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), painel.totalAReceber()),
                () -> assertEquals(new BigDecimal("0.00"), painel.totalRecebido())
        );
    }

    private static AReceberLinha aReceber(BigDecimal total) {
        return new AReceberLinha(
                UUID.randomUUID(), "1a2b3c4d", "ABC1D23", "Volkswagen Gol", "Maria da Silva",
                StatusOrdemServicoView.FINALIZADA, "11/08/26 às 09:00", total
        );
    }

    private static PagamentoLinha recebido(BigDecimal valor) {
        return new PagamentoLinha(
                UUID.randomUUID(), "1a2b3c4d", "Maria da Silva",
                FormaPagamentoView.PIX, valor, "12/08/26 às 15:30"
        );
    }
}
