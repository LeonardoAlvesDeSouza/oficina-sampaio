package br.com.oficinasampaio.financeiro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PosicaoDeCaixaTest {

    @Test
    void saldoEEntradasMenosSaidas() {
        var posicao = new PosicaoDeCaixa(new BigDecimal("1200.00"), new BigDecimal("450.50"));

        assertEquals(new BigDecimal("749.50"), posicao.saldo());
    }

    @Test
    void caixaNegativoEUmResultadoValido() {
        var posicao = new PosicaoDeCaixa(new BigDecimal("100.00"), new BigDecimal("380.00"));

        assertEquals(new BigDecimal("-280.00"), posicao.saldo());
    }

    /** Caixa sem lançamento nenhum: a tela abre em zero, não quebra. */
    @Test
    void trataAusenciaDeMovimentacaoComoZero() {
        var posicao = new PosicaoDeCaixa(null, null);

        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), posicao.entradas()),
                () -> assertEquals(new BigDecimal("0.00"), posicao.saidas()),
                () -> assertEquals(new BigDecimal("0.00"), posicao.saldo())
        );
    }
}
