package br.com.oficinasampaio.ordemservico.application;

public enum TipoItemOrdemServicoView {
    SERVICO("Serviço"),
    PECA("Peça");

    private final String rotulo;

    TipoItemOrdemServicoView(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
