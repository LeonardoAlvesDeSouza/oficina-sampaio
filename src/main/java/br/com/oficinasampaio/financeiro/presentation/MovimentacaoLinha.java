package br.com.oficinasampaio.financeiro.presentation;

import br.com.oficinasampaio.financeiro.application.MovimentacaoView;
import br.com.oficinasampaio.financeiro.application.TipoMovimentacaoView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.math.BigDecimal;
import java.util.List;

/** Uma linha do extrato do caixa. */
public record MovimentacaoLinha(
        TipoMovimentacaoView tipo,
        String descricao,
        BigDecimal valor,
        String ocorridaEm
) {

    static List<MovimentacaoLinha> de(List<MovimentacaoView> movimentacoes) {
        return movimentacoes.stream()
                .map(movimentacao -> new MovimentacaoLinha(
                        movimentacao.tipo(),
                        movimentacao.descricao(),
                        movimentacao.valor(),
                        FormatoOficina.dataHora(movimentacao.ocorridaEm())
                ))
                .toList();
    }
}
