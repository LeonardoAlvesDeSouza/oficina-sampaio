package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.financeiro.application.FormaPagamentoView;
import br.com.oficinasampaio.financeiro.application.PagamentoDaOrdem;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.math.BigDecimal;

/**
 * O pagamento da ordem já pronto para a tela: a data vira horário de Brasília
 * aqui, e não no template.
 */
public record ContaDaOrdem(FormaPagamentoView forma, BigDecimal valor, String recebidoEm) {

    static ContaDaOrdem de(PagamentoDaOrdem pagamento) {
        return new ContaDaOrdem(
                pagamento.forma(),
                pagamento.valor(),
                FormatoOficina.dataHora(pagamento.registradoEm())
        );
    }
}
