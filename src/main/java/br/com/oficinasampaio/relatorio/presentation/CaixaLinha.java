package br.com.oficinasampaio.relatorio.presentation;

import br.com.oficinasampaio.financeiro.application.MovimentacaoView;
import br.com.oficinasampaio.financeiro.application.TipoMovimentacaoView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.math.BigDecimal;
import java.util.List;

/**
 * Uma linha do extrato impresso. Entrada e saída ocupam colunas diferentes e a que
 * não se aplica vem nula — em papel, coluna vazia se lê melhor que um sinal.
 */
public record CaixaLinha(
        String ocorridaEm,
        String descricao,
        BigDecimal entrada,
        BigDecimal saida
) {

    static List<CaixaLinha> de(List<MovimentacaoView> movimentacoes) {
        return movimentacoes.stream()
                .map(movimentacao -> {
                    var entrada = movimentacao.tipo() == TipoMovimentacaoView.ENTRADA;
                    return new CaixaLinha(
                            FormatoOficina.dataHora(movimentacao.ocorridaEm()),
                            movimentacao.descricao(),
                            entrada ? movimentacao.valor() : null,
                            entrada ? null : movimentacao.valor()
                    );
                })
                .toList();
    }
}
