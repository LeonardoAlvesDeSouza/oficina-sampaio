package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.AcaoOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServico;

import java.util.List;

public enum AcaoOrdemServicoView {
    INICIAR_EXECUCAO("Iniciar execução", false),
    AGUARDAR_PECA("Aguardar peça", false),
    RETOMAR_EXECUCAO("Retomar execução", false),
    FINALIZAR("Finalizar", false),
    ENTREGAR("Registrar entrega", false),
    CANCELAR("Cancelar", true);

    private final String descricao;
    private final boolean restritaAoAdministrador;

    AcaoOrdemServicoView(String descricao, boolean restritaAoAdministrador) {
        this.descricao = descricao;
        this.restritaAoAdministrador = restritaAoAdministrador;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isRestritaAoAdministrador() {
        return restritaAoAdministrador;
    }

    AcaoOrdemServico paraDominio() {
        return AcaoOrdemServico.valueOf(name());
    }

    static List<AcaoOrdemServicoView> disponiveisPara(OrdemServico ordem) {
        return ordem.getAcoesDisponiveis().stream()
                .map(acao -> valueOf(acao.name()))
                .toList();
    }
}
