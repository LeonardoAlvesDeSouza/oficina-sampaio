package br.com.oficinasampaio.ordemservico.application;

public enum StatusOrdemServicoView {
    ABERTA("Aberta"),
    EM_EXECUCAO("Em execução"),
    AGUARDANDO_PECA("Aguardando peça"),
    FINALIZADA("Finalizada"),
    ENTREGUE("Entregue"),
    CANCELADA("Cancelada");

    private final String rotulo;

    StatusOrdemServicoView(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
