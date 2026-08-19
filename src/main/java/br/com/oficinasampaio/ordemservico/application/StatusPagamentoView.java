package br.com.oficinasampaio.ordemservico.application;

public enum StatusPagamentoView {
    PENDENTE("A receber"),
    PAGA("Paga");

    private final String rotulo;

    StatusPagamentoView(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}