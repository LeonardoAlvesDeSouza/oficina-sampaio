package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.shared.domain.FormaPagamento;

/**
 * As formas de pagamento com o nome que o balcão usa. A tradução para o domínio
 * é por {@code valueOf(name())}, guardada por teste de paridade.
 */
public enum FormaPagamentoView {
    DINHEIRO("Dinheiro"),
    PIX("PIX"),
    CARTAO_DEBITO("Cartão de débito"),
    CARTAO_CREDITO("Cartão de crédito"),
    TRANSFERENCIA("Transferência");

    private final String rotulo;

    FormaPagamentoView(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public FormaPagamento paraDominio() {
        return FormaPagamento.valueOf(name());
    }

    static FormaPagamentoView de(FormaPagamento forma) {
        return valueOf(forma.name());
    }
}