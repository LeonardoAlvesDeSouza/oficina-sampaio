package br.com.oficinasampaio.financeiro.domain;

import br.com.oficinasampaio.shared.domain.FormaPagamento;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagamentoTest {

    private static final Instant PAGO_EM = Instant.parse("2026-08-12T18:30:00Z");

    @Test
    void registraPagamentoDaOrdemComValorEmDuasCasas() {
        var ordemServicoId = UUID.randomUUID();
        var clienteId = UUID.randomUUID();

        var pagamento = Pagamento.registrar(
                ordemServicoId, clienteId, FormaPagamento.PIX, new BigDecimal("300"), PAGO_EM
        );

        assertAll(
                () -> assertEquals(ordemServicoId, pagamento.getOrdemServicoId()),
                () -> assertEquals(clienteId, pagamento.getClienteId()),
                () -> assertEquals(FormaPagamento.PIX, pagamento.getForma()),
                () -> assertEquals(new BigDecimal("300.00"), pagamento.getValor()),
                () -> assertEquals(PAGO_EM, pagamento.getRegistradoEm())
        );
    }

    @Test
    void recusaValorNaoPositivo() {
        var erro = assertThrows(RegraNegocioException.class, () -> Pagamento.registrar(
                UUID.randomUUID(), UUID.randomUUID(), FormaPagamento.DINHEIRO, BigDecimal.ZERO, PAGO_EM
        ));

        assertEquals("Valor do pagamento deve ser positivo", erro.getMessage());
    }

    @Test
    void recusaValorQueArredondariaParaZero() {
        var erro = assertThrows(RegraNegocioException.class, () -> Pagamento.registrar(
                UUID.randomUUID(), UUID.randomUUID(), FormaPagamento.DINHEIRO,
                new BigDecimal("0.001"), PAGO_EM
        ));

        assertEquals("Valor do pagamento deve ser positivo", erro.getMessage());
    }

    @Test
    void exigeOrdemClienteEFormaDePagamento() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> Pagamento.registrar(
                        null, UUID.randomUUID(), FormaPagamento.PIX, BigDecimal.TEN, PAGO_EM
                )),
                () -> assertThrows(NullPointerException.class, () -> Pagamento.registrar(
                        UUID.randomUUID(), null, FormaPagamento.PIX, BigDecimal.TEN, PAGO_EM
                )),
                () -> assertThrows(NullPointerException.class, () -> Pagamento.registrar(
                        UUID.randomUUID(), UUID.randomUUID(), null, BigDecimal.TEN, PAGO_EM
                )),
                () -> assertThrows(NullPointerException.class, () -> Pagamento.registrar(
                        UUID.randomUUID(), UUID.randomUUID(), FormaPagamento.PIX, BigDecimal.TEN, null
                ))
        );
    }
}
