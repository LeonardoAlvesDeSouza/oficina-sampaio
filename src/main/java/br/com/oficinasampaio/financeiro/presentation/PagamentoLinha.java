package br.com.oficinasampaio.financeiro.presentation;

import br.com.oficinasampaio.cliente.application.ClienteView;
import br.com.oficinasampaio.financeiro.application.FormaPagamentoView;
import br.com.oficinasampaio.financeiro.application.PagamentoView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.math.BigDecimal;
import java.util.UUID;

/** Um pagamento já recebido, com a ordem que ele quitou. */
public record PagamentoLinha(
        UUID ordemServicoId,
        String numeroOrdem,
        String cliente,
        FormaPagamentoView forma,
        BigDecimal valor,
        String registradoEm
) {

    static PagamentoLinha de(PagamentoView pagamento, ClienteView cliente) {
        return new PagamentoLinha(
                pagamento.ordemServicoId(),
                FormatoOficina.numeroOrdem(pagamento.ordemServicoId()),
                cliente.nome(),
                pagamento.forma(),
                pagamento.valor(),
                FormatoOficina.dataHora(pagamento.registradoEm())
        );
    }
}
