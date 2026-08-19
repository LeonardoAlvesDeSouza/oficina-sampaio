package br.com.oficinasampaio.financeiro.domain;

import java.math.BigDecimal;

/**
 * O que entrou e o que saiu do caixa. O saldo é sempre calculado a partir dos
 * dois totais: não existe saldo guardado em lugar nenhum, justamente para não
 * haver duas versões da verdade quando uma movimentação for corrigida.
 */
public record PosicaoDeCaixa(BigDecimal entradas, BigDecimal saidas) {

    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

    public PosicaoDeCaixa(BigDecimal entradas, BigDecimal saidas) {
        this.entradas = entradas == null ? ZERO_MONETARIO : entradas;
        this.saidas = saidas == null ? ZERO_MONETARIO : saidas;
    }

    public BigDecimal saldo() {
        return entradas.subtract(saidas);
    }
}