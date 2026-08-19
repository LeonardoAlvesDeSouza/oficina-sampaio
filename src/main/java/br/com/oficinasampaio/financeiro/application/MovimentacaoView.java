package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimentacaoView(
        UUID id,
        TipoMovimentacaoView tipo,
        String descricao,
        BigDecimal valor,
        Instant ocorridaEm
) {

    static MovimentacaoView de(MovimentacaoFinanceira movimentacao) {
        return new MovimentacaoView(
                movimentacao.getId(),
                TipoMovimentacaoView.de(movimentacao.getTipo()),
                movimentacao.getDescricao(),
                movimentacao.getValor(),
                movimentacao.getOcorridaEm()
        );
    }
}