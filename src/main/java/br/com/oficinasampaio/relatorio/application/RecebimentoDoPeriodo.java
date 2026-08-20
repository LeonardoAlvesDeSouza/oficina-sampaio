package br.com.oficinasampaio.relatorio.application;

import br.com.oficinasampaio.financeiro.application.FormaPagamentoView;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Um recebimento do período, já com o nome do cliente resolvido. */
public record RecebimentoDoPeriodo(
        UUID ordemServicoId,
        String cliente,
        FormaPagamentoView forma,
        BigDecimal valor,
        Instant recebidoEm
) {
}
