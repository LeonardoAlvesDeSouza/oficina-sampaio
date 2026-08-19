package br.com.oficinasampaio.financeiro.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * A tela do caixa em uma resposta: a posição no topo e o histórico embaixo. O
 * saldo vem calculado do domínio, não somado aqui.
 */
public record CaixaView(
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldo,
        List<MovimentacaoView> movimentacoes
) {
}