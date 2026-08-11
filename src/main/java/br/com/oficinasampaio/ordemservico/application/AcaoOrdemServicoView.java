package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.AcaoOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServico;

import java.util.List;

public enum AcaoOrdemServicoView {
    INICIAR_EXECUCAO("Iniciar execução"),
    AGUARDAR_PECA("Aguardar peça"),
    RETOMAR_EXECUCAO("Retomar execução"),
    FINALIZAR("Finalizar"),
    ENTREGAR("Registrar entrega"),
    CANCELAR("Cancelar");

    private final String descricao;

    AcaoOrdemServicoView(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
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
