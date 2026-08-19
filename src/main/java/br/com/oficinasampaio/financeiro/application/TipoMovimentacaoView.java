package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.TipoMovimentacao;

public enum TipoMovimentacaoView {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String rotulo;

    TipoMovimentacaoView(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    static TipoMovimentacaoView de(TipoMovimentacao tipo) {
        return valueOf(tipo.name());
    }
}