package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.Pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagamentoView(
        UUID id,
        UUID ordemServicoId,
        UUID clienteId,
        FormaPagamentoView forma,
        BigDecimal valor,
        Instant registradoEm
) {

    static PagamentoView de(Pagamento pagamento) {
        return new PagamentoView(
                pagamento.getId(),
                pagamento.getOrdemServicoId(),
                pagamento.getClienteId(),
                FormaPagamentoView.de(pagamento.getForma()),
                pagamento.getValor(),
                pagamento.getRegistradoEm()
        );
    }
}