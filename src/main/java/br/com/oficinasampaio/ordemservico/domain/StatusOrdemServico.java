package br.com.oficinasampaio.ordemservico.domain;

public enum StatusOrdemServico {
    ABERTA(true),
    EM_EXECUCAO(true),
    AGUARDANDO_PECA(true),
    FINALIZADA(false),
    ENTREGUE(false),
    CANCELADA(false);

    private final boolean permiteAlterarItens;

    StatusOrdemServico(boolean permiteAlterarItens) {
        this.permiteAlterarItens = permiteAlterarItens;
    }

    public boolean permiteAlterarItens() {
        return permiteAlterarItens;
    }
}
